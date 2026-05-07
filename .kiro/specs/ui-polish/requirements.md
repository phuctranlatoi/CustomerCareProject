# Requirements Document: UI Polish & Consistency

## Introduction

Sau khi phân tích toàn bộ giao diện hiện tại của ứng dụng Customer Care Android, đã xác định được nhiều điểm không nhất quán và chưa đạt chuẩn Material Design 3. Spec này tập trung vào việc chuẩn hóa và polish giao diện theo góc nhìn UX/UI designer chuyên nghiệp, không thay đổi logic nghiệp vụ.

Các vấn đề chính cần giải quyết:
- Hardcode màu sắc không hỗ trợ dark mode
- Icon system không nhất quán (mix android system icons, emoji, vector)
- Bottom navigation Admin không chuẩn MD3
- Chat toolbar quá tải
- Avatar placeholder không chuyên nghiệp
- Spacing/typography không theo grid system

## Glossary

- **MD3**: Material Design 3 (Material You)
- **Theme_Attribute**: Thuộc tính màu từ theme (`?attr/colorPrimary`, v.v.)
- **Material_Symbols**: Bộ icon vector từ Google Fonts (Material Symbols Outlined)
- **8dp_Grid**: Hệ thống spacing dựa trên bội số của 8dp
- **Initial_Avatar**: Avatar hiển thị chữ cái đầu tên người dùng với màu nền từ hash
- **BottomNavigationView**: Component navigation bar chuẩn MD3 của Material Components
- **SearchBar_MD3**: Component `SearchBar` từ Material Components 1.9+ thay thế `SearchView` native
- **Extended_FAB**: FAB có label text, collapse khi scroll
- **Status_Chip**: Chip hiển thị trạng thái (Rảnh/Bận/Offline) có thể tap để thay đổi
- **Timeline_View**: Ghi chú tiến độ hiển thị theo dạng timeline với vertical line

## Requirements

### Requirement 1: Chuẩn Hóa Màu Sắc — Xóa Hardcode

**User Story:** Là developer, tôi muốn tất cả màu sắc dùng theme attributes thay vì hardcode hex, để dark mode hoạt động đúng và dễ maintain.

#### Acceptance Criteria

1. THE UI_System SHALL replace all hardcoded color values (`#0F172A`, `#94A3B8`, `#1976D2`, `#FFFFFF`, `#475569`) with corresponding MD3 theme attributes
2. THE UI_System SHALL use `?attr/colorOnSurface` for primary text colors
3. THE UI_System SHALL use `?attr/colorOnSurfaceVariant` for secondary/hint text colors
4. THE UI_System SHALL use `?attr/colorPrimary` for brand/action colors
5. THE UI_System SHALL use `?attr/colorSurface` for card/toolbar backgrounds
6. THE UI_System SHALL use `?attr/colorSurfaceContainer` for input/list item backgrounds
7. WHEN dark mode is enabled, THE UI_System SHALL display correct dark theme colors without any hardcoded light colors showing

### Requirement 2: Chuẩn Hóa Icon System

**User Story:** Là người dùng, tôi muốn tất cả icons trong ứng dụng trông nhất quán và chuyên nghiệp, để có trải nghiệm visual đồng nhất.

#### Acceptance Criteria

1. THE UI_System SHALL replace all `@android:drawable/*` system icons with Material Symbols vector drawables
2. THE UI_System SHALL replace all emoji icons (🔑, 💬, ℹ️, ⭐, 🎫, 👤, 📊) in layouts with vector drawable icons
3. THE UI_System SHALL use 24dp size for navigation icons
4. THE UI_System SHALL use 20dp size for inline/action icons
5. THE UI_System SHALL use `?attr/colorOnSurfaceVariant` as default icon tint
6. THE UI_System SHALL use `?attr/colorPrimary` as active/selected icon tint

### Requirement 3: Admin Bottom Navigation — Migrate sang MD3

**User Story:** Là Admin, tôi muốn bottom navigation hoạt động đúng với ripple effect và active indicator, để điều hướng rõ ràng và responsive.

#### Acceptance Criteria

