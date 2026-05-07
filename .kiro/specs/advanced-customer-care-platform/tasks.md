# Implementation Plan: Advanced Customer Care Platform

## Overview

Triển khai 5 tính năng nâng cao cho nền tảng CSKH B2B Android (Java/Firebase):
1. Escalation tự động + SLA
2. Export báo cáo PDF/CSV
3. Audit Log
4. Chatbot AI sơ bộ
5. NPS Survey định kỳ

> **Trạng thái online KTV**: Giữ nguyên hành vi hiện tại — KTV vào app tự động online, thoát app tự động offline qua Firebase RTDB `onDisconnect`. Không thay đổi.

Nguyên tắc: KHÔNG xóa code cũ. Mỗi nhóm task độc lập, có thể triển khai riêng lẻ.

---

## NHÓM 1: ESCALATION TỰ ĐỘNG + SLA

- [ ] 1.1 Thêm fields escalation vào model `YeuCauHoTro.java`
  - Thêm `boolean daEscalate` (default false)
  - Thêm `Timestamp thoiGianEscalate`
  - Thêm `long thoiGianXuLy` (phút, set khi đóng ticket)
  - Thêm `List<Map<String, Object>> lichSuEscalation`
  - Thêm getter/setter cho tất cả fields mới
  - _Requirements: 2.2, 2.9, 2.10_

- [ ] 1.2 Tạo `utils/SlaHelper.java`
  - Method `getSlaThreshold(String uuTien)` trả về int (phút): Cao=15, TrungBinh=30, Thap=60
  - Method `isOverSla(Timestamp taoLuc, String uuTien)` trả về boolean
  - Method `getElapsedMinutes(Timestamp taoLuc)` trả về long
  - _Requirements: 2.1_

- [ ] 1.3 Cập nhật `TicketAdapter.java` để hiển thị badge escalation
  - Thêm `TextView tvBadgeEscalate` vào layout item ticket (nếu chưa có)
  - Trong `onBindViewHolder`: nếu `ticket.isDaEscalate() && !"DaXuLy".equals(trangThai)` → hiện badge đỏ "⚠ Quá hạn"
  - Nếu không → ẩn badge
  - _Requirements: 2.7_

- [ ] 1.4 Cập nhật `AdminTicketsFragment.java` để sort ticket escalated lên đầu
  - Sau khi nhận list từ Firestore, sort: ticket có `daEscalate = true` lên trước
  - Kết hợp với sort/filter hiện có (không xóa logic cũ)
  - _Requirements: 2.8_

- [ ] 1.5 Ghi nhận `thoiGianXuLy` khi KTV đóng ticket
  - Trong `KtvTicketDetailActivity.java`, khi đóng ticket:
    - Tính `thoiGianXuLy = (System.currentTimeMillis() - ticket.getTaoLuc().toDate().getTime()) / 60000`
    - Update field `thoiGianXuLy` lên Firestore cùng với `trangThai = "DaXuLy"`
  - _Requirements: 2.9_

- [ ] 1.6 Tạo `workers/EscalationWorker.java` (thay Cloud Function)
  - Extend `Worker` từ `androidx.work`
  - Trong `doWork()`: query Firestore `YeuCauHoTro` where `trangThai in [HangCho, ChoXuLy, DangXuLy]` AND `daEscalate == false`
  - Với mỗi ticket: tính elapsed = `(now - taoLuc.toDate().getTime()) / 60000`
  - So với ngưỡng SLA (`SlaHelper.getSlaThreshold(uuTien)`)
  - Nếu quá hạn: update `daEscalate = true`, tăng `uuTien`, append `lichSuEscalation`
  - Idempotency: chỉ xử lý ticket `daEscalate == false`
  - _Requirements: 2.2, 2.3, 2.6, 2.11_

- [ ] 1.7 Đăng ký `EscalationWorker` trong `KtvDashboardActivity` và `AdminDashboardActivity`
  - Dùng `PeriodicWorkRequest` với interval 15 phút (minimum của WorkManager)
  - `WorkManager.enqueueUniquePeriodicWork("escalation_check", KEEP, request)`
  - Chỉ đăng ký một lần khi Activity được tạo lần đầu
  - _Requirements: 2.6_

