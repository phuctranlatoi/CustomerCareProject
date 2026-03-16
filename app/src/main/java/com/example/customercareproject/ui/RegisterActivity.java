package com.example.customercareproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText edtHoTen, edtEmail, edtSoDienThoai, edtMatKhau, edtXacNhanMatKhau;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtHoTen = findViewById(R.id.edtHoTen);
        edtEmail = findViewById(R.id.edtEmail);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtMatKhau = findViewById(R.id.edtMatKhau);
        edtXacNhanMatKhau = findViewById(R.id.edtXacNhanMatKhau);
        Button btnDangKy = findViewById(R.id.btnDangKy);
        TextView tvDangNhap = findViewById(R.id.tvDangNhap);

        btnDangKy.setOnClickListener(v -> dangKy());
        tvDangNhap.setOnClickListener(v -> finish());
    }

    private void dangKy() {
        String hoTen = edtHoTen.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String sdt = edtSoDienThoai.getText().toString().trim();
        String matKhau = edtMatKhau.getText().toString().trim();
        String xacNhan = edtXacNhanMatKhau.getText().toString().trim();

        if (hoTen.isEmpty()) { edtHoTen.setError("Vui lòng nhập họ tên"); return; }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ"); return;
        }
        if (matKhau.length() < 6) { edtMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự"); return; }
        if (!matKhau.equals(xacNhan)) { edtXacNhanMatKhau.setError("Mật khẩu xác nhận không khớp"); return; }

        mAuth.createUserWithEmailAndPassword(email, matKhau)
                .addOnSuccessListener(authResult -> {
                    // Gửi email xác thực
                    authResult.getUser().sendEmailVerification()
                            .addOnSuccessListener(aVoid -> {
                                // Lưu thông tin người dùng vào Firestore
                                luuThongTinNguoiDung(authResult.getUser().getUid(), hoTen, email, sdt);
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi gửi email xác thực: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void luuThongTinNguoiDung(String uid, String hoTen, String email, String sdt) {
        Map<String, Object> nguoiDung = new HashMap<>();
        nguoiDung.put("uid", uid);
        nguoiDung.put("hoTen", hoTen);
        nguoiDung.put("email", email);
        nguoiDung.put("soDienThoai", sdt);
        nguoiDung.put("vaiTro", "KhachHang");
        nguoiDung.put("trangThai", "HoatDong");
        nguoiDung.put("taoLuc", com.google.firebase.Timestamp.now());

        db.collection("NguoiDung").document(uid)
                .set(nguoiDung)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this,
                            "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.",
                            Toast.LENGTH_LONG).show();
                    mAuth.signOut(); // Đăng xuất, yêu cầu xác thực email trước
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Rollback: xóa tài khoản Firebase nếu lưu Firestore thất bại
                    mAuth.getCurrentUser().delete();
                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
