package com.example.customercareproject.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.NguoiDung;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminKtvReviewsFragment extends Fragment {

    private KtvReviewSummaryAdapter adapter;
    private List<KtvReviewSummaryAdapter.KtvItem> allList = new ArrayList<>();
    private TextView tvTongKtv, tvDiemTBTong;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_ktv_reviews, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTongKtv = view.findViewById(R.id.tvTongKtv);
        tvDiemTBTong = view.findViewById(R.id.tvDiemTBTong);
        RecyclerView rv = view.findViewById(R.id.rvKtvReviews);
        TextInputEditText edtSearch = view.findViewById(R.id.edtTimKiemKtv);

        adapter = new KtvReviewSummaryAdapter(new ArrayList<>(), item -> {
            Intent intent = new Intent(getContext(), AdminKtvReviewDetailActivity.class);
            intent.putExtra("ktvUid", item.uid);
            intent.putExtra("ktvTen", item.tenKtv);
            startActivity(intent);
        });
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        taiDanhSach();
    }

    private void taiDanhSach() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Load tất cả KTV
        db.collection("NguoiDung").whereEqualTo("vaiTro", NguoiDung.VAI_TRO_KTV).get()
                .addOnSuccessListener(snapKtv -> {
                    if (getContext() == null) return;

                    Map<String, String> ktvTenMap = new HashMap<>();
                    Map<String, String> ktvTrangThaiMap = new HashMap<>();
                    List<String> ktvUids = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapKtv) {
                        String uid = doc.getId();
                        String ten = doc.getString("hoTen");
                        String tt = doc.getString("trangThai");
                        ktvTenMap.put(uid, ten != null ? ten : "KTV");
                        ktvTrangThaiMap.put(uid, tt != null ? tt : "Offline");
                        ktvUids.add(uid);
                    }

                    // Load tất cả DanhGiaKTV để tính điểm
                    db.collection("DanhGiaKTV").get()
                            .addOnSuccessListener(snapDG -> {
                                if (getContext() == null) return;

                                Map<String, List<Double>> diemMap = new HashMap<>();
                                for (QueryDocumentSnapshot doc : snapDG) {
                                    String ktvUid = doc.getString("ktvUid");
                                    Object s = doc.get("soSao");
                                    if (ktvUid != null && s instanceof Number) {
                                        diemMap.computeIfAbsent(ktvUid, k -> new ArrayList<>())
                                                .add(((Number) s).doubleValue());
                                    }
                                }

                                allList.clear();
                                double tongDiem = 0;
                                int tongLuot = 0;

                                for (String uid : ktvUids) {
                                    List<Double> diems = diemMap.getOrDefault(uid, new ArrayList<>());
                                    double tb = diems.isEmpty() ? 0
                                            : diems.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                                    tongDiem += tb;
                                    tongLuot += diems.size();
                                    allList.add(new KtvReviewSummaryAdapter.KtvItem(
                                            uid,
                                            ktvTenMap.get(uid),
                                            ktvTrangThaiMap.get(uid),
                                            tb,
                                            diems.size()
                                    ));
                                }

                                // Sắp xếp điểm giảm dần
                                allList.sort((a, b) -> Double.compare(b.diemTB, a.diemTB));

                                tvTongKtv.setText(allList.size() + " nhân viên");
                                double diemTBTong = allList.isEmpty() ? 0 : tongDiem / allList.size();
                                tvDiemTBTong.setText(String.format("%.1f", diemTBTong));

                                adapter.capNhat(new ArrayList<>(allList));
                            });
                });
    }

    private void filter(String query) {
        if (query.isEmpty()) {
            adapter.capNhat(new ArrayList<>(allList));
            return;
        }
        String q = query.toLowerCase();
        List<KtvReviewSummaryAdapter.KtvItem> filtered = new ArrayList<>();
        for (KtvReviewSummaryAdapter.KtvItem item : allList) {
            if (item.tenKtv != null && item.tenKtv.toLowerCase().contains(q)) {
                filtered.add(item);
            }
        }
        adapter.capNhat(filtered);
    }
}
