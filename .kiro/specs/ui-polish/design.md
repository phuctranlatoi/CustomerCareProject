# Design Document: UI Polish & Consistency

## Overview

Tài liệu này mô tả cách triển khai kỹ thuật cho các cải thiện UI/UX đã được xác định trong requirements. Nguyên tắc: **patch thêm, không xóa** — chỉ thay thế những gì cần thiết, giữ nguyên logic nghiệp vụ.

## Goals

- Chuẩn hóa toàn bộ màu sắc sang MD3 theme attributes
- Thống nhất icon system dùng Material Symbols
- Migrate Admin bottom nav sang BottomNavigationView chuẩn
- Simplify chat toolbar và input bar
- Implement InitialAvatarView cho avatar placeholder
- Enforce 8dp grid và MD3 typography

## Non-Goals

- Không thay đổi logic Firestore, authentication, hoặc business rules
- Không thêm tính năng mới (đã có trong specs khác)
- Không thay đổi cấu trúc navigation hoặc Activity/Fragment hierarchy

## Architecture

Tất cả thay đổi nằm ở tầng Presentation (XML layouts + custom views). Không có thay đổi ở ViewModel, Repository, hay data layer.

```
Presentation Layer (thay đổi)
├── res/layout/*.xml          — cập nhật layouts
├── res/drawable/ic_*.xml     — thêm Material Symbols icons
├── res/values/themes.xml     — đảm bảo theme attributes đầy đủ
└── ui/components/
    └── InitialAvatarView.java — component mới
```

## Components

### 1. InitialAvatarView

Custom view hiển thị chữ cái đầu tên với màu nền hash-based.

```java
public class InitialAvatarView extends androidx.appcompat.widget.AppCompatTextView {
    
    // Màu nền cố định cho từng hash bucket (MD3 container colors)
    private static final int[] AVATAR_COLORS = {
        0xFF1565C0, // blue
        0xFF2E7D32, // green
        0xFF6A1B9A, // purple
        0xFFE65100, // orange
        0xFF00695C, // teal
        0xFFC62828, // red
        0xFF4527A0, // deep purple
        0xFF00838F, // cyan
    };
    
    public void setName(String name) {
        if (name == null || name.isEmpty()) {
            setText("?");
            setBackgroundColor(0xFF9E9E9E);
            return;
        }
        // Lấy chữ cái đầu (uppercase)
        String initial = String.valueOf(name.charAt(0)).toUpperCase();
        setText(initial);
        // Hash tên để chọn màu
        int colorIndex = Math.abs(name.hashCode()) % AVATAR_COLORS.length;
        // Set circular background với màu
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(AVATAR_COLORS[colorIndex]);
        setBackground(bg);
        setTextColor(0xFFFFFFFF);
        setGravity(Gravity.CENTER);
    }
}
```

**Sử dụng trong XML:**
```xml
<com.example.customercareproject.ui.components.InitialAvatarView
    android:id="@+id/avatarView"
    android:layout_width="40dp"
    android:layout_height="40dp"
    android:textSize="16sp"
    android:textStyle="bold" />
```

**Sử dụng trong Java:**
```java
avatarView.setName(nguoiDung.getHoTen());
```

### 2. Icon Drawables — Material Symbols

Tạo các file vector drawable trong `res/drawable/`:

| File | Material Symbol | Usage |
|------|----------------|-------|
| `ic_arrow_back.xml` | arrow_back | Back navigation |
| `ic_person.xml` | person | Profile |
| `ic_history.xml` | history | Lịch sử |
| `ic_logout.xml` | logout | Đăng xuất |
| `ic_send.xml` | send | Gửi tin nhắn |
| `ic_attach_file.xml` | attach_file | Đính kèm |
| `ic_call.xml` | call | Gọi điện |
| `ic_lock.xml` | lock | Đổi mật khẩu |
| `ic_info.xml` | info | Thông tin |
| `ic_star.xml` | star | Rating |
| `ic_ticket.xml` | confirmation_number | Ticket |
| `ic_analytics.xml` | analytics | Phân tích |
| `ic_close.xml` | close | Đóng/Hủy |
| `ic_warning.xml` | warning | Cảnh báo |
| `ic_check.xml` | check_circle | Thành công |

### 3. Admin BottomNavigationView

**Thay thế custom LinearLayout bằng:**

