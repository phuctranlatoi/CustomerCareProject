# Material3Button Component

## Overview

`Material3Button` is a custom button component that extends `MaterialButton` with enhanced features following Material Design 3 guidelines. It provides consistent button styling and animations across the app.

## Features

### 1. Automatic Ripple Effect (200ms duration)
- Built-in ripple effect that follows Material Design 3 specifications
- Automatically configured with theme-appropriate ripple color
- 200ms duration for smooth visual feedback

### 2. Press Animation (Scale to 0.98)
- Scales down to 0.98 when pressed
- Scales back to 1.0 when released
- Uses Material Motion easing curve for smooth animation
- 100ms duration for micro-interaction

### 3. Loading State
- Shows circular progress indicator
- Disables button interaction
- Hides text and icon during loading
- Restores original state when loading completes
- Preserves original enabled state

### 4. Disabled State Styling
- Applies 38% opacity when disabled
- Resets scale animation if disabled during press
- Prevents interaction when disabled

## Usage

### XML Layout

```xml
<!-- Basic usage -->
<com.example.customercareproject.ui.components.Material3Button
    android:id="@+id/btnSubmit"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Submit" />

<!-- With loading state -->
<com.example.customercareproject.ui.components.Material3Button
    android:id="@+id/btnLoading"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Loading..."
    app:loading="true" />

<!-- Outlined style -->
<com.example.customercareproject.ui.components.Material3Button
    android:id="@+id/btnOutlined"
    style="@style/Widget.MaterialComponents.Button.OutlinedButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Outlined" />

<!-- Text style -->
<com.example.customercareproject.ui.components.Material3Button
    android:id="@+id/btnText"
    style="@style/Widget.MaterialComponents.Button.TextButton"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Text Button" />

<!-- With icon -->
<com.example.customercareproject.ui.components.Material3Button
    android:id="@+id/btnWithIcon"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Save"
    app:icon="@drawable/ic_save"
    app:iconGravity="start" />

<!-- Full width -->
<com.example.customercareproject.ui.components.Material3Button
    android:id="@+id/btnFullWidth"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:text="Continue" />

<!-- Disabled -->
<com.example.customercareproject.ui.components.Material3Button
    android:id="@+id/btnDisabled"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Disabled"
    android:enabled="false" />
```

### Java Code

```java
// Basic click listener
Material3Button btnSubmit = findViewById(R.id.btnSubmit);
btnSubmit.setOnClickListener(v -> {
    // Handle click
    Toast.makeText(this, "Button clicked!", Toast.LENGTH_SHORT).show();
});

// Toggle loading state
Material3Button btnSave = findViewById(R.id.btnSave);
btnSave.setOnClickListener(v -> {
    // Show loading
    btnSave.setLoading(true);
    
    // Simulate async operation (e.g., network request)
    new Handler(Looper.getMainLooper()).postDelayed(() -> {
        // Hide loading
        btnSave.setLoading(false);
        Toast.makeText(this, "Save complete!", Toast.LENGTH_SHORT).show();
    }, 2000);
});

// Check loading state
if (btnSave.isLoading()) {
    // Button is currently loading
}

// Enable/disable button
btnSubmit.setEnabled(false); // Applies 38% opacity
btnSubmit.setEnabled(true);  // Restores full opacity
```

## Custom Attributes

| Attribute | Type | Description | Default |
|-----------|------|-------------|---------|
| `app:loading` | boolean | Whether the button is in loading state | false |

## Public Methods

### `setLoading(boolean loading)`
Set the loading state of the button.

**Parameters:**
- `loading` - true to show loading state, false to hide

**Behavior:**
- When loading is true:
  - Button is disabled
  - Text and icon are hidden
  - Circular progress indicator is shown
- When loading is false:
  - Original text and icon are restored
  - Original enabled state is restored

**Example:**
```java
button.setLoading(true);  // Show loading
button.setLoading(false); // Hide loading
```

### `boolean isLoading()`
Check if the button is in loading state.

**Returns:**
- true if loading, false otherwise

**Example:**
```java
if (button.isLoading()) {
    // Button is loading
}
```

### `setEnabled(boolean enabled)`
Enable or disable the button.

**Parameters:**
- `enabled` - true to enable, false to disable

**Behavior:**
- Applies 38% opacity when disabled
- Applies 100% opacity when enabled
- Resets scale animation if disabled during press

**Example:**
```java
button.setEnabled(false); // Disable
button.setEnabled(true);  // Enable
```

## Animation Details

### Press Animation
- **Duration:** 100ms (DURATION_FAST)
- **Scale:** 0.98 when pressed, 1.0 when released
- **Easing:** Material Motion standard curve (FastOutSlowInInterpolator)
- **Trigger:** Touch down/up events

