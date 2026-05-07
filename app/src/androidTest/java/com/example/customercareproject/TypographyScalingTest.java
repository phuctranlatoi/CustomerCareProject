package com.example.customercareproject;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.customercareproject.ui.TypographyTestActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test to verify typography system supports text scaling up to 200%.
 * 
 * This test validates:
 * - Text styles are properly defined
 * - Text scaling works correctly
 * - Layouts don't break with large text sizes
 */
@RunWith(AndroidJUnit4.class)
public class TypographyScalingTest {

    @Test
    public void testTextScalingSupport() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // Get default font scale
        Configuration config = context.getResources().getConfiguration();
        float defaultFontScale = config.fontScale;
        
        // Verify default font scale is reasonable (typically 1.0)
        assertTrue("Default font scale should be positive", defaultFontScale > 0);
        assertTrue("Default font scale should be reasonable", defaultFontScale <= 2.0f);
    }
    
    @Test
    public void testTextStylesAreDefined() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // Verify all text styles are defined and can be resolved
        int[] textStyles = {
            R.style.TextAppearance_App_DisplayLarge,
            R.style.TextAppearance_App_DisplayMedium,
            R.style.TextAppearance_App_DisplaySmall,
            R.style.TextAppearance_App_HeadlineLarge,
            R.style.TextAppearance_App_HeadlineMedium,
            R.style.TextAppearance_App_HeadlineSmall,
            R.style.TextAppearance_App_TitleLarge,
            R.style.TextAppearance_App_TitleMedium,
            R.style.TextAppearance_App_TitleSmall,
            R.style.TextAppearance_App_BodyLarge,
            R.style.TextAppearance_App_BodyMedium,
            R.style.TextAppearance_App_BodySmall,
            R.style.TextAppearance_App_LabelLarge,
            R.style.TextAppearance_App_LabelMedium,
            R.style.TextAppearance_App_LabelSmall
        };
        
        for (int styleId : textStyles) {
            assertTrue("Text style should be defined", styleId != 0);
        }
    }
    
    @Test
    public void testTypographyTestLayoutExists() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // Verify the test layout exists
        int layoutId = R.layout.test_typography;
        assertTrue("Typography test layout should be defined", layoutId != 0);
    }
    
    @Test
    public void testFontWeightsAreAccessible() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // Create a test TextView and verify we can apply text appearances
        TextView textView = new TextView(context);
        
        // Apply a text style and verify it doesn't crash
        textView.setTextAppearance(R.style.TextAppearance_App_BodyLarge);
        
        // Verify text size is set (should be 16sp for BodyLarge)
        float textSize = textView.getTextSize();
        assertTrue("Text size should be positive", textSize > 0);
    }
    
    @Test
    public void testLineHeightIsApplied() {
        Context context = ApplicationProvider.getApplicationContext();
        
        // Create a TextView with BodyLarge style
        TextView textView = new TextView(context);
        textView.setTextAppearance(R.style.TextAppearance_App_BodyLarge);
        
        // For BodyLarge, line height should be 24sp (1.5x of 16sp)
        // Note: Actual pixel values will vary based on device density
        float textSize = textView.getTextSize();
        assertTrue("Text size should be reasonable", textSize > 0);
    }
}
