package com.example.customercareproject.ui.ktv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.TemplateTrLoi;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

    public interface OnTemplateSelected {
        void onSelected(TemplateTrLoi template);
    }

    private final List<TemplateTrLoi> danhSach;
    private final OnTemplateSelected callback;

    public TemplateAdapter(List<TemplateTrLoi> danhSach, OnTemplateSelected callback) {
        this.danhSach = danhSach;
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemplateTrLoi template = danhSach.get(position);
        holder.tvTieuDe.setText(template.getTieuDe());

        String noiDung = template.getNoiDung();
        if (noiDung != null && noiDung.length() > 50) {
            noiDung = noiDung.substring(0, 50) + "…";
        }
        holder.tvPreviewNoiDung.setText(noiDung);

        holder.itemView.setOnClickListener(v -> {
            if (callback != null) callback.onSelected(template);
        });
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    public void capNhat(List<TemplateTrLoi> list) {
        danhSach.clear();
        danhSach.addAll(list);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTieuDe, tvPreviewNoiDung;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDe = itemView.findViewById(R.id.tvTieuDe);
            tvPreviewNoiDung = itemView.findViewById(R.id.tvPreviewNoiDung);
        }
    }
}
