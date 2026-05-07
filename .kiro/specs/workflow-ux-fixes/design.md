# Design Document: Workflow UX Fixes

## Nguyên tắc thiết kế

**Không xóa code cũ.** Tất cả thay đổi là patch thêm vào logic hiện có:
- Giữ nguyên toàn bộ flow, listener, adapter đang hoạt động
- Chỉ sửa đúng dòng bị lỗi hoặc thêm code mới vào đúng chỗ
- Không refactor, không đổi tên method, không thay đổi kiến trúc

---

## Fix 1: FAB "Yêu cầu hỗ trợ" + Luồng tạo ticket từ HomeActivity

**Vấn đề:** FAB chỉ hiện Toast. Click sản phẩm → DanhGiaActivity (đúng cho đánh giá), nhưng không có đường nào tạo ticket hỗ trợ.

**Giải pháp:** Giữ nguyên click sản phẩm → DanhGiaActivity. Chỉ sửa FAB: thay Toast bằng hiện AlertDialog chọn sản phẩm từ `danhSachSanPhamHienTai` (list đã có sẵn trong HomeActivity).

### Patch trong `HomeActivity.java`

Code hiện tại (dòng cần sửa):
```java
fabYeuCau.setOnClickListener(v -> {
    android.widget.Toast.makeText(this, "Chọn sản phẩm cần hỗ trợ bên dưới", android.widget.Toast.LENGTH_SHORT).show();
});
```

Thay bằng (giữ nguyên FAB, chỉ đổi nội dung lambda):
```java
fabYeuCau.setOnClickListener(v -> {
    if (danhSachSanPhamHienTai == null || danhSachSanPhamHienTai.isEmpty()) {
        android.widget.Toast.makeText(this, "Chưa có sản phẩm đăng ký", android.widget.Toast.LENGTH_SHORT).show();
        return;
    }
    String[] tenSanPham = danhSachSanPhamHienTai.toArray(new String[0]);
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("Chọn sản phẩm cần hỗ trợ")
        .setItems(tenSanPham, (dialog, which) -> {
            Intent intent = new Intent(this, com.example.customercareproject.ui.loi.YeuCauHoTroActivity.class);
            intent.putExtra("sanPham", tenSanPham[which]);
            startActivity(intent);
        })
        .show();
});
```

Thêm field `danhSachSanPhamHienTai` vào HomeActivity và gán trong `hienThiSanPham()`:
```java
private java.util.List<String> danhSachSanPhamHienTai = new java.util.ArrayList<>();

private void hienThiSanPham(RecyclerView rvSanPham, java.util.List<String> danhSachSanPham) {
    danhSachSanPhamHienTai = danhSachSanPham; // lưu lại để FAB dùng
    // ... code hiện tại giữ nguyên
}
```

---

## Fix 2: ChatKhachHangActivity — Thêm nút "Hủy yêu cầu"

**Vấn đề:** `batDauQuetKtv()` lắng nghe vô hạn, không có nút hủy.

**Giải pháp:** Thêm Button ẩn vào layout, hiện/ẩn trong ticketListener hiện có — không đụng đến `batDauQuetKtv()` hay `dungQuetKtv()`.

### Patch trong `activity_chat_khach_hang.xml`

Thêm Button ngay sau `tvThoiGianCho` (giữ nguyên các view khác):
```xml
<Button
    android:id="@+id/btnHuyYeuCau"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Hủy yêu cầu"
    android:visibility="gone"
    style="@style/Widget.Material3.Button.TonalButton" />
```

### Patch trong `ChatKhachHangActivity.java`

Thêm field (không xóa field nào cũ):
```java
private android.widget.Button btnHuyYeuCau;
```

Trong `onCreate`, sau dòng `progressUpload = findViewById(...)`:
```java
btnHuyYeuCau = findViewById(R.id.btnHuyYeuCau);
btnHuyYeuCau.setOnClickListener(v -> {
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("Hủy yêu cầu hỗ trợ")
        .setMessage("Bạn có chắc muốn hủy yêu cầu này không?")
        .setPositiveButton("Hủy yêu cầu", (d, w) -> {
            dungQuetKtv(); // dùng method hiện có
            db.collection("YeuCauHoTro").document(ticketId)
                .update("trangThai", "DaHuy", "capNhatLuc", Timestamp.now())
                .addOnSuccessListener(v2 -> {
                    Toast.makeText(this, "Đã hủy yêu cầu", Toast.LENGTH_SHORT).show();
                    finish();
                });
        })
        .setNegativeButton("Không", null)
        .show();
});
```

Trong `ticketListener` (đoạn `if (chuaCoKtv && "HangCho".equals(ts))` và `if (chuaCoKtv && "ChoXuLy".equals(ts))`), thêm 1 dòng hiện nút:
```java
if (btnHuyYeuCau != null) btnHuyYeuCau.setVisibility(View.VISIBLE);
```

Trong các nhánh còn lại (đã có KTV hoặc DaXuLy), thêm 1 dòng ẩn nút:
```java
if (btnHuyYeuCau != null) btnHuyYeuCau.setVisibility(View.GONE);
```

