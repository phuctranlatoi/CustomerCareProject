package com.example.customercareproject.ui.ktv;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<Map<String, Object>> danhSach = new ArrayList<>();
    private final String myUid;

    public ChatAdapter(List<Map<String, Object>> danhSach, String myUid) {
        this.danhSach = new ArrayList<>(danhSach);
        this.myUid = myUid;
    }

    /** Cập nhật toàn bộ danh sách từ RTDB */
    public void capNhatRaw(List<Map<String, Object>> newList) {
        this.danhSach = new ArrayList<>(newList);
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
        Map<String, Object> msg = danhSach.get(position);

        String nguoiGuiUid = (String) msg.get("nguoiGuiUid");
        String nguoiGuiTen = (String) msg.get("nguoiGuiTen");
        String noiDung = (String) msg.get("noiDung");
        Object thoiGianObj = msg.get("thoiGian");

        String time = "";
        if (thoiGianObj != null) {
            long ts = thoiGianObj instanceof Long ? (Long) thoiGianObj : ((Number) thoiGianObj).longValue();
            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(ts));
        }

        boolean isMe = myUid.equals(nguoiGuiUid);

        if (isMe) {
            holder.layoutKtv.setVisibility(View.VISIBLE);
            holder.layoutKh.setVisibility(View.GONE);
            holder.tvNoiDungKtv.setText(noiDung);
            holder.tvThoiGianKtv.setText(time);
        } else {
            holder.layoutKh.setVisibility(View.VISIBLE);
            holder.layoutKtv.setVisibility(View.GONE);
            holder.tvTenNguoiGui.setText(nguoiGuiTen != null ? nguoiGuiTen : "");
            holder.tvNoiDungKh.setText(noiDung);
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
