# Design Document: Modern UI Redesign

## Overview

This design document outlines the technical approach for modernizing the Customer Care Android application's user interface. The redesign focuses on implementing Material Design 3 (Material You), enhancing visual aesthetics, adding smooth animations, and improving overall user experience across three user roles: Customers, KTV (Technicians), and Admins.

### Goals

- Implement Material Design 3 design system with dynamic color support
- Create a cohesive, modern visual language across all screens
- Add smooth animations and micro-interactions for better user engagement
- Improve accessibility and responsive layout support
- Maintain existing functionality while enhancing visual presentation

### Non-Goals

- Changing core business logic or data models
- Adding new features beyond UI/UX improvements
- Modifying backend APIs or Firebase structure
- Rewriting the app in a different framework (staying with Java/Android)

## Architecture

### High-Level Architecture

The UI redesign will follow a layered architecture approach:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │   Activities │  │   Fragments  │  │   Adapters   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                   UI Component Layer                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Theme System │  │  Animation   │  │   Custom     │  │
│  │              │  │   Manager    │  │  Components  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                  Material Design 3 Layer                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ Color System │  │  Typography  │  │    Shape     │  │
│  │              │  │    System    │  │   System     │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────┐
│                    Android Framework                     │
│         (Material Components, ConstraintLayout)          │
└─────────────────────────────────────────────────────────┘
```

### Design Principles

1. **Material Design 3 First**: All UI components will follow Material Design 3 specifications
2. **Progressive Enhancement**: Enhance existing screens incrementally without breaking functionality
3. **Reusability**: Create reusable components and styles to maintain consistency
4. **Performance**: Optimize animations and transitions to maintain 60fps
5. **Accessibility**: Ensure all UI elements meet WCAG AA standards

## Components and Interfaces

### 1. Theme System

#### ThemeManager
Manages application theme (light/dark mode) and dynamic colors.

**Responsibilities:**
- Detect system theme preference
- Apply Material Design 3 color schemes
- Support dynamic color extraction from wallpaper (Android 12+)
- Persist user theme preference

**Key Methods:**
```java
public class ThemeManager {
    public static void applyTheme(Context context, boolean isDarkMode);
    public static boolean isDarkModeEnabled(Context context);
    public static void saveDarkModePreference(Context context, boolean isDarkMode);
    public static boolean supportsDynamicColors();
    public static void applyDynamicColors(Activity activity);
}
```

**Resources:**
- `res/values/themes.xml` - Light theme definitions
- `res/values-night/themes.xml` - Dark theme definitions
- `res/values/colors.xml` - Color palette with MD3 tokens

### 2. Animation System

#### AnimationHelper
Centralized animation utilities for consistent motion across the app.

**Responsibilities:**
- Provide reusable animation functions
- Implement Material Motion easing curves
- Handle shared element transitions
- Manage animation durations and interpolators

**Key Methods:**
```java
public class AnimationHelper {
    // Fade animations
    public static void fadeIn(View view, int duration);
    public static void fadeOut(View view, int duration);
    
    // Scale animations
    public static void scalePress(View view);
    public static void scaleRelease(View view);
    
    // Slide animations
    public static void slideUp(View view, int duration);
    public static void slideDown(View view, int duration);
    
    // List animations
    public static void animateListItem(View view, int position);
    
    // Shared element transitions
    public static void setupSharedElementTransition(Activity activity);
}
```

**Animation Constants:**
- Fast: 100ms (micro-interactions)
- Standard: 200ms (button presses, ripples)
- Medium: 300ms (screen transitions)
- Slow: 400ms (complex animations)

### 3. Custom Components

#### Material3Button
Extended MaterialButton with consistent styling and animations.

**Features:**
- Automatic ripple effect
- Press animation (scale to 0.98)
- Loading state with progress indicator
- Disabled state styling

#### Material3Card
Extended CardView with MD3 styling.

**Features:**
- Rounded corners (configurable: 8dp, 12dp, 16dp, 28dp)
- Elevation with surface tints
- Hover effect (scale animation)
- Clickable state with ripple

#### Material3TextField
Extended TextInputLayout with enhanced validation.

**Features:**
- Floating label animation
- Inline error messages with shake animation
- Character counter
- Password strength indicator (for password fields)

#### SkeletonView
Custom view for skeleton loading states.

**Features:**
- Shimmer effect animation
- Configurable shape (rectangle, circle)
- Gradient animation

#### EmptyStateView
Custom view for empty states.

**Features:**
- Illustration display
- Title and description text
- Action button
- Fade-in animation

### 4. Layout Components

#### ResponsiveLayoutHelper
Utility for responsive layout management.

**Responsibilities:**
- Detect screen size breakpoints
- Provide spacing values based on screen size
- Support landscape orientation
- Handle two-pane layouts for tablets

**Key Methods:**
```java
public class ResponsiveLayoutHelper {
    public enum ScreenSize { COMPACT, MEDIUM, EXPANDED }
    
