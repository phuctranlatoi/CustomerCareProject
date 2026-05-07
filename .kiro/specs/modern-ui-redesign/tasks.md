# Implementation Plan: Modern UI Redesign

## Overview

This implementation plan transforms the Customer Care Android application with a modern Material Design 3 interface. The work is organized into 5 phases following the design document: Foundation setup, Core components, Screen updates, Polish & testing, and Release preparation. Each task builds incrementally to ensure the app remains functional throughout the redesign process.

## Tasks

- [ ] 1. Phase 1: Foundation - Material Design 3 Setup
  - [x] 1.1 Update Material Design 3 dependencies and configuration
    - Update `app/build.gradle.kts` to use Material Components 1.11.0+
    - Update compile SDK and target SDK if needed
    - Sync Gradle and verify no dependency conflicts
    - _Requirements: 1.1, 1.5_

  - [x] 1.2 Implement Material Design 3 color system
    - Create `res/values/colors.xml` with MD3 color tokens (primary, secondary, surface, semantic colors)
    - Define color shades (50-900) for primary and secondary colors
    - Add semantic colors (success, warning, error, info)
    - Ensure WCAG AA contrast ratios (4.5:1 for text)
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.6_

  - [x] 1.3 Create theme files for light and dark modes
    - Create `res/values/themes.xml` with light theme using MD3 color tokens
    - Create `res/values-night/themes.xml` with dark theme colors
    - Apply Material Design 3 theme attributes (colorPrimary, colorSurface, etc.)
    - _Requirements: 1.1, 4.1, 4.3_

  - [x] 1.4 Implement ThemeManager utility class
    - Create `utils/ThemeManager.java` with theme management methods
    - Implement `applyTheme()` to switch between light/dark modes
    - Implement `isDarkModeEnabled()` to detect system theme preference
    - Implement `saveDarkModePreference()` to persist user choice
    - Implement `supportsDynamicColors()` and `applyDynamicColors()` for Android 12+
    - _Requirements: 2.5, 4.1, 4.2, 4.5_

  - [x] 1.5 Define typography system
    - Create `res/values/styles.xml` with MD3 text styles (Display, Headline, Title, Body, Label)
    - Define font weights (Regular: 400, Medium: 500, Bold: 700)
    - Set line heights and letter spacing per MD3 specifications
    - Test text scaling up to 200% for accessibility
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [x] 1.6 Define shape system
    - Create `res/values/shapes.xml` with corner radius tokens (4dp, 8dp, 12dp, 16dp, 28dp)
    - Create reusable shape drawables for cards, buttons, text fields
    - Apply MD3 shape system to existing components
    - _Requirements: 1.4_

- [ ] 2. Phase 2: Core Components - Custom UI Components
  - [x] 2.1 Create AnimationHelper utility class
    - Create `utils/AnimationHelper.java` with animation constants (fast: 100ms, standard: 200ms, medium: 300ms, slow: 400ms)
    - Implement fade animations (`fadeIn()`, `fadeOut()`)
    - Implement scale animations (`scalePress()`, `scaleRelease()`)
    - Implement slide animations (`slideUp()`, `slideDown()`)
    - Implement list item stagger animation (`animateListItem()`)
    - Implement shared element transition setup (`setupSharedElementTransition()`)
    - Use Material Motion easing curves (cubic-bezier(0.4, 0.0, 0.2, 1))
    - _Requirements: 5.1, 5.2, 5.3, 5.6_

  - [x] 2.2 Create Material3Button custom component
    - Create custom button class extending MaterialButton
    - Add automatic ripple effect with 200ms duration
    - Implement press animation (scale to 0.98)
    - Add loading state with circular progress indicator
    - Style disabled state with proper opacity
    - _Requirements: 1.5, 5.4, 6.1_

  - [x] 2.3 Create Material3Card custom component
    - Create custom card class extending CardView
    - Apply configurable rounded corners (8dp, 12dp, 16dp, 28dp)
    - Implement elevation with surface tints (MD3 style)
    - Add hover effect with scale animation
    - Add clickable state with ripple effect
    - _Requirements: 1.3, 1.4, 1.5, 6.1, 8.3_

  - [x] 2.4 Create Material3TextField custom component
    - Create custom text field class extending TextInputLayout
    - Implement floating label animation
    - Add inline error message display with red color and shake animation
    - Add character counter support
    - Implement password strength indicator for password fields
    - _Requirements: 1.5, 7.2, 7.3, 7.4, 17.3_

  - [x] 2.5 Create SkeletonView custom component
    - Create custom view for skeleton loading states
    - Implement shimmer effect animation
    - Support configurable shapes (rectangle, circle)
    - Add gradient animation for loading effect
    - _Requirements: 10.5, 16.1_

  - [x] 2.6 Create EmptyStateView custom component
    - Create custom view for empty states
    - Support illustration display
    - Add title and description text with proper typography
    - Add action button with click listener
    - Implement fade-in animation when displayed
    - _Requirements: 10.4, 17.1_

  - [x] 2.7 Create ResponsiveLayoutHelper utility class
    - Create `utils/ResponsiveLayoutHelper.java` with screen size detection
    - Implement `getScreenSize()` to detect compact/medium/expanded breakpoints
    - Implement `getResponsiveSpacing()` to return spacing based on screen size
    - Implement `isTablet()` and `isLandscape()` helper methods
    - _Requirements: 14.1, 14.2, 14.3, 14.4, 14.5_

