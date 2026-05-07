package com.example.customercareproject.ui.admin;

import android.app.AlertDialog;
import android.os.Bundle;
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
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.YeuCauHoTro;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminDanhGiaXauFragment extends Fragment {

    private RecyclerView rvDanhGiaXau;
    private TextView tvEmptyDanhGiaXau;
    private DanhGiaXauAdapter adapter;
    private List<DanhGia> danhGiaList = new ArrayList<>();
    private Map<String, Integer> soLanHoTroMap = new HashMap<>();
    private int soNgayFilter = 7;

    private static final SimpleDateFormat SDF_DIALOG = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_danh_gia_xau, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvDanhGiaXau = view.findViewById(R.id.rvDanhGiaXau);
        tvEmptyDanhGiaXau = view.findViewById(R.id.tvEmptyDanhGiaXau);

        adapter = new DanhGiaXauAdapter(danhGiaList, soLanHoTroMap, danhGia -> hienThiDialogYeuCau(danhGia));
        rvDanhGiaXau.setLayoutManager(new LinearLayoutManager(getContext()));
        rvDanhGiaXau.setAdapter(adapter);

        Chip chip7 = view.findViewById(R.id.chip7Ngay);
        Chip chip30 = view.findViewById(R.id.chip30Ngay);

        chip7.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                soNgayFilter = 7;
                taiDanhSach();
            }
        });
        chip30.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                soNgayFilter = 30;
                taiDanhSach();
            }
        });

        taiDanhSach();
    }

    private void taiDanhSach() {
        if (getContext() == null) return;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -soNgayFilter);
        Timestamp tuNgay = new Timestamp(cal.getTime());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("DanhGia")
                .whereLessThanOrEqualTo("soSao", 2)
                .whereGreaterThan("taoLuc", tuNgay)
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;

                    List<DanhGia> tempList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        DanhGia dg = doc.toObject(DanhGia.class);
                        dg.setId(doc.getId());
                        tempList.add(dg);
                    }

                    if (tempList.isEmpty()) {
                        danhGiaList.clear();
                        soLanHoTroMap.clear();
                        adapter.capNhat(new ArrayList<>(danhGiaList), new HashMap<>(soLanHoTroMap));
                        hienThiEmptyState(true);
                        return;
                    }

                    hienThiEmptyState(false);
                    // Đếm số lần hỗ trợ cho mỗi uid
                    Map<String, Integer> tempMap = new HashMap<>();
                    AtomicInteger pendingCount = new AtomicInteger(tempList.size());

                    Calendar cal30 = Calendar.getInstance();
                    cal30.add(Calendar.DAY_OF_YEAR, -30);
                    Timestamp tuNgay30 = new Timestamp(cal30.getTime());

                    for (DanhGia dg : tempList) {
                        String uid = dg.getUid();
                        if (uid == null || uid.isEmpty()) {
                            if (pendingCount.decrementAndGet() == 0) {
                                danhGiaList.clear();
                                danhGiaList.addAll(tempList);
                                soLanHoTroMap.clear();
                                soLanHoTroMap.putAll(tempMap);
                                adapter.capNhat(new ArrayList<>(danhGiaList), new HashMap<>(soLanHoTroMap));
                            }
                            continue;
                        }

                        db.collection("YeuCauHoTro")
                                .whereEqualTo("uid", uid)
                                .whereGreaterThan("taoLuc", tuNgay30)
                                .get()
                                .addOnSuccessListener(ticketSnap -> {
                                    if (getContext() == null) return;
                                    tempMap.put(uid, ticketSnap.size());
                                    if (pendingCount.decrementAndGet() == 0) {
                                        danhGiaList.clear();
                                        danhGiaList.addAll(tempList);
                                        soLanHoTroMap.clear();
                                        soLanHoTroMap.putAll(tempMap);
                                        adapter.capNhat(new ArrayList<>(danhGiaList), new HashMap<>(soLanHoTroMap));
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (pendingCount.decrementAndGet() == 0) {
                                        if (getContext() == null) return;
                                        danhGiaList.clear();
                                        danhGiaList.addAll(tempList);
                                        soLanHoTroMap.clear();
                                        soLanHoTroMap.putAll(tempMap);
                                        adapter.capNhat(new ArrayList<>(danhGiaList), new HashMap<>(soLanHoTroMap));
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    hienThiEmptyState(true);
                });
    }

    private void hienThiEmptyState(boolean show) {
        if (tvEmptyDanhGiaXau == null) return;
        tvEmptyDanhGiaXau.setVisibility(show ? View.VISIBLE : View.GONE);
        rvDanhGiaXau.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void hienThiDialogYeuCau(DanhGia danhGia) {
        if (getContext() == null) return;
        String uid = danhGia.getUid();
        if (uid == null || uid.isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Yêu cầu hỗ trợ liên quan")
                    .setMessage("Không có thông tin người dùng.")
                    .setPositiveButton("Đóng", null)
                    .show();
            return;
        }

        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay30 = new Timestamp(cal30.getTime());

        FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .whereEqualTo("uid", uid)
                .whereGreaterThan("taoLuc", tuNgay30)
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;
                    if (snap.isEmpty()) {
                        new AlertDialog.Builder(getContext())
                                .setTitle("Yêu cầu hỗ trợ liên quan")
                                .setMessage("Không có yêu cầu hỗ trợ nào trong 30 ngày qua.")
                                .setPositiveButton("Đóng", null)
                                .show();
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (QueryDocumentSnapshot doc : snap) {
                        YeuCauHoTro yc = doc.toObject(YeuCauHoTro.class);
                        sb.append("• ").append(yc.getTieuDeLoi() != null ? yc.getTieuDeLoi() : "(Không tiêu đề)");
                        sb.append("\n  Trạng thái: ").append(yc.getTrangThai() != null ? yc.getTrangThai() : "");
                        sb.append("\n  KTV: ").append(yc.getKtvTen() != null ? yc.getKtvTen() : "Chưa phân công");
                        if (yc.getTaoLuc() != null) {
                            sb.append("\n  Thời gian: ").append(SDF_DIALOG.format(yc.getTaoLuc().toDate()));
                        }
                        sb.append("\n\n");
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("Yêu cầu hỗ trợ liên quan (" + snap.size() + ")")
                            .setMessage(sb.toString().trim())
                            .setPositiveButton("Đóng", null)
                            .show();
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    new AlertDialog.Builder(getContext())
                            .setTitle("Lỗi")
                            .setMessage("Không thể tải dữ liệu: " + e.getMessage())
                            .setPositiveButton("Đóng", null)
                            .show();
                });
    }
}
