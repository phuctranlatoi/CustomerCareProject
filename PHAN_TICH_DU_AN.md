# Phân Tích Dự Án: Customer Care Project (CSKH B2B)

> Ứng dụng Android chăm sóc khách hàng doanh nghiệp (B2B), sử dụng Firebase (Auth, Firestore, Cloud Functions, FCM) và tích hợp VoIP qua Stringee.

---

## 1. Tổng Quan Kiến Trúc

```
Android Client (Java, minSdk 24)
    ├── Firebase Auth       — Đăng nhập Email/Google
    ├── Firebase Firestore  — Cơ sở dữ liệu realtime
    ├── Firebase FCM        — Push notification
    ├── Firebase RTDB       — Trạng thái online KTV realtime
    ├── Cloud Functions     — Backend logic tự động (Node.js)
    ├── Stringee SDK        — VoIP gọi thoại
    └── Groq AI (LLaMA)     — Phân tích NLP đánh giá
```

### 3 Vai Trò Người Dùng

| Vai trò | Mô tả |
|---|---|
| `KhachHang` | Đại diện doanh nghiệp, gửi yêu cầu hỗ trợ, đánh giá sản phẩm |
| `KTV` | Kỹ thuật viên, nhận và xử lý ticket hỗ trợ |
| `Admin` | Quản trị viên, xem thống kê, quản lý người dùng và gói đăng ký |

---

## 2. Cấu Trúc Dữ Liệu (Firestore Collections)

| Collection | Mô tả |
|---|---|
| `NguoiDung` | Thông tin người dùng (uid, hoTen, email, vaiTro, maSoThue, chuyenMon...) |
| `YeuCauHoTro` | Ticket hỗ trợ (trangThai, ktvUid, lichSuHoTro, uuTien...) |
| `TinNhan/{ticketId}/messages` | Tin nhắn chat realtime theo ticket |
| `DanhGia` | Đánh giá sản phẩm (soSao, tags NLP, camXuc, loaiGoi...) |
| `DanhGiaKTV` | Đánh giá kỹ thuật viên sau khi đóng ticket |
| `GoiDangKy` | Gói đăng ký sản phẩm của công ty (maSoThue, sanPhamDangKy, loaiGoi) |
| `LeadKinhDoanh` | Lead tự động từ đánh giá tốt của gói dùng thử |
| `InsightCumDe` | Kết quả phân tích AI gom cụm phản hồi |

### Trạng Thái Ticket (YeuCauHoTro)

```
HangCho → ChoXuLy → DangXuLy → DaXuLy
```

- `HangCho`: Chưa có KTV rảnh, đang chờ trong hàng đợi
- `ChoXuLy`: Đã assign KTV, chờ KTV mở ticket
- `DangXuLy`: KTV đang xử lý
- `DaXuLy`: Đã đóng ticket

---

## 3. Các Chức Năng Đã Có

### 3.1 Xác Thực & Quản Lý Tài Khoản

- **Đăng nhập** bằng Email/Password hoặc Google Sign-In (`LoginActivity`)
- **Đăng ký** tài khoản khách hàng (`RegisterActivity`):
  - Validate mã số thuế (MST) với `TaxCodeValidator`
  - Kiểm tra MST có trong `GoiDangKy` và trạng thái gói còn hoạt động
  - Kiểm tra tên công ty khớp với MST
  - Gửi email xác thực sau khi đăng ký
- **Quên mật khẩu** (`ForgotPasswordActivity`)
- **Hồ sơ cá nhân** (`ProfileActivity`) — xem và cập nhật thông tin
- **Phân quyền tự động** theo `vaiTro` trong Firestore (`MainActivity` → điều hướng đến đúng dashboard)

### 3.2 Màn Hình Khách Hàng (HomeActivity)