- [ ]* 1.7 Viết property test cho SlaHelper
  - **Property P2: Escalation Idempotency** — ticket đã escalate không bị escalate lại
  - **Property P3: SLA Threshold Invariant** — `getSlaThreshold` luôn trả về đúng giá trị
  - _Requirements: 2.1, 2.11_

---

## NHÓM 2: EXPORT BÁO CÁO PDF/CSV

- [ ] 2.1 Thêm dependency iText7 vào `app/build.gradle.kts`
  - Thêm `implementation("com.itextpdf:itext7-core:7.2.5")`
  - Hoặc dùng `implementation("com.itextpdf:kernel:7.2.5")` + `implementation("com.itextpdf:layout:7.2.5")`
  - Sync Gradle, kiểm tra không conflict
  - _Requirements: 5.2_

- [ ] 2.2 Tạo `utils/ReportExporter.java`
  - Method `exportTicketsCsv(Context, List<YeuCauHoTro>, OnExportComplete)`:
    - Build CSV string với header: `ticketId,tieuDeLoi,sanPham,trangThai,uuTien,ktvTen,taoLuc,thoiGianXuLy,daDanhGiaKtv,daEscalate`
    - Lưu file qua `MediaStore` (Android 10+) hoặc `Environment.DIRECTORY_DOWNLOADS` (Android < 10)
    - Tên file: `BaoCao_Tickets_yyyy-MM-dd.csv`
  - Method `exportKtvCsv(Context, List<KtvSummary>, OnExportComplete)`:
    - Header: `ktvTen,tongTicket,diemTrungBinh,soTicketDungHan,soTicketQuaHan`
    - Tên file: `BaoCao_KTV_yyyy-MM-dd.csv`
  - Method `exportReportPdf(Context, ReportData, OnExportComplete)`:
    - Tạo PDF với iText7: tiêu đề, khoảng thời gian, bảng thống kê KTV, phân bổ cảm xúc
    - Tên file: `BaoCao_TongHop_yyyy-MM-dd.pdf`
  - Interface `OnExportComplete { onSuccess(Uri, String); onError(Exception); }`
  - _Requirements: 5.2, 5.3, 5.4, 5.7, 5.9_

- [ ] 2.3 Thêm nút "Xuất báo cáo" vào `AdminThongKeFragment.java`
  - Thêm `MaterialButton btnXuatBaoCao` vào layout `fragment_admin_thong_ke.xml`
  - Click → hiện BottomSheet chọn: "Xuất PDF" / "Xuất CSV"
  - Khi chọn: hiện `ProgressDialog "Đang tạo báo cáo..."`, disable nút
  - Gọi `ReportExporter.exportReportPdf()` hoặc `exportTicketsCsv()`
  - Khi xong: ẩn dialog, hiện Snackbar "Đã lưu tại Downloads/[tên_file]" + nút "Mở"
  - Khi lỗi: hiện Toast "Không thể tạo báo cáo. Vui lòng thử lại"
  - _Requirements: 5.1, 5.4, 5.5, 5.6_

- [ ] 2.4 Thêm nút "Xuất CSV" vào `AdminKtvReviewsFragment.java`
  - Thêm icon button xuất vào toolbar/header
  - Click → gọi `ReportExporter.exportKtvCsv()` với dữ liệu hiện tại
  - Xử lý loading và error tương tự task 2.3
  - _Requirements: 5.7_

- [ ]* 2.5 Viết property test cho ReportExporter
  - **Property P7: Report Data Consistency** — count(export) == count(input list)
  - Test CSV output có đúng số dòng = số ticket đầu vào
  - _Requirements: 5.10_

---

## NHÓM 3: AUDIT LOG

- [ ] 3.1 Tạo `model/AuditLog.java`
  - Fields: `id`, `uid`, `hoTen`, `vaiTro`, `hanhDong`, `doiTuong`, `doiTuongId`
  - Fields: `giaTriCu` (Map), `giaTriMoi` (Map), `thoiDiem` (Timestamp)
  - Constants: `HANH_DONG_TAO_KTV`, `XOA_KTV`, `KHOA_TAI_KHOAN`, `MO_KHOA_TAI_KHOAN`, `CAP_NHAT_GOI_DANG_KY`, `CAP_NHAT_LEAD`, `DONG_TICKET`
  - Constructor rỗng + constructor đầy đủ, getter/setter
  - _Requirements: 6.1_

