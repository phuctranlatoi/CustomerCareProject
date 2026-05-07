package com.example.customercareproject.ui.ktv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.GhiChuTienDo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GhiChuTienDoAdapter extends RecyclerView.Adapter<GhiChuTienDoAdapter.ViewHolder> {

    private final List<GhiChuTienDo> danhSach;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());

    public GhiChuTienDoAdapter(List<GhiChuTienDo> danhSach) {
        this.danhSach = danhSach;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ghi_chu_tien_do, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GhiChuTienDo item = danhSach.get(position);
        holder.tvKtvTen.setText(item.getKtvTen() != null ? item.getKtvTen() : "KTV");
        holder.tvNoiDung.setText(item.getNoiDung());
        if (item.getThoiDiem() != null) {
            Date date = item.getThoiDiem().toDate();
            holder.tvThoiDiem.setText(SDF.format(date));
        } else {
            holder.tvThoiDiem.setText("");
        }
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKtvTen, tvThoiDiem, tvNoiDung;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKtvTen   = itemView.findViewById(R.id.tvKtvTen);
            tvThoiDiem = itemView.findViewById(R.id.tvThoiDiem);
            tvNoiDung  = itemView.findViewById(R.id.tvNoiDung);
        }
    }
}
