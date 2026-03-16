package com.example.customercareproject.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;

public class SanPhamAdapter extends RecyclerView.Adapter<SanPhamAdapter.ViewHolder> {

    private final String[] danhSach;
    private final String[] moTa;
    private final String[] icons;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String tenSanPham);
    }

    public SanPhamAdapter(String[] danhSach, OnItemClickListener listener) {
        this.danhSach = danhSach;
        this.listener = listener;
        this.moTa = new String[]{
            "Khai báo hải quan, thông quan hàng hóa",
            "Hóa đơn điện tử đa nền tảng",
            "Kê khai thuế điện tử",
            "Bảo hiểm xã hội điện tử",
            "Văn phòng điện tử, quản lý công việc",
            "Quản lý bán hàng & POS"
        };
        this.icons = new String[]{"EC", "EI", "ET", "EB", "CO", "TP"};
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_san_pham, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvIcon.setText(icons[position]);
        holder.tvTen.setText(danhSach[position]);
        holder.tvMoTa.setText(moTa[position]);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(danhSach[position]));
    }

    @Override
    public int getItemCount() { return danhSach.length; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTen, tvMoTa;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIconSanPham);
            tvTen = itemView.findViewById(R.id.tvTenSanPham);
            tvMoTa = itemView.findViewById(R.id.tvMoTaSanPham);
        }
    }
}