### Ripple Effect
- **Duration:** 200ms (built-in MaterialButton behavior)
- **Color:** Theme-based (colorControlHighlight)
- **Trigger:** Touch events

### Loading State Transition
- **Duration:** Instant (no animation)
- **Behavior:** Text/icon hidden, progress indicator shown

## Styling

Material3Button inherits all styling from MaterialButton and supports:

- **Filled buttons** (default): Solid background color
- **Outlined buttons**: Transparent background with border
- **Text buttons**: Transparent background, no border
- **Elevated buttons**: With elevation/shadow
- **Tonal buttons**: Filled with container color

Apply styles using the `style` attribute:

```xml
<!-- Outlined -->
style="@style/Widget.MaterialComponents.Button.OutlinedButton"

<!-- Text -->
style="@style/Widget.MaterialComponents.Button.TextButton"

<!-- Elevated -->
style="@style/Widget.Material3.Button.ElevatedButton"

<!-- Tonal -->
style="@style/Widget.Material3.Button.TonalButton"
```

## Best Practices

1. **Use loading state for async operations**
   - Show loading when performing network requests
   - Show loading when processing data
   - Always hide loading when operation completes (success or error)

2. **Provide clear button text**
   - Use action verbs (Save, Submit, Continue, etc.)
   - Keep text concise (1-2 words)
   - Use sentence case

3. **Choose appropriate button style**
   - Filled: Primary actions (Submit, Save, Continue)
   - Outlined: Secondary actions (Cancel, Back)
   - Text: Tertiary actions (Skip, Learn More)

4. **Handle loading state properly**
   ```java
   button.setOnClickListener(v -> {
       button.setLoading(true);
       
       performAsyncOperation()
           .thenAccept(result -> {
               button.setLoading(false);
               // Handle success
           })
           .exceptionally(error -> {
               button.setLoading(false);
               // Handle error
               return null;
           });
   });
   ```

5. **Don't nest loading states**
   - Avoid setting loading to true when already loading
   - The component handles this gracefully, but it's better to check first

6. **Clean up handlers**
   - If using Handler for delayed operations, clean up in onDestroy()
   ```java
   @Override
   protected void onDestroy() {
       super.onDestroy();
       handler.removeCallbacksAndMessages(null);
   }
   ```

## Requirements Satisfied

This component satisfies the following requirements from the Modern UI Redesign spec:

- **Requirement 1.5**: Material Design 3 components (Buttons)
- **Requirement 5.4**: Ripple effect with 200ms duration
- **Requirement 6.1**: Press animation (scale to 0.98)

## Testing

The component includes comprehensive instrumented tests in `Material3ButtonTest.java`:

- Button creation with default state
- Loading state transitions
- Enabled/disabled state
- Click blocking when loading
- Ripple effect configuration
- Scale animation behavior
- Multiple loading toggles
- Idempotent loading state

Run tests with:
```bash
./gradlew connectedAndroidTest
```

## Demo

See `Material3ButtonDemoActivity.java` and `example_material3_button.xml` for a complete demonstration of all features.

## Dependencies

- `com.google.android.material:material:1.11.0+`
- `androidx.appcompat:appcompat`
- AnimationHelper utility class

## Related Components

- **Material3Card**: Custom card component with MD3 styling
- **Material3TextField**: Custom text field with enhanced validation
- **AnimationHelper**: Centralized animation utilities

## Migration Guide

To migrate existing MaterialButton instances to Material3Button:

1. Replace the class name in XML:
   ```xml
   <!-- Before -->
   <com.google.android.material.button.MaterialButton
       android:id="@+id/btnSubmit"
       ... />
   
   <!-- After -->
   <com.example.customercareproject.ui.components.Material3Button
       android:id="@+id/btnSubmit"
       ... />
   ```

2. Update Java code if using loading state:
   ```java
   // Before (custom implementation)
   button.setEnabled(false);
   progressBar.setVisibility(View.VISIBLE);
   
   // After
   button.setLoading(true);
   ```

3. No other changes required - all MaterialButton features are preserved!

## Troubleshooting

### Button not responding to clicks
- Check if button is in loading state: `button.isLoading()`
- Check if button is enabled: `button.isEnabled()`

### Loading state not showing
- Ensure you're calling `setLoading(true)` on the UI thread
- Check if button has text set (loading hides text)

### Press animation not working
- Ensure button is enabled
- Ensure button is not in loading state
- Check if touch events are being intercepted by parent view

### Ripple effect not visible
- Check theme configuration
- Ensure button is clickable and focusable
- Verify ripple color is set

## License

This component is part of the Customer Care Android application.
