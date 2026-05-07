# ResponsiveLayoutHelper Usage Guide

## Overview

`ResponsiveLayoutHelper` is a utility class that provides methods for detecting screen sizes and adjusting layouts according to Material Design 3 responsive breakpoints. It helps create adaptive layouts that work well across different device sizes and orientations.

## Material Design 3 Breakpoints

The helper uses Material Design 3 responsive breakpoints:

- **COMPACT**: < 600dp (phones in portrait)
- **MEDIUM**: 600dp - 840dp (tablets in portrait, phones in landscape)
- **EXPANDED**: > 840dp (tablets in landscape, large screens)

## Core Methods

### 1. Screen Size Detection

#### `getScreenSize(Context context)`

Detects the current screen size category based on Material Design 3 breakpoints.

```java
ResponsiveLayoutHelper.ScreenSize screenSize = ResponsiveLayoutHelper.getScreenSize(context);

switch (screenSize) {
    case COMPACT:
        // Phone in portrait - use single column layout
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        break;
    case MEDIUM:
        // Tablet in portrait or phone in landscape - use 2-3 columns
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        break;
    case EXPANDED:
        // Tablet in landscape - use 3-4 columns or two-pane layout
        recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        break;
}
```

### 2. Responsive Spacing

#### `getResponsiveSpacing(Context context)`

Returns spacing value in pixels based on screen size:
- COMPACT: 16dp
- MEDIUM: 24dp
- EXPANDED: 32dp

```java
// Apply responsive padding to a container
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(context);
container.setPadding(spacing, spacing, spacing, spacing);

// Use in RecyclerView ItemDecoration
recyclerView.addItemDecoration(new GridSpacingItemDecoration(
    columnCount, 
    ResponsiveLayoutHelper.getResponsiveSpacing(context)
));
```

### 3. Device Type Detection

#### `isTablet(Context context)`

Checks if the device is a tablet (smallest width >= 600dp).

```java
if (ResponsiveLayoutHelper.isTablet(context)) {
    // Show tablet-specific UI
    showTwoPaneLayout();
} else {
    // Show phone UI
    showSinglePaneLayout();
}
```

#### `isLandscape(Context context)`

Checks if the device is in landscape orientation.

```java
if (ResponsiveLayoutHelper.isLandscape(context)) {
    // Adjust layout for landscape
    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
} else {
    // Portrait layout
    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
}
```

### 4. Two-Pane Layout Detection

#### `shouldUseTwoPaneLayout(Context context)`

Determines if a two-pane (master-detail) layout should be used.

Returns `true` for:
- EXPANDED screens in any orientation
- MEDIUM screens in landscape

```java
if (ResponsiveLayoutHelper.shouldUseTwoPaneLayout(context)) {
    // Show master and detail side-by-side
    findViewById(R.id.masterPane).setVisibility(View.VISIBLE);
    findViewById(R.id.detailPane).setVisibility(View.VISIBLE);
} else {
    // Show only master, navigate to detail on selection
    findViewById(R.id.detailPane).setVisibility(View.GONE);
}
```

### 5. Grid Column Count

#### `getGridColumnCount(Context context)`

Returns recommended number of columns for grid layouts:
- COMPACT: 2 columns
- MEDIUM: 3 columns
- EXPANDED: 4 columns

```java
int columnCount = ResponsiveLayoutHelper.getGridColumnCount(context);
GridLayoutManager layoutManager = new GridLayoutManager(context, columnCount);
recyclerView.setLayoutManager(layoutManager);
```

### 6. Responsive Margin

#### `getResponsiveMargin(Context context)`

Returns margin value in pixels based on screen size:
- COMPACT: 8dp
- MEDIUM: 12dp
- EXPANDED: 16dp

```java
int margin = ResponsiveLayoutHelper.getResponsiveMargin(context);
ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
params.setMargins(margin, margin, margin, margin);
view.setLayoutParams(params);
```

## Utility Methods

### Unit Conversion

#### `dpToPx(Context context, int dp)`

Converts dp (density-independent pixels) to pixels.

```java
int paddingPx = ResponsiveLayoutHelper.dpToPx(context, 16);
view.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
```

#### `pxToDp(Context context, int px)`

Converts pixels to dp.

```java
int widthPx = view.getWidth();
int widthDp = ResponsiveLayoutHelper.pxToDp(context, widthPx);
Log.d("ViewSize", "Width: " + widthDp + "dp");
```

#### `getScreenHeightDp(Context context)`

Gets the screen height in dp.

```java
int heightDp = ResponsiveLayoutHelper.getScreenHeightDp(context);
if (heightDp < 600) {
    // Compact height - reduce vertical spacing
}
```

## Common Use Cases

### 1. Adaptive RecyclerView Grid

```java
public class ProductsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        
        // Set column count based on screen size
        int columnCount = ResponsiveLayoutHelper.getGridColumnCount(this);
        GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
        recyclerView.setLayoutManager(layoutManager);
        
        // Add responsive spacing between items
        int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing));
        
        // Set adapter
        recyclerView.setAdapter(new ProductAdapter(products));
    }
}
```

### 2. Master-Detail Layout

