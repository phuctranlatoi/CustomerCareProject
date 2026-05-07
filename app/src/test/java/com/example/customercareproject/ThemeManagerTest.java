package com.example.customercareproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import com.example.customercareproject.utils.ThemeManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ThemeManager utility class
 * Tests theme management, dark mode detection, and preference persistence
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = Build.VERSION_CODES.TIRAMISU)
public class ThemeManagerTest {

    @Mock
    private Context mockContext;
    
    @Mock
    private SharedPreferences mockPrefs;
    
    @Mock
    private SharedPreferences.Editor mockEditor;
    
    @Mock
    private Resources mockResources;
    
    @Mock
    private Configuration mockConfiguration;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mock behavior
        when(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockEditor.putBoolean(anyString(), anyBoolean())).thenReturn(mockEditor);
        when(mockEditor.putInt(anyString(), anyInt())).thenReturn(mockEditor);
        when(mockContext.getResources()).thenReturn(mockResources);
        when(mockResources.getConfiguration()).thenReturn(mockConfiguration);
    }

    @Test
    public void testSaveDarkModePreference() {
        // Test saving dark mode enabled
        ThemeManager.saveDarkModePreference(mockContext, true);
        verify(mockEditor).putBoolean("dark_mode_enabled", true);
        verify(mockEditor).apply();
        
        // Test saving dark mode disabled
        ThemeManager.saveDarkModePreference(mockContext, false);
        verify(mockEditor).putBoolean("dark_mode_enabled", false);
        verify(mockEditor, times(2)).apply();
    }

    @Test
    public void testGetSavedDarkModePreference() {
        // Test when dark mode is saved as enabled
        when(mockPrefs.getBoolean("dark_mode_enabled", false)).thenReturn(true);
        assertTrue(ThemeManager.getSavedDarkModePreference(mockContext));
        
        // Test when dark mode is saved as disabled
        when(mockPrefs.getBoolean("dark_mode_enabled", false)).thenReturn(false);
        assertFalse(ThemeManager.getSavedDarkModePreference(mockContext));
    }

    @Test
    public void testGetSavedThemeMode() {
        // Test system theme mode
        when(mockPrefs.getInt("theme_override", ThemeManager.THEME_MODE_SYSTEM))
                .thenReturn(ThemeManager.THEME_MODE_SYSTEM);
        assertEquals(ThemeManager.THEME_MODE_SYSTEM, ThemeManager.getSavedThemeMode(mockContext));
        
        // Test light theme mode
        when(mockPrefs.getInt("theme_override", ThemeManager.THEME_MODE_SYSTEM))
                .thenReturn(ThemeManager.THEME_MODE_LIGHT);
        assertEquals(ThemeManager.THEME_MODE_LIGHT, ThemeManager.getSavedThemeMode(mockContext));
        
        // Test dark theme mode
        when(mockPrefs.getInt("theme_override", ThemeManager.THEME_MODE_SYSTEM))
                .thenReturn(ThemeManager.THEME_MODE_DARK);
        assertEquals(ThemeManager.THEME_MODE_DARK, ThemeManager.getSavedThemeMode(mockContext));
    }

    @Test
    public void testIsDarkModeEnabled_WithOverride() {
        // Test when user has explicitly set dark mode
        when(mockPrefs.getInt("theme_override", ThemeManager.THEME_MODE_SYSTEM))
                .thenReturn(ThemeManager.THEME_MODE_DARK);
        assertTrue(ThemeManager.isDarkModeEnabled(mockContext));
        
        // Test when user has explicitly set light mode
        when(mockPrefs.getInt("theme_override", ThemeManager.THEME_MODE_SYSTEM))
                .thenReturn(ThemeManager.THEME_MODE_LIGHT);
        assertFalse(ThemeManager.isDarkModeEnabled(mockContext));
    }

    @Test
    public void testIsDarkModeEnabled_SystemSetting() {
        // Test when following system setting (dark)
        when(mockPrefs.getInt("theme_override", ThemeManager.THEME_MODE_SYSTEM))
                .thenReturn(ThemeManager.THEME_MODE_SYSTEM);
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_YES;
        assertTrue(ThemeManager.isDarkModeEnabled(mockContext));
        
        // Test when following system setting (light)
        mockConfiguration.uiMode = Configuration.UI_MODE_NIGHT_NO;
        assertFalse(ThemeManager.isDarkModeEnabled(mockContext));
    }

    @Test
    public void testSupportsDynamicColors() {
        // Dynamic colors are supported on Android 12+ (API 31+)
        boolean supported = ThemeManager.supportsDynamicColors();
        
        // This will depend on the test SDK version
        // For API 33 (Tiramisu), it should be true
        assertTrue(supported);
    }

    @Test
    @Config(sdk = Build.VERSION_CODES.R)
    public void testSupportsDynamicColors_OlderAndroid() {
        // Dynamic colors should not be supported on Android 11 (API 30)
        boolean supported = ThemeManager.supportsDynamicColors();
        assertFalse(supported);
    }

    @Test
    public void testThemeModeConstants() {
        // Verify theme mode constants are defined correctly
        assertEquals(0, ThemeManager.THEME_MODE_SYSTEM);
        assertEquals(1, ThemeManager.THEME_MODE_LIGHT);
        assertEquals(2, ThemeManager.THEME_MODE_DARK);
    }
}
