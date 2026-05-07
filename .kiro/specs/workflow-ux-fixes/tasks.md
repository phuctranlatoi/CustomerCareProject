# Implementation Plan: Workflow UX Fixes

## Nguyên tắc thực hiện
- **Không xóa code cũ** — chỉ patch thêm vào logic hiện có
- Mỗi task chỉ sửa đúng file được chỉ định
- Giữ nguyên toàn bộ listener, adapter, method đang hoạt động

## Tasks

- [x] 1. Fix FAB "Yêu cầu hỗ trợ" trong HomeActivity
  - [x] 1.1 Thêm field `danhSachSanPhamHienTai` vào `HomeActivity.java`
    - Khai báo `private java.util.List<String> danhSachSanPhamHienTai = new java.util.ArrayList<>();`
    - Trong method `hienThiSanPham()`, thêm dòng `danhSachSanPhamHienTai = danhSachSanPham;` trước khi tạo adapter
    - Giữ nguyên toàn bộ code còn lại trong `hienThiSanPham()`
    - _Bugfix: 1.1, 2.1_

  - [x] 1.2 Sửa lambda FAB trong `HomeActivity.java`
    - Tìm đoạn `fabYeuCau.setOnClickListener(v -> { android.widget.Toast.makeText(...) })`
    - Thay nội dung lambda bằng: kiểm tra `danhSachSanPhamHienTai` rỗng → Toast; không rỗng → hiện `AlertDialog.Builder` với `setItems()` danh sách sản phẩm
    - Khi user chọn sản phẩm: `startActivity(new Intent(this, YeuCauHoTroActivity.class).putExtra("sanPham", tenChon))`
    - Không thay đổi bất kỳ dòng nào khác trong `onCreate()`
    - _Bugfix: 1.1, 2.1_

- [x] 2. Thêm nút "Hủy yêu cầu" vào ChatKhachHangActivity
  - [x] 2.1 Thêm Button vào `activity_chat_khach_hang.xml`
    - Thêm `<Button android:id="@+id/btnHuyYeuCau" android:visibility="gone" android:text="Hủy yêu cầu" style="@style/Widget.Material3.Button.TonalButton"/>` ngay sau view `tvThoiGianCho`
    - Không xóa hoặc di chuyển view nào khác
    - _Bugfix: 3.3_

  - [x] 2.2 Thêm field và setup click trong `ChatKhachHangActivity.java`
    - Thêm field `private android.widget.Button btnHuyYeuCau;`
    - Trong `onCreate()`, sau dòng `progressUpload = findViewById(...)`, thêm: `btnHuyYeuCau = findViewById(R.id.btnHuyYeuCau);` và setup `setOnClickListener` với `AlertDialog` xác nhận → gọi `dungQuetKtv()` → update Firestore `trangThai = "DaHuy"` → `finish()`
    - _Bugfix: 3.3_

  - [x] 2.3 Hiện/ẩn nút trong `ticketListener` của `ChatKhachHangActivity.java`
    - Trong nhánh `chuaCoKtv && "HangCho"` và `chuaCoKtv && "ChoXuLy"`: thêm `if (btnHuyYeuCau != null) btnHuyYeuCau.setVisibility(View.VISIBLE);`
    - Trong nhánh `!chuaCoKtv` (đã có KTV) và nhánh `"DaXuLy"`: thêm `if (btnHuyYeuCau != null) btnHuyYeuCau.setVisibility(View.GONE);`
    - Không thay đổi logic hiện có trong các nhánh đó
    - _Bugfix: 3.3_

