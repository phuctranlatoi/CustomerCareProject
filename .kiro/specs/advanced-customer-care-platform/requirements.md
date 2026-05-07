# Requirements Document

## Introduction

Tài liệu này mô tả các yêu cầu cho spec **Advanced Customer Care Platform** — nâng cấp toàn diện nền tảng CSKH B2B Android (Java/Firebase). Spec này bổ sung 10 nhóm tính năng còn thiếu sau khi 4 spec trước đã hoàn thành: FCM Push Notifications đầy đủ, Escalation tự động, Offline Mode & Data Caching, Security & Rate Limiting, Export Báo Cáo, Audit Log, Quản lý Ca Làm Việc KTV, Video Call, Chatbot AI sơ bộ, và NPS Survey định kỳ.

Các tính năng được ưu tiên theo impact/effort:
- **Ưu tiên cao**: FCM Notifications đầy đủ (Req 1), Escalation tự động (Req 2), Offline Mode (Req 3), Security & Rate Limiting (Req 4)
- **Ưu tiên trung bình**: Export Báo Cáo (Req 5), Audit Log (Req 6), Quản lý Ca KTV (Req 7)
- **Ưu tiên thấp/optional**: Video Call (Req 8), Chatbot AI (Req 9), NPS Survey (Req 10)

---

## Glossary

- **He_Thong**: Ứng dụng Android CSKH B2B sử dụng Firebase/Firestore/Cloud Functions.
- **KhachHang**: Người dùng có `vaiTro = "KhachHang"`, đại diện doanh nghiệp B2B.
- **KTV**: Kỹ thuật viên hỗ trợ, `vaiTro = "KTV"` trong collection `NguoiDung`.
- **Admin**: Quản trị viên hệ thống, `vaiTro = "Admin"` trong collection `NguoiDung`.
- **YeuCauHoTro**: Document trong collection `YeuCauHoTro`, đại diện một ticket hỗ trợ. Có các trạng thái: `HangCho → ChoXuLy → DangXuLy → DaXuLy`.
- **TinNhan**: Document trong sub-collection `TinNhan/{ticketId}/messages`, lưu tin nhắn chat realtime.
- **NguoiDung**: Document trong collection `NguoiDung`, lưu thông tin người dùng và trạng thái KTV (`Ran/DangBan/Offline`).
- **GoiDangKy**: Document trong collection `GoiDangKy`, liên kết `maSoThue` với danh sách sản phẩm đăng ký.
- **DanhGia**: Document trong collection `DanhGia`, lưu đánh giá sản phẩm của KhachHang.
- **DanhGiaKTV**: Document trong collection `DanhGiaKTV`, lưu đánh giá KTV sau khi đóng ticket.
- **AuditLog**: Document trong collection `AuditLog`, ghi lại mọi hành động quan trọng của Admin/KTV.
- **CaLamViec**: Document trong collection `CaLamViec`, định nghĩa ca làm việc của KTV (giờ bắt đầu, giờ kết thúc, ngày trong tuần).
- **NPS_Survey**: Document trong collection `NpsSurvey`, lưu kết quả khảo sát Net Promoter Score định kỳ.
- **FCM_Token**: Token Firebase Cloud Messaging lưu trong field `fcmToken` của document `NguoiDung`.
- **Escalation**: Quá trình tự động tăng mức ưu tiên và chuyển ticket lên cấp cao hơn khi quá hạn xử lý.
- **SLA**: Service Level Agreement — cam kết thời gian phản hồi. Ngưỡng cảnh báo: 30 phút cho ticket thường, 15 phút cho ticket ưu tiên Cao.
- **RateLimit**: Giới hạn số lần thực hiện một hành động trong khoảng thời gian nhất định để chống spam.
- **Firestore_Security_Rules**: Bộ quy tắc bảo mật Firestore kiểm soát quyền đọc/ghi theo vai trò.
- **OfflineCache**: Dữ liệu được lưu cục bộ trên thiết bị khi không có kết nối mạng.
- **Chatbot**: Module trả lời tự động câu hỏi thường gặp trước khi assign KTV, sử dụng knowledge base có sẵn.
- **VideoCall**: Cuộc gọi video hai chiều qua Stringee SDK, nâng cấp từ voice call hiện có.
- **SanPham**: Một trong các giá trị: `ECUS5`, `E-INVOICE`, `ETAX`, `EBH`, `CLOUDOFFICE`, `TRUEPOS`.
- **UuTien**: Mức ưu tiên ticket: `Cao` (SLA 15 phút), `TrungBinh` (SLA 30 phút), `Thap` (SLA 60 phút).
- **Supervisor**: KTV có `vaiTro = "KTV"` và `laSupervisor = true`, nhận thông báo escalation.
- **LeadKinhDoanh**: Document trong collection `LeadKinhDoanh`, lead tự động từ đánh giá tốt của gói dùng thử.
- **InsightCumDe**: Document trong collection `InsightCumDe`, kết quả phân tích AI gom cụm phản hồi.
- **TemplateTrLoi**: Document trong collection `TemplateTrLoi`, câu trả lời mẫu của KTV.
- **LoiPhatSinh**: Document trong collection `LoiPhatSinh`, bài viết knowledge base về lỗi thường gặp.

---

## Requirements

---

### Requirement 1: FCM Push Notifications Đầy Đủ

