package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DanhGiaKtvAdapter extends RecyclerView.Adapter<DanhGiaKtvAdapter.VH> {

    public static class Item {
        public String tenKh, nhanXet;
        public float soSao;
        public List<String> tags;
        public Timestamp taoLuc;

        public Item(String tenKh, float soSao, String nhanXet, List<String> tags, Timestamp taoLuc) {
            this.tenKh = tenKh;
            this.soSao = soSao;
            this.nhanXet = nhanXet;
            this.tags = tags;
            this.taoLuc = taoLuc;
        }
    }

    private List<Item> list;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    private static final int STAR_ON = 0xFFF59E0B;
    private static final int STAR_OFF = 0xFFE2E8F0;

    public DanhGiaKtvAdapter(List<Item> list) { this.list = list; }

    public void capNhat(List<Item> data) { this.list = data; notifyDataSetChanged(); }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_danh_gia_ktv, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Item item = list.get(position);

        h.divider.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        // Avatar initial
        String initial = (item.tenKh != null && !item.tenKh.isEmpty())
                ? String.valueOf(item.tenKh.charAt(0)).toUpperCase() : "K";
        h.tvAvatar.setText(initial);

        h.tvTen.setText(item.tenKh != null ? item.tenKh : "Khách hàng");

        // Stars
        TextView[] stars = {h.star1, h.star2, h.star3, h.star4, h.star5};
        int filled = Math.round(item.soSao);
        for (int i = 0; i < 5; i++) {
            stars[i].setTextColor(i < filled ? STAR_ON : STAR_OFF);
        }

        // Date
        if (item.taoLuc != null) {
            h.tvNgay.setText(SDF.format(item.taoLuc.toDate()));
        }

        // Tags
        h.chipTags.removeAllViews();
        if (item.tags != null && !item.tags.isEmpty()) {
            for (String tag : item.tags) {
                Chip chip = new Chip(h.itemView.getContext());
                chip.setText(tag);
                chip.setClickable(false);
                chip.setTextSize(10f);
                chip.setChipMinHeight(28f);
                chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(0xFFEEF4FF));
                chip.setTextColor(Color.parseColor("#1976D2"));
                h.chipTags.addView(chip);
            }
        }

        // Nội dung
        if (item.nhanXet != null && !item.nhanXet.isEmpty()) {
            h.tvNhanXet.setVisibility(View.VISIBLE);
            h.tvNhanXet.setText(item.nhanXet);
        } else {
            h.tvNhanXet.setVisibility(View.GONE);
        }
    }

    @Override public int getItemCount() { return list.size(); }

    static class VH extends RecyclerView.ViewHolder {
        View divider;
        TextView tvAvatar, tvTen, tvNgay, tvNhanXet;
        TextView star1, star2, star3, star4, star5;
        ChipGroup chipTags;

        VH(@NonNull View v) {
            super(v);
            divider = v.findViewById(R.id.divider);
            tvAvatar = v.findViewById(R.id.tvAvatarKh);
            tvTen = v.findViewById(R.id.tvTenKh);
            tvNgay = v.findViewById(R.id.tvNgay);
            tvNhanXet = v.findViewById(R.id.tvNhanXet);
            star1 = v.findViewById(R.id.star1);
            star2 = v.findViewById(R.id.star2);
            star3 = v.findViewById(R.id.star3);
            star4 = v.findViewById(R.id.star4);
            star5 = v.findViewById(R.id.star5);
            chipTags = v.findViewById(R.id.chipTags);
        }
    }
}
