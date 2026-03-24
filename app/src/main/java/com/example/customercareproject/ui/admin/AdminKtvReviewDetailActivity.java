package com.example.customercareproject.ui.admin;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminKtvReviewDetailActivity extends AppCompatActivity {

    private String ktvUid, ktvTen;
    private DanhGiaKtvAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_ktv_review_detail);

        ktvUid = getIntent().getStringExtra("ktvUid");
        ktvTen = getIntent().getStringExtra("ktvTen");

        TextView tvHeader = findViewById(R.id.tvTenKtvHeader);
        tvHeader.setText(ktvTen != null ? ktvTen : "Nhân viên");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        RecyclerView rv = findViewById(R.id.rvDanhGia);
        adapter = new DanhGiaKtvAdapter(new ArrayList<>());
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        taiDanhGia();
    }

    private void taiDanhGia() {
        if (ktvUid == null) return;

        FirebaseFirestore.getInstance().collection("DanhGiaKTV")
                .whereEqualTo("ktvUid", ktvUid)
                .get()
                .addOnSuccessListener(snap -> {
                    List<DanhGiaKtvAdapter.Item> items = new ArrayList<>();
                    int[] counts = new int[6]; // index 1-5
                    double tongDiem = 0;
                    Map<String, Integer> tagCount = new HashMap<>();

                    for (QueryDocumentSnapshot doc : snap) {
                        Object sObj = doc.get("soSao");
                        float soSao = sObj instanceof Number ? ((Number) sObj).floatValue() : 0f;
                        String nhanXet = doc.getString("nhanXet");
                        String tenKh = doc.getString("ktvTen"); // fallback
                        // Lấy tên khách hàng nếu có
                        String khUid = doc.getString("khachHangUid");

                        List<String> tags = (List<String>) doc.get("tags");

                        items.add(new DanhGiaKtvAdapter.Item(
                                "Khách hàng",  // sẽ update sau nếu cần
                                soSao,
                                nhanXet,
                                tags,
                                doc.getTimestamp("taoLuc")
                        ));

                        int idx = Math.min(5, Math.max(1, Math.round(soSao)));
                        counts[idx]++;
                        tongDiem += soSao;

                        if (tags != null) {
                            for (String tag : tags) {
                                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
                            }
                        }
                    }

                    // Sắp xếp mới nhất lên đầu
                    items.sort((a, b) -> {
                        if (a.taoLuc == null) return 1;
                        if (b.taoLuc == null) return -1;
                        return b.taoLuc.compareTo(a.taoLuc);
                    });

                    int total = snap.size();
                    double diemTB = total > 0 ? tongDiem / total : 0;

                    // Update UI
                    TextView tvDiemLon = findViewById(R.id.tvDiemLon);
                    TextView tvTongLuot = findViewById(R.id.tvTongLuot);
                    TextView tvStarsDisplay = findViewById(R.id.tvStarsDisplay);

                    tvDiemLon.setText(String.format("%.1f", diemTB));
                    tvTongLuot.setText(total + " đánh giá");

                    // Stars display
                    int filled = (int) Math.round(diemTB);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 5; i++) sb.append(i < filled ? "★" : "☆");
                    tvStarsDisplay.setText(sb.toString());

                    // Star bars
                    int[][] barIds = {
                        {R.id.bar5, 5}, {R.id.bar4, 4}, {R.id.bar3, 3},
                        {R.id.bar2, 2}, {R.id.bar1, 1}
                    };
                    for (int[] pair : barIds) {
                        View barView = findViewById(pair[0]);
                        int star = pair[1];
                        TextView tvNum = barView.findViewById(R.id.tvStarNum);
                        ProgressBar pb = barView.findViewById(R.id.pbStar);
                        TextView tvCount = barView.findViewById(R.id.tvStarCount);
                        tvNum.setText(String.valueOf(star));
                        int cnt = counts[star];
                        pb.setProgress(total > 0 ? cnt * 100 / total : 0);
                        tvCount.setText(String.valueOf(cnt));
                    }

                    // Top tags chips
                    ChipGroup chipGroup = findViewById(R.id.chipGroupTags);
                    chipGroup.removeAllViews();
                    tagCount.entrySet().stream()
                            .sorted((a, b) -> b.getValue() - a.getValue())
                            .limit(8)
                            .forEach(e -> {
                                Chip chip = new Chip(this);
                                chip.setText(e.getKey() + " · " + e.getValue());
                                chip.setClickable(false);
                                chip.setTextSize(11f);
                                chip.setChipBackgroundColor(
                                        android.content.res.ColorStateList.valueOf(0xFFEEF4FF));
                                chip.setTextColor(Color.parseColor("#1976D2"));
                                chipGroup.addView(chip);
                            });

                    adapter.capNhat(items);
                });
    }
}
