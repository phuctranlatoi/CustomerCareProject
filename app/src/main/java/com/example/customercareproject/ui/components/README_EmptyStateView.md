# EmptyStateView Component

## Overview

`EmptyStateView` is a custom view component for displaying empty states with illustration, text, and action button. It follows Material Design 3 guidelines and provides a consistent empty state experience across the app.

## Features

- **Configurable Illustration**: Display custom illustrations using ImageView
- **Title Text**: Large title text using MD3 TitleLarge typography style
- **Description Text**: Descriptive text using MD3 BodyMedium typography style
- **Action Button**: Material3Button with click listener interface
- **Fade-in Animation**: Smooth fade-in animation when displayed using AnimationHelper
- **Customizable**: All elements can be customized programmatically or via XML attributes

## Usage

### XML Layout

```xml
<com.example.customercareproject.ui.components.EmptyStateView
    android:id="@+id/emptyState"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:emptyStateIllustration="@drawable/ic_empty_tickets"
    app:emptyStateTitle="@string/no_tickets_title"
    app:emptyStateDescription="@string/no_tickets_description"
    app:emptyStateActionText="@string/create_ticket"
    app:showActionButton="true" />
```

### Java Code

```java
EmptyStateView emptyState = findViewById(R.id.emptyState);

// Set illustration
emptyState.setIllustration(R.drawable.ic_empty_tickets);

// Set title and description
emptyState.setTitle("No Tickets");
emptyState.setDescription("You don't have any tickets yet.\nCreate one to get started.");

// Set action button
emptyState.setActionText("Create Ticket");
emptyState.setOnActionClickListener(() -> {
    // Handle action button click
    createNewTicket();
});

// Show with fade-in animation
emptyState.show();

// Hide with fade-out animation
emptyState.hide();
```

## Custom Attributes

| Attribute | Type | Description |
|-----------|------|-------------|
| `emptyStateIllustration` | reference | Drawable resource for the illustration |
| `emptyStateTitle` | string/reference | Title text for the empty state |
| `emptyStateDescription` | string/reference | Description text for the empty state |
| `emptyStateActionText` | string/reference | Action button text |
| `showActionButton` | boolean | Whether to show the action button (default: true) |

## Public Methods

### Display Methods

- `show()` - Show the empty state view with fade-in animation
- `hide()` - Hide the empty state view with fade-out animation

### Content Methods

- `setIllustration(Drawable drawable)` - Set illustration drawable
- `setIllustration(@DrawableRes int resId)` - Set illustration from resource ID
- `setTitle(String title)` - Set title text
- `setTitle(@StringRes int resId)` - Set title from string resource
- `setDescription(String description)` - Set description text
- `setDescription(@StringRes int resId)` - Set description from string resource
- `setActionText(String text)` - Set action button text
- `setActionText(@StringRes int resId)` - Set action button text from string resource

### Configuration Methods

- `setShowActionButton(boolean show)` - Show or hide the action button
- `isActionButtonShown()` - Check if action button is shown
- `setOnActionClickListener(OnActionClickListener listener)` - Set action button click listener

### Advanced Customization

- `getIllustrationView()` - Get the ImageView for advanced customization
- `getTitleView()` - Get the title TextView for advanced customization
- `getDescriptionView()` - Get the description TextView for advanced customization
- `getActionButton()` - Get the Material3Button for advanced customization

## Common Use Cases

### 1. Empty Tickets List

```java
emptyState.setIllustration(R.drawable.ic_empty_tickets);
emptyState.setTitle("No Tickets");
emptyState.setDescription("You don't have any tickets yet.\nCreate one to get started.");
emptyState.setActionText("Create Ticket");
emptyState.setOnActionClickListener(() -> {
    startActivity(new Intent(this, CreateTicketActivity.class));
});
emptyState.show();
```

### 2. Empty Messages

