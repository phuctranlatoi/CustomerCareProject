package com.example.customercareproject.utils;

import com.example.customercareproject.model.NguoiDung;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Smart Routing: Tìm KTV rảnh có ít ticket nhất để phân công
 */
public class SmartRouter {

    public interface OnKtvFoundCallback {
        void onFound(String ktvUid, String ktvTen);
    }

    public interface OnNotFoundCallback {
        void onNotFound();
    }

    public static void timKtvRanh(OnKtvFoundCallback onFound, OnNotFoundCallback onNotFound) {
        FirebaseFirestore.getInstance().collection("NguoiDung")
                .whereEqualTo("vaiTro", NguoiDung.VAI_TRO_KTV)
                .whereEqualTo("trangThai", NguoiDung.TRANG_THAI_RAN)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        onNotFound.onNotFound();
                        return;
                    }
                    // Load balancing: chọn KTV có ít ticket nhất
                    String bestUid = null;
                    String bestTen = null;
                    int minTicket = Integer.MAX_VALUE;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        NguoiDung ktv = doc.toObject(NguoiDung.class);
                        if (ktv.getSoTicketDangXuLy() < minTicket) {
                            minTicket = ktv.getSoTicketDangXuLy();
                            bestUid = doc.getId();
                            bestTen = ktv.getHoTen();
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

    public static void tangTicketKtv(String ktvUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("NguoiDung").document(ktvUid)
                .get()
                .addOnSuccessListener(doc -> {
                    long current = doc.getLong("soTicketDangXuLy") != null
                            ? doc.getLong("soTicketDangXuLy") : 0;
                    db.collection("NguoiDung").document(ktvUid)
                            .update("soTicketDangXuLy", current + 1);
                });
    }

    public static void giamTicketKtv(String ktvUid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("NguoiDung").document(ktvUid)
                .get()
                .addOnSuccessListener(doc -> {
                    long current = doc.getLong("soTicketDangXuLy") != null
                            ? doc.getLong("soTicketDangXuLy") : 1;
                    long newVal = Math.max(0, current - 1);
                    db.collection("NguoiDung").document(ktvUid)
                            .update("soTicketDangXuLy", newVal);
                });
    }
}
