package com.example.customercareproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Tab "Phân tích" — gom Đánh giá xấu, Lead kinh doanh, Đánh giá KTV
 * vào một màn hình dùng TabLayout nội bộ để giảm số tab bottom nav.
 */
public class AdminPhanTichFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_phan_tich, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayoutPhanTich);
        ViewPager2 viewPager = view.findViewById(R.id.viewPagerPhanTich);

        viewPager.setAdapter(new PhanTichPagerAdapter(requireActivity()));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("ĐG Xấu"); break;
                case 1: tab.setText("Analytics"); break;
                case 2: tab.setText("Lead KD"); break;
                case 3: tab.setText("Đánh giá KTV"); break;
            }
        }).attach();
    }

    static class PhanTichPagerAdapter extends FragmentStateAdapter {
        PhanTichPagerAdapter(FragmentActivity fa) { super(fa); }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0: return new AdminDanhGiaXauFragment();
                case 1: return new AdminDanhGiaAnalyticsFragment();
                case 2: return new AdminLeadFragment();
                case 3: return new AdminKtvReviewsFragment();
                default: return new AdminDanhGiaXauFragment();
            }
        }

        @Override
        public int getItemCount() { return 4; }
    }
}
