package com.example.customercareproject.ui.ktv;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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

        // Màu trạng thái
        String trangThai = t.getTrangThai();
        holder.tvTrangThai.setText(trangThai);
        switch (trangThai != null ? trangThai : "") {
            case "ChoXuLy":
                holder.tvTrangThai.setTextColor(Color.parseColor("#FF9800")); break;
            case "DangXuLy":
                holder.tvTrangThai.setTextColor(Color.parseColor("#2196F3")); break;
            case "HangCho":
                holder.tvTrangThai.setTextColor(Color.parseColor("#9C27B0")); break;
            default:
                holder.tvTrangThai.setTextColor(Color.parseColor("#4CAF50")); break;
        }

        // Màu card theo độ ưu tiên (khẩn cấp)
        String uuTien = t.getUuTien();
        if (holder.cardView != null) {
            if ("Cao".equals(uuTien)) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE")); // đỏ nhạt
                holder.tvUuTien.setText("🔴 Khẩn cấp");
                holder.tvUuTien.setTextColor(Color.parseColor("#D32F2F"));
            } else if ("Thap".equals(uuTien)) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#F1F8E9")); // xanh lá nhạt
                holder.tvUuTien.setText("🟢 Thấp");
                holder.tvUuTien.setTextColor(Color.parseColor("#388E3C"));
            } else {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFDE7")); // vàng nhạt
                holder.tvUuTien.setText("🟡 Bình thường");
                holder.tvUuTien.setTextColor(Color.parseColor("#F57F17"));
            }
        }

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
        TextView tvTieuDe, tvKhachHang, tvSanPham, tvTrangThai, tvThoiGian, tvUuTien;
        CardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvKhachHang = itemView.findViewById(R.id.tvKhachHang);
            tvSanPham = itemView.findViewById(R.id.tvSanPham);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            tvUuTien = itemView.findViewById(R.id.tvUuTien);
            // CardView là root của item_ticket
            if (itemView instanceof CardView) cardView = (CardView) itemView;
        }
    }
}
