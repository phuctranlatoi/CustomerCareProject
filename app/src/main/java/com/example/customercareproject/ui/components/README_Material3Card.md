# Material3Card Component

## Overview

`Material3Card` is a custom card component that extends `CardView` with Material Design 3 features. It provides consistent card styling and animations across the app, following Material Design 3 guidelines.

## Features

- **Configurable Rounded Corners**: Choose from 4 corner radius sizes (8dp, 12dp, 16dp, 28dp)
- **Elevation with Surface Tints**: MD3-style elevation using surface tints
- **Hover Effect**: Scale animation on press (scales to 0.98)
- **Clickable State**: Ripple effect for interactive cards
- **Easy Configuration**: XML attributes and programmatic API

## Usage

### XML Layout

```xml
<com.example.customercareproject.ui.components.Material3Card
    android:id="@+id/cardProduct"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cornerRadiusSize="medium"
    app:cardClickable="true"
    app:enableHoverEffect="true"
    app:cardElevation="2dp"
    android:layout_margin="8dp">
    
    <!-- Card content here -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Card Title"
            android:textAppearance="@style/TextAppearance.Material3.TitleMedium" />
        
        <TextView
            android:id="@+id/tvDescription"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Card description text"
            android:textAppearance="@style/TextAppearance.Material3.BodyMedium" />
    </LinearLayout>
    
</com.example.customercareproject.ui.components.Material3Card>
```

### Java Code

```java
// Find the card
Material3Card card = findViewById(R.id.cardProduct);

// Set corner radius size
card.setCornerRadiusSize(Material3Card.CornerRadiusSize.MEDIUM);

// Enable clickable state with ripple
card.setCardClickable(true);

// Enable hover effect
card.setEnableHoverEffect(true);

// Set click listener
card.setOnClickListener(v -> {
    // Handle card click
    Toast.makeText(this, "Card clicked!", Toast.LENGTH_SHORT).show();
});

// Reset card state (useful when removing from view)
card.resetState();
```

## Attributes

### XML Attributes

| Attribute | Type | Description | Default |
|-----------|------|-------------|---------|
| `app:cornerRadiusSize` | enum | Corner radius size: `small` (8dp), `medium` (12dp), `large` (16dp), `extra_large` (28dp) | `medium` |
| `app:cardClickable` | boolean | Enable clickable state with ripple effect | `false` |
| `app:enableHoverEffect` | boolean | Enable hover effect (scale animation) | `true` |
| `app:cardElevation` | dimension | Card elevation (shadow depth) | `2dp` |

### Corner Radius Sizes

- **SMALL**: 8dp - For small cards, chips
- **MEDIUM**: 12dp - Default, for standard cards
- **LARGE**: 16dp - For large cards, images
- **EXTRA_LARGE**: 28dp - For prominent cards, bottom sheets

## Public Methods

### setCornerRadiusSize(CornerRadiusSize size)
Set the corner radius size.

```java
card.setCornerRadiusSize(Material3Card.CornerRadiusSize.LARGE);
```

### getCornerRadiusSize()
Get the current corner radius size.

```java
CornerRadiusSize size = card.getCornerRadiusSize();
```

### setCardClickable(boolean clickable)
Set whether the card is clickable with ripple effect.

```java
card.setCardClickable(true);
```

### isCardClickable()
Check if the card is clickable.

```java
boolean isClickable = card.isCardClickable();
```

### setEnableHoverEffect(boolean enable)
Set whether to enable hover effect (scale animation).

```java
card.setEnableHoverEffect(true);
```

### isHoverEffectEnabled()
Check if hover effect is enabled.

```java
boolean isEnabled = card.isHoverEffectEnabled();
```

### resetState()
Reset the card to normal state (scale = 1.0). Useful when the card is removed from view while pressed.

```java
card.resetState();
```

## Examples

### Example 1: Product Card

```xml
<com.example.customercareproject.ui.components.Material3Card
    android:id="@+id/cardProduct"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cornerRadiusSize="medium"
    app:cardClickable="true"
    app:enableHoverEffect="true"
    app:cardElevation="2dp"
    android:layout_margin="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <ImageView
            android:id="@+id/ivProductIcon"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:src="@drawable/ic_product"
            android:layout_gravity="center_horizontal" />
        
        <TextView
            android:id="@+id/tvProductName"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Product Name"
            android:textAppearance="@style/TextAppearance.Material3.TitleMedium"
            android:layout_marginTop="8dp" />
        
        <TextView
            android:id="@+id/tvProductDescription"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Product description"
            android:textAppearance="@style/TextAppearance.Material3.BodySmall"
            android:layout_marginTop="4dp" />
    </LinearLayout>
</com.example.customercareproject.ui.components.Material3Card>
```

### Example 2: Statistics Card (Non-clickable)

