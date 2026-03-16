package com.example.customercareproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;

import java.util.List;

public class ThongKeSanPhamAdapter extends RecyclerView.Adapter<ThongKeSanPhamAdapter.ViewHolder> {

    private final List<String[]> danhSach; // [tenSanPham, soLuong]

    public ThongKeSanPhamAdapter(List<String[]> danhSach) {
        this.danhSach = danhSach;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String[] item = danhSach.get(position);
        holder.tvTen.setText(item[0]);
        holder.tvSo.setText(item[1] + " đánh giá");
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTen, tvSo;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTen = itemView.findViewById(android.R.id.text1);
            tvSo = itemView.findViewById(android.R.id.text2);
        }
    }
}
