# ResponsiveLayoutHelper

## Overview

`ResponsiveLayoutHelper` is a utility class that provides methods for creating responsive, adaptive layouts following Material Design 3 guidelines. It helps detect screen sizes, calculate responsive spacing, and adjust layouts for different device types and orientations.

## Features

- **Screen Size Detection**: Detect COMPACT, MEDIUM, or EXPANDED screen sizes based on Material Design 3 breakpoints
- **Responsive Spacing**: Get spacing values that adapt to screen size (16dp, 24dp, 32dp)
- **Device Type Detection**: Check if device is a tablet or phone
- **Orientation Detection**: Detect landscape vs portrait orientation
- **Two-Pane Layout Support**: Determine when to use master-detail layouts
- **Grid Column Calculation**: Get optimal column count for grid layouts
- **Unit Conversion**: Convert between dp and pixels

## Material Design 3 Breakpoints

| Screen Size | Width Range | Typical Devices | Spacing | Columns |
|-------------|-------------|-----------------|---------|---------|
| COMPACT | < 600dp | Phones (portrait) | 16dp | 2 |
| MEDIUM | 600-840dp | Tablets (portrait), Phones (landscape) | 24dp | 3 |
| EXPANDED | > 840dp | Tablets (landscape), Large screens | 32dp | 4 |

## Quick Start

### Basic Usage

```java
// Get screen size
ResponsiveLayoutHelper.ScreenSize screenSize = ResponsiveLayoutHelper.getScreenSize(context);

// Apply responsive padding
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(context);
container.setPadding(spacing, spacing, spacing, spacing);

// Setup responsive grid
int columnCount = ResponsiveLayoutHelper.getGridColumnCount(context);
recyclerView.setLayoutManager(new GridLayoutManager(context, columnCount));
```

### Check Device Type

```java
if (ResponsiveLayoutHelper.isTablet(context)) {
    // Show tablet-specific UI
    showTwoPaneLayout();
} else {
    // Show phone UI
    showSinglePaneLayout();
}
```

### Adaptive Layouts

```java
if (ResponsiveLayoutHelper.shouldUseTwoPaneLayout(context)) {
    // Show master and detail side-by-side
    masterPane.setVisibility(View.VISIBLE);
    detailPane.setVisibility(View.VISIBLE);
} else {
    // Show only master, navigate to detail
    detailPane.setVisibility(View.GONE);
}
```

## API Reference

### Screen Size Detection

#### `getScreenSize(Context context)`
Returns the current screen size category (COMPACT, MEDIUM, or EXPANDED).

**Returns:** `ScreenSize` enum value

**Example:**
```java
ResponsiveLayoutHelper.ScreenSize size = ResponsiveLayoutHelper.getScreenSize(context);
switch (size) {
    case COMPACT:
        // Phone in portrait
        break;
    case MEDIUM:
        // Tablet in portrait or phone in landscape
        break;
    case EXPANDED:
        // Tablet in landscape
        break;
}
```

### Responsive Values

#### `getResponsiveSpacing(Context context)`
Returns spacing value in pixels based on screen size.

**Returns:** `int` - Spacing in pixels (16dp, 24dp, or 32dp converted to px)

**Example:**
```java
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(context);
view.setPadding(spacing, spacing, spacing, spacing);
```

#### `getResponsiveMargin(Context context)`
Returns margin value in pixels based on screen size.

**Returns:** `int` - Margin in pixels (8dp, 12dp, or 16dp converted to px)

**Example:**
```java
int margin = ResponsiveLayoutHelper.getResponsiveMargin(context);
ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
params.setMargins(margin, margin, margin, margin);
```

### Device Detection

#### `isTablet(Context context)`
Checks if the device is a tablet (smallest width >= 600dp).

**Returns:** `boolean` - true if tablet, false if phone

**Example:**
```java
if (ResponsiveLayoutHelper.isTablet(context)) {
    // Tablet-specific code
}
```

#### `isLandscape(Context context)`
Checks if the device is in landscape orientation.

**Returns:** `boolean` - true if landscape, false if portrait

**Example:**
```java
if (ResponsiveLayoutHelper.isLandscape(context)) {
    // Landscape-specific code
}
```

### Layout Helpers

#### `shouldUseTwoPaneLayout(Context context)`
Determines if a two-pane (master-detail) layout should be used.

**Returns:** `boolean` - true for EXPANDED screens or MEDIUM screens in landscape

**Example:**
```java
if (ResponsiveLayoutHelper.shouldUseTwoPaneLayout(context)) {
    showMasterDetailLayout();
} else {
    showSinglePaneLayout();
}
```

#### `getGridColumnCount(Context context)`
Returns recommended number of columns for grid layouts.

**Returns:** `int` - 2 (COMPACT), 3 (MEDIUM), or 4 (EXPANDED)