- [ ] 3.2 Tạo `utils/AuditLogger.java`
  - Method static `log(String hanhDong, String doiTuong, String doiTuongId, Map giaTriCu, Map giaTriMoi)`
  - Lấy thông tin user hiện tại từ `FirebaseAuth` + query `NguoiDung`
  - Tạo document trong collection `AuditLog` với `thoiDiem = FieldValue.serverTimestamp()`
  - Xử lý lỗi: log lỗi nhưng không throw exception (không ảnh hưởng luồng chính)
  - _Requirements: 6.1_

- [ ] 3.3 Tích hợp `AuditLogger` vào `AdminUsersFragment.java`
  - Khi tạo KTV: gọi `AuditLogger.log(TAO_KTV, "NguoiDung", ktvUid, null, {hoTen, email})`
  - Khi xóa KTV: gọi `AuditLogger.log(XOA_KTV, "NguoiDung", ktvUid, {hoTen}, null)`
  - Khi khóa tài khoản: gọi `AuditLogger.log(KHOA_TAI_KHOAN, ...)`
  - Khi mở khóa: gọi `AuditLogger.log(MO_KHOA_TAI_KHOAN, ...)`
  - _Requirements: 6.3, 6.4_

- [ ] 3.4 Tích hợp `AuditLogger` vào `AdminGoiDangKyFragment.java`
  - Khi cập nhật `trangThai` gói: gọi `AuditLogger.log(CAP_NHAT_GOI_DANG_KY, "GoiDangKy", goiId, {trangThaiCu}, {trangThaiMoi})`
  - _Requirements: 6.2_

- [ ] 3.5 Tích hợp `AuditLogger` vào `AdminLeadFragment.java`
  - Khi cập nhật `trangThaiLead`: gọi `AuditLogger.log(CAP_NHAT_LEAD, "LeadKinhDoanh", leadId, {trangThaiCu}, {trangThaiMoi})`
  - _Requirements: 6.5_

- [ ] 3.6 Tích hợp `AuditLogger` vào `KtvTicketDetailActivity.java`
  - Khi đóng ticket: gọi `AuditLogger.log(DONG_TICKET, "YeuCauHoTro", ticketId, {trangThaiCu: "DangXuLy"}, {trangThaiMoi: "DaXuLy"})`
  - _Requirements: 6.6_

- [ ] 3.7 Tạo layout `fragment_admin_audit_log.xml`
  - `ChipGroup`: 7 ngày / 30 ngày
  - `Spinner` lọc theo `hanhDong` (Tất cả + các loại hành động)
  - `RecyclerView` danh sách
  - `Button` "Tải thêm" (pagination 20 mục/trang)
  - `TextView` empty state
  - _Requirements: 6.7, 6.9_

- [ ] 3.8 Tạo layout `item_audit_log.xml`
  - `ImageView` icon hành động (theo loại hanhDong)
  - `TextView` tên người thực hiện + vai trò
  - `TextView` mô tả hành động
  - `TextView` thời gian (dd/MM/yyyy HH:mm)
  - _Requirements: 6.8_

- [ ] 3.9 Tạo `ui/admin/AuditLogAdapter.java`
  - RecyclerView.Adapter nhận `List<AuditLog>`
  - Bind icon theo `hanhDong`, format thời gian, hiển thị mô tả
  - _Requirements: 6.8_

- [ ] 3.10 Tạo `ui/admin/AdminAuditLogFragment.java`
  - Query `AuditLog` orderBy `thoiDiem` desc, limit 20
  - Tích hợp filter theo thời gian và `hanhDong`
  - Pagination: "Tải thêm" dùng `startAfter(lastDocument)`
  - _Requirements: 6.7, 6.9_

- [ ] 3.11 Thêm tab "Audit Log" vào `AdminDashboardActivity.java`
  - Thêm tab mới vào `AdminPagerAdapter` trả về `AdminAuditLogFragment`
  - Cập nhật `getItemCount()`, arrays `tabs`, `icons`, `labels`
  - _Requirements: 6.7_

