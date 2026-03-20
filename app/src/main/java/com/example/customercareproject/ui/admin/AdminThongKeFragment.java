package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.DanhGia;
import com.example.customercareproject.model.NguoiDung;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.utils.NlpHelper;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AdminThongKeFragment extends Fragment {

    private boolean isThang = false;
    private Button btnTuan, btnThang;

    private TextView tvTongDanhGia, tvTongTicket, tvTongDanhGiaKtv, tvDiemTBKtv;
    private TextView tvTicketChoXuLy, tvTicketDangXuLy, tvTicketDaXuLy;
    private TextView tvHaiLong, tvTrungBinh, tvKhongHaiLong;
    private ProgressBar pbHaiLong, pbTrungBinh, pbKhongHaiLong;
    private RecyclerView rvThongKeSanPham, rvThongKeKtv;
    private ChipGroup chipGroupTags;

    // Gemini insight
    private TextView tvAiInsight;
    private Button btnTaoInsight;
    private List<String> cachedFeedbacks = new ArrayList<>();

    private static final String GEMINI_API_KEY = "AIzaSyBvPzRyR5yuB9FonQL_7cAb6DCjZAlZNgY";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_thong_ke, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnTuan = view.findViewById(R.id.btnTuan);
        btnThang = view.findViewById(R.id.btnThang);

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
        rvThongKeKtv = view.findViewById(R.id.rvThongKeKtv);
        rvThongKeKtv.setLayoutManager(new LinearLayoutManager(getContext()));

        chipGroupTags = view.findViewById(R.id.chipGroupTags);

        tvAiInsight  = view.findViewById(R.id.tvAiInsight);
        btnTaoInsight = view.findViewById(R.id.btnTaoInsight);
        btnTaoInsight.setOnClickListener(v -> taoGeminiInsight());

        btnTuan.setOnClickListener(v -> setFilter(false));
        btnThang.setOnClickListener(v -> setFilter(true));

        taiThongKe();
    }

    private void setFilter(boolean thang) {
        isThang = thang;
        btnTuan.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        isThang ? Color.parseColor("#F5F5F5") : Color.parseColor("#1565C0")));
        btnTuan.setTextColor(isThang ? Color.parseColor("#757575") : Color.WHITE);
        btnThang.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        isThang ? Color.parseColor("#1565C0") : Color.parseColor("#F5F5F5")));
        btnThang.setTextColor(isThang ? Color.WHITE : Color.parseColor("#757575"));
        taiThongKe();
    }

    private void taiThongKe() {
        cachedFeedbacks.clear();
        Calendar cal = Calendar.getInstance();
        if (!isThang) cal.add(Calendar.DAY_OF_YEAR, -7);
        else cal.add(Calendar.MONTH, -1);
        Timestamp tuNgay = new Timestamp(cal.getTime());

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // --- Thống kê đánh giá sản phẩm ---
        db.collection("DanhGia").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;
                    tvTongDanhGia.setText(String.valueOf(snap.size()));

                    int haiLong = 0, trungBinh = 0, khongHaiLong = 0;
                    Map<String, Integer> tagCount = new HashMap<>();
                    Map<String, Integer> spCount = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        DanhGia dg = doc.toObject(DanhGia.class);
                        if ("HaiLong".equals(dg.getCamXuc())) haiLong++;
                        else if ("TrungBinh".equals(dg.getCamXuc())) trungBinh++;
                        else khongHaiLong++;

                        if (dg.getTags() != null) {
                            for (String tag : dg.getTags()) {
                                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                            }
                        }
                        String sp = dg.getSanPham();
                        if (sp != null) spCount.put(sp, spCount.getOrDefault(sp, 0) + 1);

                        // Cache nội dung để Gemini phân tích
                        if (dg.getNoiDung() != null && !dg.getNoiDung().isEmpty()) {
                            cachedFeedbacks.add("[" + (dg.getSanPham() != null ? dg.getSanPham() : "") + "] "
                                    + dg.getSoSao() + "★ - " + dg.getNoiDung());
                        }
                    }

                    int total = snap.size() == 0 ? 1 : snap.size();
                    int pHL = haiLong * 100 / total;
                    int pTB = trungBinh * 100 / total;
                    int pKHL = khongHaiLong * 100 / total;

                    tvHaiLong.setText(pHL + "%");
                    tvTrungBinh.setText(pTB + "%");
                    tvKhongHaiLong.setText(pKHL + "%");
                    pbHaiLong.setProgress(pHL);
                    pbTrungBinh.setProgress(pTB);
                    pbKhongHaiLong.setProgress(pKHL);

                    // Top tags dạng chip
                    chipGroupTags.removeAllViews();
                    tagCount.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(6)
                            .forEach(e -> {
                                Chip chip = new Chip(getContext());
                                chip.setText(e.getKey() + " (" + e.getValue() + ")");
                                chip.setClickable(false);
                                chip.setChipBackgroundColorResource(R.color.chip_bg);
                                chip.setTextColor(Color.parseColor("#1565C0"));
                                chipGroupTags.addView(chip);
                            });

                    // Thống kê theo sản phẩm
                    int maxSp = spCount.values().stream().mapToInt(Integer::intValue).max().orElse(1);
                    List<ThongKeSanPhamAdapter.Item> spList = new ArrayList<>();
                    for (String sp : SanPham.DANH_SACH) {
                        spList.add(new ThongKeSanPhamAdapter.Item(sp, spCount.getOrDefault(sp, 0), maxSp));
                    }
                    rvThongKeSanPham.setAdapter(new ThongKeSanPhamAdapter(spList));
                });

        // --- Thống kê ticket ---
        db.collection("YeuCauHoTro").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> {
                    if (getContext() == null) return;
                    tvTongTicket.setText(String.valueOf(snap.size()));
                    int cho = 0, dang = 0, da = 0;
                    for (QueryDocumentSnapshot doc : snap) {
                        String ts = doc.getString("trangThai");
                        if ("ChoXuLy".equals(ts)) cho++;
                        else if ("DangXuLy".equals(ts)) dang++;
                        else da++;
                    }
                    tvTicketChoXuLy.setText(String.valueOf(cho));
                    tvTicketDangXuLy.setText(String.valueOf(dang));
                    tvTicketDaXuLy.setText(String.valueOf(da));
                });

        // --- Thống kê đánh giá KTV ---
        db.collection("DanhGiaKTV").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snapDG -> {
                    if (getContext() == null) return;
                    tvTongDanhGiaKtv.setText(String.valueOf(snapDG.size()));

                    // Tính điểm TB toàn bộ KTV
                    double tongDiem = 0;
                    Map<String, List<Double>> ktvDiemMap = new HashMap<>();
                    for (QueryDocumentSnapshot doc : snapDG) {
                        Object s = doc.get("soSao");
                        String ktvUid = doc.getString("ktvUid");
                        if (s instanceof Number && ktvUid != null) {
                            double d = ((Number) s).doubleValue();
                            tongDiem += d;
                            ktvDiemMap.computeIfAbsent(ktvUid, k -> new ArrayList<>()).add(d);
                        }
                    }
                    double diemTB = snapDG.size() > 0 ? tongDiem / snapDG.size() : 0;
                    tvDiemTBKtv.setText(String.format("%.1f★", diemTB));

                    // Load danh sách KTV
                    db.collection("NguoiDung").whereEqualTo("vaiTro", NguoiDung.VAI_TRO_KTV).get()
                            .addOnSuccessListener(snapKtv -> {
                                if (getContext() == null) return;
                                List<ThongKeKtvAdapter.KtvItem> ktvList = new ArrayList<>();
                                for (QueryDocumentSnapshot doc : snapKtv) {
                                    NguoiDung ktv = doc.toObject(NguoiDung.class);
                                    String uid = ktv.getUid() != null ? ktv.getUid() : doc.getId();
                                    String ten = ktv.getHoTen() != null ? ktv.getHoTen() : "KTV";

                                    // Tính điểm từ DanhGiaKTV trong kỳ
                                    List<Double> diems = ktvDiemMap.get(uid);
                                    double diemKtv = 0;
                                    int soLuot = 0;
                                    if (diems != null && !diems.isEmpty()) {
                                        soLuot = diems.size();
                                        diemKtv = diems.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                                    } else {
                                        // Fallback: dùng điểm tổng từ NguoiDung
                                        Object d = doc.get("diemDanhGia");
                                        Object sl = doc.get("soLuotDanhGia");
                                        if (d instanceof Number) diemKtv = ((Number) d).doubleValue();
                                        if (sl instanceof Number) soLuot = ((Number) sl).intValue();
                                    }

                                    ktvList.add(new ThongKeKtvAdapter.KtvItem(
                                            ten,
                                            ktv.getTrangThai(),
                                            ktv.getSoTicketDangXuLy(),
                                            ktv.getTongTicketDaXuLy(),
                                            diemKtv,
                                            soLuot
                                    ));
                                }
                                // Sắp xếp theo điểm giảm dần
                                ktvList.sort((a, b) -> Double.compare(b.diemDanhGia, a.diemDanhGia));
                                rvThongKeKtv.setAdapter(new ThongKeKtvAdapter(ktvList));
                            });
                });
    }

    private void taoGeminiInsight() {
        if (cachedFeedbacks.isEmpty()) {
            tvAiInsight.setText("Chưa có dữ liệu đánh giá trong kỳ này.");
            return;
        }

        btnTaoInsight.setEnabled(false);
        tvAiInsight.setText("⏳ Gemini đang phân tích...");

        List<String> sample = cachedFeedbacks.size() > 20
                ? cachedFeedbacks.subList(0, 20) : cachedFeedbacks;
        String danhSachFeedback = String.join("\n", sample);

        String prompt = "Bạn là chuyên gia phân tích trải nghiệm khách hàng phần mềm.\n"
                + "Dưới đây là " + sample.size() + " đánh giá từ khách hàng:\n\n"
                + danhSachFeedback + "\n\n"
                + "Hãy tổng hợp và trả lời bằng tiếng Việt với format:\n"
                + "1. Điểm mạnh nổi bật (1-2 câu)\n"
                + "2. Vấn đề cần cải thiện (1-2 câu)\n"
                + "3. Đề xuất ưu tiên hàng đầu (1 câu)\n"
                + "Ngắn gọn, súc tích, không quá 100 từ.";

        try {
            JSONObject part = new JSONObject();
            part.put("text", prompt);
            JSONArray parts = new JSONArray();
            parts.put(part);
            JSONObject content = new JSONObject();
            content.put("parts", parts);
            JSONArray contents = new JSONArray();
            contents.put(content);
            JSONObject body = new JSONObject();
            body.put("contents", contents);

            Request request = new Request.Builder()
                    .url(GEMINI_URL)
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .build();

            HTTP.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        tvAiInsight.setText("Không thể kết nối Gemini: " + e.getMessage());
                        btnTaoInsight.setEnabled(true);
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String respBody = response.body().string();
                        JSONObject json = new JSONObject(respBody);
                        String text = json.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            tvAiInsight.setText(text.trim());
                            btnTaoInsight.setEnabled(true);
                        });
                    } catch (Exception e) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            tvAiInsight.setText("Lỗi phân tích: " + e.getMessage());
                            btnTaoInsight.setEnabled(true);
                        });
                    }
                }
            });
        } catch (Exception e) {
            tvAiInsight.setText("Lỗi: " + e.getMessage());
            btnTaoInsight.setEnabled(true);
        }
    }
}
