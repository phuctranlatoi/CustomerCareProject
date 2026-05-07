package com.example.customercareproject.ui.danhgia;

import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.example.customercareproject.utils.AnimationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DanhGiaActivity extends AppCompatActivity {

    private String sanPham;
    private String loaiGoi; // "ChinhThuc" | "DungThu" | null
    
    private MaterialCardView cardTabHeader;
    private ImageView imgTabIcon;
    private TextView tvTabTitle;
    private TextView tvTabDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_gia);

        sanPham = getIntent().getStringExtra("sanPham");
        loaiGoi = getIntent().getStringExtra("loaiGoi"); // có thể null

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(sanPham != null ? sanPham : "Đánh giá");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize header views
        cardTabHeader = findViewById(R.id.cardTabHeader);
        imgTabIcon = findViewById(R.id.imgTabIcon);
        tvTabTitle = findViewById(R.id.tvTabTitle);
        tvTabDescription = findViewById(R.id.tvTabDescription);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        viewPager.setAdapter(new DanhGiaPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Giao Diện"); break;
                case 1: tab.setText("Chức Năng"); break;
                case 2: tab.setText("Lỗi Phát Sinh"); break;
            }
        }).attach();
        
        // Update header when tab changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateTabHeader(position);
            }
        });
        
        // Set initial header
        updateTabHeader(0);
    }
    
    private void updateTabHeader(int position) {
        // Apply fade animation
        AnimationHelper.fadeOut(cardTabHeader);
        
        cardTabHeader.postDelayed(() -> {
            switch (position) {
                case 0: // Giao diện
                    imgTabIcon.setImageResource(R.drawable.ic_palette);
                    tvTabTitle.setText("Đánh giá Giao diện");
                    tvTabDescription.setText("Đánh giá về thiết kế, màu sắc và trải nghiệm người dùng");
                    break;
                    
                case 1: // Chức năng
                    imgTabIcon.setImageResource(R.drawable.ic_settings);
                    tvTabTitle.setText("Đánh giá Chức năng");
                    tvTabDescription.setText("Đánh giá về tính năng, hiệu suất và độ ổn định");
                    break;
                    
                case 2: // Lỗi phát sinh
                    imgTabIcon.setImageResource(R.drawable.ic_bug_report);
                    tvTabTitle.setText("Lỗi Phát sinh");
                    tvTabDescription.setText("Xem và quản lý các lỗi đã báo cáo");
                    break;
            }
            
            AnimationHelper.fadeIn(cardTabHeader);
        }, AnimationHelper.DURATION_FAST);
    }

    class DanhGiaPagerAdapter extends FragmentStateAdapter {
        DanhGiaPagerAdapter(FragmentActivity fa) { super(fa); }

        @Override
        public Fragment createFragment(int position) {
            if (position == 2) {
                return LoiPhatSinhFragment.newInstance(sanPham);
            }
            String loai = (position == 0) ? "GiaoDien" : "ChucNang";
            return DanhGiaFormFragment.newInstance(sanPham, loai, loaiGoi);
        }

        @Override
        public int getItemCount() { return 3; }
    }
}
