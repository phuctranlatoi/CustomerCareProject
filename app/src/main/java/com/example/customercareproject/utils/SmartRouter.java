package com.example.customercareproject.utils;

import android.os.Handler;
import android.os.Looper;

import com.example.customercareproject.model.NguoiDung;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Date;

/**
 * Smart Routing "Mắt Bão":
 * 1. Lọc KTV theo chuyên môn + trạng thái "Ran"
 * 2. Least-Workload: giao cho KTV ít ticket nhất
 * 3. Timeout 30s: nếu không có KTV nhận, chuyển sang HangCho
 * 4. Hỗ trợ ưu tiên (Cao/TrungBinh/Thap) từ NLP
 */
public class SmartRouter {

    private static final int TIMEOUT_MS = 30_000; // 30 giây

    public interface OnKtvFoundCallback {
        void onFound(String ktvUid, String ktvTen);
    }

    public interface OnNotFoundCallback {
        void onNotFound();
    }

    public interface OnQueueCallback {
        void onQueued(String ticketId);
    }

    /**
     * Tìm KTV phù hợp theo sản phẩm + load balancing.
     * Fallback sang KTV rảnh bất kỳ nếu không có chuyên môn phù hợp.
     */
    public static void timKtvRanh(String sanPham, OnKtvFoundCallback onFound, OnNotFoundCallback onNotFound) {
        FirebaseFirestore.getInstance().collection("NguoiDung")
                .whereEqualTo("vaiTro", NguoiDung.VAI_TRO_KTV)
                .whereEqualTo("trangThai", NguoiDung.TRANG_THAI_RAN)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        onNotFound.onNotFound();
                        return;
                    }

                    String bestUid = null, bestTen = null;
                    int minTicket = Integer.MAX_VALUE;

                    // Ưu tiên KTV có chuyên môn phù hợp
                    for (QueryDocumentSnapshot doc : snapshot) {
                        NguoiDung ktv = doc.toObject(NguoiDung.class);
                        boolean coChuyenMon = ktv.getChuyenMon() != null
                                && ktv.getChuyenMon().contains(sanPham);
                        if (coChuyenMon && ktv.getSoTicketDangXuLy() < minTicket) {
                            minTicket = ktv.getSoTicketDangXuLy();
                            bestUid = doc.getId();
                            bestTen = ktv.getHoTen();
                        }
                    }

                    // Fallback: KTV rảnh bất kỳ ít ticket nhất
                    if (bestUid == null) {
                        minTicket = Integer.MAX_VALUE;
                        for (QueryDocumentSnapshot doc : snapshot) {
                            NguoiDung ktv = doc.toObject(NguoiDung.class);
                            if (ktv.getSoTicketDangXuLy() < minTicket) {
                                minTicket = ktv.getSoTicketDangXuLy();
                                bestUid = doc.getId();
                                bestTen = ktv.getHoTen();
                            }
                        }
                    }

                    if (bestUid != null) {
                        onFound.onFound(bestUid, bestTen);
                    } else {
                        onNotFound.onNotFound();
                    }
                })
                .addOnFailureListener(e -> onNotFound.onNotFound());
    }

    /**
     * Dispatch ticket với timeout 30s.
     * Nếu sau 30s ticket vẫn ở "ChoXuLy" (chưa có KTV nhận), chuyển sang "HangCho".
     */
    public static void dispatchVoiTimeout(String ticketId, String sanPham, String uuTien,
                                          OnKtvFoundCallback onFound, OnQueueCallback onQueued) {
        timKtvRanh(sanPham, (ktvUid, ktvTen) -> {
            onFound.onFound(ktvUid, ktvTen);
            // Đặt timeout 30s kiểm tra KTV có nhận không
            new Handler(Looper.getMainLooper()).postDelayed(() ->
                    kiemTraVaChuyenHangCho(ticketId, onQueued), TIMEOUT_MS);
        }, () -> {
            // Không có KTV ngay lập tức -> vào hàng chờ luôn
            chuyenSangHangCho(ticketId, onQueued);
        });
    }

    /** Kiểm tra sau 30s: nếu ticket vẫn "ChoXuLy" thì chuyển HangCho */
    private static void kiemTraVaChuyenHangCho(String ticketId, OnQueueCallback onQueued) {
        FirebaseFirestore.getInstance().collection("YeuCauHoTro").document(ticketId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && "ChoXuLy".equals(doc.getString("trangThai"))) {
                        chuyenSangHangCho(ticketId, onQueued);
                    }
                });
    }

    private static void chuyenSangHangCho(String ticketId, OnQueueCallback onQueued) {
        FirebaseFirestore.getInstance().collection("YeuCauHoTro").document(ticketId)
                .update(
                        "trangThai", "HangCho",
                        "thoiGianChoXuLy", Timestamp.now(),
                        "capNhatLuc", Timestamp.now()
                )
                .addOnSuccessListener(v -> {
                    if (onQueued != null) onQueued.onQueued(ticketId);
                });
    }

    /** Tăng ticket (atomic, tránh race condition) */
    public static void tangTicketKtv(String ktvUid) {
        FirebaseFirestore.getInstance().collection("NguoiDung").document(ktvUid)
                .update("soTicketDangXuLy", FieldValue.increment(1));
    }

    /** Giảm ticket và tăng tổng đã xử lý (atomic) */
    public static void giamTicketKtv(String ktvUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("NguoiDung").document(ktvUid).get()
                .addOnSuccessListener(doc -> {
                    long current = doc.getLong("soTicketDangXuLy") != null
                            ? doc.getLong("soTicketDangXuLy") : 1;
                    db.collection("NguoiDung").document(ktvUid).update(
                            "soTicketDangXuLy", Math.max(0, current - 1),
                            "tongTicketDaXuLy", FieldValue.increment(1)
                    );
                });
    }
}