- [ ]* 3.12 Viết property test cho AuditLogger
  - **Property P6: AuditLog Timestamp Ordering** — `thoiDiem` >= `capNhatLuc` của document bị thay đổi
  - _Requirements: 6.11_

---

## NHÓM 4: CHATBOT AI SƠ BỘ

- [ ] 5.1 Mở rộng model `TinNhan.java` để hỗ trợ tin nhắn Bot
  - Thêm field `String vaiTroNguoiGui` (mặc định null, backward compat)
  - Thêm field `String nguoiGuiTen` (mặc định null)
  - Thêm getter/setter, không xóa field cũ
  - _Requirements: 9.1_

- [ ] 5.2 Tạo layout `item_chat_bot.xml`
  - Layout tương tự `item_chat.xml` nhưng cho tin nhắn Bot
  - Bubble màu xanh lá nhạt, căn trái
  - Badge nhỏ "Bot" màu xanh lá bên cạnh tên
  - _Requirements: 9.6_

- [ ] 5.3 Cập nhật `ChatAdapter.java` để render tin nhắn Bot
  - Thêm `VIEW_TYPE_BOT = 3`
  - Override `getItemViewType()`: nếu `vaiTroNguoiGui == "Bot"` → trả về `VIEW_TYPE_BOT`
  - Thêm `BotViewHolder` inflate `item_chat_bot.xml`
  - _Requirements: 9.6_

- [ ] 5.4 Tạo `utils/ChatbotHelper.java` và tích hợp vào `YeuCauHoTroActivity.java`
  - Tạo `ChatbotHelper.sendBotReply(String ticketId, String tieuDeLoi, String sanPham)`
  - Chạy trên background thread (`Executors.newSingleThreadExecutor()`)
  - Query `LoiPhatSinh` where `sanPham == sanPham` từ Firestore
  - Filter: `tieuDe` hoặc `moTa` chứa keyword từ `tieuDeLoi` (split by space, case-insensitive)
  - Lấy tối đa 3 kết quả phù hợp
  - Nếu có kết quả: ghi TinNhan với nội dung liệt kê bài viết
  - Nếu không: ghi TinNhan "Yêu cầu đã ghi nhận, KTV sẽ hỗ trợ sớm."
  - Set `vaiTroNguoiGui = "Bot"`, `nguoiGuiTen = "Bot CSKH"`
  - Trong `YeuCauHoTroActivity`: gọi `ChatbotHelper.sendBotReply()` trong callback `onSuccess` sau khi tạo ticket
  - _Requirements: 9.1, 9.2, 9.3, 9.5, 9.7, 9.8_

- [ ]* 5.5 Viết unit test cho logic keyword matching của chatbot
  - Test: tieuDeLoi có keyword khớp với LoiPhatSinh → trả về đúng bài viết
  - Test: tieuDeLoi không có keyword nào khớp → trả về empty list
  - Test: giới hạn 3 tin nhắn bot/ticket
  - _Requirements: 9.2, 9.8_

---

## NHÓM 5: NPS SURVEY ĐỊNH KỲ

- [ ] 6.1 Tạo `model/NpsSurvey.java`
  - Fields: `id`, `uid`, `maSoThue`, `tenCongTy`, `diemNps` (int), `lyDo` (String), `thangKhaoSat` (String), `taoLuc` (Timestamp)
  - Validate: `diemNps` trong [0, 10]
  - Constructor rỗng + constructor đầy đủ, getter/setter
  - _Requirements: 10.1_

- [ ] 6.2 Tạo layout `dialog_nps_survey.xml`
  - `TextView` câu hỏi: "Bạn có sẵn sàng giới thiệu dịch vụ cho đối tác không?"
  - `LinearLayout` ngang: 11 nút tròn (0-10), màu gradient đỏ→xanh
  - `TextInputLayout` + `EditText` lý do (optional, hint "Lý do (không bắt buộc)")
  - `Button` "Gửi" (disabled cho đến khi chọn điểm) + `Button` "Để sau"
  - _Requirements: 10.3_