---

## Fix 3: KtvTicketDetailActivity — Dialog xác nhận nhận ticket

**Vấn đề:** Đoạn code hiện tại tự động set `DangXuLy` ngay khi mở ticket:
```java
if (!daCapNhatTrangThai && ("ChoXuLy".equals(...) || "HangCho".equals(...))) {
    daCapNhatTrangThai = true;
    db...update("trangThai", "DangXuLy", ...);
}
```

**Giải pháp:** Giữ nguyên flag `daCapNhatTrangThai` và toàn bộ logic xung quanh. Chỉ thay phần `db.update(...)` bằng gọi method mới `hienDialogNhanTicket()`.

### Patch trong `KtvTicketDetailActivity.java`

Thay đúng đoạn auto-update (không xóa gì khác):
```java
// CŨ — xóa 2 dòng này:
db.collection("YeuCauHoTro").document(ticketId)
    .update("trangThai", "DangXuLy", "capNhatLuc", Timestamp.now(),
            "ktvUid", user.getUid(), "ktvTen", tenKtv != null ? tenKtv : "");

// MỚI — thay bằng:
hienDialogNhanTicket();
```

Thêm method mới (không đụng method nào cũ):
```java
private void hienDialogNhanTicket() {
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("Nhận ticket này?")
        .setMessage("Bạn có muốn nhận và bắt đầu xử lý ticket này không?")
        .setPositiveButton("Nhận", (d, w) -> {
            db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(ktvDoc -> {
                    String tenKtv = ktvDoc.getString("hoTen");
                    db.collection("YeuCauHoTro").document(ticketId)
                        .update("trangThai", "DangXuLy",
                                "capNhatLuc", Timestamp.now(),
                                "ktvUid", user.getUid(),
                                "ktvTen", tenKtv != null ? tenKtv : "");
                });
        })
        .setNegativeButton("Xem thôi", (d, w) -> {
            // Chế độ readOnly — ẩn các nút thao tác, không thay đổi trạng thái
            if (layoutNhapGhiChu != null) layoutNhapGhiChu.setVisibility(View.GONE);
            View btnDong = findViewById(R.id.btnDongTicket);
            if (btnDong != null) btnDong.setVisibility(View.GONE);
        })
        .setCancelable(false)
        .show();
}
```

---

## Fix 4: LichSuChatActivity — Thêm chip filter theo trạng thái

**Vấn đề:** Hiển thị tất cả ticket không có filter.

**Giải pháp:** Thêm ChipGroup vào layout. Trong Activity, thêm field `danhSachGoc` và `filterHienTai`. Snapshot listener hiện tại giữ nguyên — chỉ thêm bước lưu vào `danhSachGoc` rồi gọi `apDungFilter()`.

### Patch trong `activity_lich_su_chat.xml`

Thêm HorizontalScrollView + ChipGroup ngay trước RecyclerView (không xóa view nào):
```xml
<HorizontalScrollView
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    <com.google.android.material.chip.ChipGroup
        android:id="@+id/chipGroupFilter"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:singleSelection="true"
        app:selectionRequired="true">
        <com.google.android.material.chip.Chip android:id="@+id/chipTatCa"
            android:text="Tất cả" android:checkable="true" android:checked="true"/>
        <com.google.android.material.chip.Chip android:id="@+id/chipDangXuLy"
            android:text="Đang xử lý" android:checkable="true"/>
        <com.google.android.material.chip.Chip android:id="@+id/chipDaXuLy"
            android:text="Đã xử lý" android:checkable="true"/>
        <com.google.android.material.chip.Chip android:id="@+id/chipHangCho"
            android:text="Hàng chờ" android:checkable="true"/>
    </com.google.android.material.chip.ChipGroup>
</HorizontalScrollView>
```

### Patch trong `LichSuChatActivity.java`

Thêm 2 field mới (không xóa field cũ):
```java
private List<YeuCauHoTro> danhSachGoc = new ArrayList<>();
private String filterHienTai = null; // null = tất cả
```

Trong `onCreate`, sau `rv.setAdapter(adapter)`, thêm setup chip:
```java
com.google.android.material.chip.ChipGroup chipGroup = findViewById(R.id.chipGroupFilter);
if (chipGroup != null) {
    chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
        if (checkedIds.contains(R.id.chipDangXuLy)) filterHienTai = "DangXuLy";
        else if (checkedIds.contains(R.id.chipDaXuLy)) filterHienTai = "DaXuLy";
        else if (checkedIds.contains(R.id.chipHangCho)) filterHienTai = "HangCho";
        else filterHienTai = null;
        apDungFilter();
    });
}
```

Trong snapshot listener hiện tại, thay `adapter.capNhat(list)` bằng:
```java
danhSachGoc = list; // lưu gốc
apDungFilter();     // filter rồi mới hiển thị
```

Thêm method mới:
```java
private void apDungFilter() {
    List<YeuCauHoTro> filtered = new ArrayList<>();
    for (YeuCauHoTro t : danhSachGoc) {
        if (filterHienTai == null || filterHienTai.equals(t.getTrangThai())) {
            filtered.add(t);
        }
    }
    adapter.capNhat(filtered);
    layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    rv.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
}
```

