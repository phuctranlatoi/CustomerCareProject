package com.example.customercareproject.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.ui.admin.AdminDashboardActivity;

/**
 * Helper class để hiển thị thông báo local
 * Thay thế Firebase Cloud Messaging để tránh phụ thuộc server
 */
public class NotificationHelper {
    
    private static final String CHANNEL_ID = "bad_rating_channel";
    private static final String CHANNEL_NAME = "Đánh giá xấu";
    private static final String CHANNEL_DESC = "Thông báo khi có đánh giá xấu từ khách hàng";
    private static final int NOTIFICATION_ID = 1001;
    
    /**
     * Tạo notification channel (Android 8.0+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(true);
            
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Hiển thị thông báo đánh giá xấu
     */
    public static void showBadRatingNotification(Context context, DanhGia danhGia) {
        // Tạo channel nếu chưa có
        createNotificationChannel(context);
        
        // Intent mở Admin Dashboard khi nhấn notification
        Intent intent = new Intent(context, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("open_tab", "phan_tich"); // Mở tab phân tích
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Tạo notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning_red) // Cần tạo icon này
            .setContentTitle("🔴 Đánh giá xấu - Cần xử lý ngay")
            .setContentText(String.format("%s đánh giá %s %d★", 
                danhGia.getTenCongTy() != null ? danhGia.getTenCongTy() : "Khách hàng",
                danhGia.getSanPham(),
                danhGia.getSoSao()))
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(String.format("Công ty: %s\nSản phẩm: %s\nĐánh giá: %d★\nNội dung: %s\n\nĐã tạo ticket follow-up tự động với ưu tiên cao.",
                    danhGia.getTenCongTy() != null ? danhGia.getTenCongTy() : "Không rõ",
                    danhGia.getSanPham(),
                    danhGia.getSoSao(),
                    danhGia.getNoiDung() != null ? danhGia.getNoiDung() : "Không có nội dung")))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(context.getResources().getColor(R.color.error, null));
        
        // Hiển thị notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
    
    /**
     * Hiển thị thông báo lead mới (từ dùng thử)
     */
    public static void showNewLeadNotification(Context context, String tenCongTy, String sanPham, int soSao) {
        createNotificationChannel(context);
        
        Intent intent = new Intent(context, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("open_tab", "phan_tich");
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            1, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_star_filled) // Icon sao
            .setContentTitle("⭐ Lead mới từ dùng thử")
            .setContentText(String.format("%s đánh giá %s %d★", tenCongTy, sanPham, soSao))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(context.getResources().getColor(R.color.success, null));
        
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
        }
    }
}