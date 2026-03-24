package com.example.customercareproject.ui.loi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LoiPhatSinh;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChiTietLoiActivity extends AppCompatActivity {

    private String loiId, sanPham;
    private LoiPhatSinh loiHienTai;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_loi);

        loiId = getIntent().getStringExtra("loiId");
        sanPham = getIntent().getStringExtra("sanPham");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        com.google.android.material.button.MaterialButton btnDaTuXuLy = findViewById(R.id.btnDaTuXuLy);
        com.google.android.material.button.MaterialButton btnYeuCauHoTro = findViewById(R.id.btnYeuCauHoTro);

        btnDaTuXuLy.setOnClickListener(v -> {
            Toast.makeText(this, "Vui lòng liên hệ nếu vấn đề tái diễn!", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnYeuCauHoTro.setOnClickListener(v -> {
            Intent intent = new Intent(this, YeuCauHoTroActivity.class);
            intent.putExtra("sanPham", sanPham);
            if (loiHienTai != null) {
                intent.putExtra("loiId", loiId);
                intent.putExtra("tieuDeLoi", loiHienTai.getTieuDe());
            } else {
                intent.putExtra("loiId", "");
                intent.putExtra("tieuDeLoi", "Vấn đề khác");
            }
            startActivity(intent);
        });

        if (loiId != null && !loiId.isEmpty()) {
            taiChiTietLoi();
        } else {
            // Không có lỗi cụ thể, chỉ hiện nút gửi yêu cầu
            findViewById(R.id.tvTieuDe).setVisibility(View.GONE);
            btnYeuCauHoTro.setText("Gửi yêu cầu hỗ trợ mới");
        }
    }

    private void taiChiTietLoi() {
        FirebaseFirestore.getInstance().collection("LoiPhatSinh")
                .document(loiId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    loiHienTai = doc.toObject(LoiPhatSinh.class);
                    if (loiHienTai == null) return;
                    loiHienTai.setId(doc.getId());

                    ((TextView) findViewById(R.id.tvTieuDe)).setText(loiHienTai.getTieuDe());
                    ((TextView) findViewById(R.id.tvSanPham)).setText(loiHienTai.getSanPham());
                    ((TextView) findViewById(R.id.tvMoTa)).setText(loiHienTai.getMoTa());

                    if (loiHienTai.isCoHuongDan() && loiHienTai.getCachGiaiQuyet() != null) {
                        findViewById(R.id.cardHuongDan).setVisibility(View.VISIBLE);
                        ((TextView) findViewById(R.id.tvCachGiaiQuyet)).setText(loiHienTai.getCachGiaiQuyet());
                        findViewById(R.id.btnDaTuXuLy).setVisibility(View.VISIBLE);
                    }
                });
    }
}