```java
emptyState.setIllustration(R.drawable.ic_empty_messages);
emptyState.setTitle("No Messages");
emptyState.setDescription("You don't have any messages yet.\nStart a conversation to get help.");
emptyState.setActionText("Start Chat");
emptyState.setOnActionClickListener(() -> {
    startActivity(new Intent(this, ChatActivity.class));
});
emptyState.show();
```

### 3. Empty Search Results

```java
emptyState.setIllustration(R.drawable.ic_empty_search);
emptyState.setTitle("No Results Found");
emptyState.setDescription("We couldn't find any results matching your search.\nTry different keywords.");
emptyState.setActionText("Clear Search");
emptyState.setOnActionClickListener(() -> {
    searchEditText.setText("");
    performSearch("");
});
emptyState.show();
```

### 4. Empty State Without Action Button

```java
emptyState.setIllustration(R.drawable.ic_empty_data);
emptyState.setTitle("No Data Available");
emptyState.setDescription("There is no data to display at this time.");
emptyState.setShowActionButton(false);
emptyState.show();
```

## Design Guidelines

### Illustration

- Use simple, clear illustrations that represent the empty state
- Recommended size: 200dp x 200dp
- Use vector drawables (SVG) for scalability
- Follow Material Design illustration guidelines

### Title

- Keep it short and descriptive (2-4 words)
- Use sentence case
- Clearly state what is empty

### Description

- Provide helpful context (1-2 sentences)
- Explain why the state is empty
- Suggest what the user can do next

### Action Button

- Use clear, action-oriented text (e.g., "Create Ticket", "Start Chat")
- Only show if there's a clear action the user can take
- Make the action relevant to resolving the empty state

## Animation

The EmptyStateView uses `AnimationHelper.fadeIn()` with `DURATION_MEDIUM` (300ms) for smooth appearance. The fade-in animation:

- Starts from alpha 0 (transparent)
- Animates to alpha 1 (opaque)
- Uses Material Motion easing curve
- Duration: 300ms

## Accessibility

The component follows accessibility best practices:

- Illustration has content description
- Text is readable with proper contrast ratios
- Action button meets minimum touch target size (48dp)
- Supports text scaling up to 200%
- Works with TalkBack screen reader

## Requirements Validation

This component validates the following requirements from the Modern UI Redesign spec:

- **Requirement 10.4**: Empty state illustration display
- **Requirement 17.1**: Error/empty state illustration with descriptive message

## Demo Activity

Run `EmptyStateViewDemoActivity` to see the component in action with different configurations:

```java
Intent intent = new Intent(this, EmptyStateViewDemoActivity.class);
startActivity(intent);
```

## Related Components

- `Material3Button` - Used for the action button
- `AnimationHelper` - Provides fade-in/fade-out animations
- `SkeletonView` - Used for loading states before showing empty states

## Best Practices

1. **Show empty states only when appropriate** - Don't show empty state during loading
2. **Use skeleton screens first** - Show skeleton loading, then empty state if no data
3. **Provide actionable next steps** - Always give users a way forward
4. **Use appropriate illustrations** - Match the illustration to the context
5. **Keep text concise** - Users should understand the state at a glance
6. **Test with real data** - Ensure empty states work in all scenarios

## Example Integration

```java
public class TicketsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EmptyStateView emptyState;
    private SkeletonView skeleton;
    
    private void loadTickets() {
        // Show loading state
        skeleton.setVisibility(View.VISIBLE);
        emptyState.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        
        // Fetch tickets
        ticketRepository.getTickets(new Callback() {
            @Override
            public void onSuccess(List<Ticket> tickets) {
                skeleton.setVisibility(View.GONE);
                
                if (tickets.isEmpty()) {
                    // Show empty state
                    emptyState.show();
                    recyclerView.setVisibility(View.GONE);
                } else {
                    // Show tickets
                    emptyState.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setTickets(tickets);
                }
            }
            
            @Override
            public void onError(Exception e) {
                skeleton.setVisibility(View.GONE);
                // Show error state (could use EmptyStateView for errors too)
                showErrorState(e);
            }
        });
    }
}
```
