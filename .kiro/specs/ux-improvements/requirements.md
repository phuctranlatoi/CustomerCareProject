# Requirements Document

## Introduction

Tài liệu này mô tả các yêu cầu cho feature **UX Improvements** — cải thiện trải nghiệm người dùng cho ứng dụng CSKH B2B Android. Feature bao gồm 4 nhóm tính năng mới: (E) Tìm kiếm & lọc ticket, (F) Đính kèm file/ảnh trong chat, (G) Hiển thị SLA và cảnh báo thời gian chờ, (H) Template trả lời nhanh cho KTV. Tất cả tính năng được thêm mới, không xóa code cũ.

---

## Glossary

- **LichSuChatActivity**: Màn hình lịch sử ticket của khách hàng
- **KtvDashboardActivity**: Màn hình dashboard của kỹ thuật viên (KTV), hiện có 4 tab lọc theo trạng thái
- **ChatKhachHangActivity**: Màn hình chat của khách hàng với KTV
- **KtvChatActivity**: Màn hình chat của KTV với khách hàng
- **HomeActivity**: Màn hình chính của khách hàng
- **AdminTicketsFragment**: Fragment quản lý ticket trong dashboard Admin
- **AdminKnowledgeFragment**: Fragment quản lý knowledge base trong dashboard Admin (đã có sẵn, lưu `LoiPhatSinh`)
- **YeuCauHoTro**: Model ticket hỗ trợ, có các trường: `trangThai`, `sanPham`, `uuTien`, `taoLuc`, `ktvUid`, `thoiGianChoXuLy`
- **TinNhan**: Model tin nhắn chat, lưu trong `TinNhan/{ticketId}/messages`
- **LoiPhatSinh**: Model lỗi/bài viết trong knowledge base, có `tieuDe`, `moTa`, `cachGiaiQuyet`, `sanPham`
- **Template**: Câu trả lời mẫu được KTV lưu sẵn để dùng lại, lưu trong Firestore collection `TemplateTrLoi`
- **Firebase_Storage**: Dịch vụ lưu trữ file/ảnh của Firebase
- **SLA**: Service Level Agreement — cam kết thời gian phản hồi
- **HangCho**: Trạng thái ticket khi chưa có KTV rảnh
- **KTV**: Kỹ thuật viên hỗ trợ khách hàng
- **Admin**: Quản trị viên hệ thống
- **KhachHang**: Đại diện doanh nghiệp sử dụng ứng dụng
- **SanPham**: Một trong các sản phẩm: ECUS5 VNACCS, E-INVOICE, ETAX, EBH, CLOUDOFFICE, TRUEPOS
- **UuTien**: Mức ưu tiên ticket: `Cao`, `TrungBinh`, `Thap`

---

## Requirements

### Requirement E1: Tìm Kiếm Ticket Theo Từ Khóa (Khách Hàng)

**User Story:** As a KhachHang, I want to search tickets by keyword in LichSuChatActivity, so that I can quickly find a specific support request without scrolling through the entire list.

#### Acceptance Criteria

1. THE LichSuChatActivity SHALL display a search input field at the top of the ticket list.
2. WHEN a KhachHang enters text into the search field, THE LichSuChatActivity SHALL filter the displayed ticket list to show only tickets whose `tieuDeLoi` or `sanPham` contains the entered text (case-insensitive).
3. WHEN the search field is cleared, THE LichSuChatActivity SHALL restore the full ticket list.
4. IF the search query matches no tickets, THEN THE LichSuChatActivity SHALL display an empty state message "Không tìm thấy ticket phù hợp".

### Requirement E2: Lọc Ticket Theo Trạng Thái và Thời Gian (Khách Hàng)

**User Story:** As a KhachHang, I want to filter my ticket history by status and time range in LichSuChatActivity, so that I can view only the tickets relevant to my current need.

#### Acceptance Criteria

1. THE LichSuChatActivity SHALL provide a filter control allowing selection of ticket status: Tất cả, Chờ xử lý, Hàng chờ, Đang xử lý, Đã xử lý.
2. WHEN a KhachHang selects a status filter, THE LichSuChatActivity SHALL display only tickets matching the selected `trangThai` value.
3. THE LichSuChatActivity SHALL provide a time range filter with options: Tất cả, 7 ngày qua, 30 ngày qua.
4. WHEN a KhachHang selects a time range, THE LichSuChatActivity SHALL display only tickets whose `taoLuc` falls within the selected range.
5. WHEN both status filter and time range filter are active simultaneously, THE LichSuChatActivity SHALL apply both filters together (AND logic).

### Requirement E3: Lọc Ticket Theo Sản Phẩm và Ưu Tiên (KTV)

**User Story:** As a KTV, I want to filter my ticket list by product and priority in KtvDashboardActivity, so that I can focus on the most critical tickets for a specific product.

#### Acceptance Criteria

