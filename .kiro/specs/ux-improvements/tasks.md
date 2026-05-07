# Implementation Plan: UX Improvements

## Overview

Triển khai 4 nhóm tính năng UX mới cho ứng dụng CSKH B2B Android (minSdk 26, Java). Tất cả thay đổi là **thêm mới / mở rộng**, không xóa code cũ. Thứ tự: E (Tìm kiếm & lọc) → F (Đính kèm ảnh) → G (SLA & thời gian chờ) → H (Template trả lời nhanh).

## Tasks

---

### Nhóm E — Tìm kiếm & Lọc Ticket

- [x] 1. Thêm tìm kiếm và lọc ticket trong LichSuChatActivity (Khách hàng)
  - [x] 1.1 Thêm SearchView + ChipGroup lọc trạng thái vào layout `activity_lich_su_chat.xml`
    - Thêm `SearchView` (hoặc `TextInputLayout` + `EditText`) phía trên RecyclerView hiện có
    - Thêm `HorizontalScrollView` chứa `ChipGroup` với 5 chip: Tất cả, Chờ xử lý, Hàng chờ, Đang xử lý, Đã xử lý
    - Thêm `ChipGroup` thứ hai cho lọc thời gian: Tất cả, 7 ngày qua, 30 ngày qua
    - Không xóa RecyclerView `rvLichSu` và `layoutEmpty` hiện có
    - _Requirements: E1.1, E2.1, E2.3_

  - [x] 1.2 Cập nhật `LichSuChatActivity.java` để xử lý tìm kiếm và lọc phía client
    - Lưu `danhSachGoc` (toàn bộ list từ Firestore) riêng với `danhSachHienThi`
    - Thêm method `apDungBoDanhSach()` lọc theo từ khóa (tieuDeLoi, sanPham, case-insensitive), trạng thái, và khoảng thời gian (AND logic)
    - Gắn `TextWatcher` vào SearchView → gọi `apDungBoDanhSach()`
    - Gắn `OnCheckedChangeListener` vào 2 ChipGroup → gọi `apDungBoDanhSach()`
    - Hiển thị "Không tìm thấy ticket phù hợp" khi kết quả rỗng
    - _Requirements: E1.2, E1.3, E1.4, E2.2, E2.4, E2.5_

  - [ ]* 1.3 Viết unit test cho logic lọc trong LichSuChatActivity
    - Test lọc theo từ khóa (có kết quả, không có kết quả, xóa từ khóa)
    - Test lọc theo trạng thái kết hợp thời gian (AND logic)
    - _Requirements: E1.2, E1.3, E1.4, E2.5_

- [x] 2. Thêm chip lọc sản phẩm và ưu tiên trong KtvDashboardActivity
  - [x] 2.1 Thêm 2 ChipGroup vào layout `activity_ktv_dashboard.xml`
    - Thêm `HorizontalScrollView` + `ChipGroup` lọc sản phẩm bên dưới `tabLayout` hiện có (7 chip: Tất cả + 6 sản phẩm)
    - Thêm `ChipGroup` lọc ưu tiên: Tất cả, Cao, TrungBinh, Thap
    - Không xóa `tabLayout` 4 tab và `rvTickets` hiện có
    - _Requirements: E3.1, E3.2_

  - [x] 2.2 Cập nhật `KtvDashboardActivity.java` để tích hợp filter chip vào query Firestore
    - Thêm field `filterSanPham` (String, mặc định null) và `filterUuTien` (String, mặc định null)
    - Cập nhật method `taiTickets()`: build query động — nếu `filterSanPham != null` thêm `whereEqualTo("sanPham", filterSanPham)`, tương tự cho `filterUuTien`
    - Gắn `OnCheckedChangeListener` vào 2 ChipGroup → cập nhật filter và gọi lại `taiTickets()`
    - Khi chọn "Tất cả" → set filter về null
    - _Requirements: E3.3, E3.4, E3.5, E3.6_

  - [ ]* 2.3 Viết property test cho logic build query filter
    - **Property 1: Kết hợp filter** — với bất kỳ tổ hợp (trangThai, sanPham, uuTien), query luôn chứa đúng các `whereEqualTo` tương ứng và không chứa constraint của filter "Tất cả"
    - **Validates: Requirements E3.3, E3.4, E3.5**