1. THE AdminDashboardActivity SHALL replace the custom `LinearLayout` bottom nav with `BottomNavigationView` component
2. THE BottomNavigationView SHALL use `menu/admin_bottom_nav.xml` with 5 items: Tổng quan, Người dùng, Tickets, Gói DK, Phân tích
3. THE BottomNavigationView SHALL display active indicator (pill shape) on selected tab
4. THE BottomNavigationView SHALL show ripple effect on tap
5. THE BottomNavigationView SHALL use icon size 24dp and label size 12sp
6. WHEN a tab is selected, THE BottomNavigationView SHALL highlight with `colorPrimary` icon and label

### Requirement 4: Chat Toolbar — Tách Cancel Button

**User Story:** Là khách hàng đang chờ KTV, tôi muốn nút "Hủy yêu cầu" hiển thị rõ ràng và dễ thấy, không bị ẩn trong toolbar chật.

#### Acceptance Criteria

1. THE ChatKhachHangActivity SHALL remove `btnHuyYeuCau` from inside the toolbar
2. THE ChatKhachHangActivity SHALL display a warning banner below the toolbar when ticket status is "HangCho" or "ChoXuLy" without KTV
3. THE warning banner SHALL contain: warning icon + "Đang tìm kỹ thuật viên..." text + wait time + "Hủy yêu cầu" button
4. THE warning banner SHALL use `colorWarningContainer` background with `colorOnWarningContainer` text
5. WHEN ticket gets a KTV assigned, THE warning banner SHALL hide with fade-out animation
6. THE toolbar SHALL only contain: back button + title + subtitle + call button

### Requirement 5: Chat Input Bar — Simplify

**User Story:** Là người dùng chat, tôi muốn input bar gọn gàng và dễ dùng, không bị chật với quá nhiều buttons.

#### Acceptance Criteria

1. THE chat input bar SHALL contain: text field + attach button + send button (3 elements max)
2. THE send button SHALL be an `ImageButton` (48dp) instead of `FloatingActionButton`
3. THE template button SHALL be accessible via a dedicated icon in the text field's end icon, or via long-press on the text field
4. THE progress bar for upload SHALL overlay the attach button (not take separate space)
5. THE input bar height SHALL be consistent at `wrap_content` with `paddingVertical="8dp"`

### Requirement 6: Avatar System — Initial Letter

**User Story:** Là người dùng, tôi muốn thấy avatar với chữ cái đầu tên thay vì emoji placeholder, để nhận diện người dùng dễ hơn.

#### Acceptance Criteria

1. THE UI_System SHALL implement an `InitialAvatarView` custom view that displays the first letter of a name
2. THE InitialAvatarView SHALL generate a background color deterministically from the name string (hash-based)
3. THE InitialAvatarView SHALL display white text on colored background
4. THE ProfileActivity SHALL use `InitialAvatarView` (72dp) instead of `TextView` with emoji
5. THE KtvDashboardActivity SHALL use `InitialAvatarView` (40dp) in the toolbar
6. THE chat messages SHALL use `InitialAvatarView` (32dp) as sender avatar

### Requirement 7: Home Screen — FAB Extended & Toolbar Cleanup

**User Story:** Là khách hàng, tôi muốn màn hình chính có toolbar gọn gàng và FAB rõ ràng hơn, để dễ sử dụng.

#### Acceptance Criteria

1. THE HomeActivity toolbar SHALL consolidate Profile + History + Logout buttons into a single profile avatar button
2. WHEN user taps the profile avatar, THE HomeActivity SHALL show a bottom sheet with options: Tài khoản, Lịch sử hỗ trợ, Đăng xuất
3. THE FAB SHALL be an `ExtendedFloatingActionButton` with icon + label "Yêu cầu hỗ trợ"
4. WHEN user scrolls down the product list, THE ExtendedFAB SHALL shrink to icon-only
5. WHEN user scrolls up, THE ExtendedFAB SHALL expand back to show label

### Requirement 8: Ticket Detail — Color System & Bottom Bar

**User Story:** Là KTV, tôi muốn màn hình chi tiết ticket dùng màu sắc nhất quán với hệ thống, không bị hardcode.

#### Acceptance Criteria

