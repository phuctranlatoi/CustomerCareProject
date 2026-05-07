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
    private TextView tvAiInsight;
    private MaterialButton btnTaoInsight;
    private LinearLayout llCumDeContainer;

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
        tvAiInsight      = view.findViewById(R.id.tvAiInsight);
        btnTaoInsight    = view.findViewById(R.id.btnTaoInsight);
        llCumDeContainer = view.findViewById(R.id.llCumDeContainer);

        btnTaoInsight.setOnClickListener(v -> taoAiInsight());
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
                });

        // Load cụm đã lưu từ lần phân tích trước
        taiCumTuFirestore();
    }

    private void taiCumTuFirestore() {
        FirebaseFirestore.getInstance().collection("InsightCumDe").get()
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
        tvAiInsight.setText("🔄 AI đang phân tích và gom cụm dữ liệu...");
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

    /** Render từng cụm thành card riêng trong llCumDeContainer */
    @SuppressWarnings("unchecked")
    private void hienThiCumCards(List<Map<String, Object>> cumList) {
        if (getContext() == null) return;
        llCumDeContainer.removeAllViews();

        for (Map<String, Object> cum : cumList) {
            String chuDe     = (String) cum.getOrDefault("chuDe", "Chủ đề");
            Object soLuong   = cum.get("soLuong");
            String uuTien    = (String) cum.getOrDefault("uuTien", "TrungBinh");
            List<String> danhGia = (List<String>) cum.get("danhGia");

            // Màu theo ưu tiên
            int colorBg, colorText;
            String badgeText;
            switch (uuTien) {
                case "Cao":
                    colorBg = androidx.core.content.ContextCompat.getColor(getContext(), R.color.error_container);
                    colorText = androidx.core.content.ContextCompat.getColor(getContext(), R.color.error);
                    badgeText = "🔴 Ưu tiên cao";
                    break;
                case "Thap":
                    colorBg = androidx.core.content.ContextCompat.getColor(getContext(), R.color.success_container);
                    colorText = androidx.core.content.ContextCompat.getColor(getContext(), R.color.success);
                    badgeText = "🟢 Thấp";
                    break;
                default:
                    colorBg = androidx.core.content.ContextCompat.getColor(getContext(), R.color.warning_container);
                    colorText = androidx.core.content.ContextCompat.getColor(getContext(), R.color.warning);
                    badgeText = "🟡 Trung bình";
            }

            // Card
            CardView card = new CardView(getContext());
            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, 0, 0, dpToPx(10));
            card.setLayoutParams(cardLp);
            card.setRadius(dpToPx(14));
            card.setCardElevation(0);
            card.setCardBackgroundColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.surface));

            LinearLayout inner = new LinearLayout(getContext());
            inner.setOrientation(LinearLayout.VERTICAL);
            int p = dpToPx(16);
            inner.setPadding(p, p, p, p);

            // Header row: tên + badge
            LinearLayout header = new LinearLayout(getContext());
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(android.view.Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams headerLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            headerLp.setMargins(0, 0, 0, dpToPx(6));
            header.setLayoutParams(headerLp);

            TextView tvTen = new TextView(getContext());
            tvTen.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvTen.setText(chuDe);
            tvTen.setTextSize(14);
            tvTen.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.on_surface));
            tvTen.setTypeface(null, android.graphics.Typeface.BOLD);

            TextView tvBadge = new TextView(getContext());
            tvBadge.setText(badgeText);
            tvBadge.setTextSize(11);
            tvBadge.setTextColor(colorText);
            tvBadge.setTypeface(null, android.graphics.Typeface.BOLD);
            tvBadge.setPadding(dpToPx(8), dpToPx(3), dpToPx(8), dpToPx(3));
            android.graphics.drawable.GradientDrawable badgeBg = new android.graphics.drawable.GradientDrawable();
            badgeBg.setColor(colorBg);
            badgeBg.setCornerRadius(dpToPx(20));
            tvBadge.setBackground(badgeBg);

            header.addView(tvTen);
            header.addView(tvBadge);
            inner.addView(header);

            // Số phản hồi
            TextView tvCount = new TextView(getContext());
            tvCount.setText((soLuong != null ? soLuong : 0) + " phản hồi liên quan");
            tvCount.setTextSize(12);
            tvCount.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.text_secondary));
            LinearLayout.LayoutParams countLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            countLp.setMargins(0, 0, 0, dpToPx(10));
            tvCount.setLayoutParams(countLp);
            inner.addView(tvCount);

            // Divider
            View div = new View(getContext());
            LinearLayout.LayoutParams divLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dpToPx(1));
            divLp.setMargins(0, 0, 0, dpToPx(10));
            div.setLayoutParams(divLp);
            div.setBackgroundColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.divider));
            inner.addView(div);

            // Các câu đánh giá gốc (tối đa 3)
            if (danhGia != null && !danhGia.isEmpty()) {
                int show = Math.min(danhGia.size(), 3);
                for (int i = 0; i < show; i++) {
                    TextView tvItem = new TextView(getContext());
                    tvItem.setText("• " + danhGia.get(i));
                    tvItem.setTextSize(12);
                    tvItem.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.text_primary));
                    tvItem.setLineSpacing(0, 1.4f);
                    LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    itemLp.setMargins(0, 0, 0, dpToPx(4));
                    tvItem.setLayoutParams(itemLp);
                    inner.addView(tvItem);
                }
                if (danhGia.size() > 3) {
                    TextView tvMore = new TextView(getContext());
                    tvMore.setText("Xem thêm " + (danhGia.size() - 3) + " phản hồi...");
                    tvMore.setTextSize(12);
                    tvMore.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.primary));
                    tvMore.setClickable(true);
                    tvMore.setFocusable(true);
                    tvMore.setOnClickListener(v -> hienThiDanhGiaCum(chuDe, danhGia));
                    LinearLayout.LayoutParams moreLp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    moreLp.setMargins(0, dpToPx(4), 0, 0);
                    tvMore.setLayoutParams(moreLp);
                    inner.addView(tvMore);
                }
            }

            card.addView(inner);
            llCumDeContainer.addView(card);
        }
    }

    private void hienThiDanhGiaCum(String chuDe, List<String> danhGia) {
        if (getContext() == null) return;
        if (danhGia == null || danhGia.isEmpty()) {
            new AlertDialog.Builder(getContext())
                    .setTitle(chuDe)
                    .setMessage("Chưa có phản hồi nào trong chủ đề này.")
                    .setPositiveButton("Đóng", null).show();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < danhGia.size(); i++)
            sb.append(i + 1).append(". ").append(danhGia.get(i)).append("\n\n");
        new AlertDialog.Builder(getContext())
                .setTitle(chuDe + " (" + danhGia.size() + " phản hồi)")
                .setMessage(sb.toString().trim())
                .setPositiveButton("Đóng", null).show();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
