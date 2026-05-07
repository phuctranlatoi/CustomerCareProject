package com.example.customercareproject.ui.admin;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.model.GoiDangKy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class GoiDangKyAdapter extends RecyclerView.Adapter<GoiDangKyAdapter.VH> {

    public interface Listener {
        void onSua(GoiDangKy goi, String docId);
        void onXoa(GoiDangKy goi, String docId);
    }

    private List<GoiDangKy> list;
    private List<String> docIds;
    private final Listener listener;

    public GoiDangKyAdapter(List<GoiDangKy> list, List<String> docIds, Listener listener) {
        this.list = list;
        this.docIds = docIds;
        this.listener = listener;
    }

    public void capNhat(List<GoiDangKy> newList, List<String> newIds) {
        this.list = newList;
        this.docIds = newIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goi_dang_ky, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        GoiDangKy goi = list.get(position);
        String docId = docIds.get(position);

        h.tvTenCongTy.setText(goi.getTenCongTy() != null ? goi.getTenCongTy() : "");
        h.tvMaSoThue.setText("MST: " + (goi.getMaSoThue() != null ? goi.getMaSoThue() : ""));

        // Badge: hiện số SP chính thức + dùng thử thay vì loại gói chung
        int soChinhThuc = goi.getSanPhamChinhThuc().size();
        int soDungThu   = goi.getSanPhamDungThu().size();
        String badgeText = soChinhThuc + " CT";
        if (soDungThu > 0) badgeText += " · " + soDungThu + " DT";
        h.tvLoaiGoi.setText(badgeText);
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setShape(GradientDrawable.RECTANGLE);
        badgeBg.setCornerRadius(20f);
        badgeBg.setColor((int)(soDungThu > 0 ? 
            ContextCompat.getColor(h.itemView.getContext(), R.color.primary_800) : 
            ContextCompat.getColor(h.itemView.getContext(), R.color.primary)));
        h.tvLoaiGoi.setBackground(badgeBg);

        // Trạng thái
        String ts = goi.getTrangThai();
        if (GoiDangKy.TRANG_THAI_HOAT_DONG.equals(ts)) {
            h.tvTrangThai.setText("● Hoạt động");
            h.tvTrangThai.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.success));
        } else if (GoiDangKy.TRANG_THAI_HET_HAN.equals(ts)) {
            h.tvTrangThai.setText("● Hết hạn");
            h.tvTrangThai.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.error));
        } else {
            h.tvTrangThai.setText("● Tạm dừng");
            h.tvTrangThai.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.warning));
        }

        // Chip sản phẩm — phân biệt chính thức vs dùng thử
        h.chipGroup.removeAllViews();
        List<String> chinhThuc = goi.getSanPhamChinhThuc();
        List<String> dungThu   = goi.getSanPhamDungThu();

        for (String sp : chinhThuc) {
            Chip chip = new Chip(h.itemView.getContext());
            chip.setText(sp);
            chip.setTextSize(10f);
            chip.setChipBackgroundColorResource(R.color.chip_bg);
            chip.setClickable(false);
            chip.setCheckable(false);
            h.chipGroup.addView(chip);
        }
        for (String sp : dungThu) {
            Chip chip = new Chip(h.itemView.getContext());
            chip.setText(sp + " 🔍");
            chip.setTextSize(10f);
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(h.itemView.getContext(), R.color.warning_container)));
            chip.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.on_warning_container));
            chip.setClickable(false);
            chip.setCheckable(false);
            h.chipGroup.addView(chip);
        }

        h.btnSua.setOnClickListener(v -> listener.onSua(goi, docId));
        h.btnXoa.setOnClickListener(v -> listener.onXoa(goi, docId));
    }

    @Override
    public int getItemCount() { return list != null ? list.size() : 0; }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTenCongTy, tvMaSoThue, tvLoaiGoi, tvTrangThai;
        ChipGroup chipGroup;
        MaterialButton btnSua, btnXoa;

        VH(@NonNull View v) {
            super(v);
            tvTenCongTy = v.findViewById(R.id.tvTenCongTy);
            tvMaSoThue  = v.findViewById(R.id.tvMaSoThue);
            tvLoaiGoi   = v.findViewById(R.id.tvLoaiGoi);
            tvTrangThai = v.findViewById(R.id.tvTrangThai);
            chipGroup   = v.findViewById(R.id.chipGroupSanPham);
            btnSua      = v.findViewById(R.id.btnSua);
            btnXoa      = v.findViewById(R.id.btnXoa);
        }
    }
}