- [x] 3. Thêm dialog xác nhận nhận ticket trong KtvTicketDetailActivity
  - [x] 3.1 Thêm method `hienDialogNhanTicket()` vào `KtvTicketDetailActivity.java`
    - Thêm method mới với `AlertDialog.Builder`, 2 nút: "Nhận" (update Firestore `DangXuLy`) và "Xem thôi" (ẩn `layoutNhapGhiChu` và `btnDongTicket`)
    - `setCancelable(false)` để bắt buộc chọn
    - Không đụng đến method nào khác
    - _Bugfix: 4.1, 4.2_

  - [x] 3.2 Thay đoạn auto-update trong `taiChiTietTicket()` của `KtvTicketDetailActivity.java`
    - Tìm đoạn `db.collection("YeuCauHoTro").document(ticketId).update("trangThai", "DangXuLy", ...)` bên trong `addOnSuccessListener` của `db.collection("NguoiDung").document(user.getUid()).get()`
    - Thay toàn bộ đoạn `db.collection("YeuCauHoTro")...update(...)` bằng `hienDialogNhanTicket();`
    - Giữ nguyên flag `daCapNhatTrangThai = true` và điều kiện `if (!daCapNhatTrangThai && ...)` phía trên
    - _Bugfix: 4.1_

- [x] 4. Thêm chip filter vào LichSuChatActivity
  - [x] 4.1 Thêm ChipGroup vào `activity_lich_su_chat.xml`
    - Thêm `HorizontalScrollView` chứa `ChipGroup` với 4 chip: "Tất cả" (id: chipTatCa, checked=true), "Đang xử lý" (chipDangXuLy), "Đã xử lý" (chipDaXuLy), "Hàng chờ" (chipHangCho)
    - Đặt ngay trước `RecyclerView` hiện có
    - Không xóa view nào
    - _Bugfix: 5.1, 5.2_

  - [x] 4.2 Thêm field và method `apDungFilter()` vào `LichSuChatActivity.java`
    - Thêm 2 field: `private List<YeuCauHoTro> danhSachGoc = new ArrayList<>();` và `private String filterHienTai = null;`
    - Thêm method `apDungFilter()`: duyệt `danhSachGoc` bằng vòng for, lọc theo `filterHienTai`, gọi `adapter.capNhat()` và cập nhật `layoutEmpty`/`rv` visibility
    - _Bugfix: 5.1, 5.2_

  - [x] 4.3 Kết nối ChipGroup và sửa snapshot listener trong `LichSuChatActivity.java`
    - Trong `onCreate()`, sau `rv.setAdapter(adapter)`: tìm ChipGroup, setup `setOnCheckedStateChangeListener` để cập nhật `filterHienTai` rồi gọi `apDungFilter()`
    - Trong snapshot listener hiện có: thay `adapter.capNhat(list)` + các dòng cập nhật visibility bằng `danhSachGoc = list; apDungFilter();`
    - Giữ nguyên toàn bộ logic build `list` từ snapshot
    - _Bugfix: 5.1, 5.2_

- [x] 5. Thêm gợi ý đánh giá sản phẩm sau khi đánh giá KTV
  - [x] 5.1 Thêm method `hienDialogGoiYDanhGiaSanPham()` vào `DanhGiaKTVActivity.java`
    - Thêm method mới với `AlertDialog.Builder`, 2 nút: "Đánh giá ngay" (navigate đến `DanhGiaActivity` với extra `"sanPham"` → `finish()`) và "Bỏ qua" (`finish()`)
    - `setCancelable(false)`
    - Không đụng đến method nào khác
    - _Bugfix: 6.1, 6.2_

  - [x] 5.2 Thay `finish()` trong `guiDanhGia()` của `DanhGiaKTVActivity.java`
    - Trong `addOnSuccessListener` của `db.collection("DanhGiaKTV").add(danhGia)`, tìm dòng `finish();`
    - Thay bằng: `if (sanPham != null && !sanPham.isEmpty()) { hienDialogGoiYDanhGiaSanPham(); } else { finish(); }`
    - Giữ nguyên `capNhatDiemKtv()`, `danhDauDaXuLy()`, Toast phía trên
    - _Bugfix: 6.2_

## Notes
- Thứ tự thực hiện: Task 1 → 2 → 3 → 4 → 5 (độc lập, không phụ thuộc nhau)
- Mỗi sub-task chỉ sửa đúng 1 file
- Không thêm dependency mới vào `build.gradle.kts`
- `AlertDialog.Builder` dùng `androidx.appcompat.app.AlertDialog.Builder` (đã có trong project)
