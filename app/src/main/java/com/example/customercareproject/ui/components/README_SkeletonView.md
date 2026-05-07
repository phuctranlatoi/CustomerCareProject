# SkeletonView Component

## Overview

`SkeletonView` is a custom Android view component that provides skeleton loading states with shimmer effect animation. It improves perceived performance by showing placeholder content while data is being fetched, following Material Design 3 guidelines.

## Features

- **Shimmer Effect**: Smooth gradient animation that moves across the skeleton
- **Configurable Shapes**: Support for rectangle and circle shapes
- **Customizable Colors**: Base color and shimmer color can be customized
- **Auto-start Animation**: Automatically starts shimmer when view becomes visible
- **Performance Optimized**: Automatically stops animation when view is hidden or detached

## Requirements

This component satisfies the following requirements from the Modern UI Redesign spec:
- **Requirement 10.5**: Apply skeleton loading animation while fetching ticket data
- **Requirement 16.1**: Display skeleton screens with shimmer effect while loading content

## Usage

### XML Layout

#### Basic Rectangle Skeleton (for text placeholders)

```xml
<com.example.customercareproject.ui.components.SkeletonView
    android:id="@+id/skeletonTitle"
    android:layout_width="match_parent"
    android:layout_height="20dp"
    app:skeletonShape="rectangle"
    app:shimmerDuration="1500"
    app:autoStart="true" />
```

#### Circle Skeleton (for avatar placeholders)

```xml
<com.example.customercareproject.ui.components.SkeletonView
    android:id="@+id/skeletonAvatar"
    android:layout_width="64dp"
    android:layout_height="64dp"
    app:skeletonShape="circle"
    app:shimmerDuration="1500"
    app:autoStart="true" />
```

#### Custom Colors

```xml
<com.example.customercareproject.ui.components.SkeletonView
    android:id="@+id/skeletonCustom"
    android:layout_width="match_parent"
    android:layout_height="16dp"
    app:skeletonShape="rectangle"
    app:baseColor="@color/custom_base"
    app:shimmerColor="@color/custom_shimmer"
    app:shimmerDuration="2000"
    app:autoStart="true" />
```

### Java/Kotlin Code

#### Basic Usage

```java
// Get reference to skeleton view
SkeletonView skeleton = findViewById(R.id.skeletonTitle);

// Start shimmer animation
skeleton.startShimmer();

// When data is loaded, hide skeleton
skeleton.stopShimmer();
skeleton.setVisibility(View.GONE);
```

#### Programmatic Configuration

```java
SkeletonView skeleton = findViewById(R.id.skeletonAvatar);

// Set shape
skeleton.setSkeletonShape(SkeletonView.Shape.CIRCLE);

// Set colors
skeleton.setBaseColor(Color.LTGRAY);
skeleton.setShimmerColor(Color.WHITE);

// Set animation duration
skeleton.setShimmerDuration(1500); // 1.5 seconds

// Control animation
skeleton.startShimmer();
// ... later
skeleton.stopShimmer();
```

#### Check Animation State

```java
if (skeleton.isShimmerRunning()) {
    // Shimmer is currently running
    skeleton.stopShimmer();
}
```

## Custom Attributes

| Attribute | Type | Description | Default |
|-----------|------|-------------|---------|
| `skeletonShape` | enum | Shape of skeleton: `rectangle` or `circle` | `rectangle` |
| `shimmerDuration` | integer | Duration of shimmer animation in milliseconds | `1500` |
| `baseColor` | color | Background color of skeleton | `colorSurfaceVariant` from theme |
| `shimmerColor` | color | Highlight color of shimmer effect | `colorSurface` from theme |
| `autoStart` | boolean | Whether to auto-start shimmer when view is visible | `true` |

## Common Use Cases

### 1. Loading User Profile

```xml
<LinearLayout
    android:id="@+id/skeletonContainer"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp">

    <!-- Avatar -->
    <com.example.customercareproject.ui.components.SkeletonView
        android:layout_width="64dp"
        android:layout_height="64dp"
        app:skeletonShape="circle"
        android:layout_marginEnd="16dp" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">

        <!-- Name -->
        <com.example.customercareproject.ui.components.SkeletonView
            android:layout_width="match_parent"
            android:layout_height="20dp"
            android:layout_marginBottom="8dp" />

        <!-- Email -->
        <com.example.customercareproject.ui.components.SkeletonView
            android:layout_width="180dp"
            android:layout_height="16dp" />
    </LinearLayout>
</LinearLayout>
```

### 2. Loading List Items

```xml
<!-- Repeat this for multiple skeleton items -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="12dp">

    <com.example.customercareproject.ui.components.SkeletonView
        android:layout_width="40dp"
        android:layout_height="40dp"
        app:skeletonShape="circle"
        android:layout_marginEnd="12dp" />

    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:orientation="vertical">

        <com.example.customercareproject.ui.components.SkeletonView
            android:layout_width="match_parent"
            android:layout_height="16dp"
            android:layout_marginBottom="6dp" />

        <com.example.customercareproject.ui.components.SkeletonView
            android:layout_width="160dp"
            android:layout_height="12dp" />
    </LinearLayout>
</LinearLayout>
```

