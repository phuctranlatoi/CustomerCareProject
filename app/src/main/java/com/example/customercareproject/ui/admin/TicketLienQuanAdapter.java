package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TicketLienQuanAdapter extends RecyclerView.Adapter<TicketLienQuanAdapter.VH> {

    private final List<YeuCauHoTro> list;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public TicketLienQuanAdapter(List<YeuCauHoTro> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket_lien_quan, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        YeuCauHoTro yc = list.get(position);

        // Title
        String tieuDe = yc.getTieuDeLoi() != null ? yc.getTieuDeLoi() : "Không có tiêu đề";
        h.tvTieuDe.setText(tieuDe);

        // KTV
        String ktv = yc.getKtvTen() != null ? "KTV: " + yc.getKtvTen() : "Chưa phân công";
        h.tvKtv.setText(ktv);

        // Date
        if (yc.getTaoLuc() != null) {
            h.tvNgay.setText(SDF.format(yc.getTaoLuc().toDate()));
        } else {
            h.tvNgay.setText("");
        }

        // Status dot + chip
        String ts = yc.getTrangThai();
        int dotColor;
        String chipText;
        int chipTextColor;
        int chipBgColor;

        if ("DaXuLy".equals(ts)) {
            dotColor = Color.parseColor("#4CAF50");
            chipText = "Đã xử lý";
            chipTextColor = Color.parseColor("#1B5E20");
            chipBgColor = Color.parseColor("#1A4CAF50");
        } else if ("DangXuLy".equals(ts)) {
            dotColor = Color.parseColor("#2196F3");
            chipText = "Đang xử lý";
            chipTextColor = Color.parseColor("#0D47A1");
            chipBgColor = Color.parseColor("#1A2196F3");
        } else {
            dotColor = Color.parseColor("#FF9800");
            chipText = "Chờ xử lý";
            chipTextColor = Color.parseColor("#E65100");
            chipBgColor = Color.parseColor("#1AFF9800");
        }

        // Apply dot color
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(dotColor);
        h.viewDot.setBackground(dot);

        // Apply chip
        h.tvTrangThai.setText(chipText);
        h.tvTrangThai.setTextColor(chipTextColor);
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setColor(chipBgColor);
        chipBg.setCornerRadius(20f);
        h.tvTrangThai.setBackground(chipBg);
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTieuDe, tvKtv, tvNgay, tvTrangThai;
        View viewDot;

        VH(@NonNull View v) {
            super(v);
            tvTieuDe = v.findViewById(R.id.tvTieuDeTicket);
            tvKtv = v.findViewById(R.id.tvKtvTicket);
            tvNgay = v.findViewById(R.id.tvNgayTicket);
            tvTrangThai = v.findViewById(R.id.tvTrangThaiTicket);
            viewDot = v.findViewById(R.id.viewStatusDot);
        }
    }
}