- [x] 3. Checkpoint - Verify foundation and core components
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 4. Phase 3: Screen Updates - Authentication Screens
  - [x] 4.1 Update LoginActivity with Material Design 3
    - Update `activity_login.xml` layout with MD3 components
    - Add hero illustration or gradient background
    - Replace text fields with Material3TextField (outlined style with floating labels)
    - Update buttons to use Material3Button
    - Apply fade-in animation when screen loads
    - Display inline error messages for invalid email
    - _Requirements: 7.1, 7.2, 7.4, 7.5, 17.3_

  - [x] 4.2 Update RegisterActivity with Material Design 3
    - Update `activity_register.xml` layout with MD3 components
    - Replace text fields with Material3TextField
    - Add password strength indicator for password field
    - Update buttons to use Material3Button
    - Apply fade-in animation when screen loads
    - Display inline validation errors
    - _Requirements: 7.2, 7.3, 7.4, 7.5_

  - [x] 4.3 Update ForgotPasswordActivity with Material Design 3
    - Update `activity_forgot_password.xml` layout with MD3 components
    - Replace text fields with Material3TextField
    - Update buttons to use Material3Button
    - Apply fade-in animation when screen loads
    - _Requirements: 7.1, 7.2, 7.5_

  - [ ] 4.4 Create onboarding screens (optional)
    - Create onboarding layouts with illustrations and descriptions
    - Implement ViewPager2 for swipeable screens
    - Add page indicator dots to show progress
    - Add skip button to bypass onboarding
    - Apply smooth page transition animations
    - Display only on first app launch
    - _Requirements: 25.1, 25.2, 25.3, 25.4, 25.5_

- [ ] 5. Phase 3: Screen Updates - Customer Screens
  - [x] 5.1 Update HomeActivity for customers
    - Update `activity_home.xml` layout with MD3 components
    - Display active ticket banner at top with prominent CTA button
    - Update product cards to use Material3Card in 2-column grid
    - Apply card elevation (2dp) and hover effect
    - Display warning banner for expired subscriptions (orange background)
    - Add FAB (56dp) for quick support request
    - Apply fade-in animation for content
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [x] 5.2 Update SanPhamAdapter with Material Design 3
    - Update `item_san_pham.xml` with Material3Card
    - Add product icons and descriptions with proper typography
    - Apply card elevation and hover animation
    - _Requirements: 8.2, 8.3_

  - [x] 5.3 Update ChatKhachHangActivity with modern chat interface
    - Update `activity_chat_khach_hang.xml` layout
    - Style chat bubbles with rounded corners (18dp) and 8dp spacing
    - Use gradient background for KTV messages, white for customer messages
    - Display message timestamps in small gray text
    - Apply slide-in animation when sending messages
    - Add typing indicator with animated dots
    - Support image preview with rounded corners in chat bubbles
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

  - [x] 5.4 Update chat adapter with message animations
    - Update `ChatAdapter.java` to apply slide-in animations
    - Implement stagger effect for multiple messages (50ms delay)
    - Add typing indicator view
    - _Requirements: 9.4, 5.3_

  - [x] 5.5 Update DanhGiaActivity (rating screen)
    - Update `activity_danh_gia.xml` layout with MD3 components
    - Display star rating with large touch targets (48dp minimum)
    - Apply scale animation when user taps star
    - Use Material3TextField with character counter for feedback
    - Display issue cards in grid layout with icons
    - Apply success animation with checkmark on submission
    - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5_

  - [x] 5.6 Update YeuCauHoTroActivity (support request)
    - Update `activity_yeu_cau_ho_tro.xml` layout with MD3 components
    - Replace text fields with Material3TextField
    - Update buttons to use Material3Button
    - Apply form validation with inline errors
    - _Requirements: 7.2, 7.4, 17.3_

  - [x] 5.7 Update ChiTietLoiActivity (issue detail)
    - Update `activity_chi_tiet_loi.xml` layout with MD3 components
    - Use Material3Card for information sections
    - Apply proper typography hierarchy
    - _Requirements: 1.5, 8.3_

