# Implementation Plan: UI Polish & Consistency

## Nguyên tắc
- Patch thêm, không xóa — giữ nguyên IDs và logic hiện có
- Mỗi task chỉ sửa đúng file được chỉ định
- Ưu tiên P0 → P1 → P2 → P3

## Tasks

---

### P0 — Critical: Hardcode màu & Icon system

- [x] 1. Thêm Material Symbols icon drawables
  - [x] 1.1 Tạo `res/drawable/ic_arrow_back.xml` — vector arrow_back 24dp
    - Vector path: `M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z`
    - `android:width="24dp"`, `android:height="24dp"`, `android:viewportWidth="24"`, `android:viewportHeight="24"`
    - _Requirements: 2.1_

  - [x] 1.2 Tạo `res/drawable/ic_lock.xml` — vector lock 24dp
    - Vector path cho icon khóa
    - _Requirements: 2.2_

  - [x] 1.3 Tạo `res/drawable/ic_history.xml` — vector history 24dp
    - _Requirements: 2.2_

  - [x] 1.4 Tạo `res/drawable/ic_info_outline.xml` — vector info 24dp
    - _Requirements: 2.2_

  - [x] 1.5 Tạo `res/drawable/ic_warning_amber.xml` — vector warning 24dp
    - _Requirements: 2.2_

  - [x] 1.6 Tạo `res/drawable/ic_check_circle.xml` — vector check_circle 24dp
    - _Requirements: 2.2_

  - [x] 1.7 Tạo `res/drawable/ic_add.xml` — vector add 24dp (cho Extended FAB)
    - _Requirements: 2.1_

  - [x] 1.8 Tạo `res/drawable/ic_template.xml` — vector description/template 20dp
    - _Requirements: 2.1_

  - [x] 1.9 Tạo `res/drawable/bg_send_button.xml` — circular background cho send button
    - Shape oval, fill `?attr/colorPrimary`
    - _Requirements: 5.2_

- [x] 2. Chuẩn hóa màu trong `activity_ktv_ticket_detail.xml`
  - [x] 2.1 Thay hardcode màu trong top bar
    - Tìm `android:background` hardcode → thay bằng `?attr/colorSurface`
    - Tìm `android:textColor="#0F172A"` → thay bằng `?attr/colorOnSurface`
    - Tìm `android:tint="#0F172A"` → thay bằng `?attr/colorOnSurface`
    - _Requirements: 1.1, 1.2_

  - [x] 2.2 Thay hardcode màu trong cards
    - Tìm `android:textColor="#94A3B8"` → thay bằng `?attr/colorOnSurfaceVariant`
    - Tìm `android:textColor="#475569"` → thay bằng `?attr/colorOnSurfaceVariant`
    - Tìm `android:textColor="#0F172A"` → thay bằng `?attr/colorOnSurface`
    - Tìm `app:cardBackgroundColor="#FFFFFF"` → thay bằng `?attr/colorSurface`
    - _Requirements: 1.1, 1.3, 1.5_

  - [x] 2.3 Cập nhật bottom action bar buttons
    - Thay 3 `Material3Button` với hardcode màu bằng MD3 button styles
    - `btnNhanTin`: `style="@style/Widget.Material3.Button.OutlinedButton"`, thêm `app:icon="@drawable/ic_chat"`
    - `btnGoiDienDetail`: `style="@style/Widget.Material3.Button.TonalButton"`, thêm `app:icon="@drawable/ic_call"`
    - `btnDongTicket`: `style="@style/Widget.Material3.Button"`, thêm `app:icon="@drawable/ic_check_circle"`
    - Xóa `app:backgroundTint` hardcode, xóa `android:textColor="#FFFFFF"` hardcode
    - _Requirements: 8.2_

