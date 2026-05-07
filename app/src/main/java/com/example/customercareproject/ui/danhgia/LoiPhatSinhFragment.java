package com.example.customercareproject.ui.danhgia;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LoiPhatSinh;
import com.example.customercareproject.ui.loi.ChiTietLoiActivity;
import com.example.customercareproject.ui.components.Material3TextField;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LoiPhatSinhFragment extends Fragment {

    private static final String ARG_SAN_PHAM = "sanPham";
    private String sanPham;
    private LoiAdapter adapter;
    private List<LoiPhatSinh> danhSachLoi = new ArrayList<>();
    private LinearLayout layoutRong;
    private RecyclerView rvLoi;

    public static LoiPhatSinhFragment newInstance(String sanPham) {
        LoiPhatSinhFragment f = new LoiPhatSinhFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SAN_PHAM, sanPham);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) sanPham = getArguments().getString(ARG_SAN_PHAM);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_loi_phat_sinh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLoi = view.findViewById(R.id.rvLoiPhatSinh);
        layoutRong = view.findViewById(R.id.layoutRong);
        com.example.customercareproject.ui.components.Material3TextField edtTimKiem = view.findViewById(R.id.edtTimKiem);

        adapter = new LoiAdapter(new ArrayList<>(), loi -> {
            Intent intent = new Intent(getContext(), ChiTietLoiActivity.class);
            intent.putExtra("loiId", loi.getId());
            intent.putExtra("sanPham", sanPham);
            startActivity(intent);
        });

        rvLoi.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLoi.setAdapter(adapter);

        // Nut gui yeu cau moi luon hien thi
        view.findViewById(R.id.btnGuiYeuCauMoi).setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), com.example.customercareproject.ui.loi.YeuCauHoTroActivity.class);
            intent.putExtra("sanPham", sanPham);
            intent.putExtra("loiId", "");
            intent.putExtra("tieuDeLoi", "Vấn đề khác");
            startActivity(intent);
        });

        edtTimKiem.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                locDanhSach(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        taiDanhSachLoi();
    }

    private void taiDanhSachLoi() {
        FirebaseFirestore.getInstance().collection("LoiPhatSinh")
                .whereEqualTo("sanPham", sanPham)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null || getContext() == null) return;
                    danhSachLoi.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        LoiPhatSinh loi = doc.toObject(LoiPhatSinh.class);
                        loi.setId(doc.getId());
                        danhSachLoi.add(loi);
                    }
                    capNhatUI(danhSachLoi);
                });
    }

    private void locDanhSach(String query) {
        if (query.isEmpty()) {
            capNhatUI(danhSachLoi);
            return;
        }
        List<LoiPhatSinh> filtered = new ArrayList<>();
        for (LoiPhatSinh loi : danhSachLoi) {
            if (loi.getTieuDe().toLowerCase().contains(query.toLowerCase())
                    || loi.getMoTa().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(loi);
            }
        }
        capNhatUI(filtered);
    }

    private void capNhatUI(List<LoiPhatSinh> list) {
        adapter.capNhat(list);
        boolean rong = list.isEmpty();
        rvLoi.setVisibility(rong ? View.GONE : View.VISIBLE);
        layoutRong.setVisibility(rong ? View.VISIBLE : View.GONE);
    }
}


