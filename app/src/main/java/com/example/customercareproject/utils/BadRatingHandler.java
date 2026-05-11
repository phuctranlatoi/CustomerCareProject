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
     * Tạo ticket follow-up tự động cho đánh giá xấu.
     * Sử dụng SmartRouter để tìm KTV rảnh và gán ngay.
     * Nếu không có KTV → set "HangCho" để hệ thống quét lại khi có KTV online.
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
        
        // Lấy thông tin liên hệ của khách hàng để gán vào ticket
        String uid = danhGia.getUid();
        if (uid != null && !uid.isEmpty()) {
            db.collection("NguoiDung").document(uid).get()
                .addOnSuccessListener(nguoiDungDoc -> {
                    if (nguoiDungDoc.exists()) {
                        ticket.setEmail(nguoiDungDoc.getString("email"));
                        ticket.setSoDienThoai(nguoiDungDoc.getString("soDienThoai"));
                    }
                    // Tìm KTV rảnh và lưu ticket
                    timKtvVaLuuTicket(context, db, ticket, danhGia.getSanPham());
                })
                .addOnFailureListener(e -> {
                    // Lỗi lấy thông tin KH → vẫn tạo ticket
                    timKtvVaLuuTicket(context, db, ticket, danhGia.getSanPham());
                });
        } else {
            timKtvVaLuuTicket(context, db, ticket, danhGia.getSanPham());
        }
    }
    
    /**
     * Tìm KTV rảnh qua SmartRouter, gán vào ticket rồi lưu.
     * Nếu không tìm thấy KTV → set trangThai = "HangCho" để hệ thống quét lại sau.
     */
    private static void timKtvVaLuuTicket(Context context, FirebaseFirestore db, 
                                           YeuCauHoTro ticket, String sanPham) {
        SmartRouter.timKtvRanh(sanPham,
            // Tìm thấy KTV rảnh → gán ngay
            (ktvUid, ktvTen) -> {
                ticket.setKtvUid(ktvUid);
                ticket.setKtvTen(ktvTen);
                ticket.setTrangThai("ChoXuLy");
                Log.d(TAG, "🎯 Tìm thấy KTV rảnh: " + ktvTen + " (" + ktvUid + ")");
                luuTicketVaoFirestore(context, db, ticket, ktvUid);
            },
            // Không có KTV nào rảnh → đưa vào hàng chờ
            () -> {
                ticket.setTrangThai("HangCho");
                Log.d(TAG, "⏳ Không có KTV rảnh, ticket vào hàng chờ");
                luuTicketVaoFirestore(context, db, ticket, null);
            }
        );
    }
    
    /**
     * Lưu ticket vào Firestore, tăng counter KTV nếu đã gán.
     */
    private static void luuTicketVaoFirestore(Context context, FirebaseFirestore db, 
                                               YeuCauHoTro ticket, String ktvUid) {
        db.collection("YeuCauHoTro")
            .add(ticket)
            .addOnSuccessListener(docRef -> {
                Log.d(TAG, "✅ Đã tạo ticket follow-up: " + docRef.getId());
                
                if (ktvUid != null) {
                    // Đã gán KTV → tăng counter ticket
                    SmartRouter.tangTicketKtv(ktvUid);
                    Log.d(TAG, "📊 Đã tăng counter ticket cho KTV: " + ktvUid);
                } else {
                    // Đánh dấu thời điểm vào hàng chờ
                    docRef.update("thoiGianChoXuLy", 
                        com.google.firebase.firestore.FieldValue.serverTimestamp());
                }
                
                if (context != null) {
                    String msg = ktvUid != null
                        ? "Cảm ơn phản hồi! KTV " + ticket.getKtvTen() + " sẽ liên hệ bạn sớm."
                        : "Cảm ơn phản hồi! Đang tìm kỹ thuật viên, bạn sẽ được hỗ trợ sớm.";
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
                
                // Gửi thông báo cho Admin/KTV
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
