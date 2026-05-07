package com.example.customercareproject.ui.ktv;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

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
        switch (trangThai != null ? trangThai : "") {
            case "ChoXuLy":
                holder.tvTrangThai.setText("Chờ xử lý");
                holder.tvTrangThai.setTextColor(Color.parseColor("#FF9800")); break;
            case "DangXuLy":
                holder.tvTrangThai.setText("Đang xử lý");
                holder.tvTrangThai.setTextColor(Color.parseColor("#2196F3")); break;
            case "HangCho":
                holder.tvTrangThai.setText("Hàng chờ");
                holder.tvTrangThai.setTextColor(Color.parseColor("#9C27B0")); break;
            default:
                holder.tvTrangThai.setText("Đã xử lý");
                holder.tvTrangThai.setTextColor(Color.parseColor("#4CAF50")); break;
        }

        // Màu card theo độ ưu tiên (khẩn cấp)
        String uuTien = t.getUuTien();
        if (holder.cardView != null) {
            if ("Cao".equals(uuTien)) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                holder.tvUuTien.setText("Khẩn cấp");
                holder.tvUuTien.setTextColor(Color.parseColor("#D32F2F"));
            } else if ("Thap".equals(uuTien)) {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#F1F8E9"));
                holder.tvUuTien.setText("Thấp");
                holder.tvUuTien.setTextColor(Color.parseColor("#388E3C"));
            } else {
                holder.cardView.setCardBackgroundColor(Color.parseColor("#FFFDE7"));
                holder.tvUuTien.setText("Bình thường");
                holder.tvUuTien.setTextColor(Color.parseColor("#F57F17"));
            }
        }

        if (t.getTaoLuc() != null) {
            String time = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                    .format(new Date(t.getTaoLuc().toDate().getTime()));
            holder.tvThoiGian.setText(time);
        }

        // Badge ghi chú từ KTV trước
        boolean coGhiChu = t.getLichSuHoTro() != null && !t.getLichSuHoTro().isEmpty();
        if (holder.tvBadgeGhiChu != null) {
            holder.tvBadgeGhiChu.setVisibility(coGhiChu ? View.VISIBLE : View.GONE);
        }

        // Badge Follow-up đánh giá xấu
        boolean isFollowUp = "AutoFollowUp".equals(t.getLoaiTicket());
        if (holder.tvBadgeFollowUp != null) {
            holder.tvBadgeFollowUp.setVisibility(isFollowUp ? View.VISIBLE : View.GONE);
            if (isFollowUp) {
                holder.tvBadgeFollowUp.setText("🔴 Follow-up");
                holder.tvBadgeFollowUp.setBackgroundColor(Color.parseColor("#FFCDD2"));
                holder.tvBadgeFollowUp.setTextColor(Color.parseColor("#D32F2F"));
            }
        }

        // Màu card đặc biệt cho follow-up
        if (isFollowUp && holder.cardView != null) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
            holder.cardView.setStrokeColor(Color.parseColor("#F44336"));
            holder.cardView.setStrokeWidth(2);
        }

        // Badge cảnh báo quá 30 phút (G3.1, G3.3)
        if (holder.tvBadgeQuaHan != null) {
            boolean hienBadge = false;
            if (t.getTaoLuc() != null) {
                long elapsed = (System.currentTimeMillis() - t.getTaoLuc().toDate().getTime()) / 60000L;
                boolean trangThaiCho = "HangCho".equals(trangThai) || "ChoXuLy".equals(trangThai);
                hienBadge = trangThaiCho && elapsed > 30;
            }
            holder.tvBadgeQuaHan.setVisibility(hienBadge ? View.VISIBLE : View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(t));
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTieuDe, tvKhachHang, tvSanPham, tvTrangThai, tvThoiGian, tvUuTien, tvBadgeGhiChu, tvBadgeQuaHan, tvBadgeFollowUp;
        MaterialCardView cardView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvKhachHang = itemView.findViewById(R.id.tvKhachHang);
            tvSanPham = itemView.findViewById(R.id.tvSanPham);
            tvTrangThai = itemView.findViewById(R.id.tvTrangThai);
            tvThoiGian = itemView.findViewById(R.id.tvThoiGian);
            tvUuTien = itemView.findViewById(R.id.tvUuTien);
            tvBadgeGhiChu = itemView.findViewById(R.id.tvBadgeGhiChu);
            tvBadgeQuaHan = itemView.findViewById(R.id.tvBadgeQuaHan);
            tvBadgeFollowUp = itemView.findViewById(R.id.tvBadgeFollowUp);
            // CardView là root của item_ticket
            if (itemView instanceof MaterialCardView) cardView = (MaterialCardView) itemView;
        }
    }
}