- [x] 3. Chuẩn hóa màu trong `activity_profile.xml`
  - [x] 3.1 Thay hardcode màu trong top bar và text
    - Tìm tất cả `android:textColor="#0F172A"` → `?attr/colorOnSurface`
    - Tìm tất cả `android:textColor="#94A3B8"` → `?attr/colorOnSurfaceVariant`
    - Tìm tất cả `android:textColor="#64748B"` → `?attr/colorOnSurfaceVariant`
    - Tìm `android:tint="#0F172A"` → `?attr/colorOnSurface`
    - _Requirements: 1.1, 9.1_

  - [x] 3.2 Thay emoji icons bằng vector drawables trong settings items
    - `itemDoiMatKhau`: thay `android:text="🔑"` TextView bằng `ImageView` với `android:src="@drawable/ic_lock"`
    - `itemLichSuChat`: thay `android:text="💬"` TextView bằng `ImageView` với `android:src="@drawable/ic_history"`
    - Item phiên bản: thay `android:text="ℹ️"` TextView bằng `ImageView` với `android:src="@drawable/ic_info_outline"`
    - Set `android:layout_width="24dp"`, `android:layout_height="24dp"`, `app:tint="?attr/colorOnSurfaceVariant"`
    - _Requirements: 2.2, 9.2_

  - [x] 3.3 Thêm ripple effect và cập nhật logout button
    - Thêm `android:background="?attr/selectableItemBackground"` cho `itemDoiMatKhau` và `itemLichSuChat`
    - Cập nhật `btnDangXuat`: xóa `app:backgroundTint="#FEE2E2"`, thêm `style="@style/Widget.Material3.Button.OutlinedButton"`, `app:strokeColor="?attr/colorError"`, `android:textColor="?attr/colorError"`
    - _Requirements: 9.3, 9.4_

- [x] 4. Chuẩn hóa màu trong `activity_lich_su_chat.xml`
  - Tìm `android:textColor="#0F172A"` → `?attr/colorOnSurface`
  - Tìm `android:textColor="#94A3B8"` → `?attr/colorOnSurfaceVariant`
  - _Requirements: 1.1_

---

### P1 — High: Components & Navigation

- [x] 5. Tạo `InitialAvatarView.java`
  - Tạo file `app/src/main/java/com/example/customercareproject/ui/components/InitialAvatarView.java`
  - Extend `AppCompatTextView`
  - Implement `setName(String name)`: lấy ký tự đầu uppercase, hash tên để chọn màu từ mảng 8 màu MD3
  - Implement `setSize(int dp)`: set width/height programmatically
  - Constructor nhận `Context`, `AttributeSet` (cho XML inflation)
  - Null-safe: nếu name null/empty → hiển thị "?" với màu xám
  - _Requirements: 6.1, 6.2, 6.3_

- [x] 6. Migrate Admin bottom navigation sang BottomNavigationView
  - [x] 6.1 Cập nhật `activity_admin_dashboard.xml`
    - Xóa `LinearLayout` custom bottom nav (id: `bottomNavBar` và tất cả tab LinearLayouts bên trong)
    - Thêm `BottomNavigationView` với id `bottomNavView`, `app:menu="@menu/admin_bottom_nav"`, `app:labelVisibilityMode="labeled"`
    - _Requirements: 3.1, 3.2_

  - [x] 6.2 Cập nhật `AdminDashboardActivity.java`
    - Xóa tất cả `tabThongKe.setOnClickListener`, `tabUsers.setOnClickListener`, v.v.
    - Xóa method `updateTabSelection()` hoặc tương đương
    - Thêm `BottomNavigationView bottomNavView = findViewById(R.id.bottomNavView)`
    - Thêm `bottomNavView.setOnItemSelectedListener(...)` để set `viewPager.setCurrentItem()`
    - Thêm `viewPager.registerOnPageChangeCallback(...)` để sync ngược lại
    - _Requirements: 3.3, 3.4, 3.5, 3.6_

- [x] 7. Tách warning banner ra khỏi chat toolbar
  - [x] 7.1 Cập nhật `activity_chat_khach_hang.xml`
    - Xóa `btnHuyYeuCau` và `tvThoiGianCho` khỏi bên trong toolbar
    - Thêm `MaterialCardView` id `bannerCho` (visibility=gone) sau toolbar, trước RecyclerView
    - Banner chứa: ic_warning + text "Đang tìm kỹ thuật viên..." + `tvThoiGianChoBanner` + `btnHuyYeuCauBanner`
    - Background: `@color/warning_container`, text: `@color/on_warning_container`
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

  - [x] 7.2 Cập nhật `ChatKhachHangActivity.java`
    - Thêm fields: `bannerCho`, `btnHuyYeuCauBanner`, `tvThoiGianChoBanner`
    - Trong `onCreate`: `bannerCho = findViewById(R.id.bannerCho)`, setup click listener cho `btnHuyYeuCauBanner` (logic giống `btnHuyYeuCau` cũ)
    - Trong `ticketListener`: thay `btnHuyYeuCau.setVisibility(...)` bằng `bannerCho.setVisibility(...)`
    - Giữ nguyên `btnHuyYeuCau` field (set gone) để không break code cũ
    - _Requirements: 4.5, 4.6_

