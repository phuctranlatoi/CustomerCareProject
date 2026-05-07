package com.example.customercareproject.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.YeuCauHoTro;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Handler để xử lý đánh giá xấu và tự động tạo ticket follow-up
 * Thay thế Firebase Functions để không cần Blaze Plan
 */
public class BadRatingHandler {
    
    private static final String TAG = "BadRatingHandler";
    private static final int BAD_RATING_THRESHOLD = 2; // Đánh giá ≤ 2 sao = xấu
    
    /**
     * Xử lý sau khi đánh giá được submit thành công
     * Tự động tạo ticket follow-up nếu là đánh giá xấu
     */
    public static void handleRatingSubmitted(Context context, DanhGia danhGia, String danhGiaId) {
        if (danhGia == null || danhGia.getSoSao() > BAD_RATING_THRESHOLD) {
            // Không phải đánh giá xấu, không làm gì
            return;
        }
        
        Log.d(TAG, "Phát hiện đánh giá xấu: " + danhGia.getSoSao() + " sao cho " + danhGia.getSanPham());
        
        // Tạo ticket follow-up tự động
        createFollowUpTicket(context, danhGia, danhGiaId);
        
        // Lưu vào Analytics
        saveToAnalytics(danhGia, danhGiaId);
    }
    
    /**
     * Tạo ticket follow-up tự động cho đánh giá xấu
     */
    private static void createFollowUpTicket(Context context, DanhGia danhGia, String danhGiaId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Tạo tiêu đề ticket
        String tieuDe = String.format("🔴 Follow-up đánh giá xấu: %s (%d⭐)", 
            danhGia.getSanPham(), 
            danhGia.getSoSao());
        
        // Tạo mô tả chi tiết
        StringBuilder moTa = new StringBuilder();
        moTa.append("📊 THÔNG TIN ĐÁNH GIÁ XẤU\n\n");
        moTa.append("⭐ Số sao: ").append(danhGia.getSoSao()).append("/5\n");
        moTa.append("📦 Sản phẩm: ").append(danhGia.getSanPham()).append("\n");
        moTa.append("📋 Loại: ").append(danhGia.getLoaiDanhGia()).append("\n");
        moTa.append("🎯 Bước: ").append(danhGia.getBuocDanhGia()).append("\n");
        
        if (danhGia.getLoaiGoi() != null && !danhGia.getLoaiGoi().isEmpty()) {
            moTa.append("📦 Gói: ").append(danhGia.getLoaiGoi()).append("\n");
        }
        
        if (danhGia.getTenCongTy() != null && !danhGia.getTenCongTy().isEmpty()) {
            moTa.append("🏢 Công ty: ").append(danhGia.getTenCongTy()).append("\n");
        }
        
        if (danhGia.getNoiDung() != null && !danhGia.getNoiDung().isEmpty()) {
            moTa.append("\n💬 Nội dung:\n").append(danhGia.getNoiDung()).append("\n");
        }
        
        if (danhGia.getTags() != null && !danhGia.getTags().isEmpty()) {
            moTa.append("\n🏷️ Tags: ").append(String.join(", ", danhGia.getTags())).append("\n");
        }
        
        if (danhGia.getCamXuc() != null && !danhGia.getCamXuc().isEmpty()) {
            moTa.append("😊 Cảm xúc: ").append(danhGia.getCamXuc()).append("\n");
        }
        
        moTa.append("\n⚠️ Ticket này được tạo tự động để follow-up đánh giá xấu.");
        moTa.append("\nVui lòng liên hệ khách hàng để giải quyết vấn đề.");
        
        // Tạo YeuCauHoTro object
        YeuCauHoTro ticket = new YeuCauHoTro();
        ticket.setNguoiDungId(danhGia.getUid());
        ticket.setTenNguoiDung(danhGia.getHoTen());
        ticket.setSanPham(danhGia.getSanPham());
        ticket.setTieuDe(tieuDe);
        ticket.setMoTa(moTa.toString());
        ticket.setTrangThai("ChoXuLy");
        ticket.setUuTien("Cao"); // Đánh giá xấu = ưu tiên cao
        ticket.setLoaiTicket("AutoFollowUp"); // Đánh dấu là ticket follow-up tự động
        ticket.setDanhGiaLienQuan(danhGiaId); // Link đến đánh giá gốc
        ticket.setTaoLuc(Timestamp.now());
        
        // Thêm thông tin công ty nếu có
        if (danhGia.getMaSoThue() != null) {
            ticket.setMaSoThue(danhGia.getMaSoThue());
        }
        if (danhGia.getTenCongTy() != null) {
            ticket.setTenCongTy(danhGia.getTenCongTy());
        }
        
        // Lưu vào Firestore
        db.collection("YeuCauHoTro")
            .add(ticket)
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "✅ Đã tạo ticket follow-up: " + docRef.getId());
                if (context != null) {
                    Toast.makeText(context, 
                        "Cảm ơn phản hồi! Chúng tôi sẽ liên hệ bạn sớm.", 
                        Toast.LENGTH_LONG).show();
                }
                
                // Gửi thông báo cho Admin/KTV (nếu có FCM)
                sendNotificationToAdmins(db, ticket, docRef.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Lỗi tạo ticket follow-up: " + e.getMessage(), e);
            });
    }
    
    /**
     * Lưu thông tin đánh giá xấu vào Analytics collection
     */
    private static void saveToAnalytics(DanhGia danhGia, String danhGiaId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        Map<String, Object> analyticsData = new HashMap<>();
        analyticsData.put("type", "BAD_RATING");
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
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "✅ Đã lưu analytics: " + docRef.getId());
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Lỗi lưu analytics: " + e.getMessage(), e);
            });
    }
    
    /**
     * Gửi thông báo cho Admin/KTV về đánh giá xấu
     * (Tùy chọn - cần cấu hình FCM)
     */
    private static void sendNotificationToAdmins(FirebaseFirestore db, YeuCauHoTro ticket, String ticketId) {
        // Query tất cả Admin và KTV có kinhDoanh = true
        db.collection("NguoiDung")
            .whereIn("vaiTro", java.util.Arrays.asList("Admin", "KTV"))
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int notificationCount = 0;
                for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Boolean kinhDoanh = doc.getBoolean("kinhDoanh");
                    String fcmToken = doc.getString("fcmToken");
                    
                    // Chỉ gửi cho user có kinhDoanh = true và có FCM token
                    if (Boolean.TRUE.equals(kinhDoanh) && fcmToken != null && !fcmToken.isEmpty()) {
                        // TODO: Implement FCM notification
                        // Hiện tại chỉ log, có thể implement sau
                        Log.d(TAG, "📲 Sẽ gửi thông báo đến: " + doc.getString("hoTen"));
                        notificationCount++;
                    }
                }
                Log.d(TAG, "📊 Đã chuẩn bị gửi " + notificationCount + " thông báo");
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "❌ Lỗi query Admin/KTV: " + e.getMessage(), e);
            });
    }
}
