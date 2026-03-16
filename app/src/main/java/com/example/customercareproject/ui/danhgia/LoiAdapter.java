package com.example.customercareproject.ui.danhgia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LoiPhatSinh;

import java.util.List;

public class LoiAdapter extends RecyclerView.Adapter<LoiAdapter.ViewHolder> {

    private List<LoiPhatSinh> danhSach;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LoiPhatSinh loi);
    }

    public LoiAdapter(List<LoiPhatSinh> danhSach, OnItemClickListener listener) {
        this.danhSach = danhSach;
        this.listener = listener;
    }

    public void capNhat(List<LoiPhatSinh> list) {
        this.danhSach = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loi_phat_sinh, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoiPhatSinh loi = danhSach.get(position);
        holder.tvTieuDe.setText(loi.getTieuDe());
        holder.tvMoTa.setText(loi.getMoTa());
        if (loi.isCoHuongDan()) {
            holder.tvCoHuongDan.setText("Có hướng dẫn tự xử lý");
            holder.tvCoHuongDan.setTextColor(holder.itemView.getContext().getColor(R.color.success));
        } else {
            holder.tvCoHuongDan.setText("Cần hỗ trợ kỹ thuật");
            holder.tvCoHuongDan.setTextColor(holder.itemView.getContext().getColor(R.color.warning));
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClick(loi));
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTieuDe, tvMoTa, tvCoHuongDan;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDeLoi);
            tvMoTa = itemView.findViewById(R.id.tvMoTaLoi);
            tvCoHuongDan = itemView.findViewById(R.id.tvCoHuongDan);
        }
    }
}