- [x] 8. Simplify chat input bar
  - [x] 8.1 Cập nhật `activity_chat_khach_hang.xml` — input bar
    - Bọc `btnDinhKem` và `progressUpload` trong `FrameLayout` (48x48dp)
    - Thay `TextInputEditText` standalone bằng `TextInputLayout` + `TextInputEditText` với `endIconMode="custom"` cho template
    - Thay `FloatingActionButton btnGui` bằng `ImageButton` (48dp) với `bg_send_button` background
    - Xóa `btnTemplate` standalone (chuyển vào end icon của TextInputLayout)
    - _Requirements: 5.1, 5.2, 5.3, 5.4_

  - [x] 8.2 Cập nhật `ChatKhachHangActivity.java`
    - Cập nhật `btnGui = findViewById(R.id.btnGui)` — giữ nguyên ID
    - Cập nhật `btnTemplate` logic: lấy từ `TextInputLayout.setEndIconOnClickListener()`
    - Giữ nguyên toàn bộ logic gửi tin nhắn, upload ảnh
    - _Requirements: 5.3_

- [x] 9. Cập nhật Home Screen — Profile bottom sheet & Extended FAB
  - [x] 9.1 Tạo layout `bottom_sheet_profile_menu.xml`
    - 3 items: Tài khoản (ic_person), Lịch sử hỗ trợ (ic_history), Đăng xuất (ic_logout)
    - Mỗi item: `LinearLayout` horizontal, icon 24dp + text `titleSmall` + trailing arrow
    - Background: `?attr/colorSurface`, corner top 28dp
    - _Requirements: 7.2_

  - [x] 9.2 Cập nhật `activity_home.xml`
    - Trong toolbar: xóa `btnProfile`, `btnLichSuChat`, `btnLogout`
    - Thêm `InitialAvatarView` id `avatarProfile` (36dp) ở trailing position
    - Thay `FloatingActionButton fabYeuCau` bằng `ExtendedFloatingActionButton` với `android:text="Yêu cầu hỗ trợ"` và `app:icon="@drawable/ic_add"`
    - _Requirements: 7.1, 7.3_

  - [x] 9.3 Cập nhật `HomeActivity.java`
    - Xóa `btnProfile.setOnClickListener`, `btnLichSuChat.setOnClickListener`, `btnLogout.setOnClickListener`
    - Thêm `avatarProfile = findViewById(R.id.avatarProfile)`, set name từ user data
    - Thêm click listener cho `avatarProfile` → show `BottomSheetDialog` với `bottom_sheet_profile_menu.xml`
    - Thêm `rvSanPham.addOnScrollListener(...)` để shrink/extend FAB
    - Giữ nguyên `fabYeuCau.setOnClickListener(...)` logic hiện tại
    - _Requirements: 7.2, 7.4, 7.5_

---

### P2 — Medium: Polish & Consistency

- [x] 10. Cập nhật KTV Dashboard — SearchBar MD3 & Stat Cards
  - [x] 10.1 Cập nhật `activity_ktv_dashboard.xml`
    - Thay `SearchView` native bằng `com.google.android.material.search.SearchBar` MD3
    - Bọc stat bar trong 3 `MaterialCardView` nhỏ (elevation 1dp) thay vì `LinearLayout` với divider
    - Thay `tvTrangThaiKtv` TextView bằng `Chip` component (id giữ nguyên)
    - _Requirements: 10.1, 10.2, 10.3, 10.4_

  - [x] 10.2 Cập nhật `KtvDashboardActivity.java`
    - Cập nhật `searchView` reference sang `SearchBar`
    - Cập nhật `tvTrangThaiKtv` reference sang `Chip`
    - Thêm click listener cho status Chip → dialog chọn trạng thái
    - _Requirements: 10.4_

- [x] 11. Thêm InitialAvatarView vào Profile và KTV Dashboard
  - [x] 11.1 Cập nhật `activity_profile.xml`
    - Thay `tvAvatar` (TextView emoji) bằng `InitialAvatarView` id `avatarView` (72dp)
    - _Requirements: 6.4_

  - [x] 11.2 Cập nhật `ProfileActivity.java`
    - Thay `tvAvatar.setText(...)` bằng `avatarView.setName(hoTen)`
    - _Requirements: 6.4_

  - [x] 11.3 Cập nhật `activity_ktv_dashboard.xml`
    - Thêm `InitialAvatarView` (36dp) trong top bar, trước `tvTenKtv`
    - _Requirements: 6.5_

  - [x] 11.4 Cập nhật `KtvDashboardActivity.java`
    - Thêm `avatarKtv = findViewById(R.id.avatarKtv)`, set name từ user data
    - _Requirements: 6.5_

