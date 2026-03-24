package com.example.customercareproject.ui.ktv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.utils.SmartRouter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class KtvTicketDetailActivity extends AppCompatActivity {

    private String ticketId;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private YeuCauHoTro ticketHienTai;
    private ListenerRegistration ticketListener;
    // Tránh update DangXuLy nhiều lần khi listener fire
    private boolean daCapNhatTrangThai = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ktv_ticket_detail);

        ticketId = getIntent().getStringExtra("ticketId");
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        com.google.android.material.button.MaterialButton btnNhanTin = findViewById(R.id.btnNhanTin);
        com.google.android.material.button.MaterialButton btnDongTicket = findViewById(R.id.btnDongTicket);

        // Mở chat fullscreen
        btnNhanTin.setOnClickListener(v -> {
            Intent intent = new Intent(this, KtvChatActivity.class);
            intent.putExtra("ticketId", ticketId);
            if (ticketHienTai != null) intent.putExtra("tenKhachHang", ticketHienTai.getHoTen());
            startActivity(intent);
        });

        btnDongTicket.setOnClickListener(v -> dongTicket());

        taiChiTietTicket();
    }

    private void taiChiTietTicket() {
        ticketListener = db.collection("YeuCauHoTro").document(ticketId)
                .addSnapshotListener((doc, e) -> {
                    if (doc == null || !doc.exists()) return;
                    ticketHienTai = doc.toObject(YeuCauHoTro.class);
                    if (ticketHienTai == null) return;
                    ticketHienTai.setId(doc.getId());

                    ((TextView) findViewById(R.id.tvTieuDe)).setText(ticketHienTai.getTieuDeLoi());
                    ((TextView) findViewById(R.id.tvSanPham)).setText(ticketHienTai.getSanPham());
                    ((TextView) findViewById(R.id.tvMoTa)).setText(ticketHienTai.getMoTaVanDe());
                    ((TextView) findViewById(R.id.tvHoTen)).setText("Họ tên: " + ticketHienTai.getHoTen());
                    ((TextView) findViewById(R.id.tvSdt)).setText("SĐT: " + ticketHienTai.getSoDienThoai());
                    ((TextView) findViewById(R.id.tvEmailKh)).setText("Email: " + ticketHienTai.getEmail());

                    // Ưu tiên badge
                    TextView tvUuTien = findViewById(R.id.tvUuTienDetail);
                    String uu = ticketHienTai.getUuTien();
                    if ("Cao".equals(uu)) {
                        tvUuTien.setText("Khẩn cấp");
                        tvUuTien.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                    } else if ("Thap".equals(uu)) {
                        tvUuTien.setText("Ưu tiên thấp");
                        tvUuTien.setTextColor(android.graphics.Color.parseColor("#388E3C"));
                    } else {
                        tvUuTien.setText("Bình thường");
                        tvUuTien.setTextColor(android.graphics.Color.parseColor("#F57F17"));
                    }

                    // Chuyển sang DangXuLy khi KTV mở ticket, đồng thời lưu tên KTV
                    // Chỉ update 1 lần để tránh vòng lặp listener
                    if (!daCapNhatTrangThai &&
                            ("ChoXuLy".equals(ticketHienTai.getTrangThai()) || "HangCho".equals(ticketHienTai.getTrangThai()))) {
                        daCapNhatTrangThai = true;
                        db.collection("NguoiDung").document(user.getUid()).get()
                                .addOnSuccessListener(ktvDoc -> {
                                    String tenKtv = ktvDoc.getString("hoTen");
                                    db.collection("YeuCauHoTro").document(ticketId)
                                            .update("trangThai", "DangXuLy",
                                                    "capNhatLuc", Timestamp.now(),
                                                    "ktvUid", user.getUid(),
                                                    "ktvTen", tenKtv != null ? tenKtv : "");
                                });
                    }

                    if ("DaXuLy".equals(ticketHienTai.getTrangThai())) {
                        findViewById(R.id.btnDongTicket).setVisibility(View.GONE);
                    }
                });
    }

    private void dongTicket() {
        if (ticketHienTai == null) return;
        db.collection("YeuCauHoTro").document(ticketId)
                .update("trangThai", "DaXuLy",
                        "capNhatLuc", Timestamp.now(),
                        "ktvTen", ticketHienTai.getKtvTen() != null ? ticketHienTai.getKtvTen() : "")
                .addOnSuccessListener(v -> {
                    SmartRouter.giamTicketKtv(user.getUid());
                    Toast.makeText(this, "Ticket đã đóng!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketListener != null) ticketListener.remove();
    }
}