```xml
<!-- activity_admin_dashboard.xml -->
<com.google.android.material.bottomnavigation.BottomNavigationView
    android:id="@+id/bottomNavView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:background="?attr/colorSurface"
    app:menu="@menu/admin_bottom_nav"
    app:labelVisibilityMode="labeled"
    app:itemIconSize="24dp"
    app:itemTextAppearanceActive="@style/TextAppearance.Material3.LabelMedium"
    app:itemTextAppearanceInactive="@style/TextAppearance.Material3.LabelMedium"
    app:itemActiveIndicatorStyle="@style/Widget.Material3.BottomNavigationView.ActiveIndicator" />
```

**Menu file `res/menu/admin_bottom_nav.xml` (cập nhật icons):**
```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:id="@+id/nav_thong_ke"
          android:icon="@drawable/ic_nav_dashboard"
          android:title="Tổng quan" />
    <item android:id="@+id/nav_users"
          android:icon="@drawable/ic_nav_users"
          android:title="Người dùng" />
    <item android:id="@+id/nav_tickets"
          android:icon="@drawable/ic_nav_tickets"
          android:title="Tickets" />
    <item android:id="@+id/nav_goi_dk"
          android:icon="@drawable/ic_nav_knowledge"
          android:title="Gói DK" />
    <item android:id="@+id/nav_phan_tich"
          android:icon="@drawable/ic_nav_reviews"
          android:title="Phân tích" />
</menu>
```

**Java — AdminDashboardActivity.java:**
```java
// Thay thế manual tab click listeners
BottomNavigationView bottomNav = findViewById(R.id.bottomNavView);
bottomNav.setOnItemSelectedListener(item -> {
    int id = item.getItemId();
    if (id == R.id.nav_thong_ke) viewPager.setCurrentItem(0, false);
    else if (id == R.id.nav_users) viewPager.setCurrentItem(1, false);
    else if (id == R.id.nav_tickets) viewPager.setCurrentItem(2, false);
    else if (id == R.id.nav_goi_dk) viewPager.setCurrentItem(3, false);
    else if (id == R.id.nav_phan_tich) viewPager.setCurrentItem(4, false);
    return true;
});
// Sync ViewPager → BottomNav
viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
    @Override
    public void onPageSelected(int position) {
        bottomNav.getMenu().getItem(position).setChecked(true);
    }
});
```

### 4. Chat Warning Banner

**Thêm vào `activity_chat_khach_hang.xml` sau toolbar:**

```xml
<!-- Warning banner — chỉ hiện khi HangCho/ChoXuLy không có KTV -->
<com.google.android.material.card.MaterialCardView
    android:id="@+id/bannerCho"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginHorizontal="16dp"
    android:layout_marginTop="8dp"
    android:layout_marginBottom="4dp"
    android:visibility="gone"
    app:cardCornerRadius="12dp"
    app:cardBackgroundColor="@color/warning_container"
    app:strokeWidth="0dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:padding="12dp"
        android:gravity="center_vertical">

        <ImageView
            android:layout_width="20dp"
            android:layout_height="20dp"
            android:src="@drawable/ic_warning"
            app:tint="@color/warning"
            android:layout_marginEnd="8dp"
            android:contentDescription="Cảnh báo" />

        <LinearLayout
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:orientation="vertical">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="Đang tìm kỹ thuật viên..."
                android:textAppearance="?attr/textAppearanceLabelMedium"
                android:textColor="@color/on_warning_container"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvThoiGianChoBanner"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textAppearance="?attr/textAppearanceBodySmall"
                android:textColor="@color/on_warning_container"
                android:layout_marginTop="2dp" />
        </LinearLayout>

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnHuyYeuCauBanner"
            android:layout_width="wrap_content"
            android:layout_height="32dp"
            android:text="Hủy"
            android:textSize="12sp"
            android:paddingHorizontal="12dp"
            style="@style/Widget.Material3.Button.TonalButton"
            app:backgroundTint="@color/warning"
            android:textColor="@color/on_warning" />
    </LinearLayout>
</com.google.android.material.card.MaterialCardView>
```