```xml
<com.example.customercareproject.ui.components.Material3Card
    android:layout_width="0dp"
    android:layout_height="wrap_content"
    android:layout_weight="1"
    app:cornerRadiusSize="large"
    app:cardClickable="false"
    app:enableHoverEffect="false"
    app:cardElevation="4dp"
    android:layout_margin="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp"
        android:gravity="center">
        
        <TextView
            android:id="@+id/tvStatValue"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="42"
            android:textAppearance="@style/TextAppearance.Material3.HeadlineLarge"
            android:textColor="@color/primary" />
        
        <TextView
            android:id="@+id/tvStatLabel"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Active Tickets"
            android:textAppearance="@style/TextAppearance.Material3.BodySmall"
            android:layout_marginTop="4dp" />
    </LinearLayout>
</com.example.customercareproject.ui.components.Material3Card>
```

### Example 3: Ticket Card with Extra Large Corners

```xml
<com.example.customercareproject.ui.components.Material3Card
    android:id="@+id/cardTicket"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cornerRadiusSize="extra_large"
    app:cardClickable="true"
    app:enableHoverEffect="true"
    app:cardElevation="2dp"
    android:layout_margin="8dp">
    
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="16dp">
        
        <TextView
            android:id="@+id/tvTicketId"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="#12345"
            android:textAppearance="@style/TextAppearance.Material3.TitleSmall"
            android:textColor="@color/primary" />
        
        <TextView
            android:id="@+id/tvTicketTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Ticket Title"
            android:textAppearance="@style/TextAppearance.Material3.TitleMedium"
            android:layout_marginTop="4dp" />
        
        <TextView
            android:id="@+id/tvTicketStatus"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Open"
            android:textAppearance="@style/TextAppearance.Material3.LabelSmall"
            android:background="@drawable/bg_chip"
            android:padding="4dp"
            android:layout_marginTop="8dp" />
    </LinearLayout>
</com.example.customercareproject.ui.components.Material3Card>
```

## Animation Details

### Hover Effect
- **Scale**: 0.98 (scales down slightly when pressed)
- **Duration**: 100ms (fast micro-interaction)
- **Interpolator**: FastOutSlowInInterpolator (Material Motion easing)

### Ripple Effect
- **Duration**: 200ms (automatic by MaterialButton)
- **Color**: Uses theme's `colorControlHighlight` (12% black by default)

## Design Guidelines

### When to Use Material3Card

- **Product listings**: Display products in a grid or list
- **Ticket cards**: Show support tickets with status
- **Statistics cards**: Display KPIs and metrics
- **Information cards**: Group related information
- **Action cards**: Cards that trigger actions when clicked

### Elevation Guidelines

- **1dp**: Resting elevation for cards
- **2dp**: Default elevation (recommended)
- **4dp**: Elevated cards (important content)
- **8dp**: Highly elevated cards (modals, dialogs)

### Corner Radius Guidelines

- **Small (8dp)**: Small cards, chips, compact layouts
- **Medium (12dp)**: Standard cards (recommended default)
- **Large (16dp)**: Large cards, image containers
- **Extra Large (28dp)**: Prominent cards, hero sections

## Accessibility

- **Touch Target**: Ensure card content has minimum 48dp touch targets
- **Content Description**: Add `android:contentDescription` for clickable cards
- **Focus**: Card is focusable when clickable
- **Contrast**: Ensure text has sufficient contrast against card background

## Performance Considerations

- **Animation**: Uses hardware-accelerated animations for smooth 60fps
- **Ripple**: Ripple effect is hardware-accelerated
- **Memory**: Minimal memory overhead compared to standard CardView

## Migration from CardView

To migrate from standard `CardView` to `Material3Card`:

1. Replace `androidx.cardview.widget.CardView` with `com.example.customercareproject.ui.components.Material3Card`
2. Add `app:cornerRadiusSize` attribute (optional, defaults to medium)
3. Add `app:cardClickable="true"` if card should be clickable
4. Remove manual ripple setup (handled automatically)

## Requirements Satisfied

This component satisfies the following requirements from the Modern UI Redesign spec:

- **Requirement 1.3**: Material Design 3 elevation system using surface tints
- **Requirement 1.4**: Material Design 3 shape system with rounded corners (8dp, 12dp, 16dp, 28dp)
- **Requirement 1.5**: Material Design 3 components (Cards)
- **Requirement 6.1**: Micro-interactions with scale animation (scale to 0.98) with 100ms duration
- **Requirement 8.3**: Card elevation (2dp) and hover effect on product cards

## Related Components

- **Material3Button**: Custom button with MD3 styling and animations
- **AnimationHelper**: Utility class for consistent animations
- **ThemeManager**: Theme management for light/dark modes

## Notes

- For full Material Design 3 compliance with surface tints, consider using `MaterialCardView` from Material Components library
- The current implementation uses `CardView` for compatibility and extends it with MD3 features
- Surface tints are approximated using elevation shadows (CardView limitation)
