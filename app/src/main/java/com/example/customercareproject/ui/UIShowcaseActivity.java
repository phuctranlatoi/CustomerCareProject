package com.example.customercareproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.customercareproject.R;
import com.example.customercareproject.ui.components.Material3Button;

/**
 * UIShowcaseActivity demonstrates the new Material Design 3 UI improvements
 * including dark mode support, modern cards, animations, and theme consistency.
 */
public class UIShowcaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ui_showcase);

        setupUI();
    }

    private void setupUI() {
        // Find buttons and set click listeners
        Material3Button btnCreateTicket = findViewById(R.id.btnCreateTicket);
        Material3Button btnViewReport = findViewById(R.id.btnViewReport);

        if (btnCreateTicket != null) {
            btnCreateTicket.setOnClickListener(v -> {
                Toast.makeText(this, "Tạo ticket mới - UI đã được cải thiện!", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnViewReport != null) {
            btnViewReport.setOnClickListener(v -> {
                Toast.makeText(this, "Xem báo cáo - Giao diện mới!", Toast.LENGTH_SHORT).show();
            });
        }

        // Add floating action button for theme toggle
        findViewById(R.id.fabThemeToggle).setOnClickListener(this::toggleTheme);
    }

    private void toggleTheme(View view) {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(this, "Chuyển sang Light Mode", Toast.LENGTH_SHORT).show();
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(this, "Chuyển sang Dark Mode", Toast.LENGTH_SHORT).show();
        }
        
        // Recreate activity to apply theme
        recreate();
    }
}