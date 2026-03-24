package com.example.customercareproject.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.ui.LoginActivity;
import com.example.customercareproject.ui.danhgia.DanhGiaActivity;
import com.example.customercareproject.ui.danhgiaktv.DanhGiaKTVActivity;
import com.example.customercareproject.ui.loi.ChatKhachHangActivity;
import com.example.customercareproject.ui.loi.LichSuChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HomeActivity extends AppCompatActivity {

    private CardView cardTicketActive;
    private TextView tvTicketActiveTieuDe, tvTicketActiveKtv;
    private Button btnChatNow;
    private String activeTicketId = null;
    private String activeHoTen = null;
    private boolean daDanhGiaPopup = false; // chỉ popup 1 lần mỗi lần mở app

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        TextView tvChaoMung = findViewById(R.id.tvChaoMung);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnLichSuChat = findViewById(R.id.btnLichSuChat);
        RecyclerView rvSanPham = findViewById(R.id.rvSanPham);

        cardTicketActive = findViewById(R.id.cardTicketActive);
        tvTicketActiveTieuDe = findViewById(R.id.tvTicketActiveTieuDe);
        tvTicketActiveKtv = findViewById(R.id.tvTicketActiveKtv);
        btnChatNow = findViewById(R.id.btnChatNow);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    tvChaoMung.setText(hoTen != null ? hoTen : "bạn");
                    activeHoTen = hoTen;
                });

        btnLogout.setOnClickListener(v -> {
            com.example.customercareproject.utils.StringeeManager.getInstance().reset();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnLichSuChat.setOnClickListener(v ->
                startActivity(new Intent(this, LichSuChatActivity.class)));

        btnChatNow.setOnClickListener(v -> {
            if (activeTicketId != null) {
                Intent intent = new Intent(this, ChatKhachHangActivity.class);
                intent.putExtra("ticketId", activeTicketId);
                intent.putExtra("hoTen", activeHoTen);
                startActivity(intent);
            }
        });

        // Load ticket active + kiểm tra đánh giá chờ
        db.collection("YeuCauHoTro")
                .whereEqualTo("uid", user.getUid())
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    activeTicketId = null;
                    for (QueryDocumentSnapshot doc : snap) {
                        String ts = doc.getString("trangThai");

                        // Ticket đang xử lý → hiện card
                        if (activeTicketId == null &&
                                ("ChoXuLy".equals(ts) || "DangXuLy".equals(ts) || "HangCho".equals(ts))) {
                            activeTicketId = doc.getId();
                            String tieuDe = doc.getString("tieuDeLoi");
                            String ktvTen = doc.getString("ktvTen");
                            tvTicketActiveTieuDe.setText(tieuDe != null ? tieuDe : "Đang xử lý...");
                            tvTicketActiveKtv.setText(ktvTen != null ? "KTV: " + ktvTen : "Đang tìm KTV...");
                            cardTicketActive.setVisibility(View.VISIBLE);
                        }

                        // Ticket DaXuLy chưa đánh giá → popup như Grab
                        if (!daDanhGiaPopup && "DaXuLy".equals(ts)) {
                            Boolean daDG = doc.getBoolean("daDanhGiaKtv");
                            String ktvUid = doc.getString("ktvUid");
                            if ((daDG == null || !daDG) && ktvUid != null && !ktvUid.isEmpty()) {
                                daDanhGiaPopup = true;
                                Intent intent = new Intent(this, DanhGiaKTVActivity.class);
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_TICKET_ID, doc.getId());
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_UID, ktvUid);
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_TEN, doc.getString("ktvTen"));
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_SAN_PHAM, doc.getString("sanPham"));
                                startActivity(intent);
                            }
                        }
                    }
                    if (activeTicketId == null) {
                        cardTicketActive.setVisibility(View.GONE);
                    }
                });

        SanPhamAdapter adapter = new SanPhamAdapter(SanPham.DANH_SACH, tenSanPham -> {
            Intent intent = new Intent(this, DanhGiaActivity.class);
            intent.putExtra("sanPham", tenSanPham);
            startActivity(intent);
        });

        rvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rvSanPham.setAdapter(adapter);
    }
}