**User Story:** Là KhachHang, tôi muốn nhận thông báo đẩy khi KTV phản hồi ticket của tôi, để tôi không bỏ lỡ cập nhật quan trọng dù đang dùng ứng dụng khác. Là KTV, tôi muốn nhận thông báo khi có ticket mới được assign, để tôi phản hồi kịp thời. Là Admin, tôi muốn nhận thông báo khi có đánh giá xấu mới, để tôi can thiệp sớm.

#### Acceptance Criteria

1. WHEN KhachHang đăng nhập thành công, THE He_Thong SHALL lấy FCM token hiện tại của thiết bị và lưu vào field `fcmToken` trong document `NguoiDung/{uid}` trên Firestore.

2. WHEN FCM token của thiết bị được làm mới bởi Firebase SDK, THE He_Thong SHALL tự động cập nhật field `fcmToken` trong document `NguoiDung/{uid}` tương ứng.

3. WHEN một YeuCauHoTro được assign KTV (field `ktvUid` thay đổi từ rỗng sang có giá trị), THE He_Thong SHALL gửi FCM notification đến thiết bị của KhachHang sở hữu ticket đó với tiêu đề "Ticket đã được tiếp nhận" và nội dung "[Tên KTV] đang xử lý yêu cầu của bạn".

4. WHEN KTV gửi một TinNhan mới vào sub-collection `TinNhan/{ticketId}/messages`, THE He_Thong SHALL gửi FCM notification đến thiết bị của KhachHang sở hữu ticket đó với tiêu đề "Tin nhắn mới từ KTV" và nội dung là 100 ký tự đầu của `noiDung` tin nhắn.

5. WHEN một YeuCauHoTro chuyển sang `trangThai = "DaXuLy"`, THE He_Thong SHALL gửi FCM notification đến thiết bị của KhachHang sở hữu ticket đó với tiêu đề "Ticket đã được giải quyết" và nội dung "Yêu cầu hỗ trợ [tieuDeLoi] đã được đóng. Vui lòng đánh giá KTV".

6. WHEN một YeuCauHoTro mới được tạo với `trangThai = "ChoXuLy"` và có `ktvUid`, THE He_Thong SHALL gửi FCM notification đến thiết bị của KTV được assign với tiêu đề "Ticket mới được phân công" và nội dung "[Tên khách hàng] - [Sản phẩm]: [tieuDeLoi]".

7. WHEN một DanhGia mới được tạo với `soSao <= 2`, THE He_Thong SHALL gửi FCM notification đến tất cả người dùng có `vaiTro = "Admin"` với tiêu đề "Đánh giá xấu mới" và nội dung "[tenCongTy] đánh giá [sanPham] [soSao]★".

8. WHEN KhachHang gửi một TinNhan mới vào sub-collection `TinNhan/{ticketId}/messages`, THE He_Thong SHALL gửi FCM notification đến thiết bị của KTV được assign ticket đó với tiêu đề "Tin nhắn mới từ khách hàng" và nội dung là 100 ký tự đầu của `noiDung`.

9. IF FCM token của người nhận không tồn tại hoặc không hợp lệ, THEN THE He_Thong SHALL bỏ qua lỗi FCM và ghi log lỗi vào Cloud Functions console mà không làm gián đoạn luồng chính.

10. WHEN KhachHang nhấn vào FCM notification liên quan đến một ticket, THE He_Thong SHALL mở trực tiếp màn hình `ChatKhachHangActivity` của ticket tương ứng thay vì mở màn hình chính.

11. WHEN KTV nhấn vào FCM notification liên quan đến một ticket mới, THE He_Thong SHALL mở trực tiếp màn hình `KtvTicketDetailActivity` của ticket tương ứng.

12. THE He_Thong SHALL triển khai toàn bộ logic gửi FCM trong Firebase Cloud Functions (Node.js) để đảm bảo thông báo được gửi ngay cả khi ứng dụng đang đóng.

13. FOR ALL FCM notifications được gửi thành công, THE He_Thong SHALL đảm bảo mỗi sự kiện chỉ kích hoạt đúng một lần gửi thông báo (idempotency — không gửi trùng lặp khi Cloud Function retry).


---

### Requirement 2: Escalation Tự Động

**User Story:** Là Admin, tôi muốn hệ thống tự động tăng mức ưu tiên và thông báo cho Supervisor khi ticket quá hạn SLA, để không có ticket nào bị bỏ sót mà không cần tôi theo dõi thủ công liên tục.

#### Acceptance Criteria

1. THE He_Thong SHALL định nghĩa ngưỡng SLA theo mức ưu tiên: `Cao` = 15 phút, `TrungBinh` = 30 phút, `Thap` = 60 phút, tính từ thời điểm `taoLuc` của YeuCauHoTro.

2. WHEN một YeuCauHoTro có `trangThai` trong `["HangCho", "ChoXuLy", "DangXuLy"]` và thời gian kể từ `taoLuc` vượt quá ngưỡng SLA tương ứng với `uuTien`, THE He_Thong SHALL tự động cập nhật field `daEscalate = true` và `thoiGianEscalate = serverTimestamp()` trên document YeuCauHoTro đó.

3. WHEN một YeuCauHoTro được đánh dấu `daEscalate = true` lần đầu tiên, THE He_Thong SHALL tự động tăng `uuTien` lên một bậc: `Thap → TrungBinh`, `TrungBinh → Cao`. Ticket đã ở mức `Cao` giữ nguyên.