**Java — ChatKhachHangActivity.java:**
```java
// Thêm field
private MaterialCardView bannerCho;
private Button btnHuyYeuCauBanner;
private TextView tvThoiGianChoBanner;

// Trong onCreate, sau toolbar setup:
bannerCho = findViewById(R.id.bannerCho);
btnHuyYeuCauBanner = findViewById(R.id.btnHuyYeuCauBanner);
tvThoiGianChoBanner = findViewById(R.id.tvThoiGianChoBanner);

// Gán click listener giống btnHuyYeuCau cũ
btnHuyYeuCauBanner.setOnClickListener(v -> {
    // ... logic hủy yêu cầu giữ nguyên
});

// Trong ticketListener, thay vì show/hide btnHuyYeuCau:
// Show banner khi HangCho/ChoXuLy không có KTV
if (bannerCho != null) bannerCho.setVisibility(View.VISIBLE);
// Hide banner khi có KTV hoặc DaXuLy
if (bannerCho != null) bannerCho.setVisibility(View.GONE);
```

### 5. Home Screen — Profile Bottom Sheet & Extended FAB

**Thay 3 icon buttons bằng 1 avatar + bottom sheet:**

```xml
<!-- Trong toolbar HomeActivity -->
<com.example.customercareproject.ui.components.InitialAvatarView
    android:id="@+id/avatarProfile"
    android:layout_width="36dp"
    android:layout_height="36dp"
    android:textSize="14sp"
    android:textStyle="bold"
    android:layout_marginEnd="8dp" />
```

```xml
<!-- Thay FAB thường bằng ExtendedFAB -->
<com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
    android:id="@+id/fabYeuCau"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="bottom|end"
    android:layout_margin="16dp"
    android:text="Yêu cầu hỗ trợ"
    android:contentDescription="Yêu cầu hỗ trợ"
    app:icon="@drawable/ic_add"
    app:backgroundTint="?attr/colorPrimary"
    app:iconTint="?attr/colorOnPrimary"
    android:textColor="?attr/colorOnPrimary" />
```

**Java — HomeActivity.java:**
```java
// Profile avatar click → bottom sheet
avatarProfile.setOnClickListener(v -> {
    BottomSheetDialog sheet = new BottomSheetDialog(this);
    View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_profile_menu, null);
    sheetView.findViewById(R.id.itemTaiKhoan).setOnClickListener(v2 -> {
        startActivity(new Intent(this, ProfileActivity.class));
        sheet.dismiss();
    });
    sheetView.findViewById(R.id.itemLichSu).setOnClickListener(v2 -> {
        startActivity(new Intent(this, LichSuChatActivity.class));
        sheet.dismiss();
    });
    sheetView.findViewById(R.id.itemDangXuat).setOnClickListener(v2 -> {
        // logic đăng xuất hiện tại
        sheet.dismiss();
    });
    sheet.setContentView(sheetView);
    sheet.show();
});

// Extended FAB shrink/expand khi scroll
rvSanPham.addOnScrollListener(new RecyclerView.OnScrollListener() {
    @Override
    public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
        if (dy > 0) fabYeuCau.shrink();
        else if (dy < 0) fabYeuCau.extend();
    }
});
```

### 6. Chat Input Bar — Simplify

**Cập nhật `activity_chat_khach_hang.xml` input bar:**

```xml
<LinearLayout
    android:id="@+id/layoutInputBar"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:paddingHorizontal="8dp"
    android:paddingVertical="8dp"
    android:background="?attr/colorSurface"
    android:gravity="center_vertical"
    android:elevation="6dp">

    <!-- Attach button với overlay progress -->
    <FrameLayout
        android:layout_width="48dp"
        android:layout_height="48dp">

        <ImageButton
            android:id="@+id/btnDinhKem"
            android:layout_width="48dp"
            android:layout_height="48dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@drawable/ic_attach_file"
            android:tint="?attr/colorOnSurfaceVariant"
            android:contentDescription="Đính kèm ảnh"
            android:padding="12dp" />

        <ProgressBar
            android:id="@+id/progressUpload"
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:layout_gravity="center"
            android:visibility="gone"
            android:indeterminateTint="?attr/colorPrimary" />
    </FrameLayout>

    <!-- Text input -->
    <com.google.android.material.textfield.TextInputLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:layout_marginHorizontal="4dp"
        style="@style/Widget.Material3.TextInputLayout.OutlinedBox.Dense"
        app:boxCornerRadiusTopStart="24dp"
        app:boxCornerRadiusTopEnd="24dp"
        app:boxCornerRadiusBottomStart="24dp"
        app:boxCornerRadiusBottomEnd="24dp"
        app:endIconMode="custom"
        app:endIconDrawable="@drawable/ic_template"
        app:endIconContentDescription="Template">

        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/edtTinNhan"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:hint="Nhập tin nhắn..."
            android:inputType="textMultiLine|textCapSentences"
            android:maxLines="4"
            android:textAppearance="?attr/textAppearanceBodyLarge" />
    </com.google.android.material.textfield.TextInputLayout>

    <!-- Send button -->
    <ImageButton
        android:id="@+id/btnGui"
        android:layout_width="48dp"
        android:layout_height="48dp"
        android:background="@drawable/bg_send_button"
        android:src="@drawable/ic_send"
        android:tint="?attr/colorOnPrimary"
        android:contentDescription="Gửi"
        android:padding="12dp" />
</LinearLayout>
```