### 3. Loading Text Content

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical">

    <!-- Title -->
    <com.example.customercareproject.ui.components.SkeletonView
        android:layout_width="match_parent"
        android:layout_height="24dp"
        android:layout_marginBottom="12dp" />

    <!-- Paragraph lines -->
    <com.example.customercareproject.ui.components.SkeletonView
        android:layout_width="match_parent"
        android:layout_height="14dp"
        android:layout_marginBottom="8dp" />

    <com.example.customercareproject.ui.components.SkeletonView
        android:layout_width="match_parent"
        android:layout_height="14dp"
        android:layout_marginBottom="8dp" />

    <com.example.customercareproject.ui.components.SkeletonView
        android:layout_width="240dp"
        android:layout_height="14dp" />
</LinearLayout>
```

## Best Practices

### 1. Match Skeleton to Content

Design skeleton layouts that closely match the actual content layout. This creates a smooth transition when content loads.

```java
// Show skeleton
skeletonContainer.setVisibility(View.VISIBLE);
contentContainer.setVisibility(View.GONE);

// Load data
loadData(data -> {
    // Hide skeleton, show content
    skeletonContainer.setVisibility(View.GONE);
    contentContainer.setVisibility(View.VISIBLE);
});
```

### 2. Use Appropriate Shapes

- Use **circle** shape for avatars, profile pictures, and circular icons
- Use **rectangle** shape for text, images, and rectangular content

### 3. Consistent Animation Duration

Use consistent shimmer duration across your app (default 1500ms is recommended).

### 4. Auto-start by Default

Keep `autoStart="true"` (default) so skeletons automatically animate when visible. This reduces boilerplate code.

### 5. Container Visibility

Use a container to group skeleton views and toggle visibility between skeleton and actual content:

```xml
<FrameLayout>
    <!-- Skeleton container -->
    <LinearLayout
        android:id="@+id/skeletonContainer"
        android:visibility="visible">
        <!-- Skeleton views here -->
    </LinearLayout>

    <!-- Content container -->
    <LinearLayout
        android:id="@+id/contentContainer"
        android:visibility="gone">
        <!-- Actual content here -->
    </LinearLayout>
</FrameLayout>
```

## Performance Considerations

### Automatic Lifecycle Management

`SkeletonView` automatically manages animation lifecycle:

- **Starts** shimmer when view becomes visible
- **Stops** shimmer when view is hidden or detached from window
- This prevents unnecessary animations and saves battery

### Manual Control

If you need manual control, set `autoStart="false"`:

```xml
<com.example.customercareproject.ui.components.SkeletonView
    android:id="@+id/skeleton"
    android:layout_width="match_parent"
    android:layout_height="20dp"
    app:autoStart="false" />
```

Then control manually:

```java
skeleton.startShimmer();
// ... later
skeleton.stopShimmer();
```

## Accessibility

`SkeletonView` is a visual loading indicator. For accessibility:

1. **Announce Loading State**: Use `announceForAccessibility()` when showing skeleton

```java
skeletonContainer.setVisibility(View.VISIBLE);
skeletonContainer.announceForAccessibility("Loading content");
```

2. **Announce Content Loaded**: Announce when content is ready

```java
contentContainer.setVisibility(View.VISIBLE);
contentContainer.announceForAccessibility("Content loaded");
```

3. **Set Content Description**: Add content description to skeleton container

```xml
<LinearLayout
    android:id="@+id/skeletonContainer"
    android:contentDescription="Loading placeholder">
    <!-- Skeleton views -->
</LinearLayout>
```

## Demo Activity

A complete demo showcasing all features is available in `SkeletonViewDemoActivity`. To run the demo:

1. Add the activity to your `AndroidManifest.xml` (if not already added)
2. Launch the activity from your app or via ADB:

```bash
adb shell am start -n com.example.customercareproject/.ui.components.SkeletonViewDemoActivity
```

The demo includes:
- Different skeleton shapes (circle, rectangle)
- Various sizes (small, medium, large)
- Text placeholders
- List item skeletons
- Interactive controls to toggle loading/content states

## Material Design 3 Compliance

`SkeletonView` follows Material Design 3 guidelines:

- Uses theme colors (`colorSurfaceVariant`, `colorSurface`)
- Applies rounded corners (8dp) for rectangle shapes
- Smooth, subtle shimmer animation
- Respects system theme (light/dark mode)

## Troubleshooting

### Shimmer not visible

**Problem**: Shimmer animation is not visible

**Solutions**:
1. Check that `autoStart="true"` or call `startShimmer()` manually
2. Ensure view has non-zero width and height
3. Verify view is visible (`visibility="visible"`)
4. Check that base color and shimmer color have sufficient contrast

### Animation not stopping

**Problem**: Animation continues when view is hidden

**Solution**: `SkeletonView` automatically stops animation when hidden. If using manual control, ensure you call `stopShimmer()`.

### Performance issues

**Problem**: App feels sluggish with many skeleton views

**Solutions**:
1. Limit number of visible skeleton views (use RecyclerView for lists)
2. Increase `shimmerDuration` to reduce animation frequency
3. Ensure skeleton views are properly hidden when not needed

## Related Components

- **Material3Card**: Use with skeleton views for card loading states
- **Material3Button**: Show loading state in buttons
- **EmptyStateView**: Show when no data is available (after loading completes)

## Version History

- **v1.0** (2024): Initial implementation with rectangle and circle shapes, shimmer animation, and auto-start support

## Support

For issues or questions about `SkeletonView`, please refer to:
- Design document: `.kiro/specs/modern-ui-redesign/design.md`
- Requirements: `.kiro/specs/modern-ui-redesign/requirements.md`
- Demo activity: `SkeletonViewDemoActivity.java`
