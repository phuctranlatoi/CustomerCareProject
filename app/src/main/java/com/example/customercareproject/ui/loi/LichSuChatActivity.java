package com.example.customercareproject.ui.loi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.ui.danhgiaktv.DanhGiaKTVActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LichSuChatActivity extends AppCompatActivity {

    private ListenerRegistration listener;
    private RecyclerView rv;
    private LinearLayout layoutEmpty;
    private LichSuAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su_chat);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        rv = findViewById(R.id.rvLichSu);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new LichSuAdapter(new ArrayList<>(), ticket -> {
            // Nếu DaXuLy chưa đánh giá → mở đánh giá trước
            if ("DaXuLy".equals(ticket.getTrangThai())
                    && !ticket.isDaDanhGiaKtv()
                    && ticket.getKtvUid() != null && !ticket.getKtvUid().isEmpty()) {
                Intent intent = new Intent(this, DanhGiaKTVActivity.class);
                intent.putExtra(DanhGiaKTVActivity.EXTRA_TICKET_ID, ticket.getId());
                intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_UID, ticket.getKtvUid());
                intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_TEN, ticket.getKtvTen());
                intent.putExtra(DanhGiaKTVActivity.EXTRA_SAN_PHAM, ticket.getSanPham());
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, ChatKhachHangActivity.class);
                intent.putExtra("ticketId", ticket.getId());
                intent.putExtra("hoTen", ticket.getHoTen());
                intent.putExtra("trangThai", ticket.getTrangThai());
                startActivity(intent);
            }
        });
        rv.setAdapter(adapter);

        // Realtime listener
        listener = FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .whereEqualTo("uid", user.getUid())
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    List<YeuCauHoTro> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        YeuCauHoTro t = doc.toObject(YeuCauHoTro.class);
                        t.setId(doc.getId());
                        list.add(t);
                    }
                    adapter.capNhat(list);
                    if (list.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rv.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rv.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }

    // Adapter nội bộ
    static class LichSuAdapter extends RecyclerView.Adapter<LichSuAdapter.VH> {
        private List<YeuCauHoTro> list;
        private final OnClick onClick;

        interface OnClick { void on(YeuCauHoTro t); }

        LichSuAdapter(List<YeuCauHoTro> list, OnClick onClick) {
            this.list = list;
            this.onClick = onClick;
        }

        void capNhat(List<YeuCauHoTro> newList) {
            this.list = newList;
            notifyDataSetChanged();
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_lich_su_chat, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            YeuCauHoTro t = list.get(pos);
            h.tvTieuDe.setText(t.getTieuDeLoi());
            h.tvSanPham.setText(t.getSanPham());
            h.tvKtv.setText(t.getKtvTen() != null && !t.getKtvTen().isEmpty()
                    ? "KTV: " + t.getKtvTen() : "Chưa phân công");

            String ts = t.getTrangThai();
            if ("DaXuLy".equals(ts)) {
                if (!t.isDaDanhGiaKtv() && t.getKtvUid() != null && !t.getKtvUid().isEmpty()) {
                    h.tvTrangThai.setText("⭐ Chờ đánh giá");
                    h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#FF9800"));
                    h.btnChat.setText("Đánh giá ngay");
                } else {
                    h.tvTrangThai.setText("Đã xử lý");
                    h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                    h.btnChat.setText("Xem lịch sử");
                }
            } else if ("DangXuLy".equals(ts)) {
                h.tvTrangThai.setText("Đang xử lý");
                h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#2196F3"));
            } else if ("HangCho".equals(ts)) {
                h.tvTrangThai.setText("Hàng chờ");
                h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#9C27B0"));
            } else {
                h.tvTrangThai.setText("Chờ xử lý");
                h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            }

            if (t.getTaoLuc() != null) {
                h.tvThoiGian.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date(t.getTaoLuc().toDate().getTime())));
            }
            h.itemView.setOnClickListener(v -> onClick.on(t));
            h.btnChat.setOnClickListener(v -> onClick.on(t));
            // Set text nút cho các trạng thái không phải DaXuLy
            if (!"DaXuLy".equals(ts)) {
                h.btnChat.setText("Tiếp tục chat");
            }
        }

        @Override public int getItemCount() { return list.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTieuDe, tvSanPham, tvKtv, tvTrangThai, tvThoiGian;
            android.widget.Button btnChat;
            VH(@NonNull View v) {
                super(v);
                tvTieuDe = v.findViewById(R.id.tvTieuDe);
                tvSanPham = v.findViewById(R.id.tvSanPham);
                tvKtv = v.findViewById(R.id.tvKtv);
                tvTrangThai = v.findViewById(R.id.tvTrangThai);
                tvThoiGian = v.findViewById(R.id.tvThoiGian);
                btnChat = v.findViewById(R.id.btnChat);
            }
        }
    }
}