- [ ] 6.3 Tích hợp NPS dialog vào `HomeActivity.java`
  - Khi nhận Intent với action `"ACTION_NPS_SURVEY"` (từ FCM notification):
    - Kiểm tra SharedPreferences `nps_skip_until`: nếu chưa hết 7 ngày → không hiện
    - Kiểm tra `nps_last_shown`: nếu đã hiện tháng này → không hiện
    - Nếu hợp lệ → inflate và hiện `dialog_nps_survey.xml`
  - Khi gửi: validate `diemNps` trong [0, 10], lưu vào collection `NpsSurvey`
  - Khi "Để sau": lưu `nps_skip_until = now + 7 ngày` vào SharedPreferences
  - _Requirements: 10.3, 10.4, 10.8_

- [ ] 6.4 Hiển thị card NPS trong `AdminThongKeFragment.java`
  - Query `NpsSurvey` where `thangKhaoSat == currentMonth`
  - Tính: `promoters = count(diemNps >= 9)`, `detractors = count(diemNps <= 6)`, `total = count(all)`
  - Tính `nps = (promoters - detractors) * 100.0 / total`
  - Thêm card "NPS Tháng Này" vào layout `fragment_admin_thong_ke.xml`:
    - Điểm NPS lớn (màu xanh/đỏ tùy dương/âm)
    - Số người tham gia
    - 3 dòng: Promoters (9-10), Passives (7-8), Detractors (0-6)
  - _Requirements: 10.6, 10.7_

- [ ] 6.5 Tích hợp kiểm tra NPS vào `HomeActivity.java` (thay Cloud Function)
  - Trong `onResume()`: đọc `SharedPreferences` key `nps_last_shown` và `nps_skip_until`
  - Lấy `currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date())`
  - Nếu `!lastShown.equals(currentMonth)` AND `now > skipUntil`:
    - Query Firestore `NpsSurvey` where `uid == currentUid` AND `thangKhaoSat == currentMonth`
    - Nếu chưa có survey tháng này → hiển thị `dialog_nps_survey.xml`
  - Idempotency: không hiển thị lại nếu đã có survey tháng này trong Firestore
  - _Requirements: 10.3, 10.5_

- [ ]* 6.6 Viết property test cho NPS calculation
  - **Property P4: NPS Score Range** — `diemNps` luôn trong [0, 10]
  - **Property P5: NPS Calculation Consistency** — `NPS = (P - D) / total * 100` nhất quán
  - Generate random list diemNps, assert tính toán đúng
  - _Requirements: 10.9, 10.10_

---

## NHÓM 6: KIỂM TRA CUỐI

- [ ] 6.1 Checkpoint — Escalation + Export
  - Kiểm tra badge "⚠ Quá hạn" hiển thị đúng trên ticket list
  - Kiểm tra file CSV/PDF được tạo và lưu vào Downloads
  - Kiểm tra ProgressDialog hiển thị khi đang xuất

- [ ] 6.2 Checkpoint — Audit Log
  - Kiểm tra AuditLog được ghi khi tạo/xóa/khóa KTV
  - Kiểm tra tab Audit Log hiển thị đúng dữ liệu và filter hoạt động

- [ ] 6.3 Checkpoint — Chatbot + NPS
  - Kiểm tra tin nhắn Bot hiển thị với badge "Bot" màu xanh lá
  - Kiểm tra dialog NPS hiển thị đúng và lưu dữ liệu
  - Kiểm tra card NPS trong AdminThongKeFragment tính toán đúng

---

## Notes

- Tasks đánh dấu `*` là optional (property tests), có thể bỏ qua để MVP nhanh hơn
- Mỗi nhóm task có thể triển khai độc lập, không phụ thuộc nhau
- **Không dùng Cloud Functions** — toàn bộ chạy trên Android client (Spark plan)
- Escalation dùng `WorkManager` — chỉ chạy khi có người dùng đang active trên app
- Chatbot chạy client-side trong `YeuCauHoTroActivity` sau khi tạo ticket thành công
- NPS Survey kiểm tra khi mở `HomeActivity`, không cần push notification
- `AuditLogger.log()` không được throw exception — lỗi chỉ log, không ảnh hưởng luồng chính
- Backward compatibility: `vaiTroNguoiGui` null trong TinNhan cũ = tin nhắn thường (không phải Bot)
