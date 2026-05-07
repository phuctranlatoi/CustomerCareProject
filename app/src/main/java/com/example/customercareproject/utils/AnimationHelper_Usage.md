# AnimationHelper Usage Guide

## Overview

`AnimationHelper` is a utility class that provides centralized animation functions for consistent motion across the Customer Care Android app. All animations follow Material Design 3 Motion guidelines with Material Motion easing curves.

## Animation Duration Constants

The class provides four standard duration constants:

- **DURATION_FAST** (100ms): For micro-interactions like button presses
- **DURATION_STANDARD** (200ms): For button presses and ripples
- **DURATION_MEDIUM** (300ms): For screen transitions
- **DURATION_SLOW** (400ms): For complex animations

## Easing Curves

All animations use the Material Motion standard easing curve (cubic-bezier(0.4, 0.0, 0.2, 1)), implemented using `FastOutSlowInInterpolator` in Android.

## Available Animations

### 1. Fade Animations

#### Fade In
Animates a view from transparent to opaque.

```java
// With default duration (300ms)
AnimationHelper.fadeIn(myView);

// With custom duration
AnimationHelper.fadeIn(myView, AnimationHelper.DURATION_FAST);
```

**Use cases:**
- Loading content after data fetch
- Showing success messages
- Revealing hidden UI elements

#### Fade Out
Animates a view from opaque to transparent and sets visibility to GONE.

```java
// With default duration (300ms)
AnimationHelper.fadeOut(myView);

// With custom duration
AnimationHelper.fadeOut(myView, AnimationHelper.DURATION_STANDARD);
```

**Use cases:**
- Hiding error messages
- Dismissing notifications
- Removing UI elements

### 2. Scale Animations

#### Scale Press
Scales a view down to 0.98 when pressed (micro-interaction).

```java
button.setOnTouchListener((v, event) -> {
    if (event.getAction() == MotionEvent.ACTION_DOWN) {
        AnimationHelper.scalePress(v);
    } else if (event.getAction() == MotionEvent.ACTION_UP || 
               event.getAction() == MotionEvent.ACTION_CANCEL) {
        AnimationHelper.scaleRelease(v);
    }
    return false;
});
```

**Use cases:**
- Button press feedback
- Card press interactions
- Interactive elements

#### Scale Release
Scales a view back to normal size (1.0).

```java
AnimationHelper.scaleRelease(myButton);
```

### 3. Slide Animations

#### Slide Up
Slides a view up from below with fade-in effect.

```java
// With default duration (300ms)
AnimationHelper.slideUp(bottomSheet);

// With custom duration
AnimationHelper.slideUp(dialog, AnimationHelper.DURATION_SLOW);
```

**Use cases:**
- Opening bottom sheets
- Showing dialogs
- Revealing panels from bottom

#### Slide Down
Slides a view down and fades out, then sets visibility to GONE.

```java
// With default duration (300ms)
AnimationHelper.slideDown(bottomSheet);

// With custom duration
AnimationHelper.slideDown(dialog, AnimationHelper.DURATION_MEDIUM);
```

**Use cases:**
- Dismissing bottom sheets
- Closing dialogs
- Hiding panels

### 4. List Item Animation

Animates list items with fade-in and stagger effect (50ms delay per item).

```java
@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    // Bind data...
    
    // Animate item
    AnimationHelper.animateListItem(holder.itemView, position);
}
```

**Use cases:**
- RecyclerView item animations
- List loading animations
- Staggered content reveal

### 5. Shared Element Transitions

Sets up Material Container Transform for shared element transitions between activities.

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    // Call BEFORE setContentView()
    AnimationHelper.setupSharedElementTransition(this);
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
}
```

**Use cases:**
- Activity transitions with shared elements
- Card to detail screen transitions
- Image expansion animations

### 6. Utility Animations

#### Shake
Shakes a view horizontally to indicate an error.

```java
if (!isValidEmail(email)) {
    AnimationHelper.shake(emailField);
    emailField.setError("Invalid email");
}
```

**Use cases:**
- Form validation errors
- Invalid input feedback
- Error indication

#### Bounce
Creates a bounce effect by scaling up and down.

```java
AnimationHelper.bounce(successIcon);
```

**Use cases:**
- Success confirmations
- Achievement unlocks
- Positive feedback

#### Rotate
Rotates a view by specified degrees.

```java
// Rotate 180 degrees clockwise
AnimationHelper.rotate(arrowIcon, 180f, AnimationHelper.DURATION_STANDARD);

// Rotate back
AnimationHelper.rotate(arrowIcon, 0f, AnimationHelper.DURATION_STANDARD);
```

**Use cases:**
- Expanding/collapsing sections
- Dropdown indicators
- Icon state changes

#### Pulse
Creates a pulsing effect by scaling up and down repeatedly.

```java
// Pulse 3 times
AnimationHelper.pulse(notificationBadge, 3);