- Hiển thị danh sách sản phẩm **theo gói đăng ký** của công ty (lọc từ `GoiDangKy`)
- Hiển thị **card ticket đang xử lý** (realtime) — nếu có ticket ChoXuLy/DangXuLy/HangCho
- **Popup đánh giá KTV** tự động khi có ticket DaXuLy chưa đánh giá (giống Grab)
- Nút xem **lịch sử chat** (`LichSuChatActivity`)
- Nút xem **hồ sơ cá nhân**

### 3.3 Hệ Thống Ticket Hỗ Trợ

**Tạo ticket** (`YeuCauHoTroActivity`):
- Khách hàng chọn sản phẩm, mô tả vấn đề
- Hệ thống tự động tìm KTV rảnh qua `SmartRouter`
- Nếu không có KTV → lưu với `trangThai = "HangCho"`, Cloud Function tự assign sau

**Chat realtime** (`ChatKhachHangActivity`):
- Gửi/nhận tin nhắn realtime qua Firestore
- Tự động quét KTV rảnh realtime khi ticket ở trạng thái HangCho
- Thuật toán assign KTV: ưu tiên chuyên môn → ít ticket nhất (dùng Firestore transaction tránh race condition)
- Nút gọi thoại VoIP đến KTV
- Tự động mở màn hình đánh giá KTV khi ticket đóng

**Lịch sử chat** (`LichSuChatActivity`):
- Xem lại toàn bộ các ticket đã tạo

**Chi tiết lỗi** (`ChiTietLoiActivity`):
- Xem chi tiết một lỗi cụ thể

### 3.4 Đánh Giá Sản Phẩm

**DanhGiaActivity** — 3 tab:
1. **Giao Diện** (`DanhGiaFormFragment` với loai="GiaoDien")
2. **Chức Năng** (`DanhGiaFormFragment` với loai="ChucNang")
3. **Lỗi Phát Sinh** (`LoiPhatSinhFragment`)

**Kiểm soát quyền đánh giá** (`DanhGiaFormFragment`):
- Truy vấn `GoiDangKy` theo MST của user
- Chỉ hiển thị form nếu sản phẩm nằm trong `sanPhamDangKy` và gói đang hoạt động
- Gắn `loaiGoi` (ChinhThuc/DungThu), `maSoThue`, `tenCongTy` vào DanhGia khi lưu

**Phân tích NLP** (`NlpHelper`):
- Gọi Groq AI (LLaMA 3.1) để phân tích nội dung đánh giá
- Trả về: `tags` (chủ đề), `camXuc` (HaiLong/TrungBinh/KhongHaiLong), `uuTien` (Cao/TrungBinh/Thap), `tomTat`
- Fallback keyword matching nếu AI lỗi

### 3.5 Đánh Giá KTV

**DanhGiaKTVActivity**:
- RatingBar 1-5 sao
- Chip tags động (tốt/kém tùy số sao)
- Nhận xét tự do
- Tự động cập nhật `diemDanhGia` trung bình của KTV
- Đánh dấu `daDanhGiaKtv = true` trên ticket

### 3.6 Dashboard KTV (KtvDashboardActivity)

- Hiển thị trạng thái KTV: Rảnh / Đang bận / Offline (badge màu)
- **Tự động cập nhật trạng thái**: Rảnh khi không có ticket, Bận khi có ticket ChoXuLy/DangXuLy
- **4 tab lọc ticket**: Chờ xử lý | Hàng chờ | Đang xử lý | Đã xử lý
- Hiển thị tổng ticket đã xử lý
- Notification khi có ticket mới
- Đồng bộ trạng thái qua Firebase RTDB (onDisconnect tự set Offline)

**Chi tiết ticket KTV** (`KtvTicketDetailActivity`):
- Xem đầy đủ thông tin khách hàng và vấn đề
- Hiển thị mức ưu tiên (Khẩn cấp/Bình thường/Thấp)
- **Ghi chú tiến độ** (KTV Handoff):
  - Xem lịch sử ghi chú từ KTV trước
  - Thêm ghi chú mới (validate: không rỗng, tối đa 1000 ký tự)
  - Lưu bằng `FieldValue.arrayUnion()` vào `lichSuHoTro`
  - Read-only khi ticket đã đóng
