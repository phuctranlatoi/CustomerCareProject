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
        TextView tvEmail = findViewById(R.id.tvEmail);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        ImageButton btnLichSuChat = findViewById(R.id.btnLichSuChat);
        RecyclerView rvSanPham = findViewById(R.id.rvSanPham);

        cardTicketActive = findViewById(R.id.cardTicketActive);
        tvTicketActiveTieuDe = findViewById(R.id.tvTicketActiveTieuDe);
        tvTicketActiveKtv = findViewById(R.id.tvTicketActiveKtv);
        btnChatNow = findViewById(R.id.btnChatNow);

        // Lấy tên từ Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    tvChaoMung.setText("Xin chào, " + (hoTen != null ? hoTen : "bạn") + "!");
                    activeHoTen = hoTen;
                });
        tvEmail.setText(user.getEmail());

        btnLogout.setOnClickListener(v -> {
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

        // Load ticket đang active (ChoXuLy hoặc DangXuLy)
        db.collection("YeuCauHoTro")
                .whereEqualTo("uid", user.getUid())
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    activeTicketId = null;
                    for (QueryDocumentSnapshot doc : snap) {
                        String ts = doc.getString("trangThai");
                        if ("ChoXuLy".equals(ts) || "DangXuLy".equals(ts) || "HangCho".equals(ts)) {
                            activeTicketId = doc.getId();
                            String tieuDe = doc.getString("tieuDeLoi");
                            String ktvTen = doc.getString("ktvTen");
                            tvTicketActiveTieuDe.setText(tieuDe != null ? tieuDe : "Đang xử lý...");
                            tvTicketActiveKtv.setText(ktvTen != null ? "KTV: " + ktvTen : "Đang tìm KTV...");
                            cardTicketActive.setVisibility(View.VISIBLE);
                            break;
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
