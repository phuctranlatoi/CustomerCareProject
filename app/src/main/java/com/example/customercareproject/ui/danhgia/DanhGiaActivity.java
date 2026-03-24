package com.example.customercareproject.ui.danhgia;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DanhGiaActivity extends AppCompatActivity {

    private String sanPham;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danh_gia);

        sanPham = getIntent().getStringExtra("sanPham");

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvTieuDe = findViewById(R.id.tvTieuDe);
        tvTieuDe.setText(sanPham);

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
    }

    class DanhGiaPagerAdapter extends FragmentStateAdapter {
        DanhGiaPagerAdapter(FragmentActivity fa) { super(fa); }

        @Override
        public Fragment createFragment(int position) {
            if (position == 2) {
                return LoiPhatSinhFragment.newInstance(sanPham);
            }
            String loai = (position == 0) ? "GiaoDien" : "ChucNang";
            return DanhGiaFormFragment.newInstance(sanPham, loai);
        }

        @Override
        public int getItemCount() { return 3; }
    }
}
