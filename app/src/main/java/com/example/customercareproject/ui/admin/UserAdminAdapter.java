package com.example.customercareproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.NguoiDung;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdminAdapter extends RecyclerView.Adapter<UserAdminAdapter.ViewHolder> {

    private List<NguoiDung> danhSach;
    private static final String[] VAI_TRO_OPTIONS = {
            NguoiDung.VAI_TRO_KHACH_HANG, NguoiDung.VAI_TRO_KTV, NguoiDung.VAI_TRO_ADMIN
    };

    public UserAdminAdapter(List<NguoiDung> danhSach) { this.danhSach = danhSach; }

    public void capNhat(List<NguoiDung> list) { this.danhSach = list; notifyDataSetChanged(); }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NguoiDung nd = danhSach.get(position);
        holder.tvHoTen.setText(nd.getHoTen());
        holder.tvEmail.setText(nd.getEmail());
        holder.tvVaiTro.setText(nd.getVaiTro());

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(holder.itemView.getContext(),
                android.R.layout.simple_spinner_item, VAI_TRO_OPTIONS);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinnerVaiTro.setAdapter(spAdapter);

        // Set current selection
        for (int i = 0; i < VAI_TRO_OPTIONS.length; i++) {
            if (VAI_TRO_OPTIONS[i].equals(nd.getVaiTro())) {
                holder.spinnerVaiTro.setSelection(i);
                break;
            }
        }

        holder.spinnerVaiTro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean firstCall = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (firstCall) { firstCall = false; return; } // Bỏ qua lần đầu
                String newVaiTro = VAI_TRO_OPTIONS[pos];
                FirebaseFirestore.getInstance().collection("NguoiDung")
                        .document(nd.getUid())
                        .update("vaiTro", newVaiTro);
                nd.setVaiTro(newVaiTro);
                holder.tvVaiTro.setText(newVaiTro);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    @Override
    public int getItemCount() { return danhSach.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHoTen, tvEmail, tvVaiTro;
        Spinner spinnerVaiTro;
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHoTen = itemView.findViewById(R.id.tvHoTen);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvVaiTro = itemView.findViewById(R.id.tvVaiTro);
            spinnerVaiTro = itemView.findViewById(R.id.spinnerVaiTro);
        }
    }
}
