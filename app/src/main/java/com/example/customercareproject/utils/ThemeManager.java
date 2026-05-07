package com.example.customercareproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.color.DynamicColors;

/**
 * ThemeManager - Manages application theme (light/dark mode) and dynamic colors
 * 
 * This utility class provides methods to:
 * - Detect system theme preference
 * - Apply Material Design 3 color schemes
 * - Support dynamic color extraction from wallpaper (Android 12+)
 * - Persist user theme preference
 * 
 * Requirements: 2.5, 4.1, 4.2, 4.5
 */
public class ThemeManager {
    
    private static final String PREFS_NAME = "theme_preferences";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_THEME_OVERRIDE = "theme_override";
    
    // Theme mode constants
    public static final int THEME_MODE_SYSTEM = 0;
    public static final int THEME_MODE_LIGHT = 1;
    public static final int THEME_MODE_DARK = 2;
    
    /**
     * Apply theme based on user preference or system setting
     * 
     * @param context Application context
     * @param isDarkMode True to enable dark mode, false for light mode
     */
    public static void applyTheme(Context context, boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        // Save preference
        saveDarkModePreference(context, isDarkMode);
    }
    
    /**
     * Apply theme based on theme mode (system, light, or dark)
     * 
     * @param context Application context
     * @param themeMode One of THEME_MODE_SYSTEM, THEME_MODE_LIGHT, or THEME_MODE_DARK
     */
    public static void applyThemeMode(Context context, int themeMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME_OVERRIDE, themeMode).apply();
        
        switch (themeMode) {
            case THEME_MODE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_MODE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_MODE_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }
    
    /**
     * Check if dark mode is currently enabled
     * 
     * @param context Application context
     * @return True if dark mode is enabled, false otherwise
     */
    public static boolean isDarkModeEnabled(Context context) {
        // Check if user has overridden the theme
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int themeOverride = prefs.getInt(KEY_THEME_OVERRIDE, THEME_MODE_SYSTEM);
        
        if (themeOverride == THEME_MODE_DARK) {
            return true;
        } else if (themeOverride == THEME_MODE_LIGHT) {
            return false;
        }
        
        // Otherwise, check system setting
        int nightModeFlags = context.getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
    
    /**
     * Save user's dark mode preference
     * 
     * @param context Application context
     * @param isDarkMode True if dark mode is enabled, false otherwise
     */
    public static void saveDarkModePreference(Context context, boolean isDarkMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
    }
    
    /**
     * Get saved dark mode preference
     * 
     * @param context Application context
     * @return True if dark mode preference is saved as enabled, false otherwise
     */
    public static boolean getSavedDarkModePreference(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
    
    /**
     * Get saved theme mode preference
     * 
     * @param context Application context
     * @return Theme mode (THEME_MODE_SYSTEM, THEME_MODE_LIGHT, or THEME_MODE_DARK)
     */
    public static int getSavedThemeMode(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_THEME_OVERRIDE, THEME_MODE_SYSTEM);
    }
    
    /**
     * Check if device supports dynamic colors (Android 12+)
     * 
     * @return True if dynamic colors are supported, false otherwise
     */
    public static boolean supportsDynamicColors() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
    
    /**
     * Apply dynamic colors to activity if supported (Android 12+)
     * Dynamic colors extract color scheme from system wallpaper
     * 
     * @param activity Activity to apply dynamic colors to
     */
    public static void applyDynamicColors(Activity activity) {
        if (supportsDynamicColors()) {
            DynamicColors.applyToActivityIfAvailable(activity);
        }
    }
    
    /**
     * Apply dynamic colors to activity with fallback
     * If dynamic colors are not available, uses static theme colors
     * 
     * @param activity Activity to apply dynamic colors to
     * @param onSuccess Callback when dynamic colors are applied
     * @param onFallback Callback when falling back to static colors
     */
    public static void applyDynamicColorsWithFallback(Activity activity, 
                                                       Runnable onSuccess, 
                                                       Runnable onFallback) {
        if (supportsDynamicColors()) {
            DynamicColors.applyToActivityIfAvailable(activity);
            if (onSuccess != null) {
                onSuccess.run();
            }
        } else {
            if (onFallback != null) {
                onFallback.run();
            }
        }
    }
    
    /**
     * Initialize theme on app startup
     * Applies saved theme preference or follows system setting
     * 
     * @param context Application context
     */
    public static void initializeTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int themeMode = prefs.getInt(KEY_THEME_OVERRIDE, THEME_MODE_SYSTEM);
        applyThemeMode(context, themeMode);
    }
    
    /**
     * Toggle between light and dark mode
     * 
     * @param context Application context
     */
    public static void toggleTheme(Context context) {
        boolean currentlyDark = isDarkModeEnabled(context);
        applyTheme(context, !currentlyDark);
    }
    
    /**
     * Reset theme to system default
     * 
     * @param context Application context
     */
    public static void resetToSystemTheme(Context context) {
        applyThemeMode(context, THEME_MODE_SYSTEM);
    }
}
