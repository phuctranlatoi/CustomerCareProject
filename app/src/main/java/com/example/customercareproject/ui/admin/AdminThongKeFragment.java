package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.utils.NlpHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminThongKeFragment extends Fragment {

    private boolean isThang = false;
    private Chip btnTuan, btnThang;

    private TextView tvTongDanhGia, tvTongTicket, tvTongDanhGiaKtv, tvDiemTBKtv;
    private TextView tvTicketChoXuLy, tvTicketDangXuLy, tvTicketDaXuLy;
    private TextView tvHaiLong, tvTrungBinh, tvKhongHaiLong;
    private ProgressBar pbHaiLong, pbTrungBinh, pbKhongHaiLong;
    private RecyclerView rvThongKeSanPham, rvThongKeKtv;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_thong_ke, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnTuan          = view.findViewById(R.id.btnTuan);
        btnThang         = view.findViewById(R.id.btnThang);
        tvTongDanhGia    = view.findViewById(R.id.tvTongDanhGia);
        tvTongTicket     = view.findViewById(R.id.tvTongTicket);
        tvTongDanhGiaKtv = view.findViewById(R.id.tvTongDanhGiaKtv);
        tvDiemTBKtv      = view.findViewById(R.id.tvDiemTBKtv);
        tvTicketChoXuLy  = view.findViewById(R.id.tvTicketChoXuLy);
        tvTicketDangXuLy = view.findViewById(R.id.tvTicketDangXuLy);
        tvTicketDaXuLy   = view.findViewById(R.id.tvTicketDaXuLy);
        tvHaiLong        = view.findViewById(R.id.tvHaiLong);
        tvTrungBinh      = view.findViewById(R.id.tvTrungBinh);
        tvKhongHaiLong   = view.findViewById(R.id.tvKhongHaiLong);
        pbHaiLong        = view.findViewById(R.id.pbHaiLong);
        pbTrungBinh      = view.findViewById(R.id.pbTrungBinh);
        pbKhongHaiLong   = view.findViewById(R.id.pbKhongHaiLong);
        rvThongKeSanPham = view.findViewById(R.id.rvThongKeSanPham);
        rvThongKeSanPham.setLayoutManager(new LinearLayoutManager(getContext()));
        rvThongKeKtv     = view.findViewById(R.id.rvThongKeKtv);
        rvThongKeKtv.setLayoutManager(new LinearLayoutManager(getContext()));

        btnTuan.setOnClickListener(v -> setFilter(false));
        btnThang.setOnClickListener(v -> setFilter(true));

        taiThongKe();
    }

    private void setFilter(boolean thang) {
        isThang = thang;
        btnTuan.setChecked(!thang);
        btnThang.setChecked(thang);
        taiThongKe();
    }

    private void taiThongKe() {
        Calendar cal = Calendar.getInstance();
        if (!isThang) cal.add(Calendar.DAY_OF_YEAR, -7);
        else cal.add(Calendar.MONTH, -1);
        Timestamp tuNgay = new Timestamp(cal.getTime());
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("DanhGia").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;
                    tvTongDanhGia.setText(String.valueOf(snap.size()));
                    int haiLong = 0, trungBinh = 0, khongHaiLong = 0;
                    Map<String, Integer> spCount = new HashMap<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        DanhGia dg = doc.toObject(DanhGia.class);
                        if ("HaiLong".equals(dg.getCamXuc())) haiLong++;
                        else if ("TrungBinh".equals(dg.getCamXuc())) trungBinh++;
                        else khongHaiLong++;
                        String sp = dg.getSanPham();
                        if (sp != null) spCount.put(sp, spCount.getOrDefault(sp, 0) + 1);
                    }
                    int total = snap.size() == 0 ? 1 : snap.size();
                    tvHaiLong.setText(haiLong * 100 / total + "%");
                    tvTrungBinh.setText(trungBinh * 100 / total + "%");
                    tvKhongHaiLong.setText(khongHaiLong * 100 / total + "%");
                    pbHaiLong.setProgress(haiLong * 100 / total);
                    pbTrungBinh.setProgress(trungBinh * 100 / total);
                    pbKhongHaiLong.setProgress(khongHaiLong * 100 / total);

                    int maxSp = spCount.values().stream().mapToInt(Integer::intValue).max().orElse(1);
                    List<ThongKeSanPhamAdapter.Item> spList = new ArrayList<>();
                    for (String sp : SanPham.DANH_SACH)
                        spList.add(new ThongKeSanPhamAdapter.Item(sp, spCount.getOrDefault(sp, 0), maxSp));
                    rvThongKeSanPham.setAdapter(new ThongKeSanPhamAdapter(spList));
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ThongKe", "Lỗi tải đánh giá: " + e.getMessage(), e);
                    if (getContext() != null) tvTongDanhGia.setText("0");
                });

        db.collection("YeuCauHoTro").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;
                    tvTongTicket.setText(String.valueOf(snap.size()));
                    int cho = 0, dang = 0, da = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        String ts = doc.getString("trangThai");
                        if ("ChoXuLy".equals(ts) || "HangCho".equals(ts)) cho++;
                        else if ("DangXuLy".equals(ts)) dang++;
                        else da++;
                    }
                    tvTicketChoXuLy.setText(String.valueOf(cho));
                    tvTicketDangXuLy.setText(String.valueOf(dang));
                    tvTicketDaXuLy.setText(String.valueOf(da));
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ThongKe", "Lỗi tải ticket: " + e.getMessage(), e);
                    if (getContext() != null) tvTongTicket.setText("0");
                });

        db.collection("DanhGiaKTV").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snapDG -> {
                    if (getContext() == null) return;
                    tvTongDanhGiaKtv.setText(String.valueOf(snapDG.size()));
                    double tongDiem = 0;
                    for (QueryDocumentSnapshot doc : snapDG) {
                        Object s = doc.get("soSao");
                        if (s instanceof Number) tongDiem += ((Number) s).doubleValue();
                    }
                    double diemTB = snapDG.size() > 0 ? tongDiem / snapDG.size() : 0;
                    tvDiemTBKtv.setText(String.format("%.1f★", diemTB));
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("ThongKe", "Lỗi tải DG KTV: " + e.getMessage(), e);
                    if (getContext() != null) {
                        tvTongDanhGiaKtv.setText("0");
                        tvDiemTBKtv.setText("0.0★");
                    }
                });


    }



    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