    public static ScreenSize getScreenSize(Context context);
    public static int getResponsiveSpacing(Context context);
    public static boolean isTablet(Context context);
    public static boolean isLandscape(Context context);
}
```

### 5. Color System

#### Material Design 3 Color Tokens

**Primary Colors:**
- `primary` - Main brand color
- `on_primary` - Text/icons on primary
- `primary_container` - Containers using primary
- `on_primary_container` - Text on primary containers

**Secondary Colors:**
- `secondary` - Accent color
- `on_secondary` - Text/icons on secondary
- `secondary_container` - Containers using secondary
- `on_secondary_container` - Text on secondary containers

**Surface Colors:**
- `surface` - Default surface color
- `surface_variant` - Variant surface color
- `surface_container` - Container surfaces
- `surface_container_high` - Elevated containers
- `surface_container_highest` - Highest elevation

**Semantic Colors:**
- `success` (#10B981) - Success states
- `warning` (#F59E0B) - Warning states
- `error` (#EF4444) - Error states
- `info` (#3B82F6) - Informational states

**Dynamic Color Support:**
For Android 12+ devices, colors will be extracted from system wallpaper using `DynamicColors.applyToActivityIfAvailable()`.

### 6. Typography System

#### Text Styles

**Display:**
- Display Large: 57sp, Regular
- Display Medium: 45sp, Regular
- Display Small: 36sp, Regular

**Headline:**
- Headline Large: 32sp, Regular
- Headline Medium: 28sp, Regular
- Headline Small: 24sp, Regular

**Title:**
- Title Large: 22sp, Medium (500)
- Title Medium: 16sp, Medium (500)
- Title Small: 14sp, Medium (500)

**Body:**
- Body Large: 16sp, Regular, line height 24sp
- Body Medium: 14sp, Regular, line height 20sp
- Body Small: 12sp, Regular, line height 16sp

**Label:**
- Label Large: 14sp, Medium (500)
- Label Medium: 12sp, Medium (500)
- Label Small: 11sp, Medium (500)

**Implementation:**
All text styles will be defined in `res/values/styles.xml` and applied using `android:textAppearance`.

### 7. Shape System

#### Corner Radius Tokens

- Extra Small: 4dp (chips, small buttons)
- Small: 8dp (cards, text fields)
- Medium: 12dp (images, medium cards)
- Large: 16dp (large cards, dialogs)
- Extra Large: 28dp (bottom sheets, FABs)

**Implementation:**
Shape styles will be defined in `res/values/shapes.xml` using `<shape>` drawables.

## Data Models

No new data models are required for this UI redesign. Existing models will remain unchanged:

- `NguoiDung` (User)
- `YeuCauHoTro` (Support Request/Ticket)
- `TinNhan` (Message)
- `SanPham` (Product)
- `DanhGia` (Rating)
- `LoiPhatSinh` (Issue)
- `GoiDangKy` (Subscription Package)
- `LeadKinhDoanh` (Business Lead)

## Error Handling

### Error State Patterns

#### 1. Inline Errors (Form Validation)
- Display error message below input field
- Use error color (#EF4444)
- Apply shake animation to field
- Show error icon

**Implementation:**
```java
TextInputLayout.setError("Error message");
AnimationHelper.shake(textInputLayout);
```

#### 2. Snackbar Errors (Temporary)
- Display at bottom of screen
- Auto-dismiss after 4 seconds
- Include action button if applicable
- Color-coded by severity

**Types:**
- Error: Red background, white text
- Warning: Orange background, dark text
- Success: Green background, white text
- Info: Blue background, white text

#### 3. Empty States
- Display illustration
- Show descriptive message
- Provide action button (e.g., "Retry", "Add Item")
- Fade-in animation

#### 4. Network Errors
- Display error illustration
- Show error message
- Provide "Retry" button
- Cache last successful data when possible

#### 5. Loading Errors
- Replace skeleton screen with error state
- Show error message
- Provide "Retry" button

### Error Handling Strategy

1. **Graceful Degradation**: Show cached data when network fails
2. **User Feedback**: Always inform user of errors with clear messages
3. **Recovery Actions**: Provide actionable buttons (Retry, Dismiss, etc.)
4. **Logging**: Log errors for debugging (Firebase Crashlytics)

## Testing Strategy

### Testing Approach

Since this is a UI/UX redesign focused on visual presentation, animations, and styling, **property-based testing is NOT applicable**. Instead, we will use:

1. **Snapshot Testing**: Capture and compare UI screenshots
2. **Visual Regression Testing**: Detect unintended visual changes
3. **UI Instrumentation Tests**: Verify user interactions and animations
4. **Manual Testing**: Validate visual design and user experience
5. **Accessibility Testing**: Verify TalkBack and contrast ratios

### 1. Snapshot Testing

**Tool**: Screenshot testing library (e.g., Shot, Paparazzi)

**Approach:**
- Capture screenshots of each screen in light and dark themes
- Compare against baseline images
- Flag any visual differences for review

**Test Coverage:**
- All major screens (Login, Home, Chat, Dashboard, etc.)
- Different states (empty, loading, error, success)
- Light and dark themes
- Different screen sizes (phone, tablet)

**Example Test:**
```java
@Test
public void testLoginScreenAppearance() {
    // Arrange
    launchActivity(LoginActivity.class);
    
    // Act & Assert
    compareScreenshot("login_screen_light");
}

