package com.example.customercareproject.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.example.customercareproject.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavView;

    // 5 tab: Tổng quan | Người dùng | Tickets | Gói DK | Phân tích
    private static final int TAB_COUNT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        android.widget.TextView tvEmail = findViewById(R.id.tvAdminEmail);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && tvEmail != null) tvEmail.setText(user.getEmail());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            com.example.customercareproject.utils.StringeeManager.getInstance().reset();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // Add UI Showcase access via long press on logout button
        findViewById(R.id.btnLogout).setOnLongClickListener(v -> {
            startActivity(new Intent(this, com.example.customercareproject.ui.UIShowcaseActivity.class));
            return true;
        });

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new AdminPagerAdapter(this));
        viewPager.setUserInputEnabled(false);

        bottomNavView = findViewById(R.id.bottomNavView);
        bottomNavView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_thong_ke) viewPager.setCurrentItem(0, false);
            else if (id == R.id.nav_users) viewPager.setCurrentItem(1, false);
            else if (id == R.id.nav_tickets) viewPager.setCurrentItem(2, false);
            else if (id == R.id.nav_goi_dk) viewPager.setCurrentItem(3, false);
            else if (id == R.id.nav_phan_tich) viewPager.setCurrentItem(4, false);
            return true;
        });

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNavView.getMenu().getItem(position).setChecked(true);
            }
        });

        // Xử lý intent từ notification
        handleNotificationIntent();
    }

    private void handleNotificationIntent() {
        Intent intent = getIntent();
        if (intent != null && "phan_tich".equals(intent.getStringExtra("open_tab"))) {
            // Mở tab phân tích khi nhấn notification đánh giá xấu
            bottomNavView.getMenu().getItem(4).setChecked(true);
            viewPager.setCurrentItem(4, false);
        }
    }

    static class AdminPagerAdapter extends FragmentStateAdapter {
        AdminPagerAdapter(FragmentActivity fa) { super(fa); }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new AdminThongKeFragment();
                case 1: return new AdminUsersFragment();
                case 2: return new AdminTicketsFragment();
                case 3: return new AdminGoiDangKyFragment();
                case 4: return new AdminPhanTichFragment();
                default: return new AdminThongKeFragment();
            }
        }

        @Override public int getItemCount() { return TAB_COUNT; }
    }
}
