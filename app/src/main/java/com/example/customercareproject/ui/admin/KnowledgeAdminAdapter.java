package com.example.customercareproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LoiPhatSinh;

import java.util.List;

public class KnowledgeAdminAdapter extends RecyclerView.Adapter<KnowledgeAdminAdapter.ViewHolder> {

    private List<LoiPhatSinh> danhSach;
    private final OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(String loiId);
    }

    public KnowledgeAdminAdapter(List<LoiPhatSinh> danhSach, OnDeleteListener deleteListener) {
        this.danhSach = danhSach;
        this.deleteListener = deleteListener;
    }

    public void capNhat(List<LoiPhatSinh> list) { this.danhSach = list; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loi_phat_sinh, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoiPhatSinh loi = danhSach.get(position);
        holder.tvTieuDe.setText(loi.getTieuDe());
        holder.tvMoTa.setText(loi.getMoTa());
        holder.tvCoHuongDan.setText(loi.isCoHuongDan() ? "Có hướng dẫn" : "Cần KTV");
        holder.tvCoHuongDan.setTextColor(holder.itemView.getContext().getColor(
                loi.isCoHuongDan() ? R.color.success : R.color.warning));
        holder.itemView.setOnLongClickListener(v -> {
            deleteListener.onDelete(loi.getId());
            return true;
        });
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
