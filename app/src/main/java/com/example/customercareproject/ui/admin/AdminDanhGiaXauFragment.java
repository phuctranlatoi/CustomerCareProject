package com.example.customercareproject.ui.admin;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

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
    private View layoutEmptyDanhGiaXau;
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
        layoutEmptyDanhGiaXau = view.findViewById(R.id.tvEmptyDanhGiaXau);

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
        
        // Chỉ filter theo taoLuc để tránh cần composite index
        // Filter soSao <= 2 sẽ thực hiện trong code Java
        db.collection("DanhGia")
                .whereGreaterThan("taoLuc", tuNgay)
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;

                    List<DanhGia> tempList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        DanhGia dg = doc.toObject(DanhGia.class);
                        dg.setId(doc.getId());
                        // Filter soSao <= 2 trong code
                        if (dg.getSoSao() <= 2) {
                            tempList.add(dg);
                        }
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
                    android.util.Log.e("DanhGiaXau", "Lỗi tải đánh giá xấu: " + e.getMessage(), e);
                    hienThiEmptyState(true);
                });
    }

    private void hienThiEmptyState(boolean show) {
        if (layoutEmptyDanhGiaXau == null) return;
        layoutEmptyDanhGiaXau.setVisibility(show ? View.VISIBLE : View.GONE);
        rvDanhGiaXau.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void hienThiDialogYeuCau(DanhGia danhGia) {
        if (getContext() == null) return;
        String uid = danhGia.getUid();
        if (uid == null || uid.isEmpty()) {
            hienBottomSheetRong(danhGia);
            return;
        }

        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay30 = new Timestamp(cal30.getTime());

        FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .whereEqualTo("uid", uid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;

                    List<YeuCauHoTro> filtered = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        YeuCauHoTro yc = doc.toObject(YeuCauHoTro.class);
                        yc.setId(doc.getId());
                        if (yc.getTaoLuc() != null && yc.getTaoLuc().compareTo(tuNgay30) > 0) {
                            filtered.add(yc);
                        }
                    }
                    filtered.sort((a, b) -> {
                        if (a.getTaoLuc() == null || b.getTaoLuc() == null) return 0;
                        return b.getTaoLuc().compareTo(a.getTaoLuc());
                    });

                    hienBottomSheet(danhGia, filtered);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("DanhGiaXau", "Lỗi tải YeuCauHoTro: " + e.getMessage(), e);
                    if (getContext() != null) hienBottomSheetRong(danhGia);
                });
    }

    private void hienBottomSheetRong(DanhGia danhGia) {
        hienBottomSheet(danhGia, new ArrayList<>());
    }

    private void hienBottomSheet(DanhGia danhGia, List<YeuCauHoTro> tickets) {
        if (getContext() == null) return;

        BottomSheetDialog sheet = new BottomSheetDialog(getContext());
        View sheetView = LayoutInflater.from(getContext())
                .inflate(R.layout.bottom_sheet_danh_gia_xau, null);
        sheet.setContentView(sheetView);

        // Avatar initial
        TextView tvInitial = sheetView.findViewById(R.id.tvAvatarInitial);
        String ten = danhGia.getHoTen();
        tvInitial.setText(ten != null && !ten.isEmpty() ? String.valueOf(ten.charAt(0)).toUpperCase() : "K");

        // Tên khách hàng
        TextView tvTen = sheetView.findViewById(R.id.tvTenKhachHang);
        tvTen.setText(ten != null ? ten : "Khách hàng");

        // Sản phẩm
        TextView tvSP = sheetView.findViewById(R.id.tvSanPhamDanhGia);
        String spText = "";
        if (danhGia.getSanPham() != null) spText += danhGia.getSanPham();
        if (danhGia.getTenCongTy() != null && !danhGia.getTenCongTy().isEmpty())
            spText += " • " + danhGia.getTenCongTy();
        tvSP.setText(spText);

        // Số sao
        TextView tvSao = sheetView.findViewById(R.id.tvSoSaoDialog);
        StringBuilder stars = new StringBuilder();
        int so = danhGia.getSoSao();
        for (int i = 0; i < so; i++) stars.append("★");
        for (int i = so; i < 5; i++) stars.append("☆");
        tvSao.setText(stars.toString() + " " + so + "/5");

        // Nội dung
        android.view.View cardNoiDung = sheetView.findViewById(R.id.cardNoiDung);
        TextView tvNoiDung = sheetView.findViewById(R.id.tvNoiDungDanhGia);
        String nd = danhGia.getNoiDung();
        if (nd != null && !nd.isEmpty()) {
            tvNoiDung.setText("\"“" + nd + "”\"");
            cardNoiDung.setVisibility(android.view.View.VISIBLE);
        } else {
            cardNoiDung.setVisibility(android.view.View.GONE);
        }

        // Số lượng ticket
        TextView tvSoLuong = sheetView.findViewById(R.id.tvSoLuongTicket);
        tvSoLuong.setText(tickets.isEmpty() ? "" : tickets.size() + " yêu cầu");

        // Danh sách ticket
        RecyclerView rv = sheetView.findViewById(R.id.rvTicketLienQuan);
        android.view.View llEmpty = sheetView.findViewById(R.id.llEmptyTicket);

        if (tickets.isEmpty()) {
            rv.setVisibility(android.view.View.GONE);
            llEmpty.setVisibility(android.view.View.VISIBLE);
        } else {
            rv.setVisibility(android.view.View.VISIBLE);
            llEmpty.setVisibility(android.view.View.GONE);
            rv.setLayoutManager(new LinearLayoutManager(getContext()));
            rv.setAdapter(new TicketLienQuanAdapter(tickets));
        }

        sheet.show();
    }
}