- Nút nhắn tin → `KtvChatActivity`
- Nút đóng ticket → cập nhật `DaXuLy`, giảm counter KTV

**Chat KTV** (`KtvChatActivity`):
- Chat realtime với khách hàng

### 3.7 Gọi Thoại VoIP (Stringee)

- **StringeeManager**: Singleton quản lý kết nối Stringee, xử lý cuộc gọi đến
- **StringeeTokenHelper**: Tạo JWT token cho Stringee
- **VoiceCallActivity**: Giao diện gọi đi (nút nghe/cúp)
- **IncomingCallActivity**: Giao diện nhận cuộc gọi đến
- Tự động reconnect khi mất kết nối
- Lấy tên người gọi từ Firestore để hiển thị

### 3.8 Dashboard Admin (AdminDashboardActivity)

5 tab chính:

| Tab | Fragment | Chức năng |
|---|---|---|
| Tổng quan | `AdminThongKeFragment` | Thống kê tổng hợp + AI insight |
| Người dùng | `AdminUsersFragment` | Quản lý user, tạo KTV |
| Tickets | `AdminTicketsFragment` | Xem tất cả ticket |
| Gói ĐK | `AdminGoiDangKyFragment` | Quản lý gói đăng ký |
| Phân tích | `AdminPhanTichFragment` | ĐG xấu + Lead KD + ĐG KTV |

**AdminThongKeFragment**:
- Bộ lọc 7 ngày / 1 tháng
- Tổng đánh giá, tổng ticket, điểm TB KTV
- Phân bổ cảm xúc (HaiLong/TrungBinh/KhongHaiLong) với progress bar
- Thống kê theo sản phẩm (bar chart đơn giản)
- Thống kê KTV
- **Phân tích AI**: Gọi Groq AI gom cụm phản hồi, hiển thị card theo ưu tiên màu sắc

**AdminUsersFragment**:
- Danh sách người dùng
- Tạo tài khoản KTV mới (dialog)
- Khóa/mở khóa tài khoản

**AdminTicketsFragment**:
- Xem tất cả ticket với filter trạng thái

**AdminGoiDangKyFragment**:
- Thêm/sửa/xóa gói đăng ký
- Thêm/gỡ sản phẩm (phân biệt Chính thức / Dùng thử)
- Reset dữ liệu mẫu (DataSeeder)

**AdminPhanTichFragment** (3 sub-tab):
- **ĐG Xấu** (`AdminDanhGiaXauFragment`): Danh sách đánh giá soSao ≤ 2, filter 7/30 ngày, đếm số lần hỗ trợ liên quan
- **Lead KD** (`AdminLeadFragment`): Danh sách lead từ dùng thử, cập nhật trạng thái lead
- **Đánh giá KTV** (`AdminKtvReviewsFragment` + `AdminKtvReviewDetailActivity`): Tổng hợp đánh giá từng KTV

### 3.9 Cloud Functions (Backend Tự Động)

| Function | Trigger | Mô tả |
|---|---|---|
| `onKtvOnline` | KTV chuyển sang "Ran" | Quét HangCho, assign ticket phù hợp (tối đa 5 ticket/KTV) |
| `onTicketCreated` | Ticket mới tạo | Tìm KTV rảnh và assign ngay |
| `scheduledRouter` | Mỗi 1 phút | Quét HangCho, assign cho KTV rảnh (backup) |
| `onDanhGiaTot` | DanhGia mới tạo | Nếu DungThu + soSao ≥ 4 → tạo LeadKinhDoanh + gửi FCM |