```java
public class TicketsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tickets);
        
        if (ResponsiveLayoutHelper.shouldUseTwoPaneLayout(this)) {
            // Two-pane layout for tablets
            findViewById(R.id.ticketListPane).setVisibility(View.VISIBLE);
            findViewById(R.id.ticketDetailPane).setVisibility(View.VISIBLE);
            
            // Show first ticket detail by default
            showTicketDetail(tickets.get(0));
        } else {
            // Single-pane layout for phones
            findViewById(R.id.ticketDetailPane).setVisibility(View.GONE);
            
            // Navigate to detail activity on item click
            ticketList.setOnItemClickListener((parent, view, position, id) -> {
                Intent intent = new Intent(this, TicketDetailActivity.class);
                intent.putExtra("ticket_id", tickets.get(position).getId());
                startActivity(intent);
            });
        }
    }
}
```

### 3. Responsive Container Padding

```java
public class ChatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        View container = findViewById(R.id.chatContainer);
        
        // Apply responsive padding
        int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
        container.setPadding(spacing, spacing, spacing, spacing);
        
        // Apply responsive margin to message bubbles
        int margin = ResponsiveLayoutHelper.getResponsiveMargin(this);
        // Use margin in adapter for message items
    }
}
```

### 4. Orientation-Specific Layout

```java
public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        LinearLayout statsContainer = findViewById(R.id.statsContainer);
        
        if (ResponsiveLayoutHelper.isLandscape(this)) {
            // Horizontal layout for landscape
            statsContainer.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            // Vertical layout for portrait
            statsContainer.setOrientation(LinearLayout.VERTICAL);
        }
    }
}
```

### 5. Tablet-Specific Features

```java
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        if (ResponsiveLayoutHelper.isTablet(this)) {
            // Show additional information on tablets
            findViewById(R.id.sidebarPanel).setVisibility(View.VISIBLE);
            
            // Use larger images
            ImageView heroImage = findViewById(R.id.heroImage);
            heroImage.getLayoutParams().height = ResponsiveLayoutHelper.dpToPx(this, 400);
        } else {
            // Hide sidebar on phones
            findViewById(R.id.sidebarPanel).setVisibility(View.GONE);
            
            // Use smaller images
            ImageView heroImage = findViewById(R.id.heroImage);
            heroImage.getLayoutParams().height = ResponsiveLayoutHelper.dpToPx(this, 200);
        }
    }
}
```

### 6. Dynamic Grid Spacing ItemDecoration

```java
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spanCount;
    private final int spacing;
    
    public GridSpacingItemDecoration(int spanCount, int spacing) {
        this.spanCount = spanCount;
        this.spacing = spacing;
    }
    
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        int column = position % spanCount;
        
        outRect.left = spacing - column * spacing / spanCount;
        outRect.right = (column + 1) * spacing / spanCount;
        
        if (position < spanCount) {
            outRect.top = spacing;
        }
        outRect.bottom = spacing;
    }
}

// Usage
int columnCount = ResponsiveLayoutHelper.getGridColumnCount(context);
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(context);
recyclerView.addItemDecoration(new GridSpacingItemDecoration(columnCount, spacing));
```

## Best Practices

1. **Always check for null context**: All methods handle null context gracefully, but it's good practice to ensure context is valid.

2. **Use responsive spacing consistently**: Apply `getResponsiveSpacing()` for padding and margins to maintain consistent spacing across screen sizes.

3. **Adapt grid columns**: Use `getGridColumnCount()` for grid layouts to ensure optimal content density.

4. **Consider orientation changes**: Handle orientation changes by checking `isLandscape()` and adjusting layouts accordingly.

5. **Test on multiple devices**: Test your responsive layouts on phones, tablets, and different orientations.

6. **Use two-pane layouts wisely**: Implement master-detail patterns using `shouldUseTwoPaneLayout()` for better tablet experiences.

7. **Combine with ConstraintLayout**: Use ResponsiveLayoutHelper with ConstraintLayout for maximum flexibility.

## Configuration Changes

Handle configuration changes (orientation, screen size) by recalculating responsive values:

```java
@Override
public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    
    // Recalculate responsive values
    updateLayoutForScreenSize();
}

private void updateLayoutForScreenSize() {
    int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
    int columnCount = ResponsiveLayoutHelper.getGridColumnCount(this);
    
    // Update RecyclerView
    GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
    if (layoutManager != null) {
        layoutManager.setSpanCount(columnCount);
    }
    
    // Update padding
    container.setPadding(spacing, spacing, spacing, spacing);
    
    // Update two-pane layout
    if (ResponsiveLayoutHelper.shouldUseTwoPaneLayout(this)) {
        showTwoPaneLayout();
    } else {
        showSinglePaneLayout();
    }
}
```

## Integration with Material Design 3

ResponsiveLayoutHelper is designed to work seamlessly with Material Design 3 components:

```java
// Use with Material3Card
Material3Card card = findViewById(R.id.card);
int margin = ResponsiveLayoutHelper.getResponsiveMargin(this);
ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) card.getLayoutParams();
params.setMargins(margin, margin, margin, margin);

// Use with Material3Button
Material3Button button = findViewById(R.id.button);
int spacing = ResponsiveLayoutHelper.getResponsiveSpacing(this);
button.setPadding(spacing, spacing / 2, spacing, spacing / 2);

// Adjust FAB size based on screen size
FloatingActionButton fab = findViewById(R.id.fab);
if (ResponsiveLayoutHelper.getScreenSize(this) == ResponsiveLayoutHelper.ScreenSize.EXPANDED) {
    fab.setSize(FloatingActionButton.SIZE_NORMAL);
} else {
    fab.setSize(FloatingActionButton.SIZE_MINI);
}
```

## Summary

ResponsiveLayoutHelper provides a simple, consistent way to create adaptive layouts that follow Material Design 3 guidelines. By using these utility methods, you can ensure your app looks great on all device sizes and orientations while maintaining a cohesive design system.