4. WHEN một YeuCauHoTro được đánh dấu `daEscalate = true`, THE He_Thong SHALL gửi FCM notification đến tất cả người dùng có `laSupervisor = true` với tiêu đề "Ticket quá hạn cần xử lý" và nội dung "[tieuDeLoi] - [tenCongTy] - Đã chờ [X] phút".

5. WHEN một YeuCauHoTro được đánh dấu `daEscalate = true` và `uuTien = "Cao"` mà vẫn chưa chuyển sang `DangXuLy` sau thêm 15 phút, THE He_Thong SHALL gửi FCM notification đến tất cả người dùng có `vaiTro = "Admin"` với tiêu đề "KHẨN: Ticket cần Admin can thiệp".

6. THE He_Thong SHALL triển khai logic kiểm tra escalation trong Cloud Function chạy theo lịch mỗi 5 phút, quét tất cả YeuCauHoTro chưa đóng và chưa escalate.

7. WHEN Admin hoặc KTV xem danh sách ticket, THE He_Thong SHALL hiển thị badge màu đỏ "⚠ Quá hạn" trên card của mọi YeuCauHoTro có `daEscalate = true` và `trangThai != "DaXuLy"`.

8. WHEN Admin truy cập `AdminTicketsFragment`, THE He_Thong SHALL sắp xếp các ticket có `daEscalate = true` lên đầu danh sách, trước các ticket chưa escalate.

9. WHEN một YeuCauHoTro chuyển sang `trangThai = "DaXuLy"`, THE He_Thong SHALL ghi nhận field `thoiGianXuLy` = (thời điểm đóng - `taoLuc`) tính bằng phút, để phục vụ thống kê SLA.

10. THE He_Thong SHALL lưu lịch sử escalation vào field `lichSuEscalation` (List) trong document YeuCauHoTro, mỗi phần tử gồm: `thoiDiem` (Timestamp), `uuTienTruoc` (String), `uuTienSau` (String), `lyDo` (String).

11. FOR ALL ticket được escalate, THE He_Thong SHALL đảm bảo mỗi ticket chỉ được escalate một lần cho mỗi ngưỡng SLA (không escalate lặp lại khi Cloud Function chạy nhiều lần — idempotency).


---

### Requirement 3: Offline Mode & Data Caching

**User Story:** Là KhachHang, tôi muốn xem lại lịch sử ticket và tin nhắn khi không có mạng, để tôi vẫn có thể tra cứu thông tin hỗ trợ đã nhận dù đang ở vùng mất sóng. Là KTV, tôi muốn xem danh sách ticket đang xử lý khi mất mạng, để tôi biết cần làm gì khi mạng trở lại.

#### Acceptance Criteria

1. THE He_Thong SHALL bật Firestore offline persistence bằng cách gọi `FirebaseFirestore.getInstance().setFirestoreSettings()` với `setPersistenceEnabled(true)` và `setCacheSizeBytes(CACHE_SIZE_UNLIMITED)` trong lớp `Application` khi khởi động.

2. WHILE thiết bị không có kết nối mạng (kiểm tra qua `ConnectivityManager`), THE He_Thong SHALL hiển thị banner cố định màu vàng ở đầu màn hình với nội dung "Đang offline — Dữ liệu có thể chưa cập nhật" trên tất cả màn hình chính (`HomeActivity`, `LichSuChatActivity`, `KtvDashboardActivity`).

3. WHILE thiết bị không có kết nối mạng, THE He_Thong SHALL vẫn hiển thị danh sách YeuCauHoTro từ Firestore local cache cho cả KhachHang (trong `LichSuChatActivity`) và KTV (trong `KtvDashboardActivity`).

4. WHILE thiết bị không có kết nối mạng, THE He_Thong SHALL vẫn hiển thị lịch sử TinNhan từ Firestore local cache trong `ChatKhachHangActivity` và `KtvChatActivity`.

5. WHEN thiết bị mất kết nối mạng trong khi KhachHang đang gõ tin nhắn, THE He_Thong SHALL hiển thị thông báo "Không có kết nối mạng. Tin nhắn sẽ được gửi khi có mạng trở lại" và vô hiệu hóa nút gửi.

6. WHEN kết nối mạng được khôi phục, THE He_Thong SHALL ẩn banner offline, tự động đồng bộ dữ liệu Firestore, và hiển thị Toast "Đã kết nối lại — Đang cập nhật dữ liệu".

7. THE He_Thong SHALL đăng ký lắng nghe trạng thái mạng qua `ConnectivityManager.NetworkCallback` trong `MainActivity` và phát broadcast đến các Activity đang active.

8. WHILE thiết bị không có kết nối mạng, THE He_Thong SHALL vô hiệu hóa các nút tạo ticket mới, gửi tin nhắn, và gọi VoIP, đồng thời hiển thị tooltip "Cần kết nối mạng để thực hiện".

9. WHEN KhachHang mở `ChatKhachHangActivity` khi offline, THE He_Thong SHALL hiển thị tối đa 50 tin nhắn gần nhất từ cache mà không cần kết nối mạng.

10. THE He_Thong SHALL cache dữ liệu `GoiDangKy` và `LoiPhatSinh` (dữ liệu tĩnh ít thay đổi) trong SharedPreferences với TTL 24 giờ, để giảm số lần query Firestore khi có mạng.

11. FOR ALL dữ liệu được đọc từ Firestore local cache khi offline, THE He_Thong SHALL hiển thị đúng dữ liệu đã được cache lần cuối khi online (round-trip: dữ liệu cache = dữ liệu đã đọc khi online).


---

### Requirement 4: Security & Rate Limiting