**Thuật toán assign KTV**:
1. Ưu tiên KTV có chuyên môn (`chuyenMon`) phù hợp sản phẩm
2. Trong số đó, chọn KTV ít ticket nhất (`soTicketDangXuLy`)
3. Fallback: KTV rảnh bất kỳ ít ticket nhất
4. Dùng Firestore transaction để tránh race condition

### 3.10 Smart Router (Client-side)

`SmartRouter.java`:
- `timKtvRanh()`: Tìm KTV rảnh one-shot khi tạo ticket
- `tangTicketKtv()`: Tăng counter khi assign
- `giamTicketKtv()`: Giảm counter + tăng tổng đã xử lý khi đóng ticket

### 3.11 Gói Đăng Ký & Lead Kinh Doanh

**GoiDangKy**:
- Mỗi công ty (MST) có 1 gói
- Phân biệt `sanPhamChinhThuc` và `sanPhamDungThu`
- Trạng thái: HoatDong / HetHan / TamDung

**Lead tự động**:
- Khi khách dùng thử đánh giá ≥ 4 sao → Cloud Function tạo `LeadKinhDoanh`
- Gửi FCM đến nhân viên kinh doanh (`kinhDoanh = true`)
- Admin theo dõi và cập nhật trạng thái lead: Mới → Đang tư vấn → Đã đăng ký / Từ chối

---

## 4. Quy Trình Hoạt Động

### 4.1 Quy Trình Đăng Ký & Đăng Nhập

```
Người dùng → RegisterActivity
    → Nhập: hoTen, email, SDT, MST, tenCongTy, matKhau
    → Validate MST với GoiDangKy (phải tồn tại + HoatDong)
    → Firebase Auth tạo tài khoản
    → Gửi email xác thực
    → Lưu NguoiDung vào Firestore (vaiTro = "KhachHang")
    → Đăng xuất, yêu cầu xác thực email

Đăng nhập → LoginActivity
    → Email/Password hoặc Google Sign-In
    → Firebase Auth xác thực
    → MainActivity đọc vaiTro từ Firestore
    → Điều hướng: KhachHang → HomeActivity | KTV → KtvDashboardActivity | Admin → AdminDashboardActivity
```

### 4.2 Quy Trình Tạo & Xử Lý Ticket

```
Khách hàng tạo ticket (YeuCauHoTroActivity)
    → SmartRouter.timKtvRanh(sanPham)
    ├── Có KTV rảnh → assign ngay, trangThai = "ChoXuLy"
    └── Không có KTV → trangThai = "HangCho"

Cloud Function onTicketCreated (nếu HangCho)
    → timKtvRanh() → assign → trangThai = "ChoXuLy"

Cloud Function scheduledRouter (mỗi 1 phút)
    → Quét HangCho còn sót → assign

Khách hàng mở ChatKhachHangActivity
    → Lắng nghe realtime ticket
    ├── HangCho/ChoXuLy chưa có KTV → batDauQuetKtv() (realtime scan)
    ├── Có KTV → hiển thị tên KTV
    └── DaXuLy → chế độ đọc + popup đánh giá KTV

KTV nhận ticket (KtvDashboardActivity)
    → Notification khi có ticket mới
    → Mở KtvTicketDetailActivity
    → Tự động cập nhật trangThai = "DangXuLy"
    → Xử lý: chat, ghi chú tiến độ, gọi thoại
    → Đóng ticket → trangThai = "DaXuLy"
    → SmartRouter.giamTicketKtv()
```

### 4.3 Quy Trình Đánh Giá Sản Phẩm