@Test
public void testLoginScreenDarkMode() {
    // Arrange
    enableDarkMode();
    launchActivity(LoginActivity.class);
    
    // Act & Assert
    compareScreenshot("login_screen_dark");
}
```

### 2. UI Instrumentation Tests

**Tool**: Espresso

**Approach:**
- Test user interactions (button clicks, text input, scrolling)
- Verify animations complete successfully
- Check visibility of UI elements
- Validate navigation flows

**Test Coverage:**
- Button press animations
- Screen transitions
- Form validation and error display
- Loading states
- Empty states

**Example Test:**
```java
@Test
public void testButtonPressAnimation() {
    // Arrange
    onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    
    // Act
    onView(withId(R.id.btnLogin)).perform(click());
    
    // Assert - verify button scales down then up
    // (Animation verification would use IdlingResource)
}

@Test
public void testFormValidationError() {
    // Arrange
    onView(withId(R.id.edtEmail)).perform(typeText("invalid-email"));
    
    // Act
    onView(withId(R.id.btnLogin)).perform(click());
    
    // Assert
    onView(withId(R.id.edtEmail))
        .check(matches(hasErrorText("Invalid email format")));
}
```

### 3. Accessibility Testing

**Tool**: Accessibility Scanner, TalkBack

**Approach:**
- Verify content descriptions for all interactive elements
- Test with TalkBack screen reader
- Validate touch target sizes (minimum 48dp)
- Check color contrast ratios (WCAG AA)
- Test text scaling up to 200%

**Test Coverage:**
- All interactive elements have content descriptions
- Focus order is logical
- Touch targets meet minimum size
- Text remains readable at 200% scale
- Color contrast meets WCAG AA (4.5:1 for text)

**Example Test:**
```java
@Test
public void testAccessibilityContentDescriptions() {
    // Verify all ImageButtons have content descriptions
    onView(withId(R.id.btnProfile))
        .check(matches(withContentDescription("Tài khoản")));
    
    onView(withId(R.id.btnLogout))
        .check(matches(withContentDescription("Đăng xuất")));
}

