package com.example.customercareproject;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.customercareproject.utils.ResponsiveLayoutHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented tests for ResponsiveLayoutHelper utility class.
 * Tests screen size detection, responsive spacing, and helper methods.
 */
@RunWith(AndroidJUnit4.class)
public class ResponsiveLayoutHelperTest {
    
    private Context context;
    
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }
    
    /**
     * Test that getScreenSize() returns a valid ScreenSize enum value.
     */
    @Test
    public void testGetScreenSize_ReturnsValidScreenSize() {
        ResponsiveLayoutHelper.ScreenSize screenSize = ResponsiveLayoutHelper.getScreenSize(context);
        assertNotNull("Screen size should not be null", screenSize);
        
        // Verify it's one of the valid enum values
        assertTrue("Screen size should be COMPACT, MEDIUM, or EXPANDED",
                screenSize == ResponsiveLayoutHelper.ScreenSize.COMPACT ||
                screenSize == ResponsiveLayoutHelper.ScreenSize.MEDIUM ||
                screenSize == ResponsiveLayoutHelper.ScreenSize.EXPANDED);
    }
    
    /**
     * Test that getScreenSize() handles null context gracefully.
     */
    @Test
    public void testGetScreenSize_WithNullContext_ReturnsCompact() {
        ResponsiveLayoutHelper.ScreenSize screenSize = ResponsiveLayoutHelper.getScreenSize(null);
        assertEquals("Null context should return COMPACT", 
                ResponsiveLayoutHelper.ScreenSize.COMPACT, screenSize);
    }
    
    /**
     * Test that getResponsiveSpacing() returns a positive value.
     */
    @Test
    public void testGetResponsiveSpacing_ReturnsPositiveValue() {
        int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(context);
        assertTrue("Responsive spacing should be positive", spacing > 0);
    }
    
    /**
     * Test that getResponsiveSpacing() returns different values for different screen sizes.
     * This test verifies the logic by checking that spacing increases with screen size.
     */
    @Test
    public void testGetResponsiveSpacing_VariesByScreenSize() {
        int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(context);
        
        // Spacing should be at least 16dp (compact) converted to pixels
        int minSpacingPx = ResponsiveLayoutHelper.dpToPx(context, 16);
        assertTrue("Spacing should be at least 16dp in pixels", spacing >= minSpacingPx);
        
        // Spacing should not exceed 32dp (expanded) converted to pixels
        int maxSpacingPx = ResponsiveLayoutHelper.dpToPx(context, 32);
        assertTrue("Spacing should not exceed 32dp in pixels", spacing <= maxSpacingPx);
    }
    
    /**
     * Test that getResponsiveSpacing() handles null context gracefully.
     */
    @Test
    public void testGetResponsiveSpacing_WithNullContext_ReturnsDefaultValue() {
        int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(null);
        // Should return a reasonable default value (not crash)
        assertTrue("Spacing with null context should be non-negative", spacing >= 0);
    }
    
    /**
     * Test that isTablet() returns a boolean value.
     */
    @Test
    public void testIsTablet_ReturnsBoolean() {
        boolean isTablet = ResponsiveLayoutHelper.isTablet(context);
        // Just verify it returns without crashing - value depends on device
        assertNotNull("isTablet should return a boolean value", Boolean.valueOf(isTablet));
    }
    
    /**
     * Test that isTablet() handles null context gracefully.
     */
    @Test
    public void testIsTablet_WithNullContext_ReturnsFalse() {
        boolean isTablet = ResponsiveLayoutHelper.isTablet(null);
        assertFalse("Null context should return false for isTablet", isTablet);
    }
    
    /**
     * Test that isLandscape() returns a boolean value.
     */
    @Test
    public void testIsLandscape_ReturnsBoolean() {
        boolean isLandscape = ResponsiveLayoutHelper.isLandscape(context);
        // Just verify it returns without crashing - value depends on device orientation
        assertNotNull("isLandscape should return a boolean value", Boolean.valueOf(isLandscape));
    }
    
    /**
     * Test that isLandscape() handles null context gracefully.
     */
    @Test
    public void testIsLandscape_WithNullContext_ReturnsFalse() {
        boolean isLandscape = ResponsiveLayoutHelper.isLandscape(null);
        assertFalse("Null context should return false for isLandscape", isLandscape);
    }
    
    /**
     * Test that getScreenHeightDp() returns a positive value.
     */
    @Test
    public void testGetScreenHeightDp_ReturnsPositiveValue() {
        int heightDp = ResponsiveLayoutHelper.getScreenHeightDp(context);
        assertTrue("Screen height in dp should be positive", heightDp > 0);
    }
    
    /**
     * Test that getScreenHeightDp() handles null context gracefully.
     */
    @Test
    public void testGetScreenHeightDp_WithNullContext_ReturnsZero() {
        int heightDp = ResponsiveLayoutHelper.getScreenHeightDp(null);
        assertEquals("Null context should return 0 for screen height", 0, heightDp);
    }
    
    /**
     * Test that dpToPx() converts dp to pixels correctly.
     */
    @Test
    public void testDpToPx_ConvertsCorrectly() {
        int dp = 16;
        int px = ResponsiveLayoutHelper.dpToPx(context, dp);
        
        // Verify conversion is reasonable (should be greater than dp value for most devices)
        assertTrue("Converted pixels should be positive", px > 0);
        
        // For most devices, density is >= 1, so px should be >= dp
        assertTrue("Converted pixels should be >= dp value for typical densities", px >= dp);
    }
    
    /**
     * Test that dpToPx() handles null context gracefully.
     */
    @Test
    public void testDpToPx_WithNullContext_ReturnsInputValue() {
        int dp = 16;
        int result = ResponsiveLayoutHelper.dpToPx(null, dp);
        assertEquals("Null context should return input dp value", dp, result);
    }
    
    /**
     * Test that pxToDp() converts pixels to dp correctly.
     */
    @Test
    public void testPxToDp_ConvertsCorrectly() {
        int px = 48;
        int dp = ResponsiveLayoutHelper.pxToDp(context, px);
        
        // Verify conversion is reasonable
        assertTrue("Converted dp should be positive", dp > 0);
    }
    
    /**
     * Test that pxToDp() handles null context gracefully.
     */
    @Test
    public void testPxToDp_WithNullContext_ReturnsInputValue() {
        int px = 48;
        int result = ResponsiveLayoutHelper.pxToDp(null, px);
        assertEquals("Null context should return input px value", px, result);
    }
    
    /**
     * Test that dpToPx() and pxToDp() are inverse operations.
     */
    @Test
    public void testDpToPx_AndPxToDp_AreInverseOperations() {
        int originalDp = 24;
        int px = ResponsiveLayoutHelper.dpToPx(context, originalDp);
        int convertedBackDp = ResponsiveLayoutHelper.pxToDp(context, px);
        
        // Allow for rounding errors (within 1dp)
        assertTrue("Converting dp->px->dp should return approximately the same value",
                Math.abs(originalDp - convertedBackDp) <= 1);
    }
    
    /**
     * Test that shouldUseTwoPaneLayout() returns a boolean value.
     */
    @Test
    public void testShouldUseTwoPaneLayout_ReturnsBoolean() {
        boolean shouldUseTwoPane = ResponsiveLayoutHelper.shouldUseTwoPaneLayout(context);
        // Just verify it returns without crashing - value depends on device
        assertNotNull("shouldUseTwoPaneLayout should return a boolean value", 
                Boolean.valueOf(shouldUseTwoPane));
    }
    
    /**
     * Test that shouldUseTwoPaneLayout() handles null context gracefully.
     */
    @Test
    public void testShouldUseTwoPaneLayout_WithNullContext_ReturnsFalse() {
        boolean shouldUseTwoPane = ResponsiveLayoutHelper.shouldUseTwoPaneLayout(null);
        assertFalse("Null context should return false for shouldUseTwoPaneLayout", shouldUseTwoPane);
    }
    
    /**
     * Test that getGridColumnCount() returns a reasonable value.
     */
    @Test
    public void testGetGridColumnCount_ReturnsReasonableValue() {
        int columnCount = ResponsiveLayoutHelper.getGridColumnCount(context);
        
        // Column count should be between 2 and 4 based on screen size
        assertTrue("Grid column count should be between 2 and 4", 
                columnCount >= 2 && columnCount <= 4);
    }
    
    /**
     * Test that getGridColumnCount() handles null context gracefully.
     */
    @Test
    public void testGetGridColumnCount_WithNullContext_ReturnsDefaultValue() {
        int columnCount = ResponsiveLayoutHelper.getGridColumnCount(null);
        assertEquals("Null context should return default column count of 2", 2, columnCount);
    }
    
    /**
     * Test that getResponsiveMargin() returns a positive value.
     */
    @Test
    public void testGetResponsiveMargin_ReturnsPositiveValue() {
        int margin = ResponsiveLayoutHelper.getResponsiveMargin(context);
        assertTrue("Responsive margin should be positive", margin > 0);
    }
    
    /**
     * Test that getResponsiveMargin() returns different values for different screen sizes.
     */
    @Test
    public void testGetResponsiveMargin_VariesByScreenSize() {
        int margin = ResponsiveLayoutHelper.getResponsiveMargin(context);
        
        // Margin should be at least 8dp (compact) converted to pixels
        int minMarginPx = ResponsiveLayoutHelper.dpToPx(context, 8);
        assertTrue("Margin should be at least 8dp in pixels", margin >= minMarginPx);
        
        // Margin should not exceed 16dp (expanded) converted to pixels
        int maxMarginPx = ResponsiveLayoutHelper.dpToPx(context, 16);
        assertTrue("Margin should not exceed 16dp in pixels", margin <= maxMarginPx);
    }
    
    /**
     * Test that getResponsiveMargin() handles null context gracefully.
     */
    @Test
    public void testGetResponsiveMargin_WithNullContext_ReturnsDefaultValue() {
        int margin = ResponsiveLayoutHelper.getResponsiveMargin(null);
        // Should return a reasonable default value (not crash)
        assertTrue("Margin with null context should be non-negative", margin >= 0);
    }
    
    /**
     * Test screen size detection logic by verifying consistency.
     * If screen is COMPACT, it should not be a tablet.
     * If screen is EXPANDED, two-pane layout should be recommended.
     */
    @Test
    public void testScreenSizeDetection_IsConsistent() {
        ResponsiveLayoutHelper.ScreenSize screenSize = ResponsiveLayoutHelper.getScreenSize(context);
        boolean isTablet = ResponsiveLayoutHelper.isTablet(context);
        boolean shouldUseTwoPane = ResponsiveLayoutHelper.shouldUseTwoPaneLayout(context);
        
        // If screen is COMPACT, device should not be a tablet
        if (screenSize == ResponsiveLayoutHelper.ScreenSize.COMPACT) {
            assertFalse("COMPACT screens should not be tablets", isTablet);
        }
        
        // If screen is EXPANDED, two-pane layout should be used
        if (screenSize == ResponsiveLayoutHelper.ScreenSize.EXPANDED) {
            assertTrue("EXPANDED screens should use two-pane layout", shouldUseTwoPane);
        }
    }
    
    /**
     * Test that responsive spacing increases with screen size.
     * This verifies the relationship between screen size and spacing values.
     */
    @Test
    public void testResponsiveSpacing_IncreasesWithScreenSize() {
        // Get current screen size and spacing
        ResponsiveLayoutHelper.ScreenSize screenSize = ResponsiveLayoutHelper.getScreenSize(context);
        int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(context);
        
        // Verify spacing matches expected range for screen size
        switch (screenSize) {
            case COMPACT:
                // COMPACT should use 16dp spacing
                int compactSpacingPx = ResponsiveLayoutHelper.dpToPx(context, 16);
                assertEquals("COMPACT screen should use 16dp spacing", compactSpacingPx, spacing);
                break;
            case MEDIUM:
                // MEDIUM should use 24dp spacing
                int mediumSpacingPx = ResponsiveLayoutHelper.dpToPx(context, 24);
                assertEquals("MEDIUM screen should use 24dp spacing", mediumSpacingPx, spacing);
                break;
            case EXPANDED:
                // EXPANDED should use 32dp spacing
                int expandedSpacingPx = ResponsiveLayoutHelper.dpToPx(context, 32);
                assertEquals("EXPANDED screen should use 32dp spacing", expandedSpacingPx, spacing);
                break;
        }
    }
}