1. THE KtvDashboardActivity SHALL add a product filter chip group below the existing 4 status tabs, with options: Tất cả, ECUS5 VNACCS, E-INVOICE, ETAX, EBH, CLOUDOFFICE, TRUEPOS.
2. THE KtvDashboardActivity SHALL add a priority filter chip group, with options: Tất cả, Cao, TrungBinh, Thap.
3. WHEN a KTV selects a product filter chip, THE KtvDashboardActivity SHALL query Firestore with the additional `whereEqualTo("sanPham", selectedProduct)` constraint combined with the existing status filter.
4. WHEN a KTV selects a priority filter chip, THE KtvDashboardActivity SHALL query Firestore with the additional `whereEqualTo("uuTien", selectedPriority)` constraint combined with the existing status and product filters.
5. WHEN a KTV selects "Tất cả" for product or priority, THE KtvDashboardActivity SHALL remove that filter constraint from the query.
6. WHEN filter criteria change, THE KtvDashboardActivity SHALL re-execute the Firestore listener and update the ticket list in real time.

---

### Requirement F1: Đính Kèm Ảnh Khi Chat (Khách Hàng)

**User Story:** As a KhachHang, I want to attach a screenshot image when chatting in ChatKhachHangActivity, so that I can visually show the KTV the error I am experiencing.

#### Acceptance Criteria

1. THE ChatKhachHangActivity SHALL display an image attachment button in the input bar alongside the existing send button.
2. WHEN a KhachHang taps the attachment button, THE ChatKhachHangActivity SHALL open the device image picker (gallery or camera).
3. WHEN a KhachHang selects an image, THE ChatKhachHangActivity SHALL upload the image to Firebase_Storage at path `chat_images/{ticketId}/{timestamp}_{filename}`.
4. WHEN the image upload completes successfully, THE ChatKhachHangActivity SHALL save a message to `TinNhan/{ticketId}/messages` with field `loaiTin = "anh"` and `anhUrl` containing the Firebase_Storage download URL.
5. IF the image upload fails, THEN THE ChatKhachHangActivity SHALL display a Toast error message "Tải ảnh lên thất bại, vui lòng thử lại".
6. WHILE an image is uploading, THE ChatKhachHangActivity SHALL display a progress indicator and disable the attachment button to prevent duplicate uploads.
7. THE ChatKhachHangActivity SHALL only allow image files (JPEG, PNG) with maximum size 10 MB; IF a file exceeds this limit, THEN THE ChatKhachHangActivity SHALL display a Toast "Ảnh quá lớn, vui lòng chọn ảnh dưới 10MB".

### Requirement F2: Hiển Thị Ảnh Đính Kèm Trong Chat (KTV)

**User Story:** As a KTV, I want to view attached images sent by customers in KtvChatActivity, so that I can understand the issue visually without asking for additional description.

#### Acceptance Criteria

1. WHEN a message with `loaiTin = "anh"` is received, THE KtvChatActivity SHALL render an image thumbnail in the chat bubble instead of text.
2. WHEN a KTV taps an image thumbnail, THE KtvChatActivity SHALL open the image in a full-screen viewer.
3. THE ChatAdapter SHALL support rendering both `loaiTin = "van_ban"` (default) and `loaiTin = "anh"` message types without breaking existing text message display.
4. IF an image URL fails to load, THEN THE ChatAdapter SHALL display a placeholder icon indicating the image could not be loaded.

### Requirement F3: Lưu Trữ Ảnh Trên Firebase Storage

**User Story:** As a system, I want images to be stored in Firebase Storage with proper structure, so that URLs are persistent and accessible to both customer and KTV.

#### Acceptance Criteria

1. THE System SHALL store uploaded images in Firebase_Storage under path `chat_images/{ticketId}/{timestamp}_{originalFilename}`.
2. THE System SHALL store the Firebase_Storage download URL in the `anhUrl` field of the corresponding TinNhan document.
3. THE System SHALL set `loaiTin = "van_ban"` as the default value for all existing text messages to maintain backward compatibility.

---

### Requirement G1: Hiển Thị Thời Gian Chờ Ước Tính (Khách Hàng)

**User Story:** As a KhachHang, I want to see an estimated wait time when my ticket is in HangCho status, so that I know how long I need to wait before a KTV is assigned.

#### Acceptance Criteria

1. WHEN a ticket has `trangThai = "HangCho"`, THE ChatKhachHangActivity SHALL display an estimated wait time label below the subtitle, calculated as: `soKtvRanh > 0` → "Ước tính ~X phút" (where X = position in queue / soKtvRanh * 5), `soKtvRanh = 0` → "Hiện chưa có kỹ thuật viên rảnh, vui lòng chờ".
2. THE ChatKhachHangActivity SHALL update the estimated wait time every 60 seconds by re-querying the count of KTV with `trangThai = "Ran"`.
3. WHEN the ticket transitions out of HangCho status, THE ChatKhachHangActivity SHALL hide the estimated wait time label.