1. THE KtvTicketDetailActivity toolbar SHALL use `?attr/colorSurface` background and `?attr/colorOnSurface` text
2. THE bottom action bar SHALL use MD3 button styles: "Nhắn tin" (outlined), "Gọi điện" (tonal), "Đóng ticket" (filled)
3. THE priority chips SHALL use semantic colors: Cao=`colorError`, TrungBinh=`colorWarning`, Thap=`colorSuccess`
4. THE progress notes timeline SHALL display a vertical line (2dp, `colorOutlineVariant`) connecting note items
5. THE customer info card SHALL use `?attr/colorOnSurface` and `?attr/colorOnSurfaceVariant` for text

### Requirement 9: Profile Screen — Vector Icons & Cleanup

**User Story:** Là người dùng, tôi muốn màn hình profile trông chuyên nghiệp với icons nhất quán.

#### Acceptance Criteria

1. THE ProfileActivity toolbar SHALL use theme attributes instead of hardcoded `#0F172A`
2. THE settings items SHALL use vector drawable icons instead of emoji (🔑→ic_lock, 💬→ic_history, ℹ️→ic_info)
3. THE settings items SHALL have `?attr/selectableItemBackground` for proper ripple effect
4. THE logout button SHALL use outlined style with `colorError` text and `colorErrorContainer` background
5. THE dividers between settings items SHALL use `?attr/colorOutlineVariant`

### Requirement 10: KTV Dashboard — SearchBar MD3 & Stat Cards

**User Story:** Là KTV, tôi muốn dashboard có search bar hiện đại và stat cards rõ ràng hơn.

#### Acceptance Criteria

1. THE KtvDashboardActivity SHALL replace `SearchView` native with `SearchBar` MD3 component
2. THE stat bar SHALL display 3 `MaterialCardView` items (elevation 1dp) instead of plain `LinearLayout` with dividers
3. EACH stat card SHALL contain: icon (24dp, colored), number (20sp bold), label (12sp)
4. THE status badge SHALL be a `Chip` component that opens a dialog to change status when tapped
5. THE logout button SHALL be replaced with a profile avatar in the toolbar trailing position

### Requirement 11: Spacing & Typography Consistency

**User Story:** Là người dùng, tôi muốn giao diện có spacing đều đặn và text hierarchy rõ ràng.

#### Acceptance Criteria

1. THE UI_System SHALL enforce 8dp grid: all padding/margin values SHALL be multiples of 4dp (4, 8, 12, 16, 20, 24, 32dp)
2. THE UI_System SHALL replace all hardcoded `textSize` values with `textAppearance` MD3 attributes
3. THE screen background SHALL consistently use `?attr/colorSurfaceContainerLow`
4. THE card backgrounds SHALL consistently use `?attr/colorSurface` or `?attr/colorSurfaceContainer`
5. THE section headers SHALL use `textAppearanceTitleMedium` with `colorOnSurface`
6. THE metadata/caption text SHALL use `textAppearanceBodySmall` with `colorOnSurfaceVariant`

### Requirement 12: Empty States — Dùng EmptyStateView Component

**User Story:** Là người dùng, tôi muốn empty states trông chuyên nghiệp với illustration thay vì emoji.

#### Acceptance Criteria

1. THE LichSuChatActivity SHALL replace emoji empty state with `EmptyStateView` component
2. THE KtvDashboardActivity SHALL use `EmptyStateView` for no-tickets state (đã có `layout_empty_state` include)
3. THE EmptyStateView SHALL display: vector illustration + title + description + optional action button
4. THE empty state illustration SHALL use `colorPrimaryContainer` tinted vector drawable
5. WHEN filter is active and returns no results, THE empty state message SHALL reflect the active filter

### Requirement 13: Chat — Date Separator & Message Polish

**User Story:** Là người dùng chat, tôi muốn thấy ngày tháng phân cách giữa các nhóm tin nhắn, để dễ theo dõi lịch sử.

#### Acceptance Criteria

1. THE ChatAdapter SHALL insert date separator items between messages from different days
2. THE date separator SHALL display: centered text with date (e.g., "Hôm nay", "Hôm qua", "12/05/2026")
3. THE date separator text SHALL use `textAppearanceLabelMedium` with `colorOnSurfaceVariant`
4. THE date separator SHALL have a horizontal line on both sides
5. THE message bubbles SHALL have consistent 8dp vertical spacing between messages from same sender, 16dp between different senders