- [x] 12. Thay emoji icons trong Admin stats bằng vector drawables
  - Cập nhật `fragment_admin_thong_ke.xml`
  - Thay `android:text="⭐"` TextView bằng `ImageView` với `android:src="@drawable/ic_star"`
  - Thay `android:text="🎫"` bằng `ImageView` với `android:src="@drawable/ic_ticket"`
  - Thay `android:text="👤"` bằng `ImageView` với `android:src="@drawable/ic_person"`
  - Thay `android:text="📊"` bằng `ImageView` với `android:src="@drawable/ic_analytics"`
  - Set `android:layout_width="32dp"`, `android:layout_height="32dp"`, `app:tint="?attr/colorPrimary"`
  - _Requirements: 2.2_

- [x] 13. Cập nhật Empty States dùng EmptyStateView
  - [x] 13.1 Cập nhật `activity_lich_su_chat.xml`
    - Thay `layoutEmpty` LinearLayout (emoji + text) bằng `EmptyStateView` component
    - Set title "Chưa có lịch sử hỗ trợ", description "Các cuộc trò chuyện với KTV sẽ hiển thị ở đây"
    - _Requirements: 12.1, 12.3_

  - [x] 13.2 Cập nhật `LichSuChatActivity.java`
    - Cập nhật reference `layoutEmpty` sang `EmptyStateView`
    - Khi filter active và rỗng: set message "Không tìm thấy ticket phù hợp"
    - _Requirements: 12.5_

- [x] 14. Thêm date separator vào ChatAdapter
  - [x] 14.1 Tạo layout `item_chat_date_separator.xml`
    - `LinearLayout` horizontal, gravity center
    - `View` (0dp weight=1, height=1dp, background=`?attr/colorOutlineVariant`)
    - `TextView` (padding 8dp, `textAppearanceLabelMedium`, `colorOnSurfaceVariant`)
    - `View` (0dp weight=1, height=1dp, background=`?attr/colorOutlineVariant`)
    - _Requirements: 13.1, 13.2, 13.3, 13.4_

  - [x] 14.2 Cập nhật `ChatAdapter.java`
    - Thêm `VIEW_TYPE_DATE_SEPARATOR = 10` constant
    - Thêm inner class `DateSeparatorViewHolder`
    - Trong `getItemViewType()`: kiểm tra nếu item là date separator → trả về `VIEW_TYPE_DATE_SEPARATOR`
    - Trong `onCreateViewHolder()`: inflate `item_chat_date_separator.xml` cho type này
    - Trong `onBindViewHolder()`: bind date text ("Hôm nay", "Hôm qua", hoặc "dd/MM/yyyy")
    - Thêm method `insertDateSeparators(List<Map> messages)`: duyệt list, chèn separator khi ngày thay đổi
    - _Requirements: 13.1, 13.5_

---

### P3 — Low: Nice to Have

- [ ] 15. Thêm notification badge cho Admin Tickets tab
  - Sau khi migrate sang `BottomNavigationView`, thêm badge count
  - Trong `AdminDashboardActivity.java`: lắng nghe count ticket `ChoXuLy` + `HangCho`
  - `bottomNavView.getOrCreateBadge(R.id.nav_tickets).setNumber(count)`
  - Ẩn badge khi count = 0
  - _Requirements: 3.6_

- [ ] 16. Thêm message "đã xem" indicator trong chat
  - Thêm field `daDoc` (boolean) vào `TinNhan` model
  - Trong `item_chat.xml`: thêm `TextView` "✓✓" nhỏ bên dưới bubble KTV
  - Trong `ChatAdapter`: hiển thị indicator khi `daDoc = true`
  - _Requirements: 13.5_

- [ ] 17. Chip màu theo sản phẩm
  - Tạo map sản phẩm → màu: ECUS5=#1565C0, E-INVOICE=#2E7D32, ETAX=#6A1B9A, EBH=#E65100, CLOUDOFFICE=#00695C, TRUEPOS=#C62828
  - Áp dụng trong `item_lich_su_chat.xml` cho `tvSanPham` chip
  - Áp dụng trong `KtvTicketDetailActivity` cho `tvSanPham` chip
  - _Requirements: 11.6_

## Notes

- Tasks P0 và P1 là bắt buộc để đạt chuẩn production
- Tasks P2 là nên làm để hoàn thiện visual quality
- Tasks P3 (đánh dấu `*`) là optional
- Thứ tự thực hiện: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8 → 9 → 10 → 11 → 12 → 13 → 14
- Không thêm dependency mới — tất cả dùng Material Components đã có
- `InitialAvatarView` không cần Glide hay Picasso — pure Android drawing
