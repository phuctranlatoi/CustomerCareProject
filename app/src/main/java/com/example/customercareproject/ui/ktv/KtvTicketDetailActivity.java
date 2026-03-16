package com.example.customercareproject.ui.ktv;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.TinNhan;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.utils.SmartRouter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class KtvTicketDetailActivity extends AppCompatActivity {

    private String ticketId;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ChatAdapter chatAdapter;
    private ListenerRegistration chatListener;
    private YeuCauHoTro ticketHienTai;
    private String tenKtv = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ktv_ticket_detail);

        ticketId = getIntent().getStringExtra("ticketId");
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvChat = findViewById(R.id.rvChat);
        chatAdapter = new ChatAdapter(new ArrayList<>(), user != null ? user.getUid() : "");
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        Button btnGuiChat = findViewById(R.id.btnGuiChat);
        Button btnDongTicket = findViewById(R.id.btnDongTicket);
        TextInputEditText edtPhanHoi = findViewById(R.id.edtPhanHoi);

        // Lấy tên KTV
        if (user != null) {
            db.collection("NguoiDung").document(user.getUid()).get()
                    .addOnSuccessListener(doc -> tenKtv = doc.getString("hoTen") != null ? doc.getString("hoTen") : "KTV");
        }

        btnGuiChat.setOnClickListener(v -> {
            String nd = edtPhanHoi.getText() != null ? edtPhanHoi.getText().toString().trim() : "";
            if (nd.isEmpty()) return;
            guiTinNhan(nd);
            edtPhanHoi.setText("");
        });

        btnDongTicket.setOnClickListener(v -> dongTicket());

        taiChiTietTicket();
        laNgheChat();
    }

    private void taiChiTietTicket() {
        db.collection("YeuCauHoTro").document(ticketId).get()
                .addOnSuccessListener(doc -> {
                    ticketHienTai = doc.toObject(YeuCauHoTro.class);
                    if (ticketHienTai == null) return;
                    ticketHienTai.setId(doc.getId());

                    ((TextView) findViewById(R.id.tvTieuDe)).setText(ticketHienTai.getTieuDeLoi());
                    ((TextView) findViewById(R.id.tvSanPham)).setText(ticketHienTai.getSanPham());
                    ((TextView) findViewById(R.id.tvMoTa)).setText(ticketHienTai.getMoTaVanDe());
                    ((TextView) findViewById(R.id.tvHoTen)).setText("Họ tên: " + ticketHienTai.getHoTen());
                    ((TextView) findViewById(R.id.tvSdt)).setText("SĐT: " + ticketHienTai.getSoDienThoai());
                    ((TextView) findViewById(R.id.tvEmailKh)).setText("Email: " + ticketHienTai.getEmail());

                    // Chuyển sang DangXuLy nếu đang ChoXuLy
                    if ("ChoXuLy".equals(ticketHienTai.getTrangThai())) {
                        db.collection("YeuCauHoTro").document(ticketId)
                                .update("trangThai", "DangXuLy", "capNhatLuc", Timestamp.now());
                    }

                    // Ẩn nút đóng nếu đã xử lý
                    if ("DaXuLy".equals(ticketHienTai.getTrangThai())) {
                        findViewById(R.id.btnDongTicket).setVisibility(View.GONE);
                    }
                });
    }

    private void guiTinNhan(String noiDung) {
        if (user == null) return;
        TinNhan tin = new TinNhan(ticketId, user.getUid(), tenKtv, "KTV", noiDung);
        db.collection("TinNhan").add(tin)
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void laNgheChat() {
        chatListener = db.collection("TinNhan")
                .whereEqualTo("ticketId", ticketId)
                .orderBy("thoiGian", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    List<TinNhan> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        TinNhan t = doc.toObject(TinNhan.class);
                        t.setId(doc.getId());
                        list.add(t);
                    }
                    chatAdapter.capNhat(list);
                    if (!list.isEmpty()) {
                        ((RecyclerView) findViewById(R.id.rvChat))
                                .scrollToPosition(list.size() - 1);
                    }
                });
    }

    private void dongTicket() {
        if (ticketHienTai == null) return;
        db.collection("YeuCauHoTro").document(ticketId)
                .update("trangThai", "DaXuLy", "capNhatLuc", Timestamp.now())
                .addOnSuccessListener(v -> {
                    SmartRouter.giamTicketKtv(user.getUid());
                    Toast.makeText(this, "Ticket đã đóng!", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) chatListener.remove();
    }
}
