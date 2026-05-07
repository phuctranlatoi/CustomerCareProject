# Tasks: Cải thiện chức năng & Giao diện

> Nguyên tắc: KHÔNG xóa code cũ. Mỗi task độc lập, làm từng cái một.

---

## NHÓM 1: SỬA LỖI LOGIC HIỆN CÓ

- [x] 1.1 Fix đếm sai trạng thái ticket trong AdminThongKeFragment
  - Hiện tại: `HangCho` không được tính vào biến `cho` nên số liệu sai
  - Sửa: `if ("ChoXuLy".equals(ts) || "HangCho".equals(ts)) cho++;`
  - File: `AdminThongKeFragment.java`

- [x] 1.2 Fix template load race condition trong KtvChatActivity
  - Hiện tại: `currentSanPham` được set bất đồng bộ, bấm nút template ngay khi mở → danh sách trống
  - Disable nút template cho đến khi `currentSanPham` được load xong từ Firestore
  - File: `KtvChatActivity.java`

- [x] 1.3 Chống tạo ticket trùng trong YeuCauHoTroActivity
  - Hiện tại: bấm "Gửi" nhiều lần khi mạng chậm → tạo nhiều ticket giống nhau
  - Disable nút + hiện ProgressBar sau lần bấm đầu tiên, re-enable nếu có lỗi
  - File: `YeuCauHoTroActivity.java`

- [x] 1.4 Fix click ticket trong AdminTicketsFragment không làm gì
  - Hiện tại: `ticket -> {}` — click vào ticket không có action
  - Mở `KtvTicketDetailActivity` với flag read-only khi admin click
  - File: `AdminTicketsFragment.java`

- [x] 1.5 Giới hạn query tab "Đã xử lý" trong KtvDashboardActivity
  - Hiện tại: load toàn bộ lịch sử không giới hạn → chậm khi nhiều ticket
  - Thêm `.limit(50)` cho query tab `DaXuLy`
  - File: `KtvDashboardActivity.java`

---

## NHÓM 2: CẢI THIỆN CHỨC NĂNG HIỆN CÓ

- [x] 2.1 HomeActivity — Thông báo khi gói hết hạn hoặc tạm dừng
  - Hiện tại: gói `HetHan`/`TamDung` → hiện danh sách trống, không giải thích
  - Thêm card thông báo rõ ràng: "Gói đăng ký đã hết hạn. Vui lòng liên hệ để gia hạn."
  - File: `HomeActivity.java`, `activity_home.xml`

- [x] 2.2 KtvTicketDetailActivity — Nút gọi điện trực tiếp
  - Hiện tại: muốn gọi phải vào chat rồi mới bấm nút gọi
  - Thêm nút "Gọi điện" trực tiếp trên màn hình detail, lấy `uid` từ `ticket.uid`
  - File: `KtvTicketDetailActivity.java`, `activity_ktv_ticket_detail.xml`

- [x] 2.3 KtvTicketDetailActivity — Dialog xác nhận trước khi đóng ticket
  - Hiện tại: bấm "Đóng ticket" → đóng ngay không hỏi
  - Thêm AlertDialog: "Bạn có chắc muốn đóng ticket này?"
  - File: `KtvTicketDetailActivity.java`

- [x] 2.4 KtvTicketDetailActivity — Quick note preset
  - Thêm row chip phía trên form ghi chú: "Đang kiểm tra", "Chờ phản hồi khách", "Đã liên hệ điện thoại", "Đã giải quyết"
  - Click chip → điền sẵn vào `edtGhiChu`, KTV có thể sửa thêm
  - Giữ nguyên form ghi chú tự do hiện có
  - File: `KtvTicketDetailActivity.java`, `activity_ktv_ticket_detail.xml`

- [x] 2.5 KtvDashboardActivity — Tìm kiếm ticket
  - Thêm SearchView phía trên danh sách
  - Filter client-side theo `hoTen` hoặc `sanPham`
  - Kết hợp với tab filter trạng thái hiện có, không xóa tab
  - File: `KtvDashboardActivity.java`, `activity_ktv_dashboard.xml`

- [x] 2.6 KtvDashboardActivity — Thống kê cá nhân
  - Thêm card nhỏ ở đầu màn hình: rating trung bình (từ `DanhGiaKTV`), số ticket đang xử lý (từ `soTicketDangXuLy`), tổng đã xử lý
  - Load một lần khi mở màn hình
  - File: `KtvDashboardActivity.java`, `activity_ktv_dashboard.xml`

- [x] 2.7 AdminTicketsFragment — Bộ lọc theo sản phẩm và KTV
  - Thêm dropdown lọc theo sản phẩm (từ `SanPham.DANH_SACH`)
  - Thêm dropdown lọc theo KTV (query `NguoiDung` có `vaiTro=KTV`)
  - Filter client-side trên `danhSachGoc`, kết hợp với switch "Quá hạn" hiện có
  - File: `AdminTicketsFragment.java`, `fragment_admin_tickets.xml`

