package com.example.customercareproject.ui.ktv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {

    private List<YeuCauHoTro> danhSach;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(YeuCauHoTro ticket);
    }

    public TicketAdapter(List<YeuCauHoTro> danhSach, OnItemClickListener listener) {
        this.danhSach = danhSach;
        this.listener = listener;
    }

    public void capNhat(List<YeuCauHoTro> list) {
        this.danhSach = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        YeuCauHoTro t = danhSach.get(position);
        holder.tvTieuDe.setText(t.getTieuDeLoi());
        holder.tvKhachHang.setText("KH: " + t.getHoTen() + " - " + t.getSoDienThoai());
        holder.tvSanPham.setText(t.getSanPham());

        String trangThai = t.getTrangThai();
        holder.tvTrangThai.setText(trangThai);
        int color;
        switch (trangThai) {
            case "ChoXuLy": color = holder.itemView.getContext().getColor(R.color.warning); break;
            case "DangXuLy": color = holder.itemView.getContext().getColor(R.color.primary); break;
            default: color = holder.itemView.getContext().getColor(R.color.success); break;
        }
        holder.tvTrangThai.setTextColor(color);

        if (t.getTaoLuc() != null) {
            String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    .format(new Date(t.getTaoLuc().toDate().getTime()));
            holder.tvThoiGian.setText(time);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(t));
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTieuDe, tvKhachHang, tvSanPham, tvTrangThai, tvThoiGian;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvKhachHang = itemView.findViewById(R.id.tvKhachHang);
            tvSanPham = itemView.findViewById(R.id.tvSanPham);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
        }
    }
}
