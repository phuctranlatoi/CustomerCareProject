package com.example.customercareproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;

import java.util.List;

public class KtvReviewSummaryAdapter extends RecyclerView.Adapter<KtvReviewSummaryAdapter.VH> {

    public static class KtvItem {
        public String uid, tenKtv, trangThai;
        public double diemTB;
        public int soLuot;

        public KtvItem(String uid, String tenKtv, String trangThai, double diemTB, int soLuot) {
            this.uid = uid;
            this.tenKtv = tenKtv;
            this.trangThai = trangThai;
            this.diemTB = diemTB;
            this.soLuot = soLuot;
        }
    }

    public interface OnKtvClick { void onClick(KtvItem item); }

    private List<KtvItem> list;
    private final OnKtvClick listener;

    public KtvReviewSummaryAdapter(List<KtvItem> list, OnKtvClick listener) {
        this.list = list;
        this.listener = listener;
    }

    public void capNhat(List<KtvItem> data) { this.list = data; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ktv_review_summary, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        KtvItem item = list.get(position);

        String initial = (item.tenKtv != null && !item.tenKtv.isEmpty())
                ? String.valueOf(item.tenKtv.charAt(0)).toUpperCase() : "K";
        h.tvAvatar.setText(initial);
        h.tvTen.setText(item.tenKtv);
        h.tvDiem.setText(String.format("%.1f", item.diemTB));
        h.tvSoLuot.setText("(" + item.soLuot + " đánh giá)");

        // Stars
        int filled = (int) Math.round(item.diemTB);
        TextView[] stars = {h.sao1, h.sao2, h.sao3, h.sao4, h.sao5};
        int starOnColor = ContextCompat.getColor(h.itemView.getContext(), R.color.star_active);
        int starOffColor = ContextCompat.getColor(h.itemView.getContext(), R.color.star_inactive);
        for (int i = 0; i < 5; i++) {
            stars[i].setTextColor(i < filled ? starOnColor : starOffColor);
        }

        // Trạng thái
        if ("Ran".equals(item.trangThai)) {
            h.tvTrangThai.setText("● Rảnh");
            h.tvTrangThai.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.success));
        } else if ("DangBan".equals(item.trangThai)) {
            h.tvTrangThai.setText("● Đang bận");
            h.tvTrangThai.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.warning));
        } else {
            h.tvTrangThai.setText("● Offline");
            h.tvTrangThai.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.text_secondary));
        }

        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvTen, tvDiem, tvSoLuot, tvTrangThai;
        TextView sao1, sao2, sao3, sao4, sao5;

        VH(@NonNull View v) {
            super(v);
            tvAvatar = v.findViewById(R.id.tvAvatar);
            tvTen = v.findViewById(R.id.tvTenKtv);
            tvDiem = v.findViewById(R.id.tvDiem);
            tvSoLuot = v.findViewById(R.id.tvSoLuot);
            tvTrangThai = v.findViewById(R.id.tvTrangThai);
            sao1 = v.findViewById(R.id.tvSao1);
            sao2 = v.findViewById(R.id.tvSao2);
            sao3 = v.findViewById(R.id.tvSao3);
            sao4 = v.findViewById(R.id.tvSao4);
            sao5 = v.findViewById(R.id.tvSao5);
        }
    }
}