**User Story:** Là Admin, tôi muốn hệ thống có Firestore Security Rules đầy đủ và rate limiting để ngăn chặn spam và truy cập trái phép, đảm bảo dữ liệu khách hàng được bảo vệ đúng cách.

#### Acceptance Criteria

1. THE He_Thong SHALL triển khai Firestore Security Rules đảm bảo: KhachHang chỉ đọc/ghi được document `NguoiDung/{uid}` của chính mình; KhachHang chỉ đọc được YeuCauHoTro có `uid` khớp với UID của mình; KTV chỉ đọc/ghi được YeuCauHoTro có `ktvUid` khớp với UID của mình; Admin đọc/ghi được tất cả collections.

2. THE He_Thong SHALL triển khai Firestore Security Rules ngăn KhachHang ghi trực tiếp vào collection `LeadKinhDoanh`, `AuditLog`, `InsightCumDe`, và `DanhGiaKTV`.

3. THE He_Thong SHALL triển khai Firestore Security Rules kiểm tra khi KhachHang tạo DanhGia: field `uid` trong document DanhGia phải khớp với UID của người dùng đang xác thực (`request.auth.uid`).

4. THE He_Thong SHALL triển khai Firestore Security Rules kiểm tra khi KhachHang tạo YeuCauHoTro: field `uid` phải khớp với `request.auth.uid` và `trangThai` ban đầu phải là `"HangCho"` hoặc `"ChoXuLy"`.

5. THE He_Thong SHALL triển khai rate limiting cho việc tạo YeuCauHoTro: mỗi KhachHang (theo `uid`) không được tạo quá 5 ticket trong vòng 1 giờ. IF KhachHang vượt giới hạn, THEN THE He_Thong SHALL từ chối tạo ticket và hiển thị thông báo "Bạn đã tạo quá nhiều yêu cầu hỗ trợ. Vui lòng thử lại sau [X] phút".

6. THE He_Thong SHALL triển khai rate limiting cho việc gửi TinNhan: mỗi người dùng không được gửi quá 30 tin nhắn trong vòng 1 phút trong cùng một ticket. IF vượt giới hạn, THEN THE He_Thong SHALL vô hiệu hóa nút gửi trong 60 giây và hiển thị thông báo "Gửi quá nhanh, vui lòng chờ".

7. THE He_Thong SHALL chuyển Groq API key từ hardcode trong source code sang Firebase Remote Config với key `groq_api_key`, đọc giá trị tại runtime thay vì compile time.

8. THE He_Thong SHALL triển khai rate limiting cho việc gọi Groq AI: không quá 10 lần gọi `NlpHelper.phanTichGemini()` trong vòng 1 phút từ cùng một thiết bị. IF vượt giới hạn, THEN THE He_Thong SHALL sử dụng fallback keyword matching thay vì gọi API.

9. THE He_Thong SHALL validate tất cả input từ người dùng trước khi lưu Firestore: `tieuDeLoi` không rỗng và tối đa 200 ký tự; `moTaVanDe` tối đa 2000 ký tự; `noiDung` tin nhắn không rỗng và tối đa 1000 ký tự.

10. THE He_Thong SHALL bật lại kiểm tra `isEmailVerified()` trong `LoginActivity`: IF người dùng đăng nhập bằng email/password mà chưa xác thực email, THEN THE He_Thong SHALL hiển thị thông báo "Vui lòng xác thực email trước khi đăng nhập" và cung cấp nút "Gửi lại email xác thực".

11. THE He_Thong SHALL lưu rate limit counter trong Firestore collection `RateLimit/{uid}` với field `ticketCount` (số ticket tạo trong giờ hiện tại) và `resetTime` (timestamp reset counter), sử dụng Firestore transaction để tránh race condition.

12. FOR ALL Firestore Security Rules được triển khai, THE He_Thong SHALL đảm bảo mọi request đọc/ghi đều được kiểm tra quyền trước khi thực thi (không có collection nào ở chế độ `allow read, write: if true`).


---

### Requirement 5: Export Báo Cáo PDF/Excel

**User Story:** Là Admin, tôi muốn xuất báo cáo hiệu suất KTV và thống kê ticket ra file PDF hoặc Excel, để tôi có thể chia sẻ với ban lãnh đạo và lưu trữ hồ sơ định kỳ mà không cần chụp màn hình thủ công.

#### Acceptance Criteria

1. THE He_Thong SHALL thêm nút "Xuất báo cáo" vào `AdminThongKeFragment`, cho phép Admin chọn định dạng xuất: PDF hoặc Excel (CSV).

2. WHEN Admin nhấn "Xuất báo cáo PDF", THE He_Thong SHALL tạo file PDF chứa: tiêu đề báo cáo, khoảng thời gian (7 ngày hoặc 30 ngày theo bộ lọc hiện tại), tổng số ticket, tỷ lệ giải quyết đúng SLA, bảng thống kê từng KTV (tên, số ticket xử lý, điểm đánh giá trung bình, tỷ lệ đúng SLA), và biểu đồ phân bổ cảm xúc đánh giá.

3. WHEN Admin nhấn "Xuất báo cáo Excel (CSV)", THE He_Thong SHALL tạo file CSV với các cột: `ticketId`, `tieuDeLoi`, `sanPham`, `trangThai`, `uuTien`, `ktvTen`, `taoLuc`, `thoiGianXuLy` (phút), `daDanhGiaKtv`, `daEscalate`.

