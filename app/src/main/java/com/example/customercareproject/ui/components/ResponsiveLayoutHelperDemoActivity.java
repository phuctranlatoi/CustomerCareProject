package com.example.customercareproject.ui.components;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.utils.ResponsiveLayoutHelper;
import com.google.android.material.card.MaterialCardView;

/**
 * Demo activity showcasing ResponsiveLayoutHelper utility methods.
 * Demonstrates screen size detection, responsive spacing, and adaptive layouts.
 */
public class ResponsiveLayoutHelperDemoActivity extends AppCompatActivity {
    
    private TextView tvScreenSize;
    private TextView tvScreenWidth;
    private TextView tvScreenHeight;
    private TextView tvIsTablet;
    private TextView tvIsLandscape;
    private TextView tvResponsiveSpacing;
    private TextView tvResponsiveMargin;
    private TextView tvGridColumns;
    private TextView tvTwoPaneLayout;
    
    private LinearLayout infoContainer;
    private MaterialCardView demoCard;
    private RecyclerView demoRecyclerView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responsive_layout_helper_demo);
        
        initializeViews();
        displayResponsiveInfo();
        applyResponsiveLayout();
    }
    
    /**
     * Initialize all views.
     */
    private void initializeViews() {
        tvScreenSize = findViewById(R.id.tvScreenSize);
        tvScreenWidth = findViewById(R.id.tvScreenWidth);
        tvScreenHeight = findViewById(R.id.tvScreenHeight);
        tvIsTablet = findViewById(R.id.tvIsTablet);
        tvIsLandscape = findViewById(R.id.tvIsLandscape);
        tvResponsiveSpacing = findViewById(R.id.tvResponsiveSpacing);
        tvResponsiveMargin = findViewById(R.id.tvResponsiveMargin);
        tvGridColumns = findViewById(R.id.tvGridColumns);
        tvTwoPaneLayout = findViewById(R.id.tvTwoPaneLayout);
        
        infoContainer = findViewById(R.id.infoContainer);
        demoCard = findViewById(R.id.demoCard);
        demoRecyclerView = findViewById(R.id.demoRecyclerView);
    }
    
    /**
     * Display responsive layout information.
     */
    private void displayResponsiveInfo() {
        // Screen size
        ResponsiveLayoutHelper.ScreenSize screenSize = ResponsiveLayoutHelper.getScreenSize(this);
        tvScreenSize.setText("Screen Size: " + screenSize.name());
        
        // Screen dimensions
        Configuration config = getResources().getConfiguration();
        tvScreenWidth.setText("Screen Width: " + config.screenWidthDp + "dp");
        tvScreenHeight.setText("Screen Height: " + ResponsiveLayoutHelper.getScreenHeightDp(this) + "dp");
        
        // Device type
        boolean isTablet = ResponsiveLayoutHelper.isTablet(this);
        tvIsTablet.setText("Is Tablet: " + (isTablet ? "Yes" : "No"));
        
        // Orientation
        boolean isLandscape = ResponsiveLayoutHelper.isLandscape(this);
        tvIsLandscape.setText("Is Landscape: " + (isLandscape ? "Yes" : "No"));
        
        // Responsive spacing
        int spacingPx = ResponsiveLayoutHelper.getResponsiveSpacing(this);
        int spacingDp = ResponsiveLayoutHelper.pxToDp(this, spacingPx);
        tvResponsiveSpacing.setText("Responsive Spacing: " + spacingDp + "dp (" + spacingPx + "px)");
        
        // Responsive margin
        int marginPx = ResponsiveLayoutHelper.getResponsiveMargin(this);
        int marginDp = ResponsiveLayoutHelper.pxToDp(this, marginPx);
        tvResponsiveMargin.setText("Responsive Margin: " + marginDp + "dp (" + marginPx + "px)");
        
        // Grid columns
        int gridColumns = ResponsiveLayoutHelper.getGridColumnCount(this);
        tvGridColumns.setText("Grid Columns: " + gridColumns);
        
        // Two-pane layout
        boolean shouldUseTwoPane = ResponsiveLayoutHelper.shouldUseTwoPaneLayout(this);
        tvTwoPaneLayout.setText("Use Two-Pane Layout: " + (shouldUseTwoPane ? "Yes" : "No"));
    }
    
    /**
     * Apply responsive layout adjustments.
     */
    private void applyResponsiveLayout() {
        // Apply responsive padding to container
        int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
        infoContainer.setPadding(spacing, spacing, spacing, spacing);
        
        // Apply responsive margin to demo card
        int margin = ResponsiveLayoutHelper.getResponsiveMargin(this);
        if (demoCard != null) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) demoCard.getLayoutParams();
            params.setMargins(margin, margin, margin, margin);
            demoCard.setLayoutParams(params);
        }
        
        // Setup RecyclerView with responsive grid
        if (demoRecyclerView != null) {
            int columnCount = ResponsiveLayoutHelper.getGridColumnCount(this);
            GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
            demoRecyclerView.setLayoutManager(layoutManager);
        }
    }
    
    /**
     * Handle configuration changes (orientation, screen size).
     */
    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        // Update displayed information
        displayResponsiveInfo();
        
        // Reapply responsive layout
        applyResponsiveLayout();
    }
}