- [ ] 6. Phase 3: Screen Updates - KTV Screens
  - [ ] 6.1 Update KtvDashboardActivity
    - Update `activity_ktv_dashboard.xml` layout with MD3 components
    - Display status badge (Rảnh/Bận/Offline) with colored dot and background
    - Display statistics cards in horizontal row with icons and numbers
    - Use Material Design 3 tabs for ticket filtering with indicator animation
    - Display EmptyStateView when no tickets available
    - Apply SkeletonView loading animation while fetching data
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

  - [ ] 6.2 Update TicketAdapter with Material Design 3
    - Update ticket item layout with Material3Card
    - Apply card elevation and ripple effect
    - Use proper typography for ticket information
    - Add status chips with colored backgrounds
    - _Requirements: 8.3, 12.2_

  - [ ] 6.3 Update KtvChatActivity with modern chat interface
    - Update chat layout with MD3 components
    - Apply same chat bubble styling as customer chat
    - Add template quick reply chips
    - Apply message animations
    - _Requirements: 9.1, 9.2, 9.3, 9.4_

  - [ ] 6.4 Update KtvTicketDetailActivity
    - Update `activity_ktv_ticket_detail.xml` layout with MD3 components
    - Display ticket information in separate Material3Cards (ticket info, customer info, progress notes)
    - Use chips for tags (product, priority) with colored backgrounds
    - Display progress notes timeline with vertical line and timestamps
    - Add quick note chips (Đang kiểm tra, Chờ phản hồi) for fast input
    - Display action buttons (Chat, Call, Close) in bottom bar with icons
    - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 7. Phase 3: Screen Updates - Admin Screens
  - [ ] 7.1 Update AdminDashboardActivity
    - Update `activity_admin_dashboard.xml` layout with MD3 components
    - Display KPI cards in 2x2 grid with large numbers and trend indicators
    - Use horizontal progress bars for satisfaction rate visualization
    - Apply color-coded status indicators (green, orange, red)
    - Apply card elevation and shadow for visual hierarchy
    - Add smooth animation when data loads
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

  - [ ] 7.2 Update AdminThongKeFragment (statistics)
    - Update `fragment_admin_thong_ke.xml` layout with MD3 components
    - Display statistics cards with Material3Card
    - Apply proper typography and spacing
    - Add loading states with SkeletonView
    - _Requirements: 11.1, 11.5, 16.1_

  - [ ] 7.3 Update AdminTicketsFragment
    - Update `fragment_admin_tickets.xml` layout with MD3 components
    - Use Material3Card for ticket items
    - Add filter chips with checkable state
    - Display active filter count badge
    - Apply fade animation when results update
    - _Requirements: 20.3, 20.4, 20.5_

  - [ ] 7.4 Update AdminUsersFragment
    - Update `fragment_admin_users.xml` layout with MD3 components
    - Update user item layout with Material3Card
    - Add search bar with rounded corners (28dp) and search icon
    - Display search suggestions in dropdown
    - _Requirements: 20.1, 20.2_

  - [ ] 7.5 Update AdminKnowledgeFragment
    - Update `fragment_admin_knowledge.xml` layout with MD3 components
    - Use Material3Card for knowledge base items
    - Add FAB for adding new articles
    - _Requirements: 8.5_

  - [ ] 7.6 Update AdminKtvReviewsFragment
    - Update `fragment_admin_ktv_reviews.xml` layout with MD3 components
    - Display review summary cards with Material3Card
    - Use color-coded ratings (green: good, orange: average, red: poor)
    - _Requirements: 11.4_

  - [ ] 7.7 Update AdminPhanTichFragment (analytics)
    - Update `fragment_admin_phan_tich.xml` layout with MD3 components
    - Implement bar charts with rounded corners and colored bars
    - Implement line charts with smooth curves and gradient fill
    - Implement pie charts with percentage labels and legend
    - Apply progressive animation (bars grow from 0)
    - Support chart interaction (tap to show tooltip)
    - _Requirements: 23.1, 23.2, 23.3, 23.4, 23.5_

  - [ ] 7.8 Update AdminGoiDangKyFragment (subscription packages)
    - Update `fragment_admin_goi_dang_ky.xml` layout with MD3 components
    - Display package cards with Material3Card
    - Use proper typography for pricing and features
    - _Requirements: 8.3_

  - [ ] 7.9 Update AdminLeadFragment (business leads)
    - Update `fragment_admin_lead.xml` layout with MD3 components
    - Display lead cards with Material3Card
    - Add status chips with colored backgrounds
    - _Requirements: 12.2_

  - [ ] 7.10 Update AdminDanhGiaXauFragment (poor ratings)
    - Update `fragment_admin_danh_gia_xau.xml` layout with MD3 components
    - Display rating cards with Material3Card
    - Use error color for poor ratings
    - _Requirements: 11.4_