### 7. Ticket Detail — Bottom Bar MD3

**Cập nhật bottom action bar:**

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:padding="16dp"
    android:background="?attr/colorSurface"
    android:elevation="8dp">

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnNhanTin"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:layout_weight="1"
        android:layout_marginEnd="8dp"
        android:text="Nhắn tin"
        app:icon="@drawable/ic_chat"
        style="@style/Widget.Material3.Button.OutlinedButton" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnGoiDienDetail"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:layout_weight="1"
        android:layout_marginEnd="8dp"
        android:text="Gọi điện"
        app:icon="@drawable/ic_call"
        style="@style/Widget.Material3.Button.TonalButton" />

    <com.google.android.material.button.MaterialButton
        android:id="@+id/btnDongTicket"
        android:layout_width="0dp"
        android:layout_height="48dp"
        android:layout_weight="1"
        android:text="Đóng ticket"
        app:icon="@drawable/ic_check"
        style="@style/Widget.Material3.Button" />
</LinearLayout>
```

### 8. Date Separator trong Chat

**Thêm ViewType mới vào ChatAdapter:**

```java
// Trong ChatAdapter.java
private static final int VIEW_TYPE_DATE_SEPARATOR = 10;

// Thêm vào getItemViewType():
// Nếu item là DateSeparator → trả về VIEW_TYPE_DATE_SEPARATOR

// Layout item_chat_date_separator.xml:
// <LinearLayout horizontal gravity=center>
//   <View width=0dp weight=1 height=1dp background=colorOutlineVariant />
//   <TextView text="Hôm nay" textAppearance=labelMedium textColor=colorOnSurfaceVariant padding=8dp />
//   <View width=0dp weight=1 height=1dp background=colorOutlineVariant />
// </LinearLayout>
```

## Data Models

Không thay đổi data models. Chỉ thêm:
- `InitialAvatarView.java` — custom view mới
- `bottom_sheet_profile_menu.xml` — layout bottom sheet mới
- `item_chat_date_separator.xml` — layout item mới cho chat

## Error Handling

Tất cả thay đổi là UI-only, không có error handling mới cần thiết. `InitialAvatarView.setName(null)` xử lý null-safe với fallback "?".

## Testing Strategy

Không cần property-based testing cho UI polish. Kiểm tra thủ công:

1. **Dark mode**: Bật dark mode → kiểm tra tất cả màn hình không có màu hardcode sai
2. **Icon consistency**: Kiểm tra tất cả icons dùng cùng style (Outlined)
3. **Bottom nav Admin**: Kiểm tra ripple, active indicator, badge
4. **Chat banner**: Kiểm tra hiện/ẩn đúng theo trạng thái ticket
5. **Extended FAB**: Kiểm tra shrink/expand khi scroll
6. **InitialAvatarView**: Kiểm tra với tên tiếng Việt, tên null, tên 1 ký tự
7. **Input bar**: Kiểm tra trên màn hình nhỏ (360dp width)

## Implementation Notes

- Tất cả thay đổi layout: chỉ cập nhật XML, không xóa view IDs đang được dùng trong Java
- `btnHuyYeuCau` cũ: giữ nguyên trong code Java, chỉ ẩn bằng `visibility="gone"` và thêm `bannerCho` mới
- Admin bottom nav: giữ nguyên `tabThongKe`, `tabUsers`, v.v. IDs trong Java, thêm `bottomNavView` mới và sync với ViewPager
- Không thay đổi `fabYeuCau` ID — chỉ đổi từ `FloatingActionButton` sang `ExtendedFloatingActionButton`
