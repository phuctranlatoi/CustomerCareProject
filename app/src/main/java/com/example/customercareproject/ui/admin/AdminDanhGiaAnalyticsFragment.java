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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.utils.NlpHelper;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
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

    // Views
    private TextView tvKpiTongYeuCau, tvKpiTongDanhGia, tvKpiDiemKtv, tvKpiDanhGiaXau;
    private ViewPager2 viewPagerChart;
    private LinearLayout llIndicator;
    private TextView tvChartTitle;
    private TextView tvAiInsight;
    private MaterialButton btnTaoInsight;
    private LinearLayout llCumDeContainer;
    private RecyclerView rvThongKeKtv;
    
    private FirebaseFirestore db;
    private static final SimpleDateFormat SDF_DAY = new SimpleDateFormat("dd/MM", Locale.getDefault());
    
    // Dữ liệu cache
    private List<YeuCauHoTro> listYc30 = new ArrayList<>();
    private List<DanhGia> listDg30 = new ArrayList<>();
    private Map<String, List<Double>> ktvScores = new HashMap<>();
    private int totalKtvRatings = 0;
    private double sumKtvScore = 0;
    
    // Label cache cho các biểu đồ
    private String topSpXau = "...", topTag = "...", topCongTy = "...";
    private String topKhachHang = "...", topYeuCauNhieu = "...";

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
        
        btnTaoInsight.setOnClickListener(v -> taoAiInsight());
        
        taiDuLieuToanCuc();
    }

    private void initViews(View view) {
        tvKpiTongYeuCau = view.findViewById(R.id.tvKpiTongYeuCau);
        tvKpiTongDanhGia = view.findViewById(R.id.tvKpiTongDanhGia);
        tvKpiDiemKtv = view.findViewById(R.id.tvKpiDiemKtv);
        tvKpiDanhGiaXau = view.findViewById(R.id.tvKpiDanhGiaXau);
        
        viewPagerChart = view.findViewById(R.id.viewPagerChart);
        llIndicator = view.findViewById(R.id.llIndicator);
        tvChartTitle = view.findViewById(R.id.tvChartTitle);
        
        tvAiInsight = view.findViewById(R.id.tvAiInsight);
        btnTaoInsight = view.findViewById(R.id.btnTaoInsight);
        llCumDeContainer = view.findViewById(R.id.llCumDeContainer);
        
        rvThongKeKtv = view.findViewById(R.id.rvThongKeKtv);
        rvThongKeKtv.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupViewPager() {
        viewPagerChart.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);
                String[] titles = {
                    "XU HƯỚNG YÊU CẦU 7 NGÀY",
                    "PHÂN BỐ CẢM XÚC (30 NGÀY)",
                    "SẢN PHẨM BỊ ĐÁNH GIÁ XẤU"
                };
                tvChartTitle.setText(titles[position]);
            }
        });

        // Chống xung đột vuốt với Tab Layout bên ngoài
        View child = viewPagerChart.getChildAt(0);
        if (child instanceof RecyclerView) {
            child.setOnTouchListener((v, event) -> {
                int action = event.getAction();
                if (action == android.view.MotionEvent.ACTION_DOWN || action == android.view.MotionEvent.ACTION_MOVE) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                } else if (action == android.view.MotionEvent.ACTION_UP || action == android.view.MotionEvent.ACTION_CANCEL) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return false;
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

    private void taiDuLieuToanCuc() {
        Calendar cal30 = Calendar.getInstance();
        cal30.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay30 = new Timestamp(cal30.getTime());

        // 1. Tải YeuCauHoTro
        db.collection("YeuCauHoTro").whereGreaterThan("taoLuc", tuNgay30).get()
            .addOnSuccessListener(snap -> {
                listYc30.clear();
                for (QueryDocumentSnapshot doc : snap) {
                    listYc30.add(doc.toObject(YeuCauHoTro.class));
                }
                kiemTraHoanThanhTaiData();
            });

        // 2. Tải DanhGia
        db.collection("DanhGia").whereGreaterThan("taoLuc", tuNgay30).get()
            .addOnSuccessListener(snap -> {
                listDg30.clear();
                for (QueryDocumentSnapshot doc : snap) {
                    listDg30.add(doc.toObject(DanhGia.class));
                }
                kiemTraHoanThanhTaiData();
            });

        // 3. Tải DanhGiaKTV
        db.collection("DanhGiaKTV").whereGreaterThan("taoLuc", tuNgay30).get()
            .addOnSuccessListener(snap -> {
                totalKtvRatings = snap.size();
                sumKtvScore = 0;
                ktvScores.clear();
                for (QueryDocumentSnapshot doc : snap) {
                    Object s = doc.get("soSao");
                    String ktvTen = doc.getString("ktvTen");
                    if (s instanceof Number && ktvTen != null) {
                        double score = ((Number) s).doubleValue();
                        sumKtvScore += score;
                        if (!ktvScores.containsKey(ktvTen)) ktvScores.put(ktvTen, new ArrayList<>());
                        ktvScores.get(ktvTen).add(score);
                    }
                }
                kiemTraHoanThanhTaiData();
            });
            
        // Load AI Insight cũ
        taiCumTuFirestore();
    }
    
    // Đếm số lượng callback để cập nhật giao diện
    private int dataLoadedCount = 0;
    private void kiemTraHoanThanhTaiData() {
        dataLoadedCount++;
        if (dataLoadedCount >= 3) {
            capNhatGiaoDien();
        }
    }

    private void capNhatGiaoDien() {
        // Cập nhật KPIs
        tvKpiTongYeuCau.setText(String.valueOf(listYc30.size()));
        tvKpiTongDanhGia.setText(String.valueOf(listDg30.size()));
        
        double diemTB = totalKtvRatings > 0 ? sumKtvScore / totalKtvRatings : 0;
        tvKpiDiemKtv.setText(String.format("%.1f", diemTB));
        
        int danhGiaXau = 0;
        for (DanhGia dg : listDg30) {
            if (dg.getSoSao() <= 2) danhGiaXau++;
        }
        tvKpiDanhGiaXau.setText(String.valueOf(danhGiaXau));

        // Phân tích dữ liệu tìm các Label nổi bật
        phanTichLabels();
        
        // Vẽ lại ViewPager2
        ChartAdapter adapter = new ChartAdapter();
        viewPagerChart.setAdapter(adapter);
        setupIndicators(3);
        
        // Cập nhật KTV Adapter
        capNhatDanhSachKtv();
    }
    
    private void phanTichLabels() {
        // Cho đánh giá xấu
        Map<String, Integer> spXauCount = new HashMap<>();
        Map<String, Integer> tagCount = new HashMap<>();
        Map<String, Integer> congTyCount = new HashMap<>();
        
        for (DanhGia dg : listDg30) {
            if (dg.getSoSao() <= 2) {
                if (dg.getSanPham() != null) spXauCount.put(dg.getSanPham(), spXauCount.getOrDefault(dg.getSanPham(), 0) + 1);
                if (dg.getTenCongTy() != null) congTyCount.put(dg.getTenCongTy(), congTyCount.getOrDefault(dg.getTenCongTy(), 0) + 1);
                if (dg.getTags() != null) {
                    for (String t : dg.getTags()) tagCount.put(t, tagCount.getOrDefault(t, 0) + 1);
                }
            }
        }
        topSpXau = timMaxKey(spXauCount);
        topTag = timMaxKey(tagCount);
        topCongTy = timMaxKey(congTyCount);
        
        // Cho toàn bộ (người yêu cầu nhiều)
        Map<String, Integer> hoTenCount = new HashMap<>();
        for (YeuCauHoTro yc : listYc30) {
            String ten = yc.getHoTen();
            if (ten != null && !ten.isEmpty()) hoTenCount.put(ten, hoTenCount.getOrDefault(ten, 0) + 1);
        }
        topKhachHang = timMaxKey(hoTenCount);
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
            h.llChartArea.removeAllViews();

            if (position == 0) {
                // Biểu đồ Ticket 7 ngày
                h.tvLabel1.setText("Đang chờ xử lý");
                h.tvLabel2.setText("Đang xử lý");
                h.tvLabel3.setText("KH Yêu cầu nhiều");
                
                int cho = 0, dang = 0;
                for(YeuCauHoTro yc : listYc30) {
                    if ("ChoXuLy".equals(yc.getTrangThai()) || "HangCho".equals(yc.getTrangThai())) cho++;
                    else if ("DangXuLy".equals(yc.getTrangThai())) dang++;
                }
                
                h.tvSanPhamXauNhat.setText(String.valueOf(cho));
                h.tvSanPhamXauNhat.setTextColor(Color.parseColor("#FF9800"));
                h.tvTagPhoBien.setText(String.valueOf(dang));
                h.tvTagPhoBien.setTextColor(Color.parseColor("#2196F3"));
                h.tvCongTyXauNhat.setText(topKhachHang);
                
                veBieuDoCotYeuCau(h.llChartArea);
                
            } else if (position == 1) {
                // Biểu đồ Cảm xúc 30 ngày (Bar chart ngang)
                h.tvLabel1.setText("Đánh giá tích cực");
                h.tvLabel2.setText("Vấn đề phổ biến");
                h.tvLabel3.setText("Sản phẩm bị chê");
                
                int tot = 0;
                for(DanhGia dg : listDg30) {
                    if ("HaiLong".equals(dg.getCamXuc())) tot++;
                }
                int phanTram = listDg30.size() > 0 ? (tot * 100 / listDg30.size()) : 0;
                h.tvSanPhamXauNhat.setText(phanTram + "%");
                h.tvSanPhamXauNhat.setTextColor(Color.parseColor("#4CAF50"));
                h.tvTagPhoBien.setText(topTag);
                h.tvCongTyXauNhat.setText(topSpXau);
                
                veBieuDoCamXucBar(h.llChartArea);
                
            } else {
                // Biểu đồ SP xấu nhất
                h.tvLabel1.setText("SP xấu nhất");
                h.tvLabel2.setText("Vấn đề phổ biến");
                h.tvLabel3.setText("Công ty yêu cầu nhiều nhất");
                
                h.tvSanPhamXauNhat.setText(topSpXau);
                h.tvSanPhamXauNhat.setTextColor(Color.parseColor("#EF5350"));
                h.tvTagPhoBien.setText(topTag);
                h.tvCongTyXauNhat.setText(topCongTy);
                
                veBieuDoSanPhamXau(h.llChartArea);
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }

        class VH extends RecyclerView.ViewHolder {
            LinearLayout llChartArea;
            TextView tvSanPhamXauNhat, tvTagPhoBien, tvCongTyXauNhat;
            TextView tvLabel1, tvLabel2, tvLabel3;
            VH(@NonNull View itemView) {
                super(itemView);
                llChartArea = itemView.findViewById(R.id.llChartArea);
                tvSanPhamXauNhat = itemView.findViewById(R.id.tvSanPhamXauNhat);
                tvTagPhoBien = itemView.findViewById(R.id.tvTagPhoBien);
                tvCongTyXauNhat = itemView.findViewById(R.id.tvCongTyXauNhat);
                tvLabel1 = itemView.findViewById(R.id.tvLabel1);
                tvLabel2 = itemView.findViewById(R.id.tvLabel2);
                tvLabel3 = itemView.findViewById(R.id.tvLabel3);
            }
        }
    }

    // ===== VẼ BIỂU ĐỒ 1: YÊU CẦU 7 NGÀY =====
    private void veBieuDoCotYeuCau(LinearLayout container) {
        if (getContext() == null) return;
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        
        LinkedHashMap<String, Integer> ngayCount = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -i);
            ngayCount.put(SDF_DAY.format(c.getTime()), 0);
        }
        
        for (YeuCauHoTro yc : listYc30) {
            if (yc.getTaoLuc() != null) {
                String key = SDF_DAY.format(yc.getTaoLuc().toDate());
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
            LinearLayout.LayoutParams colParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
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
            barBg.setColor(getResColor(androidx.appcompat.R.attr.colorPrimary));
            
            bar.setBackground(barBg);
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, barHeight);
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

    // ===== VẼ BIỂU ĐỒ 2: CẢM XÚC BAR CHART NGANG =====
    private void veBieuDoCamXucBar(LinearLayout container) {
        if (getContext() == null) return;
        
        Map<String, Integer> cxCount = new LinkedHashMap<>();
        cxCount.put("Hài lòng", 0);
        cxCount.put("Trung bình", 0);
        cxCount.put("Không hài lòng", 0);
        cxCount.put("Không rõ", 0);
        
        for (DanhGia dg : listDg30) {
            String cx = dg.getCamXuc();
            if ("HaiLong".equals(cx)) cxCount.put("Hài lòng", cxCount.get("Hài lòng") + 1);
            else if ("TrungBinh".equals(cx)) cxCount.put("Trung bình", cxCount.get("Trung bình") + 1);
            else if ("KhongHaiLong".equals(cx)) cxCount.put("Không hài lòng", cxCount.get("Không hài lòng") + 1);
            else cxCount.put("Không rõ", cxCount.get("Không rõ") + 1);
        }
        
        int[] barColors = { Color.parseColor("#4CAF50"), Color.parseColor("#FF9800"), Color.parseColor("#EF5350"), Color.parseColor("#9E9E9E") };
        veBieuDoNgangChung(container, cxCount, barColors);
    }

    // ===== VẼ BIỂU ĐỒ 3: SẢN PHẨM XẤU =====
    private void veBieuDoSanPhamXau(LinearLayout container) {
        if (getContext() == null) return;
        
        Map<String, Integer> spXauCount = new LinkedHashMap<>();
        for (DanhGia dg : listDg30) {
            if (dg.getSoSao() <= 2 && dg.getSanPham() != null) {
                spXauCount.put(dg.getSanPham(), spXauCount.getOrDefault(dg.getSanPham(), 0) + 1);
            }
        }
        
        int[] barColors = { Color.parseColor("#EF5350"), Color.parseColor("#FF7043"), Color.parseColor("#FFA726"), Color.parseColor("#FFCA28"), Color.parseColor("#66BB6A") };
        veBieuDoNgangChung(container, spXauCount, barColors);
    }

    // Hàm chung vẽ biểu đồ ngang
    private void veBieuDoNgangChung(LinearLayout container, Map<String, Integer> dataMap, int[] colors) {
        int maxVal = dataMap.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        int colorIdx = 0;
        
        for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
            // Chỉ hiển thị các mục có dữ liệu để tiết kiệm không gian
            if (entry.getValue() == 0 && dataMap.size() > 4) continue;
            
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dpToPx(6), 0, dpToPx(6));
            
            // Label
            TextView tvLabel = new TextView(getContext());
            tvLabel.setText(entry.getKey());
            tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            tvLabel.setTextColor(getResColor(com.google.android.material.R.attr.colorOnSurface));
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(dpToPx(85), ViewGroup.LayoutParams.WRAP_CONTENT);
            tvLabel.setLayoutParams(labelParams);
            tvLabel.setMaxLines(2);
            tvLabel.setEllipsize(android.text.TextUtils.TruncateAt.END);
            row.addView(tvLabel);
            
            // Bar
            View bar = new View(getContext());
            GradientDrawable barBg = new GradientDrawable();
            barBg.setCornerRadius(dpToPx(4));
            barBg.setColor(colors[colorIdx % colors.length]);
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
            LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            countParams.setMarginStart(dpToPx(8));
            tvCount.setLayoutParams(countParams);
            row.addView(tvCount);
            
            container.addView(row);
            colorIdx++;
        }
        
        if (container.getChildCount() == 0) {
            TextView tv = new TextView(getContext());
            tv.setText("Không có dữ liệu phù hợp");
            tv.setGravity(Gravity.CENTER);
            container.addView(tv);
        }
    }
    
    // === CẬP NHẬT KTV LIST ===
    private void capNhatDanhSachKtv() {
        if (getContext() == null) return;
        Map<String, ThongKeKtvAdapter.KtvItem> ktvMap = new HashMap<>();
        
        // Cập nhật từ Ticket
        for (YeuCauHoTro yc : listYc30) {
            String ten = yc.getKtvTen();
            if (ten == null || ten.isEmpty()) continue;
            
            if (!ktvMap.containsKey(ten)) {
                ktvMap.put(ten, new ThongKeKtvAdapter.KtvItem(ten, "Hoạt động", 0, 0, 0, 0));
            }
            ThongKeKtvAdapter.KtvItem item = ktvMap.get(ten);
            item.tongTicket++;
            if ("DangXuLy".equals(yc.getTrangThai())) item.soTicketDang++;
        }
        
        // Cập nhật từ Đánh giá KTV
        for (String ten : ktvScores.keySet()) {
            if (!ktvMap.containsKey(ten)) {
                ktvMap.put(ten, new ThongKeKtvAdapter.KtvItem(ten, "Hoạt động", 0, 0, 0, 0));
            }
        }
        
        // Tính điểm trung bình cho từng KTV
        for (ThongKeKtvAdapter.KtvItem item : ktvMap.values()) {
            if (ktvScores.containsKey(item.ten)) {
                List<Double> scores = ktvScores.get(item.ten);
                item.soLuotDanhGia = scores.size();
                double sum = 0;
                for (double s : scores) sum += s;
                item.diemDanhGia = sum / item.soLuotDanhGia;
            }
        }
        
        List<ThongKeKtvAdapter.KtvItem> listKtv = new ArrayList<>(ktvMap.values());
        listKtv.sort((a, b) -> Integer.compare(b.tongTicket, a.tongTicket));
        if (listKtv.size() > 5) listKtv = listKtv.subList(0, 5); // Chỉ lấy Top 5
        
        rvThongKeKtv.setAdapter(new ThongKeKtvAdapter(listKtv));
    }

    // === XỬ LÝ AI INSIGHTS ===
    private void taiCumTuFirestore() {
        db.collection("InsightCumDe").get()
            .addOnSuccessListener(snap -> {
                if (getContext() == null || snap.isEmpty()) return;
                List<Map<String, Object>> cumList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snap) cumList.add(doc.getData());
                hienThiCumCards(cumList);
            });
    }

    private void taoAiInsight() {
        if (getContext() == null) return;
        btnTaoInsight.setEnabled(false);
        tvAiInsight.setText("AI đang phân tích và gom cụm dữ liệu...");
        llCumDeContainer.removeAllViews();

        NlpHelper.phanTichTongHop(new NlpHelper.InsightCallback() {
            @Override
            public void onResult(String insight, List<Map<String, Object>> cumDe) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    tvAiInsight.setText(insight.isEmpty() ? "Không có đủ dữ liệu để phân tích." : insight);
                    hienThiCumCards(cumDe);
                    btnTaoInsight.setEnabled(true);
                });
            }
            @Override
            public void onError(String error) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    tvAiInsight.setText("Lỗi: " + error);
                    btnTaoInsight.setEnabled(true);
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void hienThiCumCards(List<Map<String, Object>> cumList) {
        if (getContext() == null) return;
        llCumDeContainer.removeAllViews();

        for (Map<String, Object> cum : cumList) {
            String chuDe = (String) cum.getOrDefault("chuDe", "Chủ đề");
            Object soLuong = cum.get("soLuong");
            String uuTien = (String) cum.getOrDefault("uuTien", "TrungBinh");
            List<String> danhGia = (List<String>) cum.get("danhGia");
            String nguyenNhan = (String) cum.get("nguyenNhan");
            String goiY = (String) cum.get("goiY");

            int colorBg, colorText;
            String badgeText;
            switch (uuTien) {
                case "Cao":
                    colorBg = androidx.core.content.ContextCompat.getColor(getContext(), R.color.error_container);
                    colorText = androidx.core.content.ContextCompat.getColor(getContext(), R.color.error);
                    badgeText = "Ưu tiên cao";
                    break;
                case "Thap":
                    colorBg = androidx.core.content.ContextCompat.getColor(getContext(), R.color.success_container);
                    colorText = androidx.core.content.ContextCompat.getColor(getContext(), R.color.success);
                    badgeText = "Thấp";
                    break;
                default:
                    colorBg = androidx.core.content.ContextCompat.getColor(getContext(), R.color.warning_container);
                    colorText = androidx.core.content.ContextCompat.getColor(getContext(), R.color.warning);
                    badgeText = "Trung bình";
            }

            CardView card = new CardView(getContext());
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, dpToPx(10));
            card.setLayoutParams(cardLp);
            card.setRadius(dpToPx(14));
            card.setCardElevation(dpToPx(2));
            card.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.surface));

            LinearLayout inner = new LinearLayout(getContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            int p = dpToPx(16);
            inner.setPadding(p, p, p, p);

            // Header
            LinearLayout header = new LinearLayout(getContext());
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(android.view.Gravity.CENTER_VERTICAL);
            header.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView tvTen = new TextView(getContext());
            tvTen.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvTen.setText(chuDe);
            tvTen.setTextSize(15);
            tvTen.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.on_surface));
            tvTen.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvBadge = new TextView(getContext());
            tvBadge.setText(badgeText);
            tvBadge.setTextSize(11);
            tvBadge.setTextColor(colorText);
            tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            tvBadge.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
            GradientDrawable badgeBg = new GradientDrawable();
            badgeBg.setColor(colorBg);
            badgeBg.setCornerRadius(dpToPx(20));
            tvBadge.setBackground(badgeBg);

            header.addView(tvTen);
            header.addView(tvBadge);
            inner.addView(header);

            // Số lượng
            TextView tvCount = new TextView(getContext());
            tvCount.setText((soLuong != null ? soLuong : 0) + " trường hợp");
            tvCount.setTextSize(12);
            tvCount.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.text_secondary));
            LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            countLp.setMargins(0, dpToPx(4), 0, dpToPx(10));
            tvCount.setLayoutParams(countLp);
            inner.addView(tvCount);

            // Nguyên nhân
            if (nguyenNhan != null && !nguyenNhan.isEmpty()) {
                TextView tvNN = new TextView(getContext());
                tvNN.setText("Nguyên nhân: " + nguyenNhan);
                tvNN.setTextSize(13);
                tvNN.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.on_surface_variant));
                tvNN.setLineSpacing(0, 1.2f);
                LinearLayout.LayoutParams nnLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nnLp.setMargins(0, 0, 0, dpToPx(6));
                tvNN.setLayoutParams(nnLp);
                inner.addView(tvNN);
            }

            // Gợi ý
            if (goiY != null && !goiY.isEmpty()) {
                TextView tvGY = new TextView(getContext());
                tvGY.setText("Đề xuất: " + goiY);
                tvGY.setTextSize(13);
                tvGY.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.primary));
                tvGY.setLineSpacing(0, 1.2f);
                tvGY.setTypeface(null, android.graphics.Typeface.BOLD);
                LinearLayout.LayoutParams gyLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                gyLp.setMargins(0, 0, 0, dpToPx(10));
                tvGY.setLayoutParams(gyLp);
                inner.addView(tvGY);
            }

            // Divider
            View div = new View(getContext());
            LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            divLp.setMargins(0, 0, 0, dpToPx(8));
            div.setLayoutParams(divLp);
            div.setBackgroundColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.divider));
            inner.addView(div);

            // Phản hồi gốc (chỉ hiện 2 cái)
            if (danhGia != null && !danhGia.isEmpty()) {
                int show = Math.min(danhGia.size(), 2);
                for (int i = 0; i < show; i++) {
                    TextView tvItem = new TextView(getContext());
                    tvItem.setText("• " + danhGia.get(i));
                    tvItem.setTextSize(12);
                    tvItem.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.text_primary));
                    tvItem.setLineSpacing(0, 1.3f);
                    LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    itemLp.setMargins(0, 0, 0, dpToPx(4));
                    tvItem.setLayoutParams(itemLp);
                    inner.addView(tvItem);
                }
            }

            card.addView(inner);
            llCumDeContainer.addView(card);
        }
    }

    // === HELPERS ===
    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    
    private int getResColor(int attrResId) {
        TypedValue typedValue = new TypedValue();
        if (getContext() != null && getContext().getTheme().resolveAttribute(attrResId, typedValue, true)) {
            return typedValue.data;
        }
        return Color.GRAY;
    }
}