4. WHEN file báo cáo được tạo xong, THE He_Thong SHALL lưu file vào thư mục Downloads của thiết bị và hiển thị thông báo "Báo cáo đã được lưu tại Downloads/[tên_file]" kèm nút "Mở file".

5. WHILE file báo cáo đang được tạo, THE He_Thong SHALL hiển thị ProgressDialog với nội dung "Đang tạo báo cáo..." và vô hiệu hóa nút xuất để tránh tạo trùng lặp.

6. IF quá trình tạo báo cáo thất bại (lỗi Firestore query hoặc lỗi ghi file), THEN THE He_Thong SHALL hiển thị thông báo lỗi "Không thể tạo báo cáo. Vui lòng thử lại" và ghi log lỗi.

7. THE He_Thong SHALL thêm nút "Xuất danh sách KTV" vào `AdminKtvReviewsFragment`, xuất file CSV với các cột: `ktvTen`, `tongTicket`, `diemTrungBinh`, `soTicketDungHan`, `soTicketQuaHan`.

8. THE He_Thong SHALL yêu cầu quyền `WRITE_EXTERNAL_STORAGE` (Android < 10) hoặc sử dụng `MediaStore` API (Android 10+) để lưu file vào thư mục Downloads.

9. THE He_Thong SHALL đặt tên file theo định dạng: `BaoCao_[loai]_[ngayXuat].pdf` hoặc `BaoCao_[loai]_[ngayXuat].csv`, ví dụ: `BaoCao_KTV_2024-01-15.pdf`.

10. FOR ALL báo cáo được xuất, THE He_Thong SHALL đảm bảo tổng số ticket trong báo cáo bằng tổng số ticket hiển thị trên màn hình thống kê với cùng bộ lọc thời gian (invariant: dữ liệu báo cáo = dữ liệu màn hình).


---

### Requirement 6: Audit Log Cho Admin

**User Story:** Là Admin, tôi muốn xem lịch sử đầy đủ về ai đã thay đổi gì và lúc nào trong hệ thống, để tôi có thể kiểm tra trách nhiệm và điều tra khi có sự cố.

#### Acceptance Criteria

1. THE He_Thong SHALL tạo collection `AuditLog` trong Firestore với cấu trúc mỗi document gồm: `uid` (người thực hiện), `hoTen` (tên người thực hiện), `vaiTro` (vai trò), `hanhDong` (String mô tả hành động), `doiTuong` (loại đối tượng bị thay đổi), `doiTuongId` (ID đối tượng), `giaTriCu` (Map, giá trị trước thay đổi), `giaTriMoi` (Map, giá trị sau thay đổi), `thoiDiem` (Timestamp).

2. WHEN Admin thay đổi `trangThai` của GoiDangKy (ví dụ: HoatDong → TamDung), THE He_Thong SHALL tự động ghi một document vào `AuditLog` với `hanhDong = "CAP_NHAT_GOI_DANG_KY"`, `doiTuong = "GoiDangKy"`, `giaTriCu = {trangThai: "HoatDong"}`, `giaTriMoi = {trangThai: "TamDung"}`.

3. WHEN Admin tạo hoặc xóa tài khoản KTV, THE He_Thong SHALL ghi AuditLog với `hanhDong = "TAO_KTV"` hoặc `"XOA_KTV"`, `doiTuong = "NguoiDung"`, kèm thông tin KTV bị tạo/xóa.

4. WHEN Admin khóa hoặc mở khóa tài khoản người dùng, THE He_Thong SHALL ghi AuditLog với `hanhDong = "KHOA_TAI_KHOAN"` hoặc `"MO_KHOA_TAI_KHOAN"`, `doiTuong = "NguoiDung"`.

5. WHEN Admin cập nhật trạng thái LeadKinhDoanh, THE He_Thong SHALL ghi AuditLog với `hanhDong = "CAP_NHAT_LEAD"`, `doiTuong = "LeadKinhDoanh"`, `giaTriCu = {trangThaiLead: "..."}`, `giaTriMoi = {trangThaiLead: "..."}`.

6. WHEN KTV đóng một YeuCauHoTro (chuyển sang `DaXuLy`), THE He_Thong SHALL ghi AuditLog với `hanhDong = "DONG_TICKET"`, `doiTuong = "YeuCauHoTro"`, kèm `ticketId` và tên khách hàng.

7. THE He_Thong SHALL thêm tab "Audit Log" vào `AdminDashboardActivity` (tab thứ 6 hoặc trong `AdminPhanTichFragment`), hiển thị danh sách AuditLog sắp xếp theo `thoiDiem` giảm dần với phân trang 20 mục/trang.

8. WHEN Admin xem danh sách AuditLog, THE He_Thong SHALL hiển thị mỗi mục với: icon hành động, tên người thực hiện, mô tả hành động, tên đối tượng bị thay đổi, và thời gian (định dạng "dd/MM/yyyy HH:mm").

9. THE He_Thong SHALL cung cấp bộ lọc AuditLog theo: khoảng thời gian (7 ngày / 30 ngày), loại hành động (`hanhDong`), và người thực hiện (`uid`).

10. THE He_Thong SHALL chỉ cho phép người dùng có `vaiTro = "Admin"` đọc collection `AuditLog` (được bảo vệ bởi Firestore Security Rules).

11. FOR ALL hành động được ghi vào AuditLog, THE He_Thong SHALL đảm bảo `thoiDiem` trong AuditLog luôn sau hoặc bằng `capNhatLuc` của document bị thay đổi (invariant thứ tự thời gian).


---

### Requirement 7: Quản Lý Ca Làm Việc KTV

**User Story:** Là Admin, tôi muốn đặt lịch ca làm việc cho từng KTV và hệ thống tự động set trạng thái Offline ngoài giờ làm việc, để đảm bảo ticket không được assign cho KTV đang nghỉ và KTV không cần nhớ tự set trạng thái.

#### Acceptance Criteria

1. THE He_Thong SHALL tạo collection `CaLamViec` trong Firestore với cấu trúc mỗi document gồm: `ktvUid` (String), `ktvTen` (String), `ngayTrongTuan` (List<Integer>, 1=Thứ 2 đến 7=Chủ nhật), `gioVao` (String, định dạng "HH:mm"), `gioRa` (String, định dạng "HH:mm"), `hoatDong` (Boolean).

2. WHEN Admin truy cập `AdminUsersFragment` và chọn một KTV, THE He_Thong SHALL hiển thị nút "Quản lý ca làm việc" mở dialog cho phép Admin thêm/sửa/xóa ca làm việc của KTV đó.

3. WHEN Admin thêm ca làm việc mới, THE He_Thong SHALL lưu document vào collection `CaLamViec` với thông tin: `ktvUid`, `ktvTen`, `ngayTrongTuan` (checkbox chọn các ngày), `gioVao`, `gioRa`, `hoatDong = true`.

4. IF `gioVao` >= `gioRa` khi Admin lưu ca làm việc, THEN THE He_Thong SHALL hiển thị thông báo "Giờ vào phải trước giờ ra" và không lưu.

5. THE He_Thong SHALL triển khai Cloud Function chạy theo lịch mỗi 15 phút để kiểm tra ca làm việc: với mỗi KTV có `CaLamViec` đang `hoatDong = true`, nếu thời gian hiện tại nằm ngoài tất cả ca làm việc của KTV đó trong ngày hôm nay, THE He_Thong SHALL tự động cập nhật `trangThai = "Offline"` trong document `NguoiDung/{ktvUid}`.

6. WHEN thời gian hiện tại bước vào giờ làm việc của một KTV (theo `CaLamViec`), THE He_Thong SHALL gửi FCM notification đến KTV đó với nội dung "Ca làm việc của bạn bắt đầu. Hệ thống đã set trạng thái Rảnh" và tự động cập nhật `trangThai = "Ran"` nếu KTV đang ở trạng thái `Offline`.

7. WHEN KTV tự thay đổi trạng thái thủ công trong giờ làm việc, THE He_Thong SHALL cho phép thay đổi và ghi nhận trạng thái thủ công đó (không bị ghi đè bởi Cloud Function trong vòng 30 phút tiếp theo).

8. THE He_Thong SHALL hiển thị lịch ca làm việc của KTV trong `AdminUsersFragment` dưới dạng bảng: các ngày trong tuần theo hàng ngang, giờ vào/ra theo cột.

9. WHEN Admin xem danh sách KTV trong `AdminUsersFragment`, THE He_Thong SHALL hiển thị badge "Trong ca" hoặc "Ngoài ca" bên cạnh trạng thái của mỗi KTV dựa trên `CaLamViec` hiện tại.

10. IF một KTV không có `CaLamViec` nào được cấu hình, THEN THE He_Thong SHALL không tự động thay đổi trạng thái của KTV đó (giữ nguyên hành vi hiện tại — KTV tự set thủ công).

11. FOR ALL ca làm việc được lưu, THE He_Thong SHALL đảm bảo `gioVao < gioRa` (invariant: ca làm việc hợp lệ luôn có giờ vào trước giờ ra).


---

### Requirement 8: Video Call (Optional)

**User Story:** Là KTV, tôi muốn thực hiện cuộc gọi video với khách hàng khi cần xem trực tiếp màn hình hoặc thiết bị của họ, để giải quyết vấn đề nhanh hơn so với chỉ mô tả qua text.

#### Acceptance Criteria

1. THE He_Thong SHALL thêm nút "Gọi video" vào `ChatKhachHangActivity` và `KtvChatActivity`, hiển thị cạnh nút gọi thoại hiện có.

2. WHEN KTV nhấn nút "Gọi video" trong `KtvChatActivity`, THE He_Thong SHALL khởi tạo cuộc gọi video qua Stringee SDK bằng cách gọi `StringeeCall2` với `isVideoCall = true` đến số điện thoại Stringee của KhachHang.

3. WHEN KhachHang nhận cuộc gọi video đến, THE He_Thong SHALL hiển thị màn hình `IncomingCallActivity` với thông tin người gọi và nút "Chấp nhận video" và "Từ chối".

4. WHEN cuộc gọi video được chấp nhận, THE He_Thong SHALL mở màn hình `VideoCallActivity` hiển thị: luồng video của người kia (full screen), luồng video của bản thân (picture-in-picture góc trên phải), nút tắt/bật camera, nút tắt/bật micro, và nút kết thúc cuộc gọi.

5. WHEN người dùng nhấn nút tắt camera trong `VideoCallActivity`, THE He_Thong SHALL dừng truyền luồng video của bản thân và hiển thị avatar thay thế, nhưng vẫn duy trì kết nối âm thanh.

6. IF cuộc gọi video thất bại do lỗi kết nối Stringee, THEN THE He_Thong SHALL hiển thị thông báo "Không thể kết nối cuộc gọi video. Vui lòng thử gọi thoại" và tự động đề xuất chuyển sang voice call.

7. THE He_Thong SHALL yêu cầu quyền `CAMERA` trước khi bắt đầu cuộc gọi video; IF người dùng từ chối quyền camera, THEN THE He_Thong SHALL hiển thị thông báo "Cần quyền camera để thực hiện cuộc gọi video" và không khởi tạo cuộc gọi.

8. WHERE thiết bị không hỗ trợ camera trước (`android.hardware.camera` = false), THE He_Thong SHALL ẩn nút "Gọi video" và không hiển thị tùy chọn này.


---

### Requirement 9: Chatbot AI Sơ Bộ (Optional)

**User Story:** Là KhachHang, tôi muốn nhận câu trả lời tự động cho các câu hỏi thường gặp ngay khi tạo ticket, để tôi có thể tự giải quyết vấn đề đơn giản mà không cần chờ KTV.

#### Acceptance Criteria

1. WHEN KhachHang tạo YeuCauHoTro mới và ticket được lưu với `trangThai = "HangCho"`, THE He_Thong SHALL tự động gửi một TinNhan đầu tiên từ "Bot CSKH" vào sub-collection `TinNhan/{ticketId}/messages` với `vaiTroNguoiGui = "Bot"` và `nguoiGuiTen = "Bot CSKH"`.

2. THE He_Thong SHALL tìm kiếm trong collection `LoiPhatSinh` các bài viết có `sanPham` khớp với sản phẩm của ticket và `tieuDe` hoặc `moTa` chứa từ khóa từ `tieuDeLoi` của ticket (tìm kiếm case-insensitive).

3. WHEN tìm thấy ít nhất một `LoiPhatSinh` phù hợp, THE He_Thong SHALL gửi TinNhan Bot với nội dung: "Xin chào! Tôi tìm thấy [N] bài viết có thể giúp bạn:\n1. [tieuDe1]\n2. [tieuDe2]\n...\nBạn có muốn xem hướng dẫn không? Nếu không giải quyết được, KTV sẽ hỗ trợ bạn sớm."

4. WHEN KhachHang trả lời "Có" hoặc nhấn vào tên bài viết trong tin nhắn Bot, THE He_Thong SHALL gửi TinNhan Bot tiếp theo chứa nội dung `cachGiaiQuyet` của `LoiPhatSinh` tương ứng.

5. IF không tìm thấy `LoiPhatSinh` nào phù hợp, THEN THE He_Thong SHALL gửi TinNhan Bot với nội dung: "Xin chào! Yêu cầu của bạn đã được ghi nhận. KTV sẽ hỗ trợ bạn trong thời gian sớm nhất."

6. WHEN KTV được assign vào ticket và bắt đầu chat, THE He_Thong SHALL hiển thị badge "Bot" màu xanh lá trên các tin nhắn có `vaiTroNguoiGui = "Bot"` để phân biệt với tin nhắn KTV thật.

7. THE He_Thong SHALL triển khai logic Chatbot trong Cloud Function `onTicketCreated` (đã có sẵn), thêm bước tìm kiếm `LoiPhatSinh` và gửi tin nhắn Bot sau khi xử lý assign KTV.

8. THE He_Thong SHALL giới hạn Chatbot chỉ gửi tối đa 3 tin nhắn tự động cho mỗi ticket để tránh spam.


---

### Requirement 10: NPS Survey Định Kỳ (Optional)

**User Story:** Là Admin, tôi muốn gửi khảo sát NPS (Net Promoter Score) định kỳ hàng tháng đến khách hàng, để đo lường mức độ trung thành và phát hiện sớm nguy cơ khách hàng rời bỏ dịch vụ.

#### Acceptance Criteria

1. THE He_Thong SHALL tạo collection `NpsSurvey` trong Firestore với cấu trúc mỗi document gồm: `uid` (KhachHang), `maSoThue`, `tenCongTy`, `diemNps` (Integer, 0–10), `lyDo` (String, tùy chọn), `thangKhaoSat` (String, định dạng "YYYY-MM"), `taoLuc` (Timestamp).

2. THE He_Thong SHALL triển khai Cloud Function chạy theo lịch vào ngày 1 hàng tháng lúc 9:00 sáng (múi giờ UTC+7), gửi FCM notification đến tất cả KhachHang có `trangThai = "HoatDong"` với tiêu đề "Khảo sát hàng tháng" và nội dung "Bạn có muốn giới thiệu dịch vụ của chúng tôi cho đối tác không? Tham gia khảo sát ngắn".

3. WHEN KhachHang nhấn vào FCM notification NPS, THE He_Thong SHALL mở dialog khảo sát NPS trong `HomeActivity` với: thang điểm 0–10 (dạng nút tròn), câu hỏi "Bạn có sẵn sàng giới thiệu dịch vụ của chúng tôi cho đối tác không?", và ô nhập lý do tùy chọn.

4. WHEN KhachHang gửi khảo sát NPS, THE He_Thong SHALL lưu document vào collection `NpsSurvey` với `diemNps`, `lyDo`, `thangKhaoSat` = tháng hiện tại (định dạng "YYYY-MM"), và `taoLuc`.

5. THE He_Thong SHALL chỉ gửi khảo sát NPS một lần mỗi tháng cho mỗi KhachHang: trước khi gửi FCM, Cloud Function kiểm tra xem đã có document `NpsSurvey` với `uid` và `thangKhaoSat` tương ứng chưa; IF đã có, THEN THE He_Thong SHALL bỏ qua và không gửi lại.

