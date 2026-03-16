package com.example.customercareproject.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.LoiPhatSinh;
import com.example.customercareproject.model.SanPham;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminKnowledgeFragment extends Fragment {

    private KnowledgeAdminAdapter adapter;
    private Spinner spinnerSanPham;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_knowledge, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        spinnerSanPham = view.findViewById(R.id.spinnerSanPham);
        RecyclerView rvKnowledge = view.findViewById(R.id.rvKnowledge);
        FloatingActionButton fabThem = view.findViewById(R.id.fabThemLoi);

        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, SanPham.DANH_SACH);
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSanPham.setAdapter(spAdapter);

        adapter = new KnowledgeAdminAdapter(new ArrayList<>(), loiId -> xoaLoi(loiId));
        rvKnowledge.setLayoutManager(new LinearLayoutManager(getContext()));
        rvKnowledge.setAdapter(adapter);

        spinnerSanPham.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View v, int pos, long id) {
                taiDanhSachLoi(SanPham.DANH_SACH[pos]);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        fabThem.setOnClickListener(v -> hienDialogThemLoi());
        taiDanhSachLoi(SanPham.DANH_SACH[0]);
    }

    private void taiDanhSachLoi(String sanPham) {
        db.collection("LoiPhatSinh").whereEqualTo("sanPham", sanPham).get()
                .addOnSuccessListener(snap -> {
                    List<LoiPhatSinh> list = new ArrayList<>();
                
                    for (QueryDocumentSnapshot doc : snap) {
                        LoiPhatSinh loi = doc.toObject(LoiPhatSinh.class);
                        loi.setId(doc.getId());
                        list.add(loi);
                    }
                    adapter.capNhat(list);
                });
    }

    private void hienDialogThemLoi() {
        String sanPham = SanPham.DANH_SACH[spinnerSanPham.getSelectedItemPosition()];
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_them_loi, null);
        TextInputEditText edtTieuDe = dialogView.findViewById(R.id.edtTieuDe);
        TextInputEditText edtMoTa = dialogView.findViewById(R.id.edtMoTa);
        TextInputEditText edtCachGiaiQuyet = dialogView.findViewById(R.id.edtCachGiaiQuyet);

        new AlertDialog.Builder(getContext())
                .setTitle("Thêm lỗi vào Knowledge Base")
                .setView(dialogView)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String tieuDe = edtTieuDe.getText() != null ? edtTieuDe.getText().toString().trim() : "";
                    String moTa = edtMoTa.getText() != null ? edtMoTa.getText().toString().trim() : "";
                    String cachGQ = edtCachGiaiQuyet.getText() != null ? edtCachGiaiQuyet.getText().toString().trim() : "";

                    if (tieuDe.isEmpty() || moTa.isEmpty()) {
                        Toast.makeText(getContext(), "Vui lòng nhập tiêu đề và mô tả", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("sanPham", sanPham);
                    data.put("tieuDe", tieuDe);
                    data.put("moTa", moTa);
                    data.put("cachGiaiQuyet", cachGQ);
                    data.put("coHuongDan", !cachGQ.isEmpty());
                    data.put("taoLuc", Timestamp.now());

                    db.collection("LoiPhatSinh").add(data)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(getContext(), "Đã thêm!", Toast.LENGTH_SHORT).show();
                                taiDanhSachLoi(sanPham);
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void xoaLoi(String loiId) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa lỗi này?")
                .setPositiveButton("Xóa", (d, w) -> {
                    db.collection("LoiPhatSinh").document(loiId).delete()
                            .addOnSuccessListener(v -> {
                                Toast.makeText(getContext(), "Đã xóa!", Toast.LENGTH_SHORT).show();
                                taiDanhSachLoi(SanPham.DANH_SACH[spinnerSanPham.getSelectedItemPosition()]);
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
