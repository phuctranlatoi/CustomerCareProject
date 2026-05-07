package com.example.customercareproject.ui.loi;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.ui.components.Material3Button;
import com.example.customercareproject.ui.components.Material3TextField;
import com.example.customercareproject.utils.AnimationHelper;
import com.example.customercareproject.utils.NlpHelper;
import com.example.customercareproject.utils.SmartRouter;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class YeuCauHoTroActivity extends AppCompatActivity {

    private Material3TextField tilHoTen, tilSoDienThoai, tilEmail, tilMoTaVanDe, tilTieuDe;
    private Material3Button btnGuiYeuCau;
    private ProgressBar progressBarGui;
    private String sanPham, loiId, tieuDeLoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeu_cau_ho_tro);

        sanPham = getIntent().getStringExtra("sanPham");
        loiId = getIntent().getStringExtra("loiId");
        tieuDeLoi = getIntent().getStringExtra("tieuDeLoi");

        initViews();
        setupFormValidation();
        setupInitialData();
        setupClickListeners();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.tvTieuDeLoi)).setText(tieuDeLoi);
        ((Chip) findViewById(R.id.tvSanPhamLoi)).setText(sanPham);

        tilHoTen = findViewById(R.id.tilHoTen);
        tilSoDienThoai = findViewById(R.id.tilSoDienThoai);
        tilEmail = findViewById(R.id.tilEmail);
        tilMoTaVanDe = findViewById(R.id.tilMoTaVanDe);
        tilTieuDe = findViewById(R.id.tilTieuDe);
        btnGuiYeuCau = findViewById(R.id.btnGuiYeuCau);
        progressBarGui = findViewById(R.id.progressBarGui);

        // Set input types for the text fields
        tilHoTen.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        tilSoDienThoai.setInputType(InputType.TYPE_CLASS_PHONE);
        tilEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        tilMoTaVanDe.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        tilTieuDe.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        // Set minimum lines for description
        tilMoTaVanDe.getEditText().setMinLines(4);
        tilMoTaVanDe.getEditText().setGravity(android.view.Gravity.TOP);

        // Set max lines for title
        tilTieuDe.getEditText().setMaxLines(2);

        // Nếu không có lỗi cụ thể → hiện field nhập tiêu đề
        boolean coLoiCuThe = tieuDeLoi != null && !tieuDeLoi.isEmpty()
                && !"Vấn đề khác".equals(tieuDeLoi);
        if (!coLoiCuThe) {
            tilTieuDe.setVisibility(android.view.View.VISIBLE);
            ((TextView) findViewById(R.id.tvTieuDeLoi)).setText("Vấn đề tự mô tả");
        }
    }

    private void setupFormValidation() {
        // Real-time validation for name field
        tilHoTen.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    tilHoTen.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for phone field
        tilSoDienThoai.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    tilSoDienThoai.clearError();
                    // Validate phone number format
                    String phone = s.toString().trim();
                    if (phone.length() >= 10 && !isValidPhoneNumber(phone)) {
                        tilSoDienThoai.setError("Số điện thoại không hợp lệ");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for email field
        tilEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = s.toString().trim();
                if (email.length() > 0) {
                    if (isValidEmail(email)) {
                        tilEmail.clearError();
                    } else {
                        tilEmail.setError("Email không hợp lệ");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for title field (if visible)
        tilTieuDe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    tilTieuDe.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Real-time validation for description field
        tilMoTaVanDe.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    tilMoTaVanDe.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupInitialData() {
        // Điền sẵn thông tin từ tài khoản
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            tilEmail.setText(user.getEmail());
            FirebaseFirestore.getInstance().collection("NguoiDung")
                    .document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            tilHoTen.setText(doc.getString("hoTen"));
                            tilSoDienThoai.setText(doc.getString("soDienThoai"));
                        }
                    });
        }
    }

    private void setupClickListeners() {
        btnGuiYeuCau.setOnClickListener(v -> {
            if (validateForm()) {
                guiYeuCau();
            }
        });
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate name
        String hoTen = tilHoTen.getText().trim();
        if (hoTen.isEmpty()) {
            tilHoTen.setError("Vui lòng nhập họ tên");
            isValid = false;
        } else if (hoTen.length() < 2) {
            tilHoTen.setError("Họ tên phải có ít nhất 2 ký tự");
            isValid = false;
        }

        // Validate phone
        String sdt = tilSoDienThoai.getText().trim();
        if (sdt.isEmpty()) {
            tilSoDienThoai.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!isValidPhoneNumber(sdt)) {
            tilSoDienThoai.setError("Số điện thoại không hợp lệ");
            isValid = false;
        }

        // Validate email (optional but if provided must be valid)
        String email = tilEmail.getText().trim();
        if (!email.isEmpty() && !isValidEmail(email)) {
            tilEmail.setError("Email không hợp lệ");
            isValid = false;
        }

        // Validate description
        String moTa = tilMoTaVanDe.getText().trim();
        if (moTa.isEmpty()) {
            tilMoTaVanDe.setError("Vui lòng mô tả vấn đề");
            isValid = false;
        } else if (moTa.length() < 10) {
            tilMoTaVanDe.setError("Mô tả phải có ít nhất 10 ký tự");
            isValid = false;
        }

        // Validate title if visible
        if (tilTieuDe.getVisibility() == android.view.View.VISIBLE) {
            String tieuDe = tilTieuDe.getText().trim();
            if (tieuDe.isEmpty()) {
                tilTieuDe.setError("Vui lòng nhập tiêu đề vấn đề");
                isValid = false;
            } else if (tieuDe.length() < 5) {
                tilTieuDe.setError("Tiêu đề phải có ít nhất 5 ký tự");
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPhoneNumber(String phone) {
        // Vietnamese phone number validation
        // Accepts formats: 0xxxxxxxxx, +84xxxxxxxxx, 84xxxxxxxxx
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return cleanPhone.matches("^(\\+84|84|0)[3-9]\\d{8}$");
    }

    private void guiYeuCau() {
        String hoTen = tilHoTen.getText().trim();
        String sdt = tilSoDienThoai.getText().trim();
        String email = tilEmail.getText().trim();
        String moTa = tilMoTaVanDe.getText().trim();

        // Xác định tiêu đề thực tế
        String tieuDeThucTe = tieuDeLoi;
        boolean coLoiCuThe = tieuDeLoi != null && !tieuDeLoi.isEmpty()
                && !"Vấn đề khác".equals(tieuDeLoi);
        if (!coLoiCuThe) {
            // Lấy từ field nhập tiêu đề
            String tieuDeNhap = tilTieuDe.getText().trim();
            if (tieuDeNhap.isEmpty()) {
                // Fallback: dùng 60 ký tự đầu của mô tả
                tieuDeThucTe = moTa.length() > 60 ? moTa.substring(0, 60) + "..." : moTa;
                if (tieuDeThucTe.isEmpty()) tieuDeThucTe = "Yêu cầu hỗ trợ";
            } else {
                tieuDeThucTe = tieuDeNhap;
            }
        }

        // Show loading state with animation
        btnGuiYeuCau.setLoading(true);
        AnimationHelper.fadeIn(progressBarGui);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "";

        YeuCauHoTro yeuCau = new YeuCauHoTro(uid, hoTen, email, sdt, sanPham,
                loiId != null ? loiId : "", tieuDeThucTe, moTa);

        String uuTien = NlpHelper.phanTichUuTien(moTa + " " + tieuDeLoi);
        yeuCau.setUuTien(uuTien);

        // Thử tìm KTV rảnh ngay. Nếu không có → lưu HangCho, Cloud Function tự assign sau.
        SmartRouter.timKtvRanh(sanPham, (ktvUid, ktvTen) -> {
            yeuCau.setKtvUid(ktvUid);
            yeuCau.setKtvTen(ktvTen);
            // Có KTV → trangThai = ChoXuLy
            luuYeuCau(yeuCau, ktvUid);
        }, () -> {
            // Không có KTV → trangThai = HangCho, server sẽ assign
            yeuCau.setTrangThai("HangCho");
            luuYeuCau(yeuCau, null);
        });
    }

    private void luuYeuCau(YeuCauHoTro yeuCau, String ktvUid) {
        FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .add(yeuCau)
                .addOnSuccessListener(ref -> {
                    // Hide loading state
                    AnimationHelper.fadeOut(progressBarGui);
                    btnGuiYeuCau.setLoading(false);
                    
                    if (ktvUid != null) {
                        SmartRouter.tangTicketKtv(ktvUid);
                    } else {
                        // Đánh dấu thời điểm vào hàng chờ để Cloud Function sort theo thứ tự
                        ref.update("thoiGianChoXuLy", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    }
                    
                    String msg = ktvUid != null
                            ? "Yêu cầu đã gửi! KTV " + yeuCau.getKtvTen() + " sẽ hỗ trợ bạn."
                            : "Yêu cầu đã gửi! Đang tìm kỹ thuật viên, bạn sẽ được hỗ trợ sớm nhất.";
                    
                    // Show success animation
                    AnimationHelper.bounce(btnGuiYeuCau);
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    
                    // Navigate to chat
                    android.content.Intent chatIntent = new android.content.Intent(this, ChatKhachHangActivity.class);
                    chatIntent.putExtra("ticketId", ref.getId());
                    chatIntent.putExtra("hoTen", yeuCau.getHoTen());
                    startActivity(chatIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Hide loading state and re-enable button
                    AnimationHelper.fadeOut(progressBarGui);
                    btnGuiYeuCau.setLoading(false);
                    
                    // Show error with shake animation
                    AnimationHelper.shake(btnGuiYeuCau);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
