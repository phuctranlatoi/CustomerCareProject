package com.example.customercareproject.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LeadKinhDoanh;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LeadAdapter extends RecyclerView.Adapter<LeadAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(LeadKinhDoanh lead);
    }

    private List<LeadKinhDoanh> list;
    private final OnItemClickListener listener;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public LeadAdapter(List<LeadKinhDoanh> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void capNhat(List<LeadKinhDoanh> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lead, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        LeadKinhDoanh lead = list.get(position);

        // Tên công ty
        String tenCongTy = lead.getTenCongTy();
        h.tvTenCongTy.setText(tenCongTy != null ? tenCongTy : "(Không rõ)");

        // MST
        String mst = lead.getMaSoThue();
        h.tvMaSoThue.setText(mst != null ? "MST: " + mst : "");

        // Sản phẩm
        String sanPham = lead.getSanPham();
        h.tvSanPham.setText(sanPham != null ? sanPham : "");

        // Số sao
        int soSao = lead.getSoSao();
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < soSao; i++) stars.append("★");
        for (int i = soSao; i < 5; i++) stars.append("☆");
        h.tvSoSao.setText(stars.toString());

        // Trạng thái badge
        String trangThai = lead.getTrangThaiLead();
        int badgeColor;
        String badgeText;
        if (LeadKinhDoanh.TRANG_THAI_MOI.equals(trangThai)) {
            badgeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.primary);
            badgeText = "Mới";
        } else if (LeadKinhDoanh.TRANG_THAI_DANG_TU_VAN.equals(trangThai)) {
            badgeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.warning);
            badgeText = "Đang tư vấn";
        } else if (LeadKinhDoanh.TRANG_THAI_DA_DANG_KY.equals(trangThai)) {
            badgeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.success);
            badgeText = "Đã đăng ký";
        } else if (LeadKinhDoanh.TRANG_THAI_TU_CHOI.equals(trangThai)) {
            badgeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.error);
            badgeText = "Từ chối";
        } else {
            badgeColor = ContextCompat.getColor(h.itemView.getContext(), R.color.primary);
            badgeText = trangThai != null ? trangThai : "Mới";
        }
        h.tvTrangThaiLead.setText(badgeText);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(24f);
        bg.setColor(badgeColor);
        h.tvTrangThaiLead.setBackground(bg);

        // Thời gian tạo
        Timestamp taoLuc = lead.getTaoLuc();
        if (taoLuc != null) {
            h.tvTaoLuc.setText(SDF.format(taoLuc.toDate()));
        } else {
            h.tvTaoLuc.setText("");
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(lead);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTenCongTy, tvMaSoThue, tvSanPham, tvSoSao, tvTrangThaiLead, tvTaoLuc;

        VH(@NonNull View v) {
            super(v);
            tvTenCongTy = v.findViewById(R.id.tvTenCongTy);
            tvMaSoThue = v.findViewById(R.id.tvMaSoThue);
            tvSanPham = v.findViewById(R.id.tvSanPham);
            tvSoSao = v.findViewById(R.id.tvSoSao);
            tvTrangThaiLead = v.findViewById(R.id.tvTrangThaiLead);
            tvTaoLuc = v.findViewById(R.id.tvTaoLuc);
        }
    }
}
