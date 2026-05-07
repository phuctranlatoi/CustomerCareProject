package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.customercareproject.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dashboard Analytics về xu hướng đánh giá xấu
 * KHÔNG cần API key - chỉ truy vấn Firestore và tính toán thống kê
 */
public class AdminDanhGiaAnalyticsFragment extends Fragment {

    private TextView tvTongDanhGiaXau, tvTangGiam, tvSanPhamXauNhat, tvCongTyXauNhat;
    private TextView tvTagPhoBien, tvXuHuong7Ngay, tvXuHuong30Ngay;
    
    private FirebaseFirestore db;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_danh_gia_analytics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        db = FirebaseFirestore.getInstance();
        initViews(view);
        taiDuLieuAnalytics();
    }

    private void initViews(View view) {
        tvTongDanhGiaXau = view.findViewById(R.id.tvTongDanhGiaXau);
        tvTangGiam = view.findViewById(R.id.tvTangGiam);
        tvSanPhamXauNhat = view.findViewById(R.id.tvSanPhamXauNhat);
        tvCongTyXauNhat = view.findViewById(R.id.tvCongTyXauNhat);
        tvTagPhoBien = view.findViewById(R.id.tvTagPhoBien);
        tvXuHuong7Ngay = view.findViewById(R.id.tvXuHuong7Ngay);
        tvXuHuong30Ngay = view.findViewById(R.id.tvXuHuong30Ngay);
    }

    private void taiDuLieuAnalytics() {
        // 1. Lấy đánh giá xấu 30 ngày qua
        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay30 = new Timestamp(cal30.getTime());

        db.collection("DanhGia")
                .whereLessThanOrEqualTo("soSao", 2)
                .whereGreaterThan("taoLuc", tuNgay30)
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    
                    List<Map<String, Object>> danhGiaXau = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> data = doc.getData();
                        danhGiaXau.add(data);
                    }
                    
                    // Tính toán các thống kê
                    tinhToanThongKe(danhGiaXau);
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    // Hiển thị lỗi
                });
    }

    private void tinhToanThongKe(List<Map<String, Object>> danhGiaXau) {
        // 📊 1. TỔNG SỐ ĐÁNH GIÁ XẤU
        int tongSo = danhGiaXau.size();
        tvTongDanhGiaXau.setText(String.valueOf(tongSo));

        // 📈 2. XU HƯỚNG TĂNG/GIẢM (so với 30 ngày trước)
        tinhXuHuongTangGiam(danhGiaXau);

        // 🥧 3. SẢN PHẨM CÓ NHIỀU ĐÁNH GIÁ XẤU NHẤT
        Map<String, Integer> sanPhamCount = new HashMap<>();
        for (Map<String, Object> dg : danhGiaXau) {
            String sanPham = (String) dg.get("sanPham");
            if (sanPham != null) {
                sanPhamCount.put(sanPham, sanPhamCount.getOrDefault(sanPham, 0) + 1);
            }
        }
        String sanPhamXauNhat = timMaxKey(sanPhamCount);
        int soLuongSP = sanPhamCount.getOrDefault(sanPhamXauNhat, 0);
        tvSanPhamXauNhat.setText(sanPhamXauNhat + " (" + soLuongSP + ")");

        // 🏢 4. CÔNG TY CÓ NHIỀU ĐÁNH GIÁ XẤU NHẤT
        Map<String, Integer> congTyCount = new HashMap<>();
        for (Map<String, Object> dg : danhGiaXau) {
            String congTy = (String) dg.get("tenCongTy");
            if (congTy != null) {
                congTyCount.put(congTy, congTyCount.getOrDefault(congTy, 0) + 1);
            }
        }
        String congTyXauNhat = timMaxKey(congTyCount);
        int soLuongCT = congTyCount.getOrDefault(congTyXauNhat, 0);
        tvCongTyXauNhat.setText(congTyXauNhat + " (" + soLuongCT + ")");

        // 🏷️ 5. TAG PHỔ BIẾN NHẤT
        Map<String, Integer> tagCount = new HashMap<>();
        for (Map<String, Object> dg : danhGiaXau) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) dg.get("tags");
            if (tags != null) {
                for (String tag : tags) {
                    tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                }
            }
        }
        String tagPhoBien = timMaxKey(tagCount);
        int soLuongTag = tagCount.getOrDefault(tagPhoBien, 0);
        double phanTramTag = tongSo > 0 ? (soLuongTag * 100.0 / tongSo) : 0;
        tvTagPhoBien.setText(tagPhoBien + " (" + String.format("%.1f%%", phanTramTag) + ")");

        // 📅 6. XU HƯỚNG 7 NGÀY & 30 NGÀY
        tinhXuHuongTheoKhoang(danhGiaXau);
    }

    private void tinhXuHuongTangGiam(List<Map<String, Object>> danhGiaXau) {
        // So sánh 30 ngày gần nhất với 30 ngày trước đó
        Calendar cal60 = Calendar.getInstance();
        cal60.add(Calendar.DAY_OF_YEAR, -60);
        Timestamp tuNgay60 = new Timestamp(cal60.getTime());

        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay30 = new Timestamp(cal30.getTime());

        db.collection("DanhGia")
                .whereLessThanOrEqualTo("soSao", 2)
                .whereGreaterThan("taoLuc", tuNgay60)
                .whereLessThanOrEqualTo("taoLuc", tuNgay30)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    
                    int soLuong30NgayTruoc = querySnapshot.size();
                    int soLuong30NgayGanNhat = danhGiaXau.size();
                    
                    if (soLuong30NgayTruoc == 0) {
                        tvTangGiam.setText("Không có dữ liệu so sánh");
                        return;
                    }
                    
                    double phanTramThayDoi = ((soLuong30NgayGanNhat - soLuong30NgayTruoc) * 100.0) / soLuong30NgayTruoc;
                    
                    if (phanTramThayDoi > 0) {
                        tvTangGiam.setText("📈 Tăng " + String.format("%.1f%%", phanTramThayDoi));
                        tvTangGiam.setTextColor(Color.RED);
                    } else if (phanTramThayDoi < 0) {
                        tvTangGiam.setText("📉 Giảm " + String.format("%.1f%%", Math.abs(phanTramThayDoi)));
                        tvTangGiam.setTextColor(Color.GREEN);
                    } else {
                        tvTangGiam.setText("➡️ Không đổi");
                        tvTangGiam.setTextColor(Color.GRAY);
                    }
                });
    }

    private void tinhXuHuongTheoKhoang(List<Map<String, Object>> danhGiaXau) {
        // Đếm đánh giá xấu trong 7 ngày gần nhất
        Calendar cal7 = Calendar.getInstance();
        cal7.add(Calendar.DAY_OF_YEAR, -7);
        Timestamp tuNgay7 = new Timestamp(cal7.getTime());
        
        int dem7Ngay = 0;
        for (Map<String, Object> dg : danhGiaXau) {
            Timestamp taoLuc = (Timestamp) dg.get("taoLuc");
            if (taoLuc != null && taoLuc.compareTo(tuNgay7) > 0) {
                dem7Ngay++;
            }
        }
        
        tvXuHuong7Ngay.setText(dem7Ngay + " đánh giá xấu");
        tvXuHuong30Ngay.setText(danhGiaXau.size() + " đánh giá xấu");
        
        // Màu sắc cảnh báo
        if (dem7Ngay > 10) {
            tvXuHuong7Ngay.setTextColor(Color.RED);
        } else if (dem7Ngay > 5) {
            tvXuHuong7Ngay.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else {
            tvXuHuong7Ngay.setTextColor(Color.GREEN);
        }
    }

    private String timMaxKey(Map<String, Integer> map) {
        if (map.isEmpty()) return "Không có dữ liệu";
        
        String maxKey = "";
        int maxValue = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (entry.getValue() > maxValue) {
                maxValue = entry.getValue();
                maxKey = entry.getKey();
            }
        }
        return maxKey.isEmpty() ? "Không có dữ liệu" : maxKey;
    }
}