package com.example.customercareproject.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * ResponsiveLayoutHelper provides utility methods for detecting screen sizes and adjusting layouts
 * according to Material Design 3 responsive breakpoints.
 * 
 * Material Design 3 Breakpoints:
 * - COMPACT: < 600dp (phones in portrait)
 * - MEDIUM: 600dp - 840dp (tablets in portrait, phones in landscape)
 * - EXPANDED: > 840dp (tablets in landscape, large screens)
 * 
 * Responsive Spacing:
 * - COMPACT: 16dp
 * - MEDIUM: 24dp
 * - EXPANDED: 32dp
 */
public class ResponsiveLayoutHelper {
    
    // Material Design 3 breakpoints (in dp)
    private static final int BREAKPOINT_COMPACT = 600;
    private static final int BREAKPOINT_MEDIUM = 840;
    
    // Responsive spacing values (in dp)
    private static final int SPACING_COMPACT = 16;
    private static final int SPACING_MEDIUM = 24;
    private static final int SPACING_EXPANDED = 32;
    
    // Tablet minimum screen size (in dp)
    private static final int TABLET_MIN_DP = 600;
    
    /**
     * Screen size categories based on Material Design 3 breakpoints.
     */
    public enum ScreenSize {
        /**
         * Compact screens: < 600dp width (phones in portrait)
         */
        COMPACT,
        
        /**
         * Medium screens: 600dp - 840dp width (tablets in portrait, phones in landscape)
         */
        MEDIUM,
        
        /**
         * Expanded screens: > 840dp width (tablets in landscape, large screens)
         */
        EXPANDED
    }
    
    /**
     * Get the current screen size category based on Material Design 3 breakpoints.
     * 
     * @param context The context to get screen dimensions from
     * @return The screen size category (COMPACT, MEDIUM, or EXPANDED)
     */
    public static ScreenSize getScreenSize(Context context) {
        if (context == null) {
            return ScreenSize.COMPACT;
        }
        
        int widthDp = getScreenWidthDp(context);
        
        if (widthDp < BREAKPOINT_COMPACT) {
            return ScreenSize.COMPACT;
        } else if (widthDp < BREAKPOINT_MEDIUM) {
            return ScreenSize.MEDIUM;
        } else {
            return ScreenSize.EXPANDED;
        }
    }
    
    /**
     * Get responsive spacing value based on current screen size.
     * Returns spacing in pixels (px) for use in layouts.
     * 
     * Spacing values:
     * - COMPACT: 16dp
     * - MEDIUM: 24dp
     * - EXPANDED: 32dp
     * 
     * @param context The context to get screen dimensions from
     * @return Spacing value in pixels
     */
    public static int getResponsiveSpacing(Context context) {
        if (context == null) {
            return dpToPx(context, SPACING_COMPACT);
        }
        
        ScreenSize screenSize = getScreenSize(context);
        int spacingDp;
        
        switch (screenSize) {
            case MEDIUM:
                spacingDp = SPACING_MEDIUM;
                break;
            case EXPANDED:
                spacingDp = SPACING_EXPANDED;
                break;
            case COMPACT:
            default:
                spacingDp = SPACING_COMPACT;
                break;
        }
        
        return dpToPx(context, spacingDp);
    }
    
    /**
     * Check if the current device is a tablet.
     * A device is considered a tablet if its smallest width is >= 600dp.
     * 
     * @param context The context to get screen dimensions from
     * @return true if device is a tablet, false otherwise
     */
    public static boolean isTablet(Context context) {
        if (context == null) {
            return false;
        }
        
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.smallestScreenWidthDp >= TABLET_MIN_DP;
    }
    
    /**
     * Check if the device is currently in landscape orientation.
     * 
     * @param context The context to get orientation from
     * @return true if device is in landscape, false if portrait
     */
    public static boolean isLandscape(Context context) {
        if (context == null) {
            return false;
        }
        
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    /**
     * Get the screen width in dp (density-independent pixels).
     * 
     * @param context The context to get screen dimensions from
     * @return Screen width in dp
     */
    private static int getScreenWidthDp(Context context) {
        if (context == null) {
            return 0;
        }
        
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.screenWidthDp;
    }
    
    /**
     * Get the screen height in dp (density-independent pixels).
     * 
     * @param context The context to get screen dimensions from
     * @return Screen height in dp
     */
    public static int getScreenHeightDp(Context context) {
        if (context == null) {
            return 0;
        }
        
        Configuration configuration = context.getResources().getConfiguration();
        return configuration.screenHeightDp;
    }
    
    /**
     * Convert dp to pixels.
     * 
     * @param context The context to get display metrics from
     * @param dp The value in dp to convert
     * @return The value in pixels
     */
    public static int dpToPx(Context context, int dp) {
        if (context == null) {
            return dp;
        }
        
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }
    
    /**
     * Convert pixels to dp.
     * 
     * @param context The context to get display metrics from
     * @param px The value in pixels to convert
     * @return The value in dp
     */
    public static int pxToDp(Context context, int px) {
        if (context == null) {
            return px;
        }
        
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return Math.round(px / metrics.density);
    }
    
    /**
     * Check if the device should use a two-pane layout.
     * Two-pane layouts are recommended for MEDIUM and EXPANDED screens in landscape,
     * or EXPANDED screens in any orientation.
     * 
     * @param context The context to get screen dimensions from
     * @return true if two-pane layout should be used, false otherwise
     */
    public static boolean shouldUseTwoPaneLayout(Context context) {
        if (context == null) {
            return false;
        }
        
        ScreenSize screenSize = getScreenSize(context);
        boolean landscape = isLandscape(context);
        
        // Use two-pane for EXPANDED screens in any orientation
        if (screenSize == ScreenSize.EXPANDED) {
            return true;
        }
        
        // Use two-pane for MEDIUM screens in landscape
        if (screenSize == ScreenSize.MEDIUM && landscape) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the number of columns for a grid layout based on screen size.
     * 
     * @param context The context to get screen dimensions from
     * @return Recommended number of columns (2 for COMPACT, 3 for MEDIUM, 4 for EXPANDED)
     */
    public static int getGridColumnCount(Context context) {
        if (context == null) {
            return 2;
        }
        
        ScreenSize screenSize = getScreenSize(context);
        
        switch (screenSize) {
            case MEDIUM:
                return 3;
            case EXPANDED:
                return 4;
            case COMPACT:
            default:
                return 2;
        }
    }
    
    /**
     * Get responsive margin value based on screen size.
     * Returns margin in pixels (px) for use in layouts.
     * 
     * Margin values:
     * - COMPACT: 8dp
     * - MEDIUM: 12dp
     * - EXPANDED: 16dp
     * 
     * @param context The context to get screen dimensions from
     * @return Margin value in pixels
     */
    public static int getResponsiveMargin(Context context) {
        if (context == null) {
            return dpToPx(context, 8);
        }
        
        ScreenSize screenSize = getScreenSize(context);
        int marginDp;
        
        switch (screenSize) {
            case MEDIUM:
                marginDp = 12;
                break;
            case EXPANDED:
                marginDp = 16;
                break;
            case COMPACT:
            default:
                marginDp = 8;
                break;
        }
        
        return dpToPx(context, marginDp);
    }
}