- [ ] 8. Checkpoint - Verify all screen updates
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 9. Phase 4: Polish - Micro-interactions and Animations
  - [ ] 9.1 Add button press animations across all screens
    - Apply scale animation (scale to 0.98) to all buttons on press
    - Use 100ms duration for micro-interaction
    - Apply haptic feedback on long press
    - _Requirements: 6.1, 6.2_

  - [ ] 9.2 Add list item animations
    - Apply fade-in animation to RecyclerView items
    - Implement stagger effect (50ms delay between items)
    - Apply on scroll animations
    - _Requirements: 5.3_

  - [ ] 9.3 Add form validation animations
    - Apply shake animation to fields with validation errors
    - Use 200ms duration for shake effect
    - Display inline error messages with fade-in
    - _Requirements: 17.3, 17.5_

  - [ ] 9.4 Add success/error feedback animations
    - Implement success checkmark animation for form submissions
    - Apply bounce animation for success states
    - Apply shake animation for error states
    - _Requirements: 6.5, 13.5_

  - [ ] 9.5 Add screen transition animations
    - Apply shared element transitions between screens (300ms)
    - Implement slide animations for navigation
    - Add fade transitions for dialogs and bottom sheets
    - _Requirements: 5.1, 5.2_

  - [ ] 9.6 Add pull-to-refresh animations
    - Implement smooth loading animation for pull-to-refresh
    - Use Material Design refresh indicator
    - _Requirements: 5.5_

- [ ] 10. Phase 4: Polish - Bottom Sheets, Dialogs, and Notifications
  - [ ] 10.1 Update all bottom sheets with Material Design 3
    - Apply rounded top corners (28dp) to all bottom sheets
    - Add drag handle at top for easy dismissal
    - Apply backdrop dim (60% opacity)
    - Implement smooth dismiss animation on drag down
    - _Requirements: 19.1, 19.2, 19.3, 19.4_

  - [ ] 10.2 Update all dialogs with Material Design 3
    - Apply rounded corners (28dp) to all dialogs
    - Use Material Design 3 dialog style
    - Apply slide-up animation on open
    - _Requirements: 19.5_

  - [ ] 10.3 Implement in-app notification system
    - Create snackbar component for in-app notifications
    - Use colored backgrounds (success: green, error: red, info: blue, warning: orange)
    - Display notification icon and action button
    - Apply slide-up animation on appear
    - Auto-dismiss after 4 seconds with fade-out
    - _Requirements: 21.1, 21.2, 21.3, 21.4, 21.5_

  - [ ] 10.4 Add swipe-to-dismiss functionality
    - Implement swipe-to-dismiss for list items where applicable
    - Display undo snackbar after dismissal
    - Apply smooth swipe animation
    - _Requirements: 6.3_

- [ ] 11. Phase 4: Polish - Profile, Settings, and Image Handling
  - [ ] 11.1 Update ProfileActivity
    - Update `activity_profile.xml` layout with MD3 components
    - Display user avatar with circular shape (72dp)
    - Group settings into sections with dividers and headers
    - Use list items with icons, titles, and trailing arrows
    - Display toggle switches for boolean settings with smooth animation
    - Apply ripple effect on settings item tap
    - _Requirements: 22.1, 22.2, 22.3, 22.4, 22.5_

  - [ ] 11.2 Implement image handling improvements
    - Display placeholder with shimmer effect while loading images
    - Apply rounded corners (12dp) to all images
    - Implement image caching to reduce loading time
    - Create full-screen image viewer with zoom support
    - Compress uploaded images to maximum 1MB
    - _Requirements: 24.1, 24.2, 24.3, 24.4, 24.5_

  - [ ] 11.3 Add search and filter UI components
    - Create search bar component with rounded corners (28dp) and icon
    - Implement search suggestions dropdown
    - Create filter chips with checkable state
    - Display active filter count badge
    - Apply fade animation when results update
    - _Requirements: 20.1, 20.2, 20.3, 20.4, 20.5_

