package com.example.customercareproject.utils;

import android.content.Context;
import android.util.Log;

import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.LeadKinhDoanh;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Xử lý tự động khi có đánh giá tốt từ khách hàng đang dùng gói "Dùng thử" (DungThu).
 * Tạo Lead kinh doanh (LeadKinhDoanh) để bộ phận Sales theo dõi và chốt sale.
 */
public class LeadHandler {

    private static final String TAG = "LeadHandler";
    private static final int GOOD_RATING_THRESHOLD = 4; // >= 4 sao được coi là tốt

    public static void handleGoodRatingForTrial(Context context, DanhGia danhGia, String danhGiaId) {
        // 1. Kiểm tra số sao (Chỉ lấy >= 4 sao)
        if (danhGia == null || danhGia.getSoSao() < GOOD_RATING_THRESHOLD) {
            return;
        }

        // 2. Kiểm tra loại gói (Chỉ áp dụng cho Dùng thử)
        if (!"DungThu".equals(danhGia.getLoaiGoi())) {
            return;
        }

        Log.d(TAG, "Phát hiện đánh giá tốt từ bản dùng thử: " + danhGia.getSanPham() + " (" + danhGia.getSoSao() + " sao)");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 3. Tạo record LeadKinhDoanh
        LeadKinhDoanh lead = new LeadKinhDoanh(
                danhGia.getMaSoThue() != null ? danhGia.getMaSoThue() : "",
                danhGia.getTenCongTy() != null ? danhGia.getTenCongTy() : "Khách hàng cá nhân",
                danhGia.getSanPham(),
                danhGia.getSoSao(),
                danhGiaId
        );

        db.collection("LeadKinhDoanh").add(lead)
                .addOnSuccessListener(docRef -> {
                    Log.d(TAG, "✅ Đã lưu LeadKinhDoanh: " + docRef.getId());
                    // 4. Hiển thị thông báo Push (nếu có context)
                    if (context != null) {
                        NotificationHelper.showNewLeadNotification(context, 
                                lead.getTenCongTy(), 
                                lead.getSanPham(), 
                                lead.getSoSao());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "❌ Lỗi lưu LeadKinhDoanh: " + e.getMessage()));

        // 5. Ghi log vào Analytics collection
        saveLeadToAnalytics(danhGia, danhGiaId);
    }

    private static void saveLeadToAnalytics(DanhGia danhGia, String danhGiaId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("type", "LEAD_KINH_DOANH"); // Đánh dấu đây là Lead KD
        analyticsData.put("danhGiaId", danhGiaId);
        analyticsData.put("nguoiDungId", danhGia.getUid());
        analyticsData.put("sanPham", danhGia.getSanPham());
        analyticsData.put("soSao", danhGia.getSoSao());
        analyticsData.put("loai", danhGia.getLoaiDanhGia());
        analyticsData.put("buoc", danhGia.getBuocDanhGia());
        analyticsData.put("loaiGoi", danhGia.getLoaiGoi());
        analyticsData.put("maSoThue", danhGia.getMaSoThue());
        analyticsData.put("tenCongTy", danhGia.getTenCongTy());
        analyticsData.put("tags", danhGia.getTags());
        analyticsData.put("camXuc", danhGia.getCamXuc());
        analyticsData.put("taoLuc", Timestamp.now());
        
        db.collection("Analytics")
            .add(analyticsData)
            .addOnSuccessListener(docRef -> Log.d(TAG, "✅ Đã lưu analytics LeadKD: " + docRef.getId()))
            .addOnFailureListener(e -> Log.e(TAG, "❌ Lỗi lưu analytics LeadKD: " + e.getMessage()));
    }
}
