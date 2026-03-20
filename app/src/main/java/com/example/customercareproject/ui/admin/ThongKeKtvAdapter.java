package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;

import java.util.List;

public class ThongKeKtvAdapter extends RecyclerView.Adapter<ThongKeKtvAdapter.ViewHolder> {

    public static class KtvItem {
        public String ten;
        public String trangThai;
        public int soTicketDang;
        public int tongTicket;
        public double diemDanhGia;
        public int soLuotDanhGia;

        public KtvItem(String ten, String trangThai, int soTicketDang, int tongTicket,
                       double diemDanhGia, int soLuotDanhGia) {
            this.ten = ten;
            this.trangThai = trangThai;
            this.soTicketDang = soTicketDang;
            this.tongTicket = tongTicket;
            this.diemDanhGia = diemDanhGia;
            this.soLuotDanhGia = soLuotDanhGia;
        }
    }

    private final List<KtvItem> danhSach;

    public ThongKeKtvAdapter(List<KtvItem> danhSach) {
        this.danhSach = danhSach;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_thong_ke_ktv, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KtvItem item = danhSach.get(position);

        // Avatar chữ cái đầu
        String chu = item.ten != null && !item.ten.isEmpty()
                ? String.valueOf(item.ten.charAt(0)).toUpperCase() : "K";
        holder.tvAvatar.setText(chu);

        holder.tvTen.setText(item.ten);
        holder.tvTicketInfo.setText("Đang: " + item.soTicketDang + "  |  Tổng: " + item.tongTicket);

        // Trạng thái màu sắc
        switch (item.trangThai != null ? item.trangThai : "") {
            case "Ran":
                holder.tvTrangThai.setText("● Rảnh");
                holder.tvTrangThai.setTextColor(Color.parseColor("#2E7D32"));
                break;
            case "DangBan":
                holder.tvTrangThai.setText("● Đang bận");
                holder.tvTrangThai.setTextColor(Color.parseColor("#F57F17"));
                break;
            default:
                holder.tvTrangThai.setText("● Offline");
                holder.tvTrangThai.setTextColor(Color.parseColor("#9E9E9E"));
        }

        // Điểm đánh giá
        if (item.soLuotDanhGia > 0) {
            holder.tvDiem.setText(String.format("%.1f★", item.diemDanhGia));
            holder.tvSoLuot.setText(item.soLuotDanhGia + " đánh giá");
            // Màu theo điểm
            if (item.diemDanhGia >= 4.0) {
                holder.tvDiem.setTextColor(Color.parseColor("#2E7D32"));
            } else if (item.diemDanhGia >= 3.0) {
                holder.tvDiem.setTextColor(Color.parseColor("#F57F17"));
            } else {
                holder.tvDiem.setTextColor(Color.parseColor("#C62828"));
            }
        } else {
            holder.tvDiem.setText("N/A");
            holder.tvDiem.setTextColor(Color.parseColor("#9E9E9E"));
            holder.tvSoLuot.setText("Chưa có");
        }
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvTen, tvTrangThai, tvTicketInfo, tvDiem, tvSoLuot;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar     = itemView.findViewById(R.id.tvAvatar);
            tvTen        = itemView.findViewById(R.id.tvTenKtv);
            tvTrangThai  = itemView.findViewById(R.id.tvTrangThai);
            tvTicketInfo = itemView.findViewById(R.id.tvTicketInfo);
            tvDiem       = itemView.findViewById(R.id.tvDiemKtv);
            tvSoLuot     = itemView.findViewById(R.id.tvSoLuotDG);
        }
    }
}