- [x] 2.8 AdminTicketsFragment — Admin assign thủ công KTV
  - Thêm nút "Assign KTV" trên ticket `HangCho`
  - Click → dialog chọn KTV đang Ran từ danh sách
  - Update `ktvUid`, `ktvTen`, `trangThai = "ChoXuLy"` + tăng counter KTV
  - File: `AdminTicketsFragment.java`

- [x] 2.9 KtvChatActivity — Template auto-suggest khi gõ
  - Thêm RecyclerView nhỏ phía trên input box, ẩn mặc định
  - Khi gõ >= 2 ký tự → filter template theo keyword trong `tieuDe` hoặc `noiDung`
  - Hiện tối đa 3 gợi ý, click → điền vào input và ẩn list
  - File: `KtvChatActivity.java`

---

## NHÓM 3: GIAO DIỆN — Làm mới UI hiện đại

- [x] 3.1 Redesign màn hình Login / Register
  - Bỏ layout cũ, thiết kế lại: nền trắng sạch, logo/brand ở trên, form gọn
  - Input dùng `TextInputLayout` với outline style, bo góc 12dp
  - Nút đăng nhập full-width, màu primary, bo góc 12dp
  - Thêm divider "hoặc" giữa đăng nhập email và Google
  - File: `activity_login.xml`, `activity_register.xml`

- [x] 3.2 Redesign HomeActivity
  - Header: avatar chữ cái đầu + tên user + icon profile/logout
  - Card ticket active: thiết kế nổi bật hơn, hiện rõ trạng thái bằng màu sắc (xanh/cam/xám)
  - Danh sách sản phẩm: dùng CardView bo góc 16dp, có icon sản phẩm, shadow nhẹ
  - Thêm FAB "Yêu cầu hỗ trợ" góc dưới phải
  - File: `activity_home.xml`, `item_san_pham.xml`

- [x] 3.3 Redesign KtvDashboardActivity
  - Header: avatar + tên KTV + badge trạng thái (Ran/Ban) dạng pill màu sắc
  - Card thống kê cá nhân: 3 metric nhỏ ngang hàng (rating, đang xử lý, tổng)
  - Tab layout: dùng custom tab với indicator line dưới, không dùng default
  - Item ticket trong list: hiện rõ tên khách, sản phẩm, thời gian, badge ưu tiên màu
  - File: `activity_ktv_dashboard.xml`, `TicketAdapter.java`, item layout ticket

- [x] 3.4 Redesign KtvTicketDetailActivity
  - Layout 2 section rõ ràng: thông tin ticket (trên) + ghi chú tiến độ (dưới)
  - Thông tin khách: card riêng với icon phone/email có thể click
  - Badge ưu tiên: pill màu đỏ/cam/xanh thay vì text thuần
  - Ghi chú tiến độ: timeline style (đường dọc + dot + nội dung)
  - Nút hành động: "Chat", "Gọi điện", "Đóng ticket" — layout ngang, icon + text
  - File: `activity_ktv_ticket_detail.xml`

- [x] 3.5 Redesign màn hình Chat (ChatKhachHangActivity + KtvChatActivity)
  - Header: avatar chữ cái + tên + trạng thái kết nối
  - Bubble tin nhắn: bo góc 18dp, màu phân biệt rõ KTV (xanh nhạt) vs khách (trắng/xám)
  - Timestamp nhỏ dưới mỗi bubble
  - Input bar: bo góc, icon gửi đổi màu khi có text
  - File: `activity_chat_khach_hang.xml`, `item_chat.xml`, `item_chat_image.xml`

- [x] 3.6 Redesign AdminDashboardActivity
  - Bottom navigation: icon rõ hơn, label ngắn gọn
  - Tab Thống kê: stat card dạng grid 2x2, số liệu lớn + label nhỏ bên dưới
  - Progress bar cảm xúc: thêm label % bên phải mỗi bar
  - AI insight card: thiết kế card riêng biệt, có border màu theo ưu tiên
  - File: `activity_admin_dashboard.xml`, `fragment_admin_thong_ke.xml`

- [x] 3.7 Redesign item danh sách dùng chung
  - `item_user_admin.xml`: avatar chữ cái + tên + role badge + trạng thái
  - `item_goi_dang_ky.xml`: tên công ty + MST + badge trạng thái màu + danh sách SP dạng chip
  - `item_danh_gia_xau.xml`: star rating visual + tên SP + nội dung + timestamp
  - `item_ktv_review_summary.xml`: avatar + tên KTV + star rating bar + số lượt

- [x] 3.8 Thêm Empty State cho các danh sách trống
  - Tạo layout `layout_empty_state.xml` dùng chung: icon vector + title + subtitle
  - Áp dụng vào: KtvDashboardActivity (mỗi tab), LichSuChatActivity, AdminLeadFragment, AdminTicketsFragment
  - Không dùng emoji, dùng vector drawable

- [x] 3.9 Chuẩn hóa màu sắc và typography toàn app
  - Định nghĩa color palette trong `colors.xml`: primary, secondary, surface, on-surface, error, success, warning
  - Định nghĩa text style trong `themes.xml`: heading, body, caption, label
  - Áp dụng nhất quán thay vì hardcode màu hex rải rác trong Java code
  - File: `colors.xml`, `themes.xml`














