package com.example.customercareproject.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LeadKinhDoanh;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminLeadFragment extends Fragment {

    private RecyclerView rvLead;
    private TextView tvEmptyLead;
    private LeadAdapter adapter;
    private List<LeadKinhDoanh> leadList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_lead, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvLead = view.findViewById(R.id.rvLead);
        tvEmptyLead = view.findViewById(R.id.tvEmptyLead);

        adapter = new LeadAdapter(leadList, lead -> hienThiDialogCapNhatTrangThai(lead));
        rvLead.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLead.setAdapter(adapter);

        taiDanhSach();
    }

    private void taiDanhSach() {
        if (getContext() == null) return;

        FirebaseFirestore.getInstance().collection("LeadKinhDoanh")
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    leadList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        LeadKinhDoanh lead = doc.toObject(LeadKinhDoanh.class);
                        lead.setId(doc.getId());
                        leadList.add(lead);
                    }
                    adapter.capNhat(new ArrayList<>(leadList));
                    hienThiEmptyState(leadList.isEmpty());
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    hienThiEmptyState(true);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void hienThiEmptyState(boolean show) {
        if (tvEmptyLead == null) return;
        tvEmptyLead.setVisibility(show ? View.VISIBLE : View.GONE);
        rvLead.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void hienThiDialogCapNhatTrangThai(LeadKinhDoanh lead) {
        if (getContext() == null) return;

        String[] options = {"Mới", "Đang tư vấn", "Đã đăng ký", "Từ chối"};
        final String[] values = {
                LeadKinhDoanh.TRANG_THAI_MOI,
                LeadKinhDoanh.TRANG_THAI_DANG_TU_VAN,
                LeadKinhDoanh.TRANG_THAI_DA_DANG_KY,
                LeadKinhDoanh.TRANG_THAI_TU_CHOI
        };

        // Tìm index hiện tại
        int currentIndex = 0;
        String current = lead.getTrangThaiLead();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(current)) {
                currentIndex = i;
                break;
            }
        }
        final int[] selected = {currentIndex};

        new AlertDialog.Builder(getContext())
                .setTitle("Cập nhật trạng thái lead")
                .setSingleChoiceItems(options, currentIndex, (dialog, which) -> selected[0] = which)
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    String newTrangThai = values[selected[0]];
                    capNhatTrangThai(lead, newTrangThai);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void capNhatTrangThai(LeadKinhDoanh lead, String newTrangThai) {
        if (lead.getId() == null || lead.getId().isEmpty()) return;

        FirebaseFirestore.getInstance().collection("LeadKinhDoanh")
                .document(lead.getId())
                .update(
                        "trangThaiLead", newTrangThai,
                        "capNhatLuc", Timestamp.now()
                )
                .addOnSuccessListener(aVoid -> {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                    taiDanhSach();
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    Toast.makeText(getContext(), "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
