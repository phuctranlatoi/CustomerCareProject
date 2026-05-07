package com.example.customercareproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DanhGiaXauAdapter extends RecyclerView.Adapter<DanhGiaXauAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(DanhGia danhGia);
    }

    private List<DanhGia> list;
    private Map<String, Integer> soLanHoTroMap; // uid -> soLanHoTro
    private final OnItemClickListener listener;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public DanhGiaXauAdapter(List<DanhGia> list, Map<String, Integer> soLanHoTroMap, OnItemClickListener listener) {
        this.list = list;
        this.soLanHoTroMap = soLanHoTroMap;
        this.listener = listener;
    }

    public void capNhat(List<DanhGia> newList, Map<String, Integer> newMap) {
        this.list = newList;
        this.soLanHoTroMap = newMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_danh_gia_xau, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        DanhGia dg = list.get(position);

        // Tên công ty
        String tenCongTy = dg.getTenCongTy();
        h.tvTenCongTy.setText(tenCongTy != null ? tenCongTy : "(Không rõ)");

        // MST
        String mst = dg.getMaSoThue();
        h.tvMaSoThue.setText(mst != null ? "MST: " + mst : "");

        // Sản phẩm chip
        String sanPham = dg.getSanPham();
        h.chipSanPham.setText(sanPham != null ? sanPham : "");

        // Số sao (đỏ nếu <= 2)
        int soSao = dg.getSoSao();
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < soSao; i++) stars.append("★");
        for (int i = soSao; i < 5; i++) stars.append("☆");
        h.tvSoSao.setText(stars.toString());
        if (soSao <= 2) {
            h.tvSoSao.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.error));
        } else {
            h.tvSoSao.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.warning));
        }

        // Nội dung
        String noiDung = dg.getNoiDung();
        h.tvNoiDung.setText(noiDung != null ? noiDung : "");

        // Thời gian
        Timestamp taoLuc = dg.getTaoLuc();
        if (taoLuc != null) {
            Date date = taoLuc.toDate();
            h.tvThoiGian.setText(SDF.format(date));
        } else {
            h.tvThoiGian.setText("");
        }

        // Số lần hỗ trợ (badge cam)
        String uid = dg.getUid();
        Integer soLan = (uid != null && soLanHoTroMap != null) ? soLanHoTroMap.get(uid) : null;
        if (soLan != null && soLan > 0) {
            h.tvSoLanHoTro.setText(soLan + " lần HT");
            h.tvSoLanHoTro.setVisibility(View.VISIBLE);
        } else {
            h.tvSoLanHoTro.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(dg);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTenCongTy, tvMaSoThue, tvSoSao, tvNoiDung, tvThoiGian, tvSoLanHoTro;
        Chip chipSanPham;

        VH(@NonNull View v) {
            super(v);
            tvTenCongTy = v.findViewById(R.id.tvTenCongTy);
            tvMaSoThue = v.findViewById(R.id.tvMaSoThue);
            chipSanPham = v.findViewById(R.id.chipSanPham);
            tvSoSao = v.findViewById(R.id.tvSoSao);
            tvNoiDung = v.findViewById(R.id.tvNoiDung);
            tvThoiGian = v.findViewById(R.id.tvThoiGian);
            tvSoLanHoTro = v.findViewById(R.id.tvSoLanHoTro);
        }
    }
}
