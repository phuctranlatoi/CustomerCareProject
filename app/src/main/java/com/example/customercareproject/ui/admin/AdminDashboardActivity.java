package com.example.customercareproject.ui.admin;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.customercareproject.R;
import com.example.customercareproject.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AdminDashboardActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    private LinearLayout[] tabs;
    private ImageView[] icons;
    private TextView[] labels;

    private static final int COLOR_ACTIVE   = 0xFF1976D2;
    private static final int COLOR_INACTIVE = 0xFF94A3B8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        TextView tvEmail = findViewById(R.id.tvAdminEmail);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && tvEmail != null) tvEmail.setText(user.getEmail());

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            com.example.customercareproject.utils.StringeeManager.getInstance().reset();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new AdminPagerAdapter(this));
        viewPager.setUserInputEnabled(false);

        tabs = new LinearLayout[]{
            findViewById(R.id.tabThongKe),
            findViewById(R.id.tabUsers),
            findViewById(R.id.tabTickets),
            findViewById(R.id.tabKnowledge),
            findViewById(R.id.tabReviews)
        };
        icons = new ImageView[]{
            findViewById(R.id.iconThongKe),
            findViewById(R.id.iconUsers),
            findViewById(R.id.iconTickets),
            findViewById(R.id.iconKnowledge),
            findViewById(R.id.iconReviews)
        };
        labels = new TextView[]{
            findViewById(R.id.labelThongKe),
            findViewById(R.id.labelUsers),
            findViewById(R.id.labelTickets),
            findViewById(R.id.labelKnowledge),
            findViewById(R.id.labelReviews)
        };

        for (int i = 0; i < tabs.length; i++) {
            final int idx = i;
            tabs[i].setOnClickListener(v -> selectTab(idx));
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override public void onPageSelected(int position) { selectTab(position); }
        });

        selectTab(0);
    }

    private void selectTab(int index) {
        viewPager.setCurrentItem(index, false);
        for (int i = 0; i < tabs.length; i++) {
            boolean active = (i == index);
            tabs[i].setBackground(active
                    ? ContextCompat.getDrawable(this, R.drawable.bg_nav_pill) : null);
            icons[i].setColorFilter(active ? COLOR_ACTIVE : COLOR_INACTIVE);
            labels[i].setTextColor(active ? COLOR_ACTIVE : COLOR_INACTIVE);
            labels[i].setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
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
                case 3: return new AdminKnowledgeFragment();
                case 4: return new AdminKtvReviewsFragment();
                default: return new AdminThongKeFragment();
            }
        }

        @Override public int getItemCount() { return 5; }
    }
}
