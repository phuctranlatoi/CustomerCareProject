package com.example.customercareproject.ui.danhgiaktv;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.customercareproject.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DanhGiaKTVActivity extends AppCompatActivity {

    public static final String EXTRA_TICKET_ID = "ticketId";
    public static final String EXTRA_KTV_UID   = "ktvUid";
    public static final String EXTRA_KTV_TEN   = "ktvTen";
    public static final String EXTRA_SAN_PHAM  = "sanPham";

    private static final String[] TAGS_TOT = {"Nhiệt tình", "Giải quyết nhanh", "Chuyên nghiệp", "Dễ hiểu"};
    private static final String[] TAGS_KEM = {"Phản hồi chậm", "Chưa giải quyết được", "Thái độ chưa tốt", "Giải thích khó hiểu"};

    private String ticketId, ktvUid, ktvTen, sanPham;
    private float soSao = 5f;

    private RatingBar ratingBar;
    private TextView tvSoSaoLabel;
    private ChipGroup chipGroup;
    private TextInputEditText edtNhanXet;
    private Button btnGuiDanhGia, btnBoQua;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_gia_ktv);

        ticketId = getIntent().getStringExtra(EXTRA_TICKET_ID);
        ktvUid   = getIntent().getStringExtra(EXTRA_KTV_UID);
        ktvTen   = getIntent().getStringExtra(EXTRA_KTV_TEN);
        sanPham  = getIntent().getStringExtra(EXTRA_SAN_PHAM);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Đánh giá hỗ trợ");
        toolbar.setNavigationOnClickListener(v -> finish());

        ratingBar     = findViewById(R.id.ratingBar);
        tvSoSaoLabel  = findViewById(R.id.tvSoSaoLabel);
        chipGroup     = findViewById(R.id.chipGroup);
        edtNhanXet    = findViewById(R.id.edtNhanXet);
        btnGuiDanhGia = findViewById(R.id.btnGuiDanhGia);
        btnBoQua      = findViewById(R.id.btnBoQua);

        TextView tvTenKtv = findViewById(R.id.tvTenKtv);
        tvTenKtv.setText(ktvTen != null ? ktvTen : "Kỹ thuật viên");

        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            soSao = rating;
            capNhatLabel(rating);
            capNhatChips(rating);
        });

        capNhatLabel(5f);
        capNhatChips(5f);

        btnGuiDanhGia.setOnClickListener(v -> guiDanhGia());
        btnBoQua.setOnClickListener(v -> {
            danhDauDaXuLy();
            finish();
        });
    }

    private void capNhatLabel(float rating) {
        String[] labels = {"", "Rất tệ", "Tệ", "Bình thường", "Tốt", "Xuất sắc"};
        int idx = Math.min(5, Math.max(1, Math.round(rating)));
        tvSoSaoLabel.setText(labels[idx]);
    }

    private void capNhatChips(float rating) {
        chipGroup.removeAllViews();
        String[] tags = rating >= 4 ? TAGS_TOT : TAGS_KEM;
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_bg);
            chipGroup.addView(chip);
        }
    }

    private void guiDanhGia() {
        String nhanXet = edtNhanXet.getText() != null ? edtNhanXet.getText().toString().trim() : "";
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        List<String> tagsChon = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) tagsChon.add(chip.getText().toString());
        }

        Map<String, Object> danhGia = new HashMap<>();
        danhGia.put("ticketId", ticketId);
        danhGia.put("ktvUid", ktvUid);
        danhGia.put("ktvTen", ktvTen);
        danhGia.put("khachHangUid", uid);
        danhGia.put("sanPham", sanPham);
        danhGia.put("soSao", soSao);
        danhGia.put("nhanXet", nhanXet);
        danhGia.put("tags", tagsChon);
        danhGia.put("taoLuc", Timestamp.now());

        btnGuiDanhGia.setEnabled(false);

        db.collection("DanhGiaKTV").add(danhGia)
                .addOnSuccessListener(ref -> {
                    capNhatDiemKtv();
                    danhDauDaXuLy();
                    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnGuiDanhGia.setEnabled(true);
                    Toast.makeText(this, "Lỗi gửi đánh giá", Toast.LENGTH_SHORT).show();
                });
    }

    private void capNhatDiemKtv() {
        if (ktvUid == null) return;
        db.collection("DanhGiaKTV")
                .whereEqualTo("ktvUid", ktvUid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) return;
                    double tong = 0;
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                        Object s = doc.get("soSao");
                        if (s instanceof Number) tong += ((Number) s).doubleValue();
                    }
                    double trungBinh = tong / snap.size();
                    db.collection("NguoiDung").document(ktvUid)
                            .update("diemDanhGia", trungBinh, "soLuotDanhGia", snap.size());
                });
    }

    private void danhDauDaXuLy() {
        if (ticketId != null) {
            db.collection("YeuCauHoTro").document(ticketId)
                    .update("daDanhGiaKtv", true);
        }
    }
}
