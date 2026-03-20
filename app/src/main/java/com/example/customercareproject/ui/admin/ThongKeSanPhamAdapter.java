package com.example.customercareproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;

import java.util.List;

public class ThongKeSanPhamAdapter extends RecyclerView.Adapter<ThongKeSanPhamAdapter.ViewHolder> {

    public static class Item {
        public String ten;
        public int soLuong;
        public int maxSoLuong;

        public Item(String ten, int soLuong, int maxSoLuong) {
            this.ten = ten;
            this.soLuong = soLuong;
            this.maxSoLuong = maxSoLuong;
        }
    }

    private final List<Item> danhSach;

    public ThongKeSanPhamAdapter(List<Item> danhSach) {
        this.danhSach = danhSach;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thong_ke_san_pham, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = danhSach.get(position);
        holder.tvTen.setText(item.ten);
        holder.tvSo.setText(item.soLuong + " đánh giá");
        int progress = item.maxSoLuong > 0 ? (item.soLuong * 100 / item.maxSoLuong) : 0;
        holder.pb.setProgress(progress);
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvSo;
        ProgressBar pb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(R.id.tvTenSanPham);
            tvSo  = itemView.findViewById(R.id.tvSoLuong);
            pb    = itemView.findViewById(R.id.pbSanPham);
        }
    }
}
