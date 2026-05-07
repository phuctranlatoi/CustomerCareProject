# ThemeManager Usage Guide

## Overview
The `ThemeManager` utility class provides methods to manage application themes (light/dark mode) and dynamic colors in the Customer Care Android application.

## Features
- Detect system theme preference
- Apply Material Design 3 color schemes
- Support dynamic color extraction from wallpaper (Android 12+)
- Persist user theme preference across app sessions
- Toggle between light and dark modes

## Basic Usage

### 1. Initialize Theme on App Startup

Add this to your Application class or MainActivity's `onCreate()`:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // Initialize theme before super.onCreate()
    ThemeManager.initializeTheme(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
}
```

### 2. Apply Dynamic Colors (Android 12+)

Apply dynamic colors in your Activity's `onCreate()`:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    // Apply dynamic colors if supported
    ThemeManager.applyDynamicColors(this);
    
    setContentView(R.layout.activity_main);
}
```

### 3. Toggle Theme

Add a theme toggle button in your settings or profile screen:

```java
Button btnToggleTheme = findViewById(R.id.btnToggleTheme);
btnToggleTheme.setOnClickListener(v -> {
    ThemeManager.toggleTheme(this);
    // Recreate activity to apply new theme
    recreate();
});
```

### 4. Check Current Theme

Check if dark mode is currently enabled:

```java
boolean isDark = ThemeManager.isDarkModeEnabled(this);
if (isDark) {
    // Update UI for dark mode
} else {
    // Update UI for light mode
}
```

### 5. Set Specific Theme Mode

Set a specific theme mode (system, light, or dark):

```java
// Follow system theme
ThemeManager.applyThemeMode(this, ThemeManager.THEME_MODE_SYSTEM);

// Force light theme
ThemeManager.applyThemeMode(this, ThemeManager.THEME_MODE_LIGHT);

// Force dark theme
ThemeManager.applyThemeMode(this, ThemeManager.THEME_MODE_DARK);
```

### 6. Save and Retrieve Theme Preference

```java
// Save preference
ThemeManager.saveDarkModePreference(this, true);

// Get saved preference
boolean savedDarkMode = ThemeManager.getSavedDarkModePreference(this);
```

## Example: Settings Screen with Theme Toggle

```java
public class SettingsActivity extends AppCompatActivity {
    
    private Switch switchDarkMode;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme
        ThemeManager.initializeTheme(this);
        super.onCreate(savedInstanceState);
        
        // Apply dynamic colors
        ThemeManager.applyDynamicColors(this);
        
        setContentView(R.layout.activity_settings);
        
        switchDarkMode = findViewById(R.id.switchDarkMode);
        
        // Set current state
        switchDarkMode.setChecked(ThemeManager.isDarkModeEnabled(this));
        
        // Handle toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeManager.applyTheme(this, isChecked);
            recreate(); // Recreate activity to apply theme
        });
    }
}
```

## Example: Theme Selection Dialog

```java
public void showThemeDialog() {
    String[] themes = {"System Default", "Light", "Dark"};
    int currentTheme = ThemeManager.getSavedThemeMode(this);
    
    new AlertDialog.Builder(this)
        .setTitle("Choose Theme")
        .setSingleChoiceItems(themes, currentTheme, (dialog, which) -> {
            ThemeManager.applyThemeMode(this, which);
            recreate();
            dialog.dismiss();
        })
        .show();
}
```

## Dynamic Colors with Fallback

```java
ThemeManager.applyDynamicColorsWithFallback(
    this,
    () -> {
        // Success callback - dynamic colors applied
        Log.d("Theme", "Dynamic colors applied");
    },
    () -> {
        // Fallback callback - using static colors
        Log.d("Theme", "Using static theme colors");
    }
);
```

## Theme Mode Constants

- `ThemeManager.THEME_MODE_SYSTEM` (0) - Follow system theme
- `ThemeManager.THEME_MODE_LIGHT` (1) - Force light theme
- `ThemeManager.THEME_MODE_DARK` (2) - Force dark theme

## Requirements

- Minimum SDK: 26 (Android 8.0)
- Dynamic colors require SDK 31+ (Android 12+)
- Material Components library 1.11.0+

## Notes

- Always call `recreate()` after changing theme to apply changes
- Theme preference is automatically persisted in SharedPreferences
- Dynamic colors are only available on Android 12+ devices
- The theme system follows Material Design 3 specifications