// Pulse infinitely
AnimationHelper.pulse(loadingIndicator, -1);
```

**Use cases:**
- Drawing attention to elements
- Loading indicators
- Notification badges

#### Cancel Animations
Cancels all running animations on a view.

```java
AnimationHelper.cancelAnimations(myView);
```

#### Reset View
Resets a view to default state (alpha=1, scale=1, translation=0).

```java
AnimationHelper.resetView(myView);
```

## Complete Examples

### Example 1: Animated Button Click

```java
MaterialButton button = findViewById(R.id.btnSubmit);

button.setOnTouchListener((v, event) -> {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            AnimationHelper.scalePress(v);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            AnimationHelper.scaleRelease(v);
            break;
    }
    return false;
});

button.setOnClickListener(v -> {
    // Handle click
    submitForm();
});
```

### Example 2: Bottom Sheet with Slide Animation

```java
private void showBottomSheet() {
    View bottomSheet = findViewById(R.id.bottomSheet);
    AnimationHelper.slideUp(bottomSheet);
}

private void hideBottomSheet() {
    View bottomSheet = findViewById(R.id.bottomSheet);
    AnimationHelper.slideDown(bottomSheet);
}
```

### Example 3: Form Validation with Shake

```java
private void validateAndSubmit() {
    TextInputLayout emailLayout = findViewById(R.id.tilEmail);
    String email = emailLayout.getEditText().getText().toString();
    
    if (!isValidEmail(email)) {
        AnimationHelper.shake(emailLayout);
        emailLayout.setError("Please enter a valid email");
        return;
    }
    
    // Submit form
    submitForm();
}
```

### Example 4: Success Feedback

```java
private void onFormSubmitSuccess() {
    ImageView successIcon = findViewById(R.id.ivSuccess);
    
    // Show success icon with fade in
    AnimationHelper.fadeIn(successIcon);
    
    // Bounce for emphasis
    successIcon.postDelayed(() -> {
        AnimationHelper.bounce(successIcon);
    }, 100);
    
    // Hide after 2 seconds
    successIcon.postDelayed(() -> {
        AnimationHelper.fadeOut(successIcon);
    }, 2000);
}
```

### Example 5: RecyclerView with Staggered Animation

```java
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data
        holder.bind(items.get(position));
        
        // Animate item with stagger effect
        AnimationHelper.animateListItem(holder.itemView, position);
    }
    
    // ... other methods
}
```

### Example 6: Expandable Card

```java
private boolean isExpanded = false;

cardView.setOnClickListener(v -> {
    ImageView arrowIcon = v.findViewById(R.id.ivArrow);
    View expandedContent = v.findViewById(R.id.expandedContent);
    
    if (isExpanded) {
        // Collapse
        AnimationHelper.rotate(arrowIcon, 0f, AnimationHelper.DURATION_STANDARD);
        AnimationHelper.fadeOut(expandedContent, AnimationHelper.DURATION_STANDARD);
    } else {
        // Expand
        AnimationHelper.rotate(arrowIcon, 180f, AnimationHelper.DURATION_STANDARD);
        AnimationHelper.fadeIn(expandedContent, AnimationHelper.DURATION_STANDARD);
    }
    
    isExpanded = !isExpanded;
});
```

## Best Practices

1. **Use appropriate durations**: Choose the right duration constant for your use case
   - Fast (100ms): Micro-interactions
   - Standard (200ms): Button presses
   - Medium (300ms): Screen transitions
   - Slow (400ms): Complex animations

2. **Don't over-animate**: Too many animations can be distracting. Use animations purposefully to enhance UX.

3. **Test on low-end devices**: Ensure animations run smoothly on all target devices.

4. **Respect accessibility settings**: Consider users who have reduced motion enabled in system settings.

5. **Chain animations carefully**: When chaining multiple animations, use `postDelayed()` or animation listeners to sequence them properly.

6. **Clean up**: Cancel animations when views are destroyed or recycled to prevent memory leaks.

## Requirements Mapping

This AnimationHelper implementation satisfies the following requirements from the Modern UI Redesign spec:

- **Requirement 5.1**: Screen navigation transitions (300ms duration)
- **Requirement 5.2**: Bottom sheet and dialog animations (slide-up with deceleration)
- **Requirement 5.3**: List item animations with stagger effect (50ms delay)
- **Requirement 5.6**: Material Motion easing curves (cubic-bezier(0.4, 0.0, 0.2, 1))

## Related Components

- **ThemeManager**: For theme management and dark mode support
- **Material3Button**: Custom button component with built-in animations
- **Material3Card**: Custom card component with hover effects
- **Material3TextField**: Custom text field with validation animations

## Support

For questions or issues with AnimationHelper, please refer to the Material Design 3 Motion guidelines:
https://m3.material.io/styles/motion/overview
