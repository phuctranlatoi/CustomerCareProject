package com.example.customercareproject.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.model.GoiDangKy;
import com.example.customercareproject.ui.loi.LichSuChatActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.customercareproject.ui.components.Material3TextField;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private com.example.customercareproject.ui.components.Material3TextField tilHoTen, tilSdt, edtEmail;
    private TextView tvTenHienThi, tvEmailHienThi;
    private com.example.customercareproject.ui.components.InitialAvatarView tvAvatar;
    private TextView tvTenCongTy, tvMaSoThue, tvTrangThaiGoi;
    private TextView tvLabelDungThu;
    private ChipGroup chipGroupChinhThuc, chipGroupDungThu;
    private View btnLuuProfile, btnChinhSua;

    private boolean dangChinhSua = false;
    private String maSoThueUser;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) { finish(); return; }

        // Views
        tilHoTen        = findViewById(R.id.tilHoTen);
        tilSdt          = findViewById(R.id.tilSdt);
        edtEmail        = findViewById(R.id.edtEmail);
        tvTenHienThi    = findViewById(R.id.tvTenHienThi);
        tvEmailHienThi  = findViewById(R.id.tvEmailHienThi);
        tvAvatar        = findViewById(R.id.tvAvatar);
        tvTenCongTy     = findViewById(R.id.tvTenCongTy);
        tvMaSoThue      = findViewById(R.id.tvMaSoThue);
        tvTrangThaiGoi  = findViewById(R.id.tvTrangThaiGoi);
        tvLabelDungThu  = findViewById(R.id.tvLabelDungThu);
        chipGroupChinhThuc = findViewById(R.id.chipGroupChinhThuc);
        chipGroupDungThu   = findViewById(R.id.chipGroupDungThu);
        btnLuuProfile   = findViewById(R.id.btnLuuProfile);
        btnChinhSua     = findViewById(R.id.btnChinhSua);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnChinhSua.setOnClickListener(v -> batDauChinhSua());
        btnLuuProfile.setOnClickListener(v -> luuThongTin());
        findViewById(R.id.itemDoiMatKhau).setOnClickListener(v -> doiMatKhau());
        findViewById(R.id.itemLichSuChat).setOnClickListener(v ->
                startActivity(new Intent(this, LichSuChatActivity.class)));
        findViewById(R.id.btnDangXuat).setOnClickListener(v -> xacNhanDangXuat());

        taiThongTin();
    }

    private void taiThongTin() {
        edtEmail.setText(user.getEmail());
        tvEmailHienThi.setText(user.getEmail());

        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    String sdt   = doc.getString("soDienThoai");
                    maSoThueUser = doc.getString("maSoThue");
                    String tenCT = doc.getString("tenCongTy");

                    tilHoTen.setText(hoTen);
                    tilSdt.setText(sdt);
                    tvTenHienThi.setText(hoTen != null ? hoTen : "");
                    tvTenCongTy.setText(tenCT != null ? tenCT : "—");
                    tvMaSoThue.setText(maSoThueUser != null ? maSoThueUser : "—");

                    // Avatar chữ cái đầu
                    if (tvAvatar != null) tvAvatar.setName(hoTen);

                    // Tải gói đăng ký
                    if (maSoThueUser != null && !maSoThueUser.isEmpty()) {
                        taiGoiDangKy(maSoThueUser);
                    }
                });
    }

    private void taiGoiDangKy(String maSoThue) {
        db.collection("GoiDangKy")
                .whereEqualTo("maSoThue", maSoThue)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        tvTrangThaiGoi.setText("Chưa có gói đăng ký");
                        tvTrangThaiGoi.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.text_secondary));
                        return;
                    }

                    GoiDangKy goi = snap.getDocuments().get(0).toObject(GoiDangKy.class);
                    if (goi == null) return;

                    // Trạng thái gói
                    String ts = goi.getTrangThai();
                    if (GoiDangKy.TRANG_THAI_HOAT_DONG.equals(ts)) {
                        tvTrangThaiGoi.setText("● Gói đang hoạt động");
                        tvTrangThaiGoi.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.success));
                    } else if (GoiDangKy.TRANG_THAI_HET_HAN.equals(ts)) {
                        tvTrangThaiGoi.setText("● Gói đã hết hạn");
                        tvTrangThaiGoi.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.error));
                    } else {
                        tvTrangThaiGoi.setText("● Gói tạm dừng");
                        tvTrangThaiGoi.setTextColor(ContextCompat.getColor(ProfileActivity.this, R.color.warning));
                    }

                    // Chip sản phẩm chính thức
                    chipGroupChinhThuc.removeAllViews();
                    List<String> chinhThuc = goi.getSanPhamChinhThuc();
                    for (String sp : chinhThuc) {
                        chipGroupChinhThuc.addView(taoChip(sp, false));
                    }

                    // Chip sản phẩm dùng thử
                    chipGroupDungThu.removeAllViews();
                    List<String> dungThu = goi.getSanPhamDungThu();
                    if (!dungThu.isEmpty()) {
                        tvLabelDungThu.setVisibility(View.VISIBLE);
                        for (String sp : dungThu) {
                            chipGroupDungThu.addView(taoChip(sp, true));
                        }
                    } else {
                        tvLabelDungThu.setVisibility(View.GONE);
                    }
                });
    }

    private Chip taoChip(String text, boolean isDungThu) {
        Chip chip = new Chip(this);
        chip.setText(isDungThu ? text + " 🔍" : text);
        chip.setTextSize(12f);
        chip.setClickable(false);
        chip.setCheckable(false);
        if (isDungThu) {
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.warning_container)));
            chip.setTextColor(ContextCompat.getColor(this, R.color.on_warning_container));
        } else {
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.primary_container)));
            chip.setTextColor(ContextCompat.getColor(this, R.color.on_primary_container));
        }
        return chip;
    }

    private void batDauChinhSua() {
        dangChinhSua = true;
        tilHoTen.setEnabled(true);
        tilSdt.setEnabled(true);
        tilHoTen.getEditText().requestFocus();
        btnLuuProfile.setVisibility(View.VISIBLE);
        ((TextView) btnChinhSua).setText("Hủy");
        btnChinhSua.setOnClickListener(v -> huyChinhSua());
    }

    private void huyChinhSua() {
        dangChinhSua = false;
        tilHoTen.setEnabled(false);
        tilSdt.setEnabled(false);
        btnLuuProfile.setVisibility(View.GONE);
        ((TextView) btnChinhSua).setText("Chỉnh sửa");
        btnChinhSua.setOnClickListener(v2 -> batDauChinhSua());
        taiThongTin(); // reset về giá trị cũ
    }

    private void luuThongTin() {
        String hoTen = tilHoTen.getText().trim();
        String sdt   = tilSdt.getText().trim();

        if (hoTen.isEmpty()) {
            tilHoTen.setError("Vui lòng nhập họ tên");
            return;
        }

        Map<String, Object> update = new HashMap<>();
        update.put("hoTen", hoTen);
        update.put("soDienThoai", sdt);

        db.collection("NguoiDung").document(user.getUid())
                .update(update)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Đã cập nhật thông tin", Toast.LENGTH_SHORT).show();
                    tvTenHienThi.setText(hoTen);
                    if (tvAvatar != null) tvAvatar.setName(hoTen);
                    huyChinhSua();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void doiMatKhau() {
        if (user.getEmail() == null) return;
        FirebaseAuth.getInstance().sendPasswordResetEmail(user.getEmail())
                .addOnSuccessListener(v ->
                        new AlertDialog.Builder(this)
                                .setTitle("Đổi mật khẩu")
                                .setMessage("Email đặt lại mật khẩu đã được gửi đến " + user.getEmail())
                                .setPositiveButton("OK", null)
                                .show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void xacNhanDangXuat() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (d, w) -> {
                    com.example.customercareproject.utils.StringeeManager.getInstance().reset();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}