- [ ] 3. Checkpoint E — Đảm bảo tất cả test pass
  - Ensure all tests pass, ask the user if questions arise.

---

### Nhóm F — Đính Kèm Ảnh Chat

- [x] 4. Thêm dependency Firebase Storage và Glide vào build.gradle.kts
  - Thêm `implementation(libs.firebase.storage)` vào `app/build.gradle.kts` (trong block firebase BoM)
  - Thêm `implementation("com.github.bumptech.glide:glide:4.16.0")` nếu chưa có
  - Thêm `annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")` nếu dùng Java
  - Thêm `MANAGE_MEDIA`, `READ_MEDIA_IMAGES` permission vào `AndroidManifest.xml` (API 33+) và `READ_EXTERNAL_STORAGE` (API < 33)
  - _Requirements: F3.1_

- [x] 5. Mở rộng model `TinNhan.java` để hỗ trợ tin nhắn ảnh (backward compat)
  - Thêm field `String loaiTin` (mặc định `"van_ban"`) vào `TinNhan.java`
  - Thêm field `String anhUrl` vào `TinNhan.java`
  - Thêm getter/setter cho 2 field mới
  - Không xóa bất kỳ field hay constructor nào hiện có
  - _Requirements: F3.2, F3.3_

- [x] 6. Thêm nút đính kèm ảnh vào ChatKhachHangActivity (Khách hàng)
  - [x] 6.1 Cập nhật layout `activity_chat_khach_hang.xml`
    - Thêm `ImageButton` (icon attach/image) vào `layoutInputBar` bên cạnh `btnGui` hiện có
    - Thêm `ProgressBar` (visibility GONE mặc định) trong input bar để hiển thị khi upload
    - Không xóa `edtTinNhan`, `btnGui`, `btnGoiThoai` hiện có
    - _Requirements: F1.1, F1.6_

  - [x] 6.2 Cập nhật `ChatKhachHangActivity.java` để xử lý chọn ảnh và upload
    - Thêm `ActivityResultLauncher` để mở image picker (ACTION_GET_CONTENT, type image/*)
    - Khi ảnh được chọn: kiểm tra kích thước ≤ 10MB, nếu vượt → Toast "Ảnh quá lớn, vui lòng chọn ảnh dưới 10MB"
    - Upload lên Firebase Storage path `chat_images/{ticketId}/{timestamp}_{filename}`
    - Trong khi upload: hiện ProgressBar, disable nút đính kèm
    - Khi upload thành công: lưu message với `loaiTin = "anh"`, `anhUrl = downloadUrl`, ẩn ProgressBar, enable nút
    - Khi upload thất bại: Toast "Tải ảnh lên thất bại, vui lòng thử lại", ẩn ProgressBar, enable nút
    - _Requirements: F1.2, F1.3, F1.4, F1.5, F1.6, F1.7_

  - [ ]* 6.3 Viết unit test cho logic kiểm tra kích thước ảnh
    - Test file ≤ 10MB → cho phép upload
    - Test file > 10MB → hiển thị Toast lỗi, không upload
    - _Requirements: F1.7_

- [x] 7. Mở rộng `ChatAdapter.java` để render tin nhắn ảnh
  - [x] 7.1 Thêm ViewType `VIEW_TYPE_TEXT = 0`, `VIEW_TYPE_IMAGE_ME = 1`, `VIEW_TYPE_IMAGE_OTHER = 2` vào `ChatAdapter`
    - Override `getItemViewType()`: đọc field `loaiTin` từ Map, nếu `"anh"` → trả về IMAGE viewtype tương ứng
    - Nếu `loaiTin` null hoặc `"van_ban"` → trả về TEXT viewtype (giữ nguyên logic cũ)
    - _Requirements: F2.3_

  - [x] 7.2 Tạo layout `item_chat_image.xml` cho bubble ảnh
    - Layout tương tự `item_chat.xml` nhưng thay `TextView` nội dung bằng `ImageView` (thumbnail)
    - Có layout cho cả 2 phía: KTV (phải) và Khách hàng (trái)
    - _Requirements: F2.1_

  - [x] 7.3 Thêm `ImageViewHolder` vào `ChatAdapter` và dùng Glide để load ảnh
    - Trong `onCreateViewHolder`: inflate `item_chat_image.xml` cho IMAGE viewtype
    - Trong `onBindViewHolder`: dùng `Glide.with(context).load(anhUrl).placeholder(R.drawable.ic_image_placeholder).into(imageView)`
    - Khi tap vào thumbnail → mở `FullScreenImageActivity` (tạo mới) hoặc dùng Intent ACTION_VIEW
    - Nếu load thất bại → hiển thị placeholder icon
    - _Requirements: F2.1, F2.2, F2.4_

  - [ ]* 7.4 Viết property test cho ChatAdapter getItemViewType
    - **Property 2: ViewType consistency** — với bất kỳ message nào có `loaiTin = "anh"`, `getItemViewType()` luôn trả về IMAGE viewtype; với `loaiTin = "van_ban"` hoặc null luôn trả về TEXT viewtype
    - **Validates: Requirements F2.3**

- [x] 8. Checkpoint F — Đảm bảo tất cả test pass
  - Ensure all tests pass, ask the user if questions arise.

---

### Nhóm G — SLA & Thời Gian Chờ

- [x] 9. Hiển thị thời gian chờ ước tính trong ChatKhachHangActivity
  - [x] 9.1 Thêm `TextView tvThoiGianCho` vào layout `activity_chat_khach_hang.xml`
    - Thêm TextView bên dưới `tvSubtitle`, mặc định `visibility = GONE`
    - _Requirements: G1.1, G1.3_

  - [x] 9.2 Cập nhật `ChatKhachHangActivity.java` để tính và hiển thị thời gian chờ
    - Khi ticket có `trangThai = "HangCho"`: hiện `tvThoiGianCho`, query đếm KTV có `trangThai = "Ran"`
    - Tính `soKtvRanh`: nếu > 0 → hiển thị "Ước tính ~X phút" (X = viTriHangCho / soKtvRanh * 5), nếu = 0 → "Hiện chưa có kỹ thuật viên rảnh, vui lòng chờ"
    - Dùng `Handler.postDelayed()` để cập nhật lại mỗi 60 giây
    - Khi ticket thoát khỏi HangCho → ẩn `tvThoiGianCho`, hủy Handler
    - _Requirements: G1.1, G1.2, G1.3_

  - [ ]* 9.3 Viết unit test cho công thức tính thời gian chờ ước tính
    - Test soKtvRanh > 0 với các vị trí hàng chờ khác nhau
    - Test soKtvRanh = 0 → hiển thị thông báo không có KTV
    - _Requirements: G1.1_

- [x] 10. Hiển thị thời gian đã chờ trên card ticket trong HomeActivity
  - [x] 10.1 Thêm `TextView tvDaCho` vào layout card ticket trong `activity_home.xml`
    - Thêm TextView "Đã chờ: Xm" vào `cardTicketActive`, mặc định `visibility = GONE`
    - _Requirements: G2.1, G2.3_

  - [x] 10.2 Cập nhật `HomeActivity.java` để tính và cập nhật thời gian đã chờ
    - Khi ticket có `trangThai` trong ["HangCho", "ChoXuLy"]: tính elapsed = `(now - taoLuc) / 60000` phút, hiển thị "Đã chờ: Xm"
    - Khi `trangThai = "DangXuLy"`: ẩn tvDaCho, hiển thị "KTV đang xử lý"
    - Dùng `Handler.postDelayed()` cập nhật mỗi 60 giây
    - Hủy Handler trong `onDestroy()`
    - _Requirements: G2.1, G2.2, G2.3_

  - [ ]* 10.3 Viết unit test cho logic tính elapsed time
    - **Property 3: Elapsed time monotonicity** — với bất kỳ `taoLuc` nào trong quá khứ, elapsed time luôn ≥ 0 và tăng theo thời gian
    - **Validates: Requirements G2.1**

- [x] 11. Thêm badge cảnh báo SLA và filter "Quá hạn" trong AdminTicketsFragment
  - [x] 11.1 Cập nhật `TicketAdapter.java` để hiển thị badge "⚠ Quá 30 phút"
    - Thêm `TextView tvBadgeQuaHan` vào layout `item_ticket.xml` (hoặc layout tương đương), mặc định GONE
    - Trong `onBindViewHolder`: nếu `trangThai` là "HangCho" hoặc "ChoXuLy" VÀ elapsed > 30 phút → hiện badge đỏ "⚠ Quá 30 phút"
    - Khi `trangThai = "DangXuLy"` → ẩn badge
    - _Requirements: G3.1, G3.3_

  - [x] 11.2 Cập nhật `AdminTicketsFragment.java` để sort và filter ticket quá hạn
    - Sau khi nhận list từ Firestore, sort: ticket quá hạn (elapsed > 30 phút) lên đầu
    - Thêm `Switch` hoặc `Chip` "Chỉ hiện quá hạn" vào layout `fragment_admin_tickets.xml` (tạo layout nếu chưa có)
    - Khi toggle active: lọc chỉ hiện ticket có elapsed > 30 phút
    - _Requirements: G3.2, G3.4_

  - [ ]* 11.3 Viết property test cho logic sort và filter quá hạn
    - **Property 4: Sort stability** — sau khi sort, tất cả ticket quá hạn luôn xuất hiện trước ticket chưa quá hạn
    - **Validates: Requirements G3.2**

- [x] 12. Checkpoint G — Đảm bảo tất cả test pass
  - Ensure all tests pass, ask the user if questions arise.

---

### Nhóm H — Template Trả Lời Nhanh

- [x] 13. Tạo model `TemplateTrLoi.java` và cấu trúc Firestore
  - Tạo file `app/src/main/java/com/example/customercareproject/model/TemplateTrLoi.java`
  - Fields: `String id`, `String tieuDe`, `String noiDung`, `String sanPham`, `Timestamp taoLuc`
  - Thêm constructor rỗng (bắt buộc cho Firestore deserialization) và constructor đầy đủ
  - Thêm đầy đủ getter/setter
  - _Requirements: H3.1, H3.2_

- [x] 14. Thêm nút Template và BottomSheet vào KtvChatActivity
  - [x] 14.1 Thêm `ImageButton btnTemplate` vào layout `activity_chat_khach_hang.xml` (dùng chung với KtvChatActivity)
    - Thêm button icon template vào `layoutInputBar` bên cạnh các button hiện có
    - Không xóa `btnGui`, `btnGoiThoai` hiện có
    - _Requirements: H1.1_

  - [x] 14.2 Tạo layout `bottom_sheet_template.xml`
    - `RecyclerView` để hiển thị danh sách template
    - `TextView` hiển thị "Chưa có template cho sản phẩm này" khi list rỗng
    - _Requirements: H1.2, H1.5_

  - [x] 14.3 Tạo `TemplateAdapter.java` trong package `ui/ktv`
    - Adapter đơn giản hiển thị `tieuDe` và preview `noiDung` (truncate 50 ký tự)
    - Interface callback `OnTemplateSelected` trả về `TemplateTrLoi` được chọn
    - _Requirements: H1.3_

  - [x] 14.4 Cập nhật `KtvChatActivity.java` để tích hợp template
    - Lấy `sanPham` từ Firestore document `YeuCauHoTro/{ticketId}` khi load activity
    - Khi tap `btnTemplate`: tạo `BottomSheetDialog`, inflate `bottom_sheet_template.xml`, query `TemplateTrLoi` where `sanPham == currentSanPham` orderBy `tieuDe`
    - Khi chọn template: set `edtTinNhan.setText(template.getNoiDung())`, đóng bottom sheet
    - Nếu list rỗng: hiển thị empty message
    - _Requirements: H1.2, H1.3, H1.4, H1.5_

  - [ ]* 14.5 Viết property test cho TemplateAdapter
    - **Property 5: Round-trip consistency** — template được lưu với `tieuDe` và `noiDung` bất kỳ, khi query lại theo `sanPham` phải trả về đúng `tieuDe` và `noiDung` đã lưu
    - **Validates: Requirements H3.3**

- [x] 15. Thêm tab "Template" vào AdminKnowledgeFragment
  - [x] 15.1 Cập nhật layout `fragment_admin_knowledge.xml` để thêm TabLayout + ViewPager2
    - Thêm `TabLayout` với 2 tab: "Lỗi phát sinh" (tab cũ) và "Template" (tab mới)
    - Thêm `ViewPager2` để chứa nội dung 2 tab
    - Giữ nguyên `spinnerSanPham` và `fabThemLoi` hiện có (tái sử dụng hoặc điều chỉnh visibility theo tab)
    - _Requirements: H2.1_

  - [x] 15.2 Tạo layout `fragment_template_list.xml` cho tab Template
    - `RecyclerView` hiển thị danh sách template
    - `MaterialButton` "Thêm template"
    - _Requirements: H2.2, H2.3_

  - [x] 15.3 Tạo `TemplateAdminAdapter.java` trong package `ui/admin`
    - Hiển thị `tieuDe`, `sanPham`, preview `noiDung`
    - Nút xóa với callback `OnDeleteTemplate`
    - _Requirements: H2.6_

  - [x] 15.4 Cập nhật `AdminKnowledgeFragment.java` để tích hợp tab Template
    - Refactor fragment để dùng `ViewPager2` + `FragmentStateAdapter` với 2 tab
    - Tab "Lỗi phát sinh": giữ nguyên logic `taiDanhSachLoi()` và `hienDialogThemLoi()` hiện có
    - Tab "Template": load từ `TemplateTrLoi` where `sanPham == selectedSanPham`
    - Nút "Thêm template": mở dialog với fields `tieuDe` (required), `noiDung` (required), `sanPham` (pre-filled)
    - Validate: nếu `tieuDe` hoặc `noiDung` rỗng → Toast "Vui lòng nhập tiêu đề và nội dung template"
    - Xóa template: confirmation dialog trước khi delete
    - _Requirements: H2.1, H2.2, H2.3, H2.4, H2.5, H2.6_

  - [ ]* 15.5 Viết unit test cho validation dialog thêm template
    - Test submit với tieuDe rỗng → Toast lỗi, không lưu
    - Test submit với noiDung rỗng → Toast lỗi, không lưu
    - Test submit hợp lệ → lưu document với đúng fields
    - _Requirements: H2.4, H2.5_

- [x] 16. Checkpoint H — Đảm bảo tất cả test pass
  - Ensure all tests pass, ask the user if questions arise.

---

## Notes

- Tasks đánh dấu `*` là optional, có thể bỏ qua để MVP nhanh hơn
- Mỗi task reference requirement cụ thể để traceability
- Thứ tự task đảm bảo không có code "treo" — mỗi bước tích hợp vào bước trước
- Backward compatibility: `loaiTin = "van_ban"` là default cho tin nhắn text cũ, `TinNhan` model chỉ thêm field mới
- Không xóa 4 tab cũ trong `KtvDashboardActivity`, không xóa tab "Lỗi phát sinh" trong `AdminKnowledgeFragment`
