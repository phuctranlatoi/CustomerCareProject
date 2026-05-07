# Requirements: Code Quality & Feature Enhancement

## Tổng quan dự án hiện tại

Đây là ứng dụng Android chăm sóc khách hàng doanh nghiệp (B2B), sử dụng Firebase làm backend. Hệ thống phục vụ 3 vai trò: **Khách hàng**, **KTV (Kỹ thuật viên hỗ trợ)**, và **Admin**.

---

## Các chức năng hiện có

### 1. Xác thực & Phân quyền
- Đăng nhập bằng Email/Password và Google
- Đăng ký tài khoản khách hàng
- Quên mật khẩu
- Route tự động theo vai trò (KhachHang / KTV / Admin) sau khi đăng nhập

### 2. Khách hàng
- Xem danh sách sản phẩm đã đăng ký (theo gói ChinhThuc / DungThu)
- Tạo yêu cầu hỗ trợ (chọn sản phẩm → chọn lỗi → mô tả thêm)
- Chat realtime với KTV được phân công (gửi text + ảnh)
- Gọi thoại VoIP qua Stringee SDK
- Đánh giá KTV sau khi ticket được giải quyết (giao diện kiểu Grab)
- Đánh giá sản phẩm (giao diện / chức năng / lỗi phát sinh)
- Xem lịch sử chat

### 3. KTV (Kỹ thuật viên)
- Dashboard hiển thị danh sách ticket theo trạng thái (ChoXuLy / HangCho / DangXuLy / DaXuLy)
- Quản lý trạng thái bản thân (Rảnh / DangBan / Offline) — đồng bộ RTDB
- Xem chi tiết ticket + thêm ghi chú tiến độ có timestamp
- Chat với khách hàng + dùng template trả lời nhanh
- Gọi thoại VoIP
- Đóng ticket → hệ thống tự giảm counter và cập nhật trạng thái

### 4. Phân công thông minh (SmartRouter + Cloud Functions)
- Client-side: tìm KTV rảnh phù hợp (ưu tiên chuyên môn + ít ticket nhất)
- Cloud Function `onKtvOnline`: khi KTV vừa chuyển sang Rảnh → quét HangCho và assign
- Cloud Function `onTicketCreated`: khi ticket mới tạo → thử assign ngay
- Cloud Function `scheduledRouter`: chạy mỗi 1 phút → đảm bảo không ticket nào bị bỏ sót
- Cloud Function `onDanhGiaTot`: khi user dùng thử đánh giá ≥4 sao → tạo LeadKinhDoanh

### 5. Admin
- Dashboard 5 tab: Thống kê / Người dùng / Tickets / Gói đăng ký / Phân tích
- Thống kê: tỷ lệ hài lòng, phân bố trạng thái ticket, hiệu suất KTV, top sản phẩm
- Quản lý người dùng: tạo tài khoản KTV, gán chuyên môn, khóa/mở tài khoản
- Quản lý ticket: xem tất cả ticket, phát hiện ticket quá hạn (>30 phút)
- Quản lý gói đăng ký: thêm/sửa gói, phân loại ChinhThuc/DungThu
- Phân tích AI: phân cụm feedback theo chủ đề (NlpHelper + Groq API)
- Quản lý knowledge base: lỗi phát sinh + template trả lời
- Quản lý lead: theo dõi user dùng thử có tiềm năng chuyển đổi
- Đánh giá KTV: xem chi tiết đánh giá từng KTV

### 6. AI & NLP
- `NlpHelper.phanTichGemini()`: phân tích đánh giá đơn lẻ → tags, cảm xúc, ưu tiên
- `NlpHelper.phanTichTongHop()`: phân cụm 30 ngày feedback → insight theo chủ đề
- Fallback keyword matching khi AI không khả dụng
- Lưu insight vào collection `InsightCumDe`

### 7. Voice Call
- Gọi thoại VoIP qua Stringee SDK
- Màn hình cuộc gọi đến (IncomingCallActivity) — hiển thị trên lock screen
- Màn hình cuộc gọi đi (VoiceCallActivity)
- Auto-reconnect khi mất kết nối