*Dùng vòng for thay vì stream để tương thích minSdk 24 không cần lambda phức tạp.*

---

## Fix 5: DanhGiaKTVActivity — Gợi ý đánh giá sản phẩm sau khi hoàn thành

**Vấn đề:** Sau khi lưu DanhGiaKTV thành công, code gọi `finish()` ngay.

**Giải pháp:** Giữ nguyên toàn bộ `guiDanhGia()`. Chỉ thay dòng `finish()` trong `addOnSuccessListener` bằng gọi method mới `hienDialogGoiYDanhGiaSanPham()`.

### Patch trong `DanhGiaKTVActivity.java`

Trong `guiDanhGia()`, `addOnSuccessListener` hiện tại:
```java
.addOnSuccessListener(ref -> {
    capNhatDiemKtv();
    danhDauDaXuLy();
    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
    finish(); // ← chỉ thay dòng này
})
```

Thay `finish()` bằng:
```java
.addOnSuccessListener(ref -> {
    capNhatDiemKtv();
    danhDauDaXuLy();
    Toast.makeText(this, "Cảm ơn bạn đã đánh giá!", Toast.LENGTH_SHORT).show();
    if (sanPham != null && !sanPham.isEmpty()) {
        hienDialogGoiYDanhGiaSanPham();
    } else {
        finish();
    }
})
```

Thêm method mới (không đụng method nào cũ):
```java
private void hienDialogGoiYDanhGiaSanPham() {
    new androidx.appcompat.app.AlertDialog.Builder(this)
        .setTitle("Đánh giá sản phẩm")
        .setMessage("Bạn có muốn đánh giá sản phẩm " + sanPham + " không?")
        .setPositiveButton("Đánh giá ngay", (d, w) -> {
            Intent intent = new Intent(this, com.example.customercareproject.ui.danhgia.DanhGiaActivity.class);
            intent.putExtra("sanPham", sanPham);
            startActivity(intent);
            finish();
        })
        .setNegativeButton("Bỏ qua", (d, w) -> finish())
        .setCancelable(false)
        .show();
}
```

---

## Correctness Properties

### Property 1: FAB chỉ hiện dialog khi có sản phẩm
`danhSachSanPhamHienTai` được gán trong `hienThiSanPham()` — nếu list rỗng, FAB hiện Toast thay vì dialog trống.

### Property 2: Nút "Hủy yêu cầu" chỉ hiện khi chưa có KTV
Nút chỉ `VISIBLE` trong nhánh `chuaCoKtv == true`. Các nhánh còn lại đều set `GONE`.

### Property 3: Dialog nhận ticket chỉ hiện 1 lần
`daCapNhatTrangThai = true` được set trước khi gọi `hienDialogNhanTicket()` — Firestore snapshot trigger lần 2 sẽ bỏ qua.

### Property 4: Filter không mất dữ liệu gốc
`danhSachGoc` giữ toàn bộ list từ Firestore. `apDungFilter()` tạo list mới từ `danhSachGoc`, không modify `danhSachGoc`.

### Property 5: Gợi ý đánh giá sản phẩm không block luồng chính
`danhDauDaXuLy()` và `capNhatDiemKtv()` đã chạy xong trước khi hiện dialog. Dù user chọn "Bỏ qua", `daDanhGiaKtv = true` đã được set.

---

## Error Handling

| Tình huống | Xử lý |
|---|---|
| Hủy ticket thất bại (Firestore lỗi) | Toast lỗi, không `finish()`, user thử lại |
| Dialog nhận ticket: Firestore update thất bại | Toast lỗi, `daCapNhatTrangThai` vẫn `true` — không hiện dialog lại |
| `danhSachSanPhamHienTai` null/rỗng khi FAB click | Toast "Chưa có sản phẩm đăng ký", không crash |
| `sanPham` null trong DanhGiaKTVActivity | Check null → fallback `finish()` trực tiếp |

---

## Files cần thay đổi (patch, không xóa)

| File | Thay đổi |
|---|---|
| `HomeActivity.java` | Thêm field `danhSachSanPhamHienTai`; sửa lambda FAB; gán list trong `hienThiSanPham()` |
| `activity_chat_khach_hang.xml` | Thêm Button `btnHuyYeuCau` (visibility=gone) |
| `ChatKhachHangActivity.java` | Thêm field `btnHuyYeuCau`; setup click; thêm 1 dòng show/hide trong ticketListener |
| `KtvTicketDetailActivity.java` | Thay 1 đoạn `db.update(...)` bằng `hienDialogNhanTicket()`; thêm method mới |
| `activity_lich_su_chat.xml` | Thêm HorizontalScrollView + ChipGroup trước RecyclerView |
| `LichSuChatActivity.java` | Thêm 2 field; setup chip listener; sửa snapshot listener; thêm `apDungFilter()` |
| `DanhGiaKTVActivity.java` | Thay `finish()` bằng check + `hienDialogGoiYDanhGiaSanPham()`; thêm method mới |
