package com.example.customercareproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.model.NguoiDung;
import com.example.customercareproject.ui.LoginActivity;
import com.example.customercareproject.ui.admin.AdminDashboardActivity;
import com.example.customercareproject.ui.home.HomeActivity;
import com.example.customercareproject.ui.ktv.KtvDashboardActivity;
import com.example.customercareproject.utils.DataSeeder;
import com.example.customercareproject.utils.StringeeManager;
import com.example.customercareproject.utils.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Tạo notification channel cho đánh giá xấu
        NotificationHelper.createNotificationChannel(this);

        // Seed dữ liệu mẫu (chỉ chạy 1 lần khi DB rỗng)
        DataSeeder.initDatabase(FirebaseFirestore.getInstance());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Đọc vaiTro từ Firestore để route đúng dashboard
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("NguoiDung")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String vaiTro;
                    if (!doc.exists()) {
                        // Document chưa tồn tại (ví dụ: tài khoản tạo thủ công trên Firebase Console)
                        // Tự động tạo document với vaiTro mặc định
                        android.util.Log.w("MainActivity", "NguoiDung doc not found for UID: " + user.getUid() + ", tạo mới...");
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("uid", user.getUid());
                        data.put("hoTen", user.getDisplayName() != null ? user.getDisplayName() : "");
                        data.put("email", user.getEmail() != null ? user.getEmail() : "");
                        data.put("vaiTro", NguoiDung.VAI_TRO_KHACH_HANG);
                        data.put("trangThai", "HoatDong");
                        data.put("taoLuc", com.google.firebase.Timestamp.now());
                        db.collection("NguoiDung").document(user.getUid()).set(data);
                        vaiTro = NguoiDung.VAI_TRO_KHACH_HANG;
                    } else {
                        vaiTro = doc.getString("vaiTro");
                        android.util.Log.d("MainActivity", "UID: " + user.getUid());
                        android.util.Log.d("MainActivity", "doc exists: " + doc.exists());
                        android.util.Log.d("MainActivity", "vaiTro raw: '" + vaiTro + "'");
                        if (vaiTro == null) vaiTro = NguoiDung.VAI_TRO_KHACH_HANG;
                    }

                    // Khởi tạo Stringee để nhận cuộc gọi đến
                    StringeeManager.getInstance().init(this, user.getUid());

                    Intent intent;
                    switch (vaiTro) {
                        case NguoiDung.VAI_TRO_KTV:
                            intent = new Intent(this, KtvDashboardActivity.class);
                            break;
                        case NguoiDung.VAI_TRO_ADMIN:
                            intent = new Intent(this, AdminDashboardActivity.class);
                            break;
                        default:
                            intent = new Intent(this, HomeActivity.class);
                            break;
                    }
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("MainActivity", "Lỗi đọc NguoiDung: " + e.getMessage());
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
    }
}
