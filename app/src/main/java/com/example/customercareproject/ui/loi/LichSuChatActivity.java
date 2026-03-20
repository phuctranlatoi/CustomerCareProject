package com.example.customercareproject.ui.loi;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LichSuChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lich_su_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Lịch sử hỗ trợ");
        toolbar.setNavigationOnClickListener(v -> finish());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        RecyclerView rv = findViewById(R.id.rvLichSu);
        rv.setLayoutManager(new LinearLayoutManager(this));

        FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .whereEqualTo("uid", user.getUid())
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<YeuCauHoTro> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        YeuCauHoTro t = doc.toObject(YeuCauHoTro.class);
                        t.setId(doc.getId());
                        list.add(t);
                    }
                    rv.setAdapter(new LichSuAdapter(list, ticket -> {
                        Intent intent = new Intent(this, ChatKhachHangActivity.class);
                        intent.putExtra("ticketId", ticket.getId());
                        intent.putExtra("hoTen", ticket.getHoTen());
                        startActivity(intent);
                    }));
                });
    }

    // Adapter nội bộ
    static class LichSuAdapter extends RecyclerView.Adapter<LichSuAdapter.VH> {
        private final List<YeuCauHoTro> list;
        private final OnClick onClick;

        interface OnClick { void on(YeuCauHoTro t); }

        LichSuAdapter(List<YeuCauHoTro> list, OnClick onClick) {
            this.list = list;
            this.onClick = onClick;
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
            h.tvKtv.setText(t.getKtvTen() != null ? "KTV: " + t.getKtvTen() : "Chưa phân công");

            // Màu trạng thái
            String ts = t.getTrangThai();
            h.tvTrangThai.setText(ts);
            if ("DaXuLy".equals(ts)) {
                h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if ("DangXuLy".equals(ts)) {
                h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#2196F3"));
            } else {
                h.tvTrangThai.setTextColor(android.graphics.Color.parseColor("#FF9800"));
            }

            if (t.getTaoLuc() != null) {
                h.tvThoiGian.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(new Date(t.getTaoLuc().toDate().getTime())));
            }
            h.itemView.setOnClickListener(v -> onClick.on(t));
            h.btnChat.setOnClickListener(v -> onClick.on(t));
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
