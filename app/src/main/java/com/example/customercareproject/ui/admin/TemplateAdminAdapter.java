package com.example.customercareproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.TemplateTrLoi;

import java.util.List;

public class TemplateAdminAdapter extends RecyclerView.Adapter<TemplateAdminAdapter.ViewHolder> {

    public interface OnDeleteTemplate {
        void onDelete(TemplateTrLoi template);
    }

    private List<TemplateTrLoi> danhSach;
    private final OnDeleteTemplate deleteCallback;

    public TemplateAdminAdapter(List<TemplateTrLoi> danhSach, OnDeleteTemplate deleteCallback) {
        this.danhSach = danhSach;
        this.deleteCallback = deleteCallback;
    }

    public void capNhat(List<TemplateTrLoi> list) {
        this.danhSach = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_template_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TemplateTrLoi template = danhSach.get(position);
        holder.tvTieuDeAdmin.setText(template.getTieuDe());
        holder.tvSanPhamAdmin.setText(template.getSanPham());
        holder.tvPreviewAdmin.setText(template.getNoiDung());
        holder.btnXoaTemplate.setOnClickListener(v -> deleteCallback.onDelete(template));
    }

    @Override
    public int getItemCount() {
        return danhSach != null ? danhSach.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTieuDeAdmin, tvSanPhamAdmin, tvPreviewAdmin;
        ImageButton btnXoaTemplate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTieuDeAdmin = itemView.findViewById(R.id.tvTieuDeAdmin);
            tvSanPhamAdmin = itemView.findViewById(R.id.tvSanPhamAdmin);
            tvPreviewAdmin = itemView.findViewById(R.id.tvPreviewAdmin);
            btnXoaTemplate = itemView.findViewById(R.id.btnXoaTemplate);
        }
    }
}