```
Khách hàng chọn sản phẩm (HomeActivity)
    → DanhGiaActivity (3 tab: Giao Diện | Chức Năng | Lỗi Phát Sinh)

DanhGiaFormFragment.kiemTraQuyenDanhGia()
    → Query GoiDangKy theo maSoThue
    ├── Không có gói HoatDong → hiển thị thông báo, ẩn form
    ├── Sản phẩm không trong sanPhamDangKy → hiển thị thông báo, ẩn form
    └── Hợp lệ → hiển thị form đánh giá

Gửi đánh giá
    → NlpHelper.phanTichGemini() → tags, camXuc, uuTien
    → Lưu DanhGia (kèm loaiGoi, maSoThue, tenCongTy)

Cloud Function onDanhGiaTot
    → Nếu loaiGoi = "DungThu" AND soSao >= 4
    → Tạo LeadKinhDoanh
    → Gửi FCM đến kinhDoanh=true users
```

### 4.4 Quy Trình KTV Handoff (Ghi Chú Tiến Độ)

```
KTV A xử lý ticket
    → Ghi chú tiến độ vào lichSuHoTro (arrayUnion)
    → Đóng ticket hoặc chuyển giao

KTV B nhận ticket
    → TicketAdapter hiển thị badge "Có ghi chú từ KTV trước"
    → Mở KtvTicketDetailActivity
    → Đọc toàn bộ lichSuHoTro (thứ tự thời gian tăng dần)
    → Tiếp tục từ bước KTV A đã dừng
```

### 4.5 Quy Trình Lead Kinh Doanh

```
Công ty dùng thử sản phẩm
    → Đánh giá sản phẩm ≥ 4 sao
    → Cloud Function tạo LeadKinhDoanh (trangThaiLead = "Moi")
    → FCM gửi đến nhân viên kinh doanh

Admin/Kinh doanh (AdminLeadFragment)
    → Xem danh sách lead
    → Cập nhật trạng thái: Mới → Đang tư vấn → Đã đăng ký / Từ chối
```

---

## 5. Sản Phẩm Hỗ Trợ

| Mã | Tên đầy đủ |
|---|---|
| ECUS5 | ECUS5 VNACCS |
| E-INVOICE | E-INVOICE |
| ETAX | ETAX |
| EBH | EBH (Bảo Hiểm) |
| CLOUDOFFICE | CLOUDOFFICE |
| TRUEPOS | TRUEPOS |

---

## 6. Gợi Ý Chức Năng & Cải Tiến

### 6.1 Ưu Tiên Cao (Cần thiết cho production)

#### A. Thông Báo Đẩy (Push Notification) Đầy Đủ
Hiện tại FCM chỉ dùng cho lead kinh doanh. Cần mở rộng:
- Khách hàng nhận thông báo khi KTV phản hồi tin nhắn
- KTV nhận thông báo khi có ticket mới assign
- Admin nhận thông báo khi có đánh giá xấu mới

#### B. Xác Thực Email Bắt Buộc
Code hiện tại đã comment phần kiểm tra `isEmailVerified()`. Cần bật lại để bảo mật.

#### C. Firestore Security Rules
Hiện chưa có rules đầy đủ. Cần bổ sung:
- Chỉ user đúng vai trò mới đọc/ghi được collection tương ứng
- Kiểm tra `sanPhamTrongGoi` khi lưu DanhGia
- Chỉ Admin mới đọc được LeadKinhDoanh

#### D. Xử Lý Offline / Đồng Bộ
- Hiển thị trạng thái "Đang lưu..." khi offline
- Retry tự động khi có mạng trở lại

### 6.2 Cải Tiến Trải Nghiệm Người Dùng

#### E. Tìm Kiếm & Lọc Ticket
- Khách hàng tìm kiếm ticket theo từ khóa, trạng thái, thời gian
- KTV lọc ticket theo sản phẩm, mức ưu tiên

#### F. Đính Kèm File / Ảnh
- Khách hàng đính kèm screenshot lỗi khi tạo ticket hoặc chat
- KTV đính kèm tài liệu hướng dẫn

#### G. Thời Gian Phản Hồi SLA
- Hiển thị thời gian chờ ước tính khi ticket ở HangCho
- Cảnh báo Admin khi ticket chờ quá lâu (ví dụ > 30 phút)
- Dashboard SLA: % ticket xử lý đúng hạn

