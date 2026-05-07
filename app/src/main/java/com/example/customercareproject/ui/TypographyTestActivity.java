package com.example.customercareproject.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.customercareproject.R;

/**
 * Test activity to verify Material Design 3 typography system.
 * This activity displays all text styles to ensure they render correctly
 * and support text scaling up to 200% for accessibility.
 */
public class TypographyTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_typography);
        
        // Set up action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Typography Test");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
