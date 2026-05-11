package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminDanhGiaAnalyticsFragment extends Fragment {

    private TextView tvTongDanhGiaXau, tvTangGiam, tvXuHuong7Ngay, tvXuHuong30Ngay;
    private ViewPager2 viewPagerChart;
    private LinearLayout llIndicator;
    private TextView tvChartTitle;
    
    private FirebaseFirestore db;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM", Locale.getDefault());
    
    // Dữ liệu cache để vẽ biểu đồ
    private Map<String, Integer> sanPhamCountData = new LinkedHashMap<>();
    private Map<String, Integer> camXucCountData = new LinkedHashMap<>();
    private List<Map<String, Object>> danhGiaXauData = new ArrayList<>();
    private String topSanPham = "...", topCongTy = "...", topTag = "...";

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
        setupViewPager();
        taiDuLieuAnalytics();
    }

    private void initViews(View view) {
        tvTongDanhGiaXau = view.findViewById(R.id.tvTongDanhGiaXau);
        tvTangGiam = view.findViewById(R.id.tvTangGiam);
        tvXuHuong7Ngay = view.findViewById(R.id.tvXuHuong7Ngay);
        tvXuHuong30Ngay = view.findViewById(R.id.tvXuHuong30Ngay);
        viewPagerChart = view.findViewById(R.id.viewPagerChart);
        llIndicator = view.findViewById(R.id.llIndicator);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);
    }

    private void setupViewPager() {
        viewPagerChart.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                String[] titles = {
                    "XU HƯỚNG 7 NGÀY QUA",
                    "PHÂN BỐ CẢM XÚC",
                    "ĐÁNH GIÁ XẤU THEO SẢN PHẨM"
                };
                tvChartTitle.setText(titles[position]);
            }
        });

        // Khắc phục lỗi vuốt ViewPager2 con bị ViewPager2 cha (chuyển tab) bắt sự kiện
        View child = viewPagerChart.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, android.view.MotionEvent event) {
                    int action = event.getAction();
                    if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_MOVE) {
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                    } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    return false;
                }
            });
        }
    }

    private void setupIndicators(int count) {
        llIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(8), dpToPx(8));
            params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
            dot.setLayoutParams(params);
            
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            shape.setColor(i == 0 ? getResColor(androidx.appcompat.R.attr.colorPrimary) 
                                 : getResColor(com.google.android.material.R.attr.colorOutlineVariant));
            dot.setBackground(shape);
            llIndicator.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        int count = llIndicator.getChildCount();
        for (int i = 0; i < count; i++) {
            View dot = llIndicator.getChildAt(i);
            GradientDrawable shape = (GradientDrawable) dot.getBackground();
            shape.setColor(i == position ? getResColor(androidx.appcompat.R.attr.colorPrimary) 
                                         : getResColor(com.google.android.material.R.attr.colorOutlineVariant));
        }
    }

    private void taiDuLieuAnalytics() {
        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay30 = new Timestamp(cal30.getTime());

        db.collection("DanhGia")
                .whereGreaterThan("taoLuc", tuNgay30)
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    
                    List<Map<String, Object>> danhGiaXau = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> data = doc.getData();
                        Object soSaoObj = data.get("soSao");
                        if (soSaoObj instanceof Number && ((Number) soSaoObj).intValue() <= 2) {
                            danhGiaXau.add(data);
                        }
                    }
                    
                    tinhToanThongKe(danhGiaXau);
                })
                .addOnFailureListener(e -> {
                    if (getContext() == null) return;
                    android.util.Log.e("Analytics", "Lỗi tải analytics: " + e.getMessage(), e);
                    if (tvTongDanhGiaXau != null) tvTongDanhGiaXau.setText("Lỗi");
                    if (tvTangGiam != null) tvTangGiam.setText("Lỗi dữ liệu");
                });
    }

    private void tinhToanThongKe(List<Map<String, Object>> danhGiaXau) {
        this.danhGiaXauData = danhGiaXau;
        int tongSo = danhGiaXau.size();
        tvTongDanhGiaXau.setText(String.valueOf(tongSo));

        tinhXuHuongTangGiam(danhGiaXau);

        // Sản phẩm
        sanPhamCountData.clear();
        // Cảm xúc
        camXucCountData.clear();
        camXucCountData.put("KhongHaiLong", 0);
        camXucCountData.put("TrungBinh", 0);
        camXucCountData.put("HaiLong", 0);
        camXucCountData.put("Khac", 0);
        
        for (Map<String, Object> dg : danhGiaXau) {
            String sanPham = (String) dg.get("sanPham");
            if (sanPham != null) {
                sanPhamCountData.put(sanPham, sanPhamCountData.getOrDefault(sanPham, 0) + 1);
            }
            String camXuc = (String) dg.get("camXuc");
            if ("KhongHaiLong".equals(camXuc)) {
                camXucCountData.put("KhongHaiLong", camXucCountData.get("KhongHaiLong") + 1);
            } else if ("TrungBinh".equals(camXuc)) {
                camXucCountData.put("TrungBinh", camXucCountData.get("TrungBinh") + 1);
            } else if ("HaiLong".equals(camXuc)) {
                camXucCountData.put("HaiLong", camXucCountData.get("HaiLong") + 1);
            } else {
                camXucCountData.put("Khac", camXucCountData.get("Khac") + 1);
            }
        }
        
        topSanPham = timMaxKey(sanPhamCountData);

        // Công ty
        Map<String, Integer> congTyCount = new HashMap<>();
        for (Map<String, Object> dg : danhGiaXau) {
            String congTy = (String) dg.get("tenCongTy");
            if (congTy != null && !congTy.isEmpty()) {
                congTyCount.put(congTy, congTyCount.getOrDefault(congTy, 0) + 1);
            }
        }
        topCongTy = timMaxKey(congTyCount);

        // Tag
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
        topTag = timMaxKey(tagCount);

        // KPI
        tinhXuHuongTheoKhoang(danhGiaXau);
        
        // Cập nhật ViewPager
        ChartAdapter adapter = new ChartAdapter();
        viewPagerChart.setAdapter(adapter);
        setupIndicators(3);
    }

    private void tinhXuHuongTangGiam(List<Map<String, Object>> danhGiaXau) {
        Calendar cal60 = Calendar.getInstance();
        cal60.add(Calendar.DAY_OF_YEAR, -60);
        Timestamp tuNgay60 = new Timestamp(cal60.getTime());

        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay30 = new Timestamp(cal30.getTime());

        db.collection("DanhGia")
                .whereGreaterThan("taoLuc", tuNgay60)
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (getContext() == null) return;
                    
                    int soLuong30NgayTruoc = 0;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Object soSaoObj = doc.get("soSao");
                        Object taoLucObj = doc.get("taoLuc");
                        if (soSaoObj instanceof Number && ((Number) soSaoObj).intValue() <= 2
                                && taoLucObj instanceof Timestamp) {
                            Timestamp taoLuc = (Timestamp) taoLucObj;
                            if (taoLuc.compareTo(tuNgay30) <= 0) {
                                soLuong30NgayTruoc++;
                            }
                        }
                    }
                    
                    int soLuong30NgayGanNhat = danhGiaXau.size();
                    
                    if (soLuong30NgayTruoc == 0) {
                        if (soLuong30NgayGanNhat > 0) {
                            tvTangGiam.setText("Mới phát sinh (" + soLuong30NgayGanNhat + ")");
                            tvTangGiam.setTextColor(Color.parseColor("#FF9800"));
                        } else {
                            tvTangGiam.setText("Chưa có DG xấu");
                            tvTangGiam.setTextColor(Color.parseColor("#4CAF50"));
                        }
                        return;
                    }
                    
                    double phanTramThayDoi = ((soLuong30NgayGanNhat - soLuong30NgayTruoc) * 100.0) / soLuong30NgayTruoc;
                    
                    if (phanTramThayDoi > 0) {
                        tvTangGiam.setText("Tăng " + String.format("%.1f%%", phanTramThayDoi));
                        tvTangGiam.setTextColor(Color.parseColor("#EF5350"));
                    } else if (phanTramThayDoi < 0) {
                        tvTangGiam.setText("Giảm " + String.format("%.1f%%", Math.abs(phanTramThayDoi)));
                        tvTangGiam.setTextColor(Color.parseColor("#4CAF50"));
                    } else {
                        tvTangGiam.setText("Không đổi");
                        tvTangGiam.setTextColor(Color.parseColor("#9E9E9E"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvTangGiam != null) tvTangGiam.setText("Lỗi");
                });
    }

    private void tinhXuHuongTheoKhoang(List<Map<String, Object>> danhGiaXau) {
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
        
        tvXuHuong7Ngay.setText(String.valueOf(dem7Ngay));
        tvXuHuong30Ngay.setText(String.valueOf(danhGiaXau.size()));
        
        if (dem7Ngay > 10) {
            tvXuHuong7Ngay.setTextColor(Color.parseColor("#EF5350"));
        } else if (dem7Ngay > 5) {
            tvXuHuong7Ngay.setTextColor(Color.parseColor("#FF9800"));
        } else {
            tvXuHuong7Ngay.setTextColor(Color.parseColor("#4CAF50"));
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

    // === CHART ADAPTER CHO VIEWPAGER ===
    private class ChartAdapter extends RecyclerView.Adapter<ChartAdapter.VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.page_chart_analytics, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            // Set thông tin top chung
            h.tvSanPhamXauNhat.setText(topSanPham);
            h.tvTagPhoBien.setText(topTag);
            h.tvCongTyXauNhat.setText(topCongTy);

            h.llChartArea.removeAllViews();

            if (position == 0) {
                veBieuDoXuHuong7Ngay(h.llChartArea);
            } else if (position == 1) {
                veBieuDoCamXuc(h.llChartArea);
            } else {
                veBieuDoSanPham(h.llChartArea);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        class VH extends RecyclerView.ViewHolder {
            LinearLayout llChartArea;
            TextView tvSanPhamXauNhat, tvTagPhoBien, tvCongTyXauNhat;
            VH(@NonNull View itemView) {
                super(itemView);
                llChartArea = itemView.findViewById(R.id.llChartArea);
                tvSanPhamXauNhat = itemView.findViewById(R.id.tvSanPhamXauNhat);
                tvTagPhoBien = itemView.findViewById(R.id.tvTagPhoBien);
                tvCongTyXauNhat = itemView.findViewById(R.id.tvCongTyXauNhat);
            }
        }
    }

    // ===== BIỂU ĐỒ THANH NGANG: SẢN PHẨM =====
    private void veBieuDoSanPham(LinearLayout container) {
        if (getContext() == null) return;
        
        if (sanPhamCountData.isEmpty()) {
            TextView tv = new TextView(getContext());
            tv.setText("Chưa có dữ liệu sản phẩm");
            tv.setTextColor(getResColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            container.addView(tv);
            return;
        }
        
        int maxVal = sanPhamCountData.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int[] barColors = {
            Color.parseColor("#EF5350"), Color.parseColor("#FF7043"), Color.parseColor("#FFA726"),
            Color.parseColor("#FFCA28"), Color.parseColor("#66BB6A"), Color.parseColor("#42A5F5"),
        };
        
        int colorIdx = 0;
        for (Map.Entry<String, Integer> entry : sanPhamCountData.entrySet()) {
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dpToPx(6), 0, dpToPx(6));
            
            // Label
            TextView tvLabel = new TextView(getContext());
            tvLabel.setText(entry.getKey());
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvLabel.setTextColor(getResColor(com.google.android.material.R.attr.colorOnSurface));
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                dpToPx(70), ViewGroup.LayoutParams.WRAP_CONTENT);
            tvLabel.setLayoutParams(labelParams);
            tvLabel.setMaxLines(2);
            tvLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
            row.addView(tvLabel);
            
            // Bar
            View bar = new View(getContext());
            GradientDrawable barBg = new GradientDrawable();
            barBg.setCornerRadius(dpToPx(4));
            barBg.setColor(barColors[colorIdx % barColors.length]);
            bar.setBackground(barBg);
            int barWidth = maxVal > 0 ? (int)(((float)entry.getValue() / maxVal) * dpToPx(130)) : dpToPx(4);
            barWidth = Math.max(barWidth, dpToPx(4));
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, dpToPx(16));
            barParams.setMarginStart(dpToPx(8));
            bar.setLayoutParams(barParams);
            row.addView(bar);
            
            // Count
            TextView tvCount = new TextView(getContext());
            tvCount.setText(String.valueOf(entry.getValue()));
            tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvCount.setTextColor(getResColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            countParams.setMarginStart(dpToPx(8));
            tvCount.setLayoutParams(countParams);
            row.addView(tvCount);
            
            container.addView(row);
            colorIdx++;
        }
    }

    // ===== BIỂU ĐỒ CỘT: XU HƯỚNG 7 NGÀY =====
    private void veBieuDoXuHuong7Ngay(LinearLayout container) {
        if (getContext() == null) return;
        
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        
        LinkedHashMap<String, Integer> ngayCount = new LinkedHashMap<>();
        SimpleDateFormat sdfKey = new SimpleDateFormat("dd/MM", Locale.getDefault());
        
        Calendar cal = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -i);
            ngayCount.put(sdfKey.format(c.getTime()), 0);
        }
        
        for (Map<String, Object> dg : danhGiaXauData) {
            Object taoLucObj = dg.get("taoLuc");
            if (taoLucObj instanceof Timestamp) {
                String key = sdfKey.format(((Timestamp) taoLucObj).toDate());
                if (ngayCount.containsKey(key)) {
                    ngayCount.put(key, ngayCount.get(key) + 1);
                }
            }
        }
        
        int maxVal = ngayCount.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        maxVal = Math.max(maxVal, 1);
        int maxBarHeight = dpToPx(130);
        
        for (Map.Entry<String, Integer> entry : ngayCount.entrySet()) {
            LinearLayout col = new LinearLayout(getContext());
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            colParams.setMarginStart(dpToPx(2));
            colParams.setMarginEnd(dpToPx(2));
            col.setLayoutParams(colParams);
            
            TextView tvCount = new TextView(getContext());
            tvCount.setText(entry.getValue() > 0 ? String.valueOf(entry.getValue()) : "");
            tvCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvCount.setGravity(Gravity.CENTER);
            tvCount.setTextColor(getResColor(com.google.android.material.R.attr.colorOnSurface));
            tvCount.setTypeface(null, android.graphics.Typeface.BOLD);
            col.addView(tvCount);
            
            View bar = new View(getContext());
            int barHeight = (int)(((float)entry.getValue() / maxVal) * maxBarHeight);
            barHeight = Math.max(barHeight, dpToPx(4));
            GradientDrawable barBg = new GradientDrawable();
            barBg.setCornerRadii(new float[]{dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6), 0, 0, 0, 0});
            
            float ratio = maxVal > 0 ? (float) entry.getValue() / maxVal : 0;
            if (ratio > 0.7f) barBg.setColor(Color.parseColor("#EF5350"));
            else if (ratio > 0.3f) barBg.setColor(Color.parseColor("#FFA726"));
            else barBg.setColor(Color.parseColor("#66BB6A"));
            
            bar.setBackground(barBg);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, barHeight);
            barParams.topMargin = dpToPx(4);
            bar.setLayoutParams(barParams);
            col.addView(bar);
            
            TextView tvDate = new TextView(getContext());
            tvDate.setText(entry.getKey());
            tvDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            tvDate.setGravity(Gravity.CENTER);
            tvDate.setTextColor(getResColor(com.google.android.material.R.attr.colorOnSurfaceVariant));
            tvDate.setPadding(0, dpToPx(6), 0, 0);
            col.addView(tvDate);
            
            container.addView(col);
        }
    }

    // ===== BIỂU ĐỒ TIẾN ĐỘ: CẢM XÚC =====
    private void veBieuDoCamXuc(LinearLayout container) {
        if (getContext() == null) return;
        
        int tongSo = danhGiaXauData.size();
        Map<String, String> labelMap = new LinkedHashMap<>();
        labelMap.put("KhongHaiLong", "Không hài lòng");
        labelMap.put("TrungBinh", "Trung bình");
        labelMap.put("HaiLong", "Hài lòng");
        labelMap.put("Khac", "Không rõ");
        
        Map<String, Integer> colorMap = new HashMap<>();
        colorMap.put("KhongHaiLong", Color.parseColor("#EF5350"));
        colorMap.put("TrungBinh", Color.parseColor("#FFA726"));
        colorMap.put("HaiLong", Color.parseColor("#66BB6A"));
        colorMap.put("Khac", Color.parseColor("#9E9E9E"));
        
        for (Map.Entry<String, String> entry : labelMap.entrySet()) {
            String key = entry.getKey();
            int count = camXucCountData.getOrDefault(key, 0);
            if (count == 0) continue;
            
            double phanTram = tongSo > 0 ? (count * 100.0 / tongSo) : 0;
            
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, dpToPx(8), 0, dpToPx(8));
            
            LinearLayout labelRow = new LinearLayout(getContext());
            labelRow.setOrientation(LinearLayout.HORIZONTAL);
            
            TextView tvLabel = new TextView(getContext());
            tvLabel.setText(entry.getValue());
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvLabel.setTextColor(getResColor(com.google.android.material.R.attr.colorOnSurface));
            tvLabel.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            labelRow.addView(tvLabel);
            
            TextView tvPercent = new TextView(getContext());
            tvPercent.setText(count + " (" + String.format("%.0f%%", phanTram) + ")");
            tvPercent.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            tvPercent.setTypeface(null, android.graphics.Typeface.BOLD);
            tvPercent.setTextColor(colorMap.getOrDefault(key, Color.GRAY));
            labelRow.addView(tvPercent);
            
            row.addView(labelRow);
            
            ProgressBar pb = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
            pb.setMax(100);
            pb.setProgress((int) phanTram);
            pb.setScaleY(2f);
            LinearLayout.LayoutParams pbParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(8));
            pbParams.topMargin = dpToPx(6);
            pb.setLayoutParams(pbParams);
            pb.getProgressDrawable().setColorFilter(
                colorMap.getOrDefault(key, Color.GRAY), android.graphics.PorterDuff.Mode.SRC_IN);
            row.addView(pb);
            
            container.addView(row);
        }
    }

    // === HELPERS ===
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    
    private int getResColor(int attrResId) {
        TypedValue typedValue = new TypedValue();
        if (getContext() != null && getContext().getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return Color.GRAY;
    }
}