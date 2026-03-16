package com.example.customercareproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.SanPham;
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
    private TextView tvTongDanhGia, tvTongTicket, tvHaiLong, tvTrungBinh, tvKhongHaiLong, tvTopTags;
    private RecyclerView rvThongKeSanPham;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_thong_ke, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTongDanhGia = view.findViewById(R.id.tvTongDanhGia);
        tvTongTicket = view.findViewById(R.id.tvTongTicket);
        tvHaiLong = view.findViewById(R.id.tvHaiLong);
        tvTrungBinh = view.findViewById(R.id.tvTrungBinh);
        tvKhongHaiLong = view.findViewById(R.id.tvKhongHaiLong);
        tvTopTags = view.findViewById(R.id.tvTopTags);
        rvThongKeSanPham = view.findViewById(R.id.rvThongKeSanPham);
        rvThongKeSanPham.setLayoutManager(new LinearLayoutManager(getContext()));

        Button btnTuan = view.findViewById(R.id.btnTuan);
        Button btnThang = view.findViewById(R.id.btnThang);

        btnTuan.setOnClickListener(v -> { isThang = false; taiThongKe(); });
        btnThang.setOnClickListener(v -> { isThang = true; taiThongKe(); });

        taiThongKe();
    }

    private void taiThongKe() {
        Calendar cal = Calendar.getInstance();
        if (!isThang) cal.add(Calendar.DAY_OF_YEAR, -7);
        else cal.add(Calendar.MONTH, -1);
        Timestamp tuNgay = new Timestamp(cal.getTime());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Đếm đánh giá
        db.collection("DanhGia").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> {
                    tvTongDanhGia.setText(String.valueOf(snap.size()));

                    int haiLong = 0, trungBinh = 0, khongHaiLong = 0;
                    Map<String, Integer> tagCount = new HashMap<>();
                    Map<String, Integer> spCount = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        DanhGia dg = doc.toObject(DanhGia.class);
                        // Cảm xúc
                        if ("HaiLong".equals(dg.getCamXuc())) haiLong++;
                        else if ("TrungBinh".equals(dg.getCamXuc())) trungBinh++;
                        else khongHaiLong++;
                        // Tags
                        if (dg.getTags() != null) {
                            for (String tag : dg.getTags()) {
                                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                            }
                        }
                        // Sản phẩm
                        String sp = dg.getSanPham();
                        if (sp != null) spCount.put(sp, spCount.getOrDefault(sp, 0) + 1);
                    }

                    int total = snap.size() == 0 ? 1 : snap.size();
                    tvHaiLong.setText("Hài lòng\n" + (haiLong * 100 / total) + "%");
                    tvTrungBinh.setText("Trung bình\n" + (trungBinh * 100 / total) + "%");
                    tvKhongHaiLong.setText("Không hài lòng\n" + (khongHaiLong * 100 / total) + "%");

                    // Top tags
                    StringBuilder sb = new StringBuilder();
                    tagCount.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(5)
                            .forEach(e -> sb.append(e.getKey()).append(": ").append(e.getValue()).append(" lần\n"));
                    tvTopTags.setText(sb.length() > 0 ? sb.toString() : "Chưa có dữ liệu");

                    // Thống kê sản phẩm
                    List<String[]> spList = new ArrayList<>();
                    for (String sp : SanPham.DANH_SACH) {
                        spList.add(new String[]{sp, String.valueOf(spCount.getOrDefault(sp, 0))});
                    }
                    rvThongKeSanPham.setAdapter(new ThongKeSanPhamAdapter(spList));
                });

        // Đếm ticket
        db.collection("YeuCauHoTro").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> tvTongTicket.setText(String.valueOf(snap.size())));
    }
}