@Test
public void testMinimumTouchTargetSize() {
    // Verify buttons meet 48dp minimum
    onView(withId(R.id.btnLogin))
        .check(matches(withMinimumSize(48, 48)));
}
```

### 4. Animation Testing

**Approach:**
- Use IdlingResource to wait for animations
- Verify animation durations
- Check animation states (start, end)
- Test animation cancellation

**Example Test:**
```java
@Test
public void testFadeInAnimation() {
    // Register animation idling resource
    IdlingRegistry.getInstance().register(animationIdlingResource);
    
    // Trigger animation
    onView(withId(R.id.cardView)).perform(click());
    
    // Verify view is visible after animation
    onView(withId(R.id.detailView)).check(matches(isDisplayed()));
    
    // Unregister
    IdlingRegistry.getInstance().unregister(animationIdlingResource);
}
```

### 5. Theme Testing

**Approach:**
- Test light and dark themes
- Verify dynamic color support
- Check theme persistence

**Example Test:**
```java
@Test
public void testDarkModeToggle() {
    // Enable dark mode
    ThemeManager.applyTheme(context, true);
    
    // Verify dark colors are applied
    int surfaceColor = getColorFromTheme(R.attr.colorSurface);
    assertEquals(darkSurfaceColor, surfaceColor);
}
```

### 6. Responsive Layout Testing

**Approach:**
- Test on different screen sizes (phone, tablet)
- Test landscape orientation
- Verify spacing adjustments

**Example Test:**
```java
@Test
public void testTabletLayout() {
    // Simulate tablet screen size
    setScreenSize(ScreenSize.EXPANDED);
    
    // Verify two-pane layout is displayed
    onView(withId(R.id.masterPane)).check(matches(isDisplayed()));
    onView(withId(R.id.detailPane)).check(matches(isDisplayed()));
}
```

### 7. Manual Testing Checklist

**Visual Design:**
- [ ] Colors match Material Design 3 specifications
- [ ] Typography is consistent across screens
- [ ] Spacing and alignment are correct
- [ ] Icons are properly sized and colored
- [ ] Images have correct corner radius

**Animations:**
- [ ] Screen transitions are smooth (no jank)
- [ ] Button press animations feel responsive
- [ ] Loading animations are visible and smooth
- [ ] Micro-interactions provide appropriate feedback

**Themes:**
- [ ] Light theme looks good
- [ ] Dark theme looks good
- [ ] Dynamic colors work on Android 12+
- [ ] Theme preference persists across sessions

**Accessibility:**
- [ ] TalkBack reads all elements correctly
- [ ] Focus order is logical
- [ ] Touch targets are large enough
- [ ] Text is readable at 200% scale
- [ ] Color contrast is sufficient

**Responsive Design:**
- [ ] Layouts work on small phones (< 5")
- [ ] Layouts work on large phones (> 6")
- [ ] Layouts work on tablets
- [ ] Landscape orientation works correctly

### Test Execution Strategy

1. **Unit Tests**: Run on every commit (CI/CD)
2. **Instrumentation Tests**: Run on every PR
3. **Snapshot Tests**: Run weekly and on major UI changes
4. **Manual Testing**: Perform before each release
5. **Accessibility Testing**: Perform monthly and before releases

### Success Criteria

- All instrumentation tests pass
- No visual regressions in snapshot tests
- Accessibility Scanner reports no critical issues
- Manual testing checklist 100% complete
- App maintains 60fps during animations
- No crashes related to UI changes

## Implementation Plan

### Phase 1: Foundation (Week 1-2)

1. **Setup Material Design 3**
   - Update dependencies to Material Components 1.9.0+
   - Define color system in `colors.xml`
   - Create theme files (`themes.xml`, `themes.xml (night)`)
   - Implement ThemeManager

2. **Typography System**
   - Define text styles in `styles.xml`
   - Update all TextViews to use text appearances
   - Test text scaling

3. **Shape System**
   - Define shape drawables
   - Create reusable background resources
   - Apply to existing components

### Phase 2: Core Components (Week 3-4)

1. **Custom Components**
   - Implement Material3Button
   - Implement Material3Card
   - Implement Material3TextField
   - Implement SkeletonView
   - Implement EmptyStateView

2. **Animation System**
   - Create AnimationHelper utility
   - Implement fade, scale, slide animations
   - Add shared element transitions
   - Test animation performance

### Phase 3: Screen Updates (Week 5-8)

1. **Authentication Screens**
   - Update LoginActivity
   - Update RegisterActivity
   - Update ForgotPasswordActivity
   - Add onboarding screens

2. **Customer Screens**
   - Update HomeActivity
   - Update ChatKhachHangActivity
   - Update DanhGiaActivity
   - Update YeuCauHoTroActivity

3. **KTV Screens**
   - Update KtvDashboardActivity
   - Update KtvChatActivity
   - Update KtvTicketDetailActivity

4. **Admin Screens**
   - Update AdminDashboardActivity
   - Update all admin fragments
   - Update data visualization

### Phase 4: Polish & Testing (Week 9-10)

1. **Micro-interactions**
   - Add button press animations
   - Add list item animations
   - Add form validation animations
   - Add success/error animations

2. **Testing**
   - Write instrumentation tests
   - Capture snapshot tests
   - Perform accessibility testing
   - Manual testing on multiple devices

3. **Performance Optimization**
   - Profile animation performance
   - Optimize layout hierarchies
   - Reduce overdraw
   - Test on low-end devices

### Phase 5: Release (Week 11)

1. **Final QA**
   - Complete manual testing checklist
   - Fix any remaining bugs
   - Update documentation

2. **Deployment**
   - Create release build
   - Submit to Play Store (internal testing)
   - Monitor crash reports
   - Gather user feedback

## Dependencies

### Required Libraries

```gradle
dependencies {
    // Material Design 3
    implementation 'com.google.android.material:material:1.11.0'
    
    // ConstraintLayout for responsive layouts
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    
    // ViewPager2 for onboarding
    implementation 'androidx.viewpager2:viewpager2:1.0.0'
    
    // Lottie for complex animations (optional)
    implementation 'com.airbnb.android:lottie:6.1.0'
    
    // Glide for image loading (already included)
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    
    // Testing
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
    androidTestImplementation 'androidx.test.espresso:espresso-contrib:3.5.1'
    androidTestImplementation 'androidx.test:rules:1.5.0'
    androidTestImplementation 'androidx.test:runner:1.5.2'
    
    // Screenshot testing (optional)
    androidTestImplementation 'com.facebook.testing.screenshot:core:0.15.0'
}
```

### Minimum SDK Requirements

- Minimum SDK: 26 (Android 8.0) - Current
- Target SDK: 35 (Android 15) - Current
- Compile SDK: 35 - Current

### Device Support

- Phone: All screen sizes (small to extra large)
- Tablet: 7" and 10" tablets
- Orientation: Portrait and landscape
- Density: All densities (ldpi to xxxhdpi)

## Risk Assessment

### Technical Risks

1. **Animation Performance**
   - Risk: Animations may cause jank on low-end devices
   - Mitigation: Profile on low-end devices, use hardware acceleration, simplify animations if needed

2. **Theme Compatibility**
   - Risk: Dynamic colors may not work well with all wallpapers
   - Mitigation: Provide fallback static colors, allow manual theme selection

3. **Layout Complexity**
   - Risk: Responsive layouts may be complex to maintain
   - Mitigation: Use ConstraintLayout, create reusable layout components

4. **Testing Coverage**
   - Risk: Visual bugs may slip through automated tests
   - Mitigation: Comprehensive manual testing, snapshot tests, multiple device testing

### User Experience Risks

1. **Learning Curve**
   - Risk: Users may need time to adapt to new UI
   - Mitigation: Maintain familiar navigation patterns, provide onboarding

2. **Accessibility**
   - Risk: New UI may have accessibility issues
   - Mitigation: Test with TalkBack, follow WCAG guidelines, user testing

3. **Performance Perception**
   - Risk: Animations may make app feel slower
   - Mitigation: Keep animations fast (< 300ms), allow disabling animations

## Success Metrics

### Quantitative Metrics

1. **Performance**
   - Maintain 60fps during animations (measured with GPU profiling)
   - App startup time < 2 seconds
   - Screen transition time < 300ms

2. **Accessibility**
   - 100% of interactive elements have content descriptions
   - All text meets WCAG AA contrast ratio (4.5:1)
   - All touch targets ≥ 48dp

3. **Test Coverage**
   - 80%+ code coverage for UI components
   - 100% of major screens have snapshot tests
   - Zero critical accessibility issues

### Qualitative Metrics

1. **User Feedback**
   - Positive feedback on visual design
   - Improved app store ratings
   - Reduced UI-related support tickets

2. **Design Quality**
   - Consistent with Material Design 3 guidelines
   - Cohesive visual language across all screens
   - Smooth, natural animations

## Conclusion

This design document provides a comprehensive plan for modernizing the Customer Care Android application's UI. By implementing Material Design 3, adding smooth animations, and improving accessibility, we will create a more engaging and professional user experience while maintaining all existing functionality.

The phased implementation approach allows for incremental progress and testing, reducing risk and ensuring quality. The focus on reusable components and consistent design patterns will make the codebase more maintainable and extensible for future enhancements.
