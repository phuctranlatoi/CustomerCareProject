package com.example.customercareproject.ui.components;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;

/**
 * Demo activity showcasing SkeletonView component usage.
 * 
 * This activity demonstrates:
 * - Rectangle skeleton shapes for text placeholders
 * - Circle skeleton shapes for avatar placeholders
 * - Shimmer animation effect
 * - Loading state simulation
 * - Programmatic control of skeleton views
 */
public class SkeletonViewDemoActivity extends AppCompatActivity {
    
    private LinearLayout skeletonContainer;
    private LinearLayout contentContainer;
    private Button btnSimulateLoading;
    private Button btnToggleShimmer;
    
    private SkeletonView skeletonAvatar;
    private SkeletonView skeletonTitle;
    private SkeletonView skeletonSubtitle;
    private SkeletonView skeletonDescription;
    
    private boolean isLoading = true;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skeleton_view_demo);
        
        // Setup toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("SkeletonView Demo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        initViews();
        setupListeners();
        
        // Start with loading state
        showLoadingState();
    }
    
    /**
     * Initialize views.
     */
    private void initViews() {
        skeletonContainer = findViewById(R.id.skeletonContainer);
        contentContainer = findViewById(R.id.contentContainer);
        btnSimulateLoading = findViewById(R.id.btnSimulateLoading);
        btnToggleShimmer = findViewById(R.id.btnToggleShimmer);
        
        skeletonAvatar = findViewById(R.id.skeletonAvatar);
        skeletonTitle = findViewById(R.id.skeletonTitle);
        skeletonSubtitle = findViewById(R.id.skeletonSubtitle);
        skeletonDescription = findViewById(R.id.skeletonDescription);
    }
    
    /**
     * Setup button listeners.
     */
    private void setupListeners() {
        btnSimulateLoading.setOnClickListener(v -> {
            if (isLoading) {
                // Simulate data loaded
                showContentState();
            } else {
                // Simulate loading
                showLoadingState();
            }
        });
        
        btnToggleShimmer.setOnClickListener(v -> {
            if (skeletonAvatar.isShimmerRunning()) {
                // Stop shimmer
                skeletonAvatar.stopShimmer();
                skeletonTitle.stopShimmer();
                skeletonSubtitle.stopShimmer();
                skeletonDescription.stopShimmer();
                btnToggleShimmer.setText("Start Shimmer");
            } else {
                // Start shimmer
                skeletonAvatar.startShimmer();
                skeletonTitle.startShimmer();
                skeletonSubtitle.startShimmer();
                skeletonDescription.startShimmer();
                btnToggleShimmer.setText("Stop Shimmer");
            }
        });
    }
    
    /**
     * Show loading state with skeleton views.
     */
    private void showLoadingState() {
        isLoading = true;
        skeletonContainer.setVisibility(View.VISIBLE);
        contentContainer.setVisibility(View.GONE);
        btnSimulateLoading.setText("Show Content");
        btnToggleShimmer.setEnabled(true);
        
        // Simulate loading delay (2 seconds)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isLoading) {
                showContentState();
            }
        }, 2000);
    }
    
    /**
     * Show content state (hide skeleton views).
     */
    private void showContentState() {
        isLoading = false;
        skeletonContainer.setVisibility(View.GONE);
        contentContainer.setVisibility(View.VISIBLE);
        btnSimulateLoading.setText("Show Loading");
        btnToggleShimmer.setEnabled(false);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