**Example:**
```java
int columns = ResponsiveLayoutHelper.getGridColumnCount(context);
GridLayoutManager layoutManager = new GridLayoutManager(context, columns);
recyclerView.setLayoutManager(layoutManager);
```

### Utility Methods

#### `dpToPx(Context context, int dp)`
Converts dp to pixels.

**Parameters:**
- `context` - Context for display metrics
- `dp` - Value in dp

**Returns:** `int` - Value in pixels

**Example:**
```java
int paddingPx = ResponsiveLayoutHelper.dpToPx(context, 16);
view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
```

#### `pxToDp(Context context, int px)`
Converts pixels to dp.

**Parameters:**
- `context` - Context for display metrics
- `px` - Value in pixels

**Returns:** `int` - Value in dp

**Example:**
```java
int widthDp = ResponsiveLayoutHelper.pxToDp(context, view.getWidth());
```

#### `getScreenHeightDp(Context context)`
Gets the screen height in dp.

**Returns:** `int` - Screen height in dp

**Example:**
```java
int heightDp = ResponsiveLayoutHelper.getScreenHeightDp(context);
if (heightDp < 600) {
    // Compact height
}
```

## Common Patterns

### Responsive RecyclerView Grid

```java
RecyclerView recyclerView = findViewById(R.id.recyclerView);

// Set column count based on screen size
int columnCount = ResponsiveLayoutHelper.getGridColumnCount(this);
GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
recyclerView.setLayoutManager(layoutManager);

// Add responsive spacing
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing));
```

### Master-Detail Layout

```java
if (ResponsiveLayoutHelper.shouldUseTwoPaneLayout(this)) {
    // Two-pane layout for tablets
    findViewById(R.id.masterPane).setVisibility(View.VISIBLE);
    findViewById(R.id.detailPane).setVisibility(View.VISIBLE);
} else {
    // Single-pane layout for phones
    findViewById(R.id.detailPane).setVisibility(View.GONE);
}
```

### Responsive Container Padding

```java
View container = findViewById(R.id.container);
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
container.setPadding(spacing, spacing, spacing, spacing);
```

### Orientation-Specific Layout

```java
LinearLayout container = findViewById(R.id.container);
if (ResponsiveLayoutHelper.isLandscape(this)) {
    container.setOrientation(LinearLayout.HORIZONTAL);
} else {
    container.setOrientation(LinearLayout.VERTICAL);
}
```

## Handling Configuration Changes

```java
@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    updateResponsiveLayout();
}

private void updateResponsiveLayout() {
    // Recalculate responsive values
    int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
    int columnCount = ResponsiveLayoutHelper.getGridColumnCount(this);
    
    // Update layouts
    container.setPadding(spacing, spacing, spacing, spacing);
    
    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
    if (layoutManager != null) {
        layoutManager.setSpanCount(columnCount);
    }
}
```

## Testing

Unit tests are available in `ResponsiveLayoutHelperTest.java`. Run tests with:

```bash
./gradlew connectedAndroidTest
```

## Demo

A demo activity is available at `ResponsiveLayoutHelperDemoActivity.java` that showcases all features. The demo displays:
- Current screen size and dimensions
- Device type (tablet/phone) and orientation
- Responsive spacing and margin values
- Grid column count
- Two-pane layout recommendation

## Best Practices

1. **Use responsive spacing consistently** - Apply `getResponsiveSpacing()` for padding and margins
2. **Adapt grid columns** - Use `getGridColumnCount()` for optimal content density
3. **Handle orientation changes** - Recalculate values in `onConfigurationChanged()`
4. **Test on multiple devices** - Verify layouts on phones, tablets, and different orientations
5. **Combine with ConstraintLayout** - Use with ConstraintLayout for maximum flexibility
6. **Check for null context** - All methods handle null gracefully, but validate context when possible

## Integration with Material Design 3

ResponsiveLayoutHelper works seamlessly with Material Design 3 components:

```java
// Material3Card with responsive margin
Material3Card card = findViewById(R.id.card);
int margin = ResponsiveLayoutHelper.getResponsiveMargin(this);
ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
params.setMargins(margin, margin, margin, margin);

// Material3Button with responsive padding
Material3Button button = findViewById(R.id.button);
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
button.setPadding(spacing, spacing / 2, spacing, spacing / 2);
```

## Requirements Satisfied

This utility class satisfies the following requirements from the Modern UI Redesign spec:

- **14.1**: Uses ConstraintLayout for flexible layouts
- **14.2**: Defines breakpoints (compact: <600dp, medium: 600-840dp, expanded: >840dp)
- **14.3**: Displays two-pane layout for expanded screens
- **14.4**: Uses responsive spacing (16dp, 24dp, 32dp)
- **14.5**: Supports landscape orientation with optimized layouts

## Related Components

- `AnimationHelper` - Animation utilities
- `ThemeManager` - Theme management
- `Material3Card` - Custom card component
- `Material3Button` - Custom button component

## License

Part of the Customer Care Android application.
