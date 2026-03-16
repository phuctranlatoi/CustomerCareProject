package com.example.customercareproject.ui.ktv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.TinNhan;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<TinNhan> danhSach;
    private final String myUid;

    public ChatAdapter(List<TinNhan> danhSach, String myUid) {
        this.danhSach = danhSach;
        this.myUid = myUid;
    }

    public void capNhat(List<TinNhan> list) {
        this.danhSach = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TinNhan tin = danhSach.get(position);
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(tin.getThoiGian()));

        boolean isMe = myUid.equals(tin.getNguoiGuiUid());

        if (isMe) {
            holder.layoutKtv.setVisibility(View.VISIBLE);
            holder.layoutKh.setVisibility(View.GONE);
            holder.tvNoiDungKtv.setText(tin.getNoiDung());
            holder.tvThoiGianKtv.setText(time);
        } else {
            holder.layoutKh.setVisibility(View.VISIBLE);
            holder.layoutKtv.setVisibility(View.GONE);
            holder.tvTenNguoiGui.setText(tin.getNguoiGuiTen());
            holder.tvNoiDungKh.setText(tin.getNoiDung());
            holder.tvThoiGianKh.setText(time);
        }
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutKtv, layoutKh;
        TextView tvNoiDungKtv, tvThoiGianKtv;
        TextView tvTenNguoiGui, tvNoiDungKh, tvThoiGianKh;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutKtv = itemView.findViewById(R.id.layoutKtv);
            layoutKh = itemView.findViewById(R.id.layoutKh);
            tvNoiDungKtv = itemView.findViewById(R.id.tvNoiDungKtv);
            tvThoiGianKtv = itemView.findViewById(R.id.tvThoiGianKtv);
            tvTenNguoiGui = itemView.findViewById(R.id.tvTenNguoiGui);
            tvNoiDungKh = itemView.findViewById(R.id.tvNoiDungKh);
            tvThoiGianKh = itemView.findViewById(R.id.tvThoiGianKh);
        }
    }
}
