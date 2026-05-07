package com.example.customercareproject.utils;

import com.example.customercareproject.model.NguoiDung;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * SmartRouter - Client side helper.
 * Logic quét HangCho và assign KTV đã chuyển sang Firebase Cloud Functions (functions/index.js).
 * Client chỉ cần:
 *   1. timKtvRanh() khi tạo ticket — thử assign ngay nếu có KTV
 *   2. Nếu không có KTV → lưu ticket với trangThai="HangCho" → Cloud Function tự xử lý
 *   3. tangTicketKtv / giamTicketKtv để cập nhật counter
 */
public class SmartRouter {

    public interface OnKtvFoundCallback {
        void onFound(String ktvUid, String ktvTen);
    }

    public interface OnNotFoundCallback {
        void onNotFound();
    }

    /**
     * Tìm KTV rảnh phù hợp (one-shot, dùng khi tạo ticket).
     * Nếu không có → caller lưu ticket với trangThai="HangCho",
     * Cloud Function sẽ tự assign khi có KTV online.
     * 
     * FIXED: Chỉ chọn KTV đang online (không phải Offline) và active trong 2 phút gần đây
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
                    long currentTime = System.currentTimeMillis();
                    long twoMinutesAgo = currentTime - (2 * 60 * 1000); // 2 phút

                    // Ưu tiên KTV có chuyên môn phù hợp VÀ đang online
                    for (QueryDocumentSnapshot doc : snapshot) {
                        NguoiDung ktv = doc.toObject(NguoiDung.class);
                        
                        // Kiểm tra lastSeen để đảm bảo KTV thực sự online
                        Long lastSeen = doc.getLong("lastSeen");
                        boolean isOnline = lastSeen != null && lastSeen > twoMinutesAgo;
                        
                        if (!isOnline) {
                            continue; // Bỏ qua KTV không active
                        }
                        
                        boolean coChuyenMon = ktv.getChuyenMon() != null
                                && ktv.getChuyenMon().contains(sanPham);
                        if (coChuyenMon && ktv.getSoTicketDangXuLy() < minTicket) {
                            minTicket = ktv.getSoTicketDangXuLy();
                            bestUid = doc.getId();
                            bestTen = ktv.getHoTen();
                        }
                    }

                    // Fallback: KTV rảnh bất kỳ ít ticket nhất VÀ đang online
                    if (bestUid == null) {
                        minTicket = Integer.MAX_VALUE;
                        for (QueryDocumentSnapshot doc : snapshot) {
                            NguoiDung ktv = doc.toObject(NguoiDung.class);
                            
                            // Kiểm tra lastSeen
                            Long lastSeen = doc.getLong("lastSeen");
                            boolean isOnline = lastSeen != null && lastSeen > twoMinutesAgo;
                            
                            if (!isOnline) {
                                continue; // Bỏ qua KTV không active
                            }
                            
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

    /** Tăng ticket counter khi assign KTV */
    public static void tangTicketKtv(String ktvUid) {
        FirebaseFirestore.getInstance().collection("NguoiDung").document(ktvUid)
                .update("soTicketDangXuLy", FieldValue.increment(1));
    }

    /** Giảm ticket và tăng tổng đã xử lý khi đóng ticket */
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