6. WHEN Admin truy cập `AdminThongKeFragment`, THE He_Thong SHALL hiển thị card "NPS Tháng Này" với: điểm NPS trung bình, số người tham gia khảo sát, phân loại Promoters (9–10), Passives (7–8), Detractors (0–6).

7. THE He_Thong SHALL tính điểm NPS theo công thức chuẩn: `NPS = (% Promoters) - (% Detractors)`, hiển thị kết quả trong khoảng -100 đến +100.

8. IF KhachHang bỏ qua dialog NPS (nhấn "Để sau"), THE He_Thong SHALL không hiển thị lại dialog trong 7 ngày tiếp theo, lưu trạng thái bỏ qua trong SharedPreferences.

9. FOR ALL điểm NPS được lưu, THE He_Thong SHALL đảm bảo `diemNps` luôn trong khoảng [0, 10] (invariant: điểm NPS hợp lệ).

10. FOR ALL tháng có ít nhất 1 phản hồi NPS, THE He_Thong SHALL đảm bảo điểm NPS tính toán thỏa mãn: `NPS = (soPromoters - soDetractors) / tongSoNguoi * 100`, trong đó Promoters là `diemNps >= 9`, Detractors là `diemNps <= 6` (round-trip: tính toán từ dữ liệu thô cho kết quả nhất quán).


---

## Correctness Properties (Property-Based Testing)

Phần này liệt kê các thuộc tính đúng đắn có thể kiểm tra bằng property-based testing cho các tính năng quan trọng.

### P1: FCM Token Idempotency (Req 1)
- **Property**: Với bất kỳ `uid` nào, việc lưu FCM token nhiều lần liên tiếp với cùng giá trị token phải cho kết quả giống như lưu một lần duy nhất.
- **Kiểm tra**: `saveFcmToken(uid, token); saveFcmToken(uid, token)` → `getFcmToken(uid) == token` (idempotence).

### P2: Escalation Idempotency (Req 2)
- **Property**: Với bất kỳ ticket nào đã được escalate (`daEscalate = true`), việc chạy lại Cloud Function escalation không được thay đổi `uuTien` thêm lần nữa.
- **Kiểm tra**: `escalate(ticket)` → `escalate(ticket)` → `ticket.uuTien` không thay đổi sau lần thứ hai (idempotence).

### P3: SLA Threshold Invariant (Req 2)
- **Property**: Với bất kỳ ticket nào có `uuTien` hợp lệ, ngưỡng SLA phải luôn là: `Cao` → 15 phút, `TrungBinh` → 30 phút, `Thap` → 60 phút.
- **Kiểm tra**: `getSlaThreshold(uuTien)` luôn trả về đúng giá trị tương ứng cho mọi giá trị `uuTien` hợp lệ (invariant).

### P4: Offline Cache Round-Trip (Req 3)
- **Property**: Dữ liệu YeuCauHoTro được đọc khi online và lưu vào Firestore local cache phải giống hệt dữ liệu đọc từ cache khi offline.
- **Kiểm tra**: `readOnline(ticketId)` → `goOffline()` → `readFromCache(ticketId)` → hai kết quả phải bằng nhau (round-trip).

### P5: Rate Limit Counter Invariant (Req 4)
- **Property**: Với bất kỳ `uid` nào, số ticket được tạo thành công trong 1 giờ không bao giờ vượt quá 5.
- **Kiểm tra**: Tạo 10 ticket liên tiếp từ cùng một `uid` → chỉ 5 ticket đầu thành công, 5 ticket sau bị từ chối (metamorphic: số lượng thành công ≤ giới hạn).

### P6: Report Data Invariant (Req 5)
- **Property**: Tổng số ticket trong báo cáo xuất ra phải bằng tổng số ticket truy vấn từ Firestore với cùng bộ lọc thời gian.
- **Kiểm tra**: `queryTickets(filter)` → `exportReport(filter)` → `report.totalTickets == query.size()` (invariant).

### P7: Audit Log Ordering Invariant (Req 6)
- **Property**: Với bất kỳ chuỗi hành động nào, `thoiDiem` trong AuditLog phải luôn tăng dần theo thứ tự thực hiện.
- **Kiểm tra**: Thực hiện N hành động theo thứ tự → `auditLogs[i].thoiDiem <= auditLogs[i+1].thoiDiem` với mọi i (invariant thứ tự thời gian).

### P8: Ca Làm Việc Validity Invariant (Req 7)
- **Property**: Với bất kỳ `CaLamViec` nào được lưu thành công, `gioVao` phải luôn nhỏ hơn `gioRa`.
- **Kiểm tra**: `saveCaLamViec(gioVao, gioRa)` thành công → `parseTime(gioVao) < parseTime(gioRa)` (invariant).

### P9: NPS Score Range Invariant (Req 10)
- **Property**: Với bất kỳ tập dữ liệu NPS nào, điểm NPS tính toán phải luôn nằm trong khoảng [-100, 100].
- **Kiểm tra**: `calculateNps(responses)` → `-100 <= result <= 100` với mọi tập `responses` hợp lệ (invariant).

### P10: NPS Calculation Round-Trip (Req 10)
- **Property**: Điểm NPS tính từ danh sách phản hồi thô phải nhất quán khi tính lại từ cùng dữ liệu.
- **Kiểm tra**: `calculateNps(responses) == calculateNps(responses)` — hàm tính NPS phải là pure function (idempotence).

