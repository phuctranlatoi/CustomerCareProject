package com.example.customercareproject.ui.components;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;

/**
 * Demo activity showcasing Material3Button functionality.
 * This activity demonstrates:
 * - Standard button with press animation
 * - Loading state toggle
 * - Different button styles (outlined, text)
 * - Disabled state
 * - Full-width button
 * - Button with icon
 */
public class Material3ButtonDemoActivity extends AppCompatActivity {
    
    private Material3Button btnStandard;
    private Material3Button btnLoading;
    private Material3Button btnOutlined;
    private Material3Button btnText;
    private Material3Button btnDisabled;
    private Material3Button btnFullWidth;
    private Material3Button btnWithIcon;
    
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.example_material3_button);
        
        initializeViews();
        setupClickListeners();
    }
    
    private void initializeViews() {
        btnStandard = findViewById(R.id.btnStandard);
        btnLoading = findViewById(R.id.btnLoading);
        btnOutlined = findViewById(R.id.btnOutlined);
        btnText = findViewById(R.id.btnText);
        btnDisabled = findViewById(R.id.btnDisabled);
        btnFullWidth = findViewById(R.id.btnFullWidth);
        btnWithIcon = findViewById(R.id.btnWithIcon);
    }
    
    private void setupClickListeners() {
        // Standard button - shows toast
        btnStandard.setOnClickListener(v -> {
            Toast.makeText(this, "Standard button clicked!", Toast.LENGTH_SHORT).show();
        });
        
        // Loading button - toggles loading state
        btnLoading.setOnClickListener(v -> {
            // This won't be called when loading is true
            btnLoading.setLoading(true);
            
            // Simulate async operation (e.g., network request)
            handler.postDelayed(() -> {
                btnLoading.setLoading(false);
                Toast.makeText(this, "Loading complete!", Toast.LENGTH_SHORT).show();
            }, 3000); // 3 seconds
        });
        
        // Outlined button - shows toast
        btnOutlined.setOnClickListener(v -> {
            Toast.makeText(this, "Outlined button clicked!", Toast.LENGTH_SHORT).show();
        });
        
        // Text button - shows toast
        btnText.setOnClickListener(v -> {
            Toast.makeText(this, "Text button clicked!", Toast.LENGTH_SHORT).show();
        });
        
        // Disabled button - this won't be called since button is disabled
        btnDisabled.setOnClickListener(v -> {
            Toast.makeText(this, "This shouldn't appear!", Toast.LENGTH_SHORT).show();
        });
        
        // Full-width button - demonstrates loading state
        btnFullWidth.setOnClickListener(v -> {
            btnFullWidth.setLoading(true);
            
            handler.postDelayed(() -> {
                btnFullWidth.setLoading(false);
                Toast.makeText(this, "Full-width button action complete!", Toast.LENGTH_SHORT).show();
            }, 2000);
        });
        
        // Button with icon - shows toast
        btnWithIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Button with icon clicked!", Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handler callbacks
        handler.removeCallbacksAndMessages(null);
    }
}