### Requirement G2: Hiển Thị Thời Gian Đã Chờ Trên Card Ticket (Khách Hàng)

**User Story:** As a KhachHang, I want to see how long my active ticket has been waiting on the ticket card in HomeActivity, so that I have visibility into the current wait time.

#### Acceptance Criteria

1. WHEN a ticket card is displayed in HomeActivity with `trangThai` in ["HangCho", "ChoXuLy"], THE HomeActivity SHALL display a "Đã chờ: Xm" label on the card, where X is the elapsed minutes since `taoLuc`.
2. THE HomeActivity SHALL update the elapsed wait time display every 60 seconds.
3. WHEN `trangThai = "DangXuLy"`, THE HomeActivity SHALL hide the wait time label and display "KTV đang xử lý" instead.

### Requirement G3: Cảnh Báo Admin Khi Ticket Chờ Quá 30 Phút

**User Story:** As an Admin, I want to see a red badge warning on tickets that have been waiting more than 30 minutes in AdminTicketsFragment, so that I can take action to ensure SLA compliance.

#### Acceptance Criteria

1. THE AdminTicketsFragment SHALL display a red badge "⚠ Quá 30 phút" on any ticket card where `trangThai` is "HangCho" or "ChoXuLy" AND the elapsed time since `taoLuc` exceeds 30 minutes.
2. THE AdminTicketsFragment SHALL sort overdue tickets (waiting > 30 minutes) to the top of the list.
3. WHEN a ticket is assigned to a KTV and transitions to "DangXuLy", THE AdminTicketsFragment SHALL remove the overdue badge from that ticket.
4. THE AdminTicketsFragment SHALL add a filter toggle "Chỉ hiện quá hạn" that, when active, shows only tickets with elapsed wait time > 30 minutes.

---

### Requirement H1: Lưu và Sử Dụng Template Trả Lời Nhanh (KTV)

**User Story:** As a KTV, I want to save and reuse common reply templates in KtvChatActivity, so that I can respond to frequent issues faster without retyping the same answers.

#### Acceptance Criteria

1. THE KtvChatActivity SHALL display a "Template" button in the input bar area.
2. WHEN a KTV taps the "Template" button, THE KtvChatActivity SHALL open a BottomSheetDialog displaying the list of available templates for the current ticket's `sanPham`.
3. WHEN a KTV selects a template from the BottomSheetDialog, THE KtvChatActivity SHALL populate the message input field with the template's `noiDung` text and close the bottom sheet.
4. THE KtvChatActivity SHALL load templates from Firestore collection `TemplateTrLoi` filtered by `sanPham` matching the current ticket's product, ordered by `tieuDe` ascending.
5. IF no templates exist for the current product, THEN THE KtvChatActivity SHALL display "Chưa có template cho sản phẩm này" in the bottom sheet.

### Requirement H2: Quản Lý Template Trong AdminKnowledgeFragment

**User Story:** As an Admin, I want to create, edit, and delete reply templates in AdminKnowledgeFragment, so that KTVs have an up-to-date library of standard responses.

#### Acceptance Criteria

1. THE AdminKnowledgeFragment SHALL add a "Template" tab alongside the existing knowledge base content, allowing Admin to switch between managing LoiPhatSinh and TemplateTrLoi.
2. WHEN the "Template" tab is active, THE AdminKnowledgeFragment SHALL display the list of templates from Firestore collection `TemplateTrLoi`, filtered by the selected `sanPham` from the existing spinner.
3. THE AdminKnowledgeFragment SHALL provide a "Thêm template" button that opens a dialog with fields: `tieuDe` (required), `noiDung` (required), `sanPham` (pre-filled from spinner).
4. WHEN an Admin submits the add template dialog with valid `tieuDe` and `noiDung`, THE AdminKnowledgeFragment SHALL save a new document to `TemplateTrLoi` collection with fields: `tieuDe`, `noiDung`, `sanPham`, `taoLuc`.
5. IF `tieuDe` or `noiDung` is empty when submitting, THEN THE AdminKnowledgeFragment SHALL display a Toast "Vui lòng nhập tiêu đề và nội dung template".
6. THE AdminKnowledgeFragment SHALL allow Admin to delete a template, with a confirmation dialog before deletion.

### Requirement H3: Cấu Trúc Dữ Liệu Template

**User Story:** As a system, I want templates to be stored in a dedicated Firestore collection, so that they are queryable by product and accessible to KTVs in real time.

#### Acceptance Criteria

1. THE System SHALL store templates in Firestore collection `TemplateTrLoi` with fields: `tieuDe` (String), `noiDung` (String), `sanPham` (String), `taoLuc` (Timestamp).
2. THE System SHALL support querying `TemplateTrLoi` by `sanPham` field to retrieve product-specific templates.
3. FOR ALL templates saved and then retrieved by `sanPham`, THE System SHALL return the same `tieuDe` and `noiDung` values that were saved (round-trip property).