- [ ] 12. Phase 4: Testing - Write UI Tests
  - [ ] 12.1 Write snapshot tests for all major screens
    - Capture screenshots of Login, Register, Home, Chat, Dashboard screens
    - Test both light and dark themes
    - Test different screen sizes (phone, tablet)
    - Compare against baseline images
    - _Testing: Snapshot testing_

  - [ ] 12.2 Write UI instrumentation tests for user interactions
    - Test button click animations
    - Test form validation and error display
    - Test screen transitions
    - Test loading states and empty states
    - _Testing: UI instrumentation_

  - [ ] 12.3 Write accessibility tests
    - Verify content descriptions for all interactive elements
    - Test with TalkBack screen reader
    - Validate touch target sizes (minimum 48dp)
    - Check color contrast ratios (WCAG AA: 4.5:1)
    - Test text scaling up to 200%
    - _Testing: Accessibility testing_

  - [ ] 12.4 Write animation tests
    - Test fade-in/fade-out animations
    - Test scale animations on button press
    - Test slide animations for screen transitions
    - Verify animation durations
    - _Testing: Animation testing_

  - [ ] 12.5 Write theme tests
    - Test light theme colors
    - Test dark theme colors
    - Test dynamic color support on Android 12+
    - Test theme persistence across sessions
    - _Testing: Theme testing_

  - [ ] 12.6 Write responsive layout tests
    - Test layouts on different screen sizes (compact, medium, expanded)
    - Test landscape orientation
    - Test two-pane layout on tablets
    - Verify responsive spacing adjustments
    - _Testing: Responsive layout testing_

- [ ] 13. Phase 4: Testing - Manual Testing and Performance
  - [ ] 13.1 Perform manual testing on multiple devices
    - Test on small phones (< 5")
    - Test on large phones (> 6")
    - Test on tablets (7" and 10")
    - Test in landscape orientation
    - Test with TalkBack enabled
    - _Testing: Manual testing_

  - [ ] 13.2 Profile animation performance
    - Use GPU profiling to measure frame rates
    - Ensure 60fps during animations
    - Identify and fix jank issues
    - Test on low-end devices
    - _Testing: Performance testing_

  - [ ] 13.3 Optimize layout hierarchies
    - Use Layout Inspector to identify deep hierarchies
    - Flatten layouts where possible
    - Use ConstraintLayout to reduce nesting
    - Measure layout performance improvements
    - _Testing: Performance optimization_

  - [ ] 13.4 Reduce overdraw
    - Use Debug GPU Overdraw tool
    - Remove unnecessary backgrounds
    - Optimize view hierarchies
    - Verify overdraw improvements
    - _Testing: Performance optimization_

- [ ] 14. Checkpoint - Verify all polish and testing complete
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 15. Phase 5: Release Preparation
  - [ ] 15.1 Complete final QA checklist
    - Verify all visual design elements match MD3 specifications
    - Verify all animations are smooth and consistent
    - Verify light and dark themes work correctly
    - Verify accessibility features work with TalkBack
    - Verify responsive layouts work on all screen sizes
    - _Testing: Final QA_

  - [ ] 15.2 Fix any remaining bugs
    - Address all issues found during QA
    - Verify fixes don't introduce regressions
    - Re-test affected areas
    - _Testing: Bug fixes_

  - [ ] 15.3 Update documentation
    - Document new UI components and their usage
    - Update README with design system information
    - Document animation guidelines
    - Document theme customization
    - _Documentation_

  - [ ] 15.4 Create release build
    - Update version code and version name
    - Generate signed APK/AAB
    - Test release build on multiple devices
    - Verify ProGuard rules don't break UI
    - _Release preparation_

  - [ ] 15.5 Submit to Play Store internal testing
    - Upload release build to Play Console
    - Add release notes highlighting UI improvements
    - Distribute to internal testers
    - Monitor crash reports and feedback
    - _Release deployment_

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation throughout the implementation
- The implementation follows the 5-phase plan from the design document
- All tasks focus on UI/UX improvements without changing core business logic
- Testing tasks complement the implementation to ensure quality