---

## Luồng hoạt động chính

### Luồng Khách hàng tạo ticket
```
Login → HomeActivity → YeuCauHoTroActivity
  → SmartRouter.timKtvRanh()
    → Có KTV rảnh: assign ngay → trangThai = "ChoXuLy"
    → Không có KTV: trangThai = "HangCho" → Cloud Function xử lý
  → ChatKhachHangActivity (realtime listener)
    → KTV được assign → chat bắt đầu
    → Ticket đóng → DanhGiaKTVActivity popup
```

### Luồng KTV xử lý ticket
```
Login → KtvDashboardActivity (danh sách ticket)
  → Click ticket → KtvTicketDetailActivity
    → Xem thông tin + thêm ghi chú tiến độ
    → Mở chat → KtvChatActivity
      → Gửi tin nhắn / dùng template
      → Đóng ticket → SmartRouter.giamTicketKtv()
```

### Luồng Admin quản lý
```
Login → AdminDashboardActivity (5 tabs)
  → Thống kê: xem metrics realtime
  → Người dùng: tạo KTV / khóa tài khoản
  → Tickets: monitor + phát hiện quá hạn
  → Gói DK: quản lý subscription
  → Phân tích: chạy AI analysis
```

---

## Vấn đề cần cải thiện (không xóa code cũ)

### Bảo mật (Critical)
- REQ-SEC-01: API key Groq đang hardcode trong source code → cần chuyển sang environment variable / Firebase Remote Config
- REQ-SEC-02: Chưa có Firestore Security Rules rõ ràng → cần viết rules phân quyền theo vai trò
- REQ-SEC-03: Tin nhắn chat lưu plaintext → cần xem xét mã hóa hoặc ít nhất validate input
- REQ-SEC-04: Không có rate limiting → dễ bị spam tạo ticket

### Chất lượng code (High)
- REQ-CODE-01: Nhiều ListenerRegistration không được cleanup đúng cách → memory leak
- REQ-CODE-02: Thiếu error handling ở nhiều callback Firebase
- REQ-CODE-03: Không có input validation trên form tạo ticket / đăng ký
- REQ-CODE-04: Hardcode các giá trị như MAX_TICKET=5, 30 phút timeout
- REQ-CODE-05: Không có unit test / instrumented test

### Hiệu năng (Medium)
- REQ-PERF-01: Admin queries load toàn bộ document không có pagination
- REQ-PERF-02: Ảnh upload không có compression trước khi gửi
- REQ-PERF-03: Không có caching strategy cho dữ liệu tĩnh (sản phẩm, lỗi phát sinh)
- REQ-PERF-04: Real-time listener trên collection lớn → tốn bandwidth

### UX/UI (Medium)
- REQ-UX-01: Thiếu loading state ở nhiều màn hình
- REQ-UX-02: Thông báo lỗi chưa thân thiện với người dùng
- REQ-UX-03: Không có màn hình empty state khi danh sách trống
- REQ-UX-04: Không có xác nhận trước khi thực hiện hành động quan trọng (đóng ticket, xóa)
- REQ-UX-05: Thiếu dark mode support

### Tính năng mới đề xuất (Enhancement)
- REQ-FEAT-01: Thông báo push FCM cho khách hàng khi ticket được assign / cập nhật
- REQ-FEAT-02: Tìm kiếm và lọc ticket trong KTV dashboard
- REQ-FEAT-03: Export báo cáo thống kê (PDF/Excel) cho Admin
- REQ-FEAT-04: Offline mode — cache ticket và tin nhắn khi mất mạng
- REQ-FEAT-05: Admin audit log — ghi lại mọi hành động của admin
- REQ-FEAT-06: Tự động gợi ý template khi KTV bắt đầu gõ
- REQ-FEAT-07: Escalation tự động — ticket quá hạn tự chuyển lên admin
- REQ-FEAT-08: Dashboard KTV cải thiện — hiển thị thống kê cá nhân (rating trung bình, số ticket tuần)
