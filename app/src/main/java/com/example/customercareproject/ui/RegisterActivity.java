package com.example.customercareproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;
import com.example.customercareproject.ui.components.Material3Button;
import com.example.customercareproject.ui.components.Material3TextField;
import com.example.customercareproject.utils.AnimationHelper;
import com.example.customercareproject.utils.TaxCodeValidator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private Material3TextField txtHoTen, txtEmail, txtSoDienThoai, txtMaSoThue, txtTenCongTy, txtMatKhau, txtXacNhanMatKhau;
    private Material3Button btnDangKy;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupViews();
        
        // Apply fade-in animation when screen loads
        LinearLayout registerContainer = findViewById(R.id.registerContainer);
        AnimationHelper.fadeIn(registerContainer);
    }
    
    private void setupViews() {
        txtHoTen = findViewById(R.id.txtHoTen);
        txtEmail = findViewById(R.id.txtEmail);
        txtSoDienThoai = findViewById(R.id.txtSoDienThoai);
        txtMaSoThue = findViewById(R.id.txtMaSoThue);
        txtTenCongTy = findViewById(R.id.txtTenCongTy);
        txtMatKhau = findViewById(R.id.txtMatKhau);
        txtXacNhanMatKhau = findViewById(R.id.txtXacNhanMatKhau);
        btnDangKy = findViewById(R.id.btnDangKy);
        TextView tvDangNhap = findViewById(R.id.tvDangNhap);

        // Setup inline validation for all fields
        setupInlineValidation();

        btnDangKy.setOnClickListener(v -> dangKy());
        tvDangNhap.setOnClickListener(v -> finish());
    }
    
    /**
     * Setup inline validation for all input fields with real-time feedback
     * Validates only when user leaves the field (onFocusChange) to avoid aggressive validation
     */
    private void setupInlineValidation() {
        // Name validation
        txtHoTen.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtHoTen.getError() != null) {
                    txtHoTen.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Don't validate here - too aggressive
            }
        });
        
        txtHoTen.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String name = txtHoTen.getText().trim();
                if (!name.isEmpty() && name.length() < 2) {
                    txtHoTen.setError("Họ tên phải có ít nhất 2 ký tự");
                }
            }
        });
        
        // Email validation
        txtEmail.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtEmail.getError() != null) {
                    txtEmail.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Don't validate here - too aggressive
            }
        });
        
        txtEmail.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = txtEmail.getText().trim();
                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    txtEmail.setError("Email không hợp lệ");
                }
            }
        });
        
        // Tax code validation
        txtMaSoThue.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtMaSoThue.getError() != null) {
                    txtMaSoThue.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Don't validate here - too aggressive
            }
        });
        
        txtMaSoThue.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String taxCode = txtMaSoThue.getText().trim();
                if (!taxCode.isEmpty() && !TaxCodeValidator.isValid(taxCode)) {
                    txtMaSoThue.setError(TaxCodeValidator.getErrorMessage(taxCode));
                }
            }
        });
        
        // Company name validation
        txtTenCongTy.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtTenCongTy.getError() != null) {
                    txtTenCongTy.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Don't validate here - too aggressive
            }
        });
        
        txtTenCongTy.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String companyName = txtTenCongTy.getText().trim();
                if (!companyName.isEmpty() && companyName.length() < 2) {
                    txtTenCongTy.setError("Tên công ty phải có ít nhất 2 ký tự");
                }
            }
        });
        
        // Password validation (strength indicator is automatic via Material3TextField)
        txtMatKhau.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtMatKhau.getError() != null) {
                    txtMatKhau.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Don't validate here - too aggressive
            }
        });
        
        txtMatKhau.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String password = txtMatKhau.getText().trim();
                if (!password.isEmpty() && password.length() < 6) {
                    txtMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
                }
            }
        });
        
        // Confirm password validation
        txtXacNhanMatKhau.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (txtXacNhanMatKhau.getError() != null) {
                    txtXacNhanMatKhau.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Don't validate here - too aggressive
            }
        });
        
        txtXacNhanMatKhau.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String confirmPassword = txtXacNhanMatKhau.getText().trim();
                String password = txtMatKhau.getText().trim();
                if (!confirmPassword.isEmpty() && !confirmPassword.equals(password)) {
                    txtXacNhanMatKhau.setError("Mật khẩu xác nhận không khớp");
                }
            }
        });
    }

    private void dangKy() {
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSoDienThoai.getText().trim();
        String maSoThue = txtMaSoThue.getText().trim();
        String tenCongTy = txtTenCongTy.getText().trim();
        String matKhau = txtMatKhau.getText().trim();
        String xacNhan = txtXacNhanMatKhau.getText().trim();

        // Validate all fields with inline error display
        boolean hasError = false;
        
        if (hoTen.isEmpty()) {
            txtHoTen.setError("Vui lòng nhập họ tên");
            hasError = true;
        }
        
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Email không hợp lệ");
            hasError = true;
        }
        
        if (!TaxCodeValidator.isValid(maSoThue)) {
            txtMaSoThue.setError(TaxCodeValidator.getErrorMessage(maSoThue));
            hasError = true;
        }
        
        if (tenCongTy.isEmpty()) {
            txtTenCongTy.setError("Vui lòng nhập tên công ty");
            hasError = true;
        }
        
        if (matKhau.length() < 6) {
            txtMatKhau.setError("Mật khẩu phải có ít nhất 6 ký tự");
            hasError = true;
        }
        
        if (!matKhau.equals(xacNhan)) {
            txtXacNhanMatKhau.setError("Mật khẩu xác nhận không khớp");
            hasError = true;
        }
        
        if (hasError) {
            return;
        }

        // Show loading state
        btnDangKy.setLoading(true);

        // Kiểm tra mã số thuế có trong hệ thống không
        kiemTraMaSoThue(maSoThue, tenCongTy, () -> {
            // Mã số thuế hợp lệ → tiếp tục đăng ký
            taoTaiKhoan(hoTen, email, sdt, maSoThue, tenCongTy, matKhau);
        }, () -> {
            // Mã số thuế không hợp lệ → hide loading
            btnDangKy.setLoading(false);
        });
    }

    private void kiemTraMaSoThue(String maSoThue, String tenCongTy, Runnable onSuccess, Runnable onFailure) {
        db.collection("GoiDangKy")
                .whereEqualTo("maSoThue", maSoThue)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        txtMaSoThue.setError("Công ty chưa đăng ký sử dụng dịch vụ. Vui lòng liên hệ admin.");
                        onFailure.run();
                        return;
                    }
                    
                    // Kiểm tra tên công ty có khớp không (tùy chọn - có thể bỏ)
                    String tenCongTyDB = snap.getDocuments().get(0).getString("tenCongTy");
                    if (tenCongTyDB != null && !tenCongTyDB.equalsIgnoreCase(tenCongTy)) {
                        txtTenCongTy.setError("Tên công ty không khớp với mã số thuế. Tên đúng: " + tenCongTyDB);
                        onFailure.run();
                        return;
                    }
                    
                    // Kiểm tra trạng thái gói
                    String trangThai = snap.getDocuments().get(0).getString("trangThai");
                    if (!"HoatDong".equals(trangThai)) {
                        txtMaSoThue.setError("Gói dịch vụ của công ty đã hết hạn hoặc tạm dừng.");
                        onFailure.run();
                        return;
                    }
                    
                    onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi kiểm tra mã số thuế: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    onFailure.run();
                });
    }

    private void taoTaiKhoan(String hoTen, String email, String sdt, String maSoThue, String tenCongTy, String matKhau) {
        mAuth.createUserWithEmailAndPassword(email, matKhau)
                .addOnSuccessListener(authResult -> {
                    // Gửi email xác thực
                    authResult.getUser().sendEmailVerification()
                            .addOnSuccessListener(aVoid -> {
                                // Lưu thông tin người dùng vào Firestore
                                luuThongTinNguoiDung(authResult.getUser().getUid(), hoTen, email, sdt, maSoThue, tenCongTy);
                            })
                            .addOnFailureListener(e -> {
                                btnDangKy.setLoading(false);
                                Toast.makeText(this, "Lỗi gửi email xác thực: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnDangKy.setLoading(false);
                    Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void luuThongTinNguoiDung(String uid, String hoTen, String email, String sdt, String maSoThue, String tenCongTy) {
        Map<String, Object> nguoiDung = new HashMap<>();
        nguoiDung.put("uid", uid);
        nguoiDung.put("hoTen", hoTen);
        nguoiDung.put("email", email);
        nguoiDung.put("soDienThoai", sdt);
        nguoiDung.put("maSoThue", maSoThue);
        nguoiDung.put("tenCongTy", tenCongTy);
        nguoiDung.put("vaiTro", "KhachHang");
        nguoiDung.put("trangThai", "HoatDong");
        nguoiDung.put("taoLuc", com.google.firebase.Timestamp.now());

        db.collection("NguoiDung").document(uid)
                .set(nguoiDung)
                .addOnSuccessListener(aVoid -> {
                    btnDangKy.setLoading(false);
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
                    btnDangKy.setLoading(false);
                    Toast.makeText(this, "Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