#### H. Template Trả Lời Nhanh
- KTV có thể lưu và dùng lại các câu trả lời thường gặp
- Gợi ý câu trả lời dựa trên loại lỗi

### 6.3 Tính Năng Quản Trị Nâng Cao

#### I. Báo Cáo Xuất File
- Xuất báo cáo thống kê ra Excel/PDF
- Báo cáo theo khoảng thời gian tùy chọn
- Báo cáo hiệu suất từng KTV

#### J. Quản Lý Ca Làm Việc KTV
- Admin đặt lịch ca cho KTV
- Tự động set trạng thái Offline ngoài giờ làm việc
- Phân công ticket theo ca

#### K. Escalation (Leo Thang Vấn Đề)
- Tự động chuyển ticket lên cấp cao hơn nếu KTV không phản hồi trong X phút
- Thông báo Supervisor khi ticket bị escalate

#### L. Knowledge Base (Cơ Sở Tri Thức)
- KTV tạo bài viết hướng dẫn cho từng loại lỗi thường gặp
- Khách hàng tự tìm kiếm giải pháp trước khi tạo ticket
- Gợi ý bài viết liên quan khi khách tạo ticket

### 6.4 Phân Tích & AI Nâng Cao

#### M. Dự Đoán Loại Lỗi
- Khi khách nhập mô tả vấn đề, AI gợi ý loại lỗi và giải pháp tự động
- Tự động phân loại ticket và gán ưu tiên

#### N. Phân Tích Xu Hướng
- Biểu đồ xu hướng đánh giá theo thời gian
- Phát hiện sản phẩm đang có vấn đề tăng đột biến
- So sánh hiệu suất giữa các KTV

#### O. Chatbot Hỗ Trợ Sơ Bộ
- Bot trả lời tự động các câu hỏi thường gặp
- Chỉ chuyển sang KTV khi bot không giải quyết được

### 6.5 Tích Hợp & Mở Rộng

#### P. Đa Ngôn Ngữ
- Hỗ trợ tiếng Anh cho khách hàng nước ngoài
- Tự động dịch tin nhắn

#### Q. Video Call
- Nâng cấp từ voice call lên video call qua Stringee
- Hữu ích khi KTV cần xem màn hình khách hàng

#### R. Tích Hợp CRM
- Đồng bộ lead với hệ thống CRM bên ngoài (Salesforce, HubSpot)
- Webhook khi trạng thái lead thay đổi

#### S. Đánh Giá Định Kỳ (NPS)
- Gửi khảo sát NPS (Net Promoter Score) định kỳ cho khách hàng
- Theo dõi xu hướng NPS theo thời gian

### 6.6 Bảo Mật & Hiệu Năng

#### T. Rate Limiting
- Giới hạn số lần tạo ticket trong 1 giờ để tránh spam
- Giới hạn số lần gọi AI phân tích

#### U. Audit Log
- Ghi lại mọi thay đổi quan trọng (ai thay đổi gì, lúc nào)
- Admin xem lịch sử thay đổi gói đăng ký, trạng thái ticket

#### V. Backup & Recovery
- Tự động backup dữ liệu Firestore định kỳ
- Quy trình khôi phục khi có sự cố

---

## 7. Tóm Tắt Spec Hiện Tại (.kiro/specs/enterprise-customer-care-improvements)

Spec đã hoàn thành 4 tính năng:

| # | Tính năng | Trạng thái |
|---|---|---|
| 1 | Thống kê đánh giá xấu (soSao ≤ 2) | ✅ Hoàn thành |
| 2 | Kiểm soát quyền đánh giá theo gói | ✅ Hoàn thành |
| 3 | Gói dùng thử + Lead kinh doanh tự động | ✅ Hoàn thành |
| 4 | KTV Handoff — ghi chú tiến độ | ✅ Hoàn thành |
