# Requirements Document

## Introduction

Feature này cải tiến hệ thống Chăm Sóc Khách Hàng B2B (Android/Firebase) theo bốn hướng chính:
1. Thống kê và cảnh báo đánh giá xấu từ khách hàng doanh nghiệp.
2. Kiểm soát quyền đánh giá sản phẩm theo gói đăng ký thực tế.
3. Hỗ trợ gói dùng thử sản phẩm và chuyển lead sang bộ phận kinh doanh.
4. Ghi chú tiến độ hỗ trợ (KTV Handoff) để KTV kế tiếp tiếp tục đúng bước.

---

## Glossary

- **He_Thong**: Ứng dụng Android CSKH B2B sử dụng Firebase/Firestore.
- **KhachHang**: Người dùng có vai trò `KhachHang`, đại diện cho một doanh nghiệp (maSoThue).
- **KTV**: Kỹ thuật viên hỗ trợ, vai trò `KTV` trong NguoiDung.
- **Admin**: Quản trị viên hệ thống, vai trò `Admin` trong NguoiDung.
- **DanhGia**: Document trong collection `DanhGia`, chứa đánh giá sản phẩm của KhachHang (soSao 1–5).
- **DanhGia_Xau**: DanhGia có `soSao <= 2`.
- **YeuCauHoTro**: Document trong collection `YeuCauHoTro`, đại diện cho một ticket hỗ trợ.
- **GoiDangKy**: Document trong collection `GoiDangKy`, liên kết maSoThue với danh sách `sanPhamDangKy` và `loaiGoi`.
- **GoiChinhThuc**: GoiDangKy có `loaiGoi = "ChinhThuc"` và `trangThai = "HoatDong"`.
- **GoiDungThu**: GoiDangKy có `loaiGoi = "DungThu"` và `trangThai = "HoatDong"`.
- **SanPhamDangKy**: Danh sách sản phẩm (`List<String>`) trong GoiDangKy mà doanh nghiệp được phép sử dụng/đánh giá.
- **LichSuHoTro**: Danh sách các bản ghi tiến độ hỗ trợ (sub-collection hoặc field array) trong YeuCauHoTro.
- **GhiChuTienDo**: Một bản ghi trong LichSuHoTro, gồm nội dung ghi chú, uid KTV, tên KTV, và thời điểm ghi.
- **BoPhanKinhDoanh**: Nhóm người dùng Admin/KTV được gắn tag `kinhDoanh = true` trong NguoiDung, nhận thông báo lead dùng thử.
- **SanPham**: Một trong các giá trị: `ECUS5`, `E-INVOICE`, `ETAX`, `EBH`, `CLOUDOFFICE`, `TRUEPOS`.
- **KhoangThoiGian_GanDay**: 30 ngày tính từ thời điểm xuất hiện DanhGia_Xau.

---

## Requirements

### Requirement 1: Thống kê đánh giá xấu và lịch sử hỗ trợ liên quan

**User Story:** Là Admin, tôi muốn xem danh sách các đánh giá xấu kèm số lần CSKH đã hỗ trợ doanh nghiệp đó trong 30 ngày gần nhất, để ưu tiên theo dõi và can thiệp kịp thời.

#### Acceptance Criteria

1. WHEN Admin truy cập màn hình thống kê, THE He_Thong SHALL truy vấn collection `DanhGia` và trả về danh sách các DanhGia_Xau (`soSao <= 2`) được sắp xếp theo `taoLuc` giảm dần.

2. WHEN He_Thong hiển thị một DanhGia_Xau, THE He_Thong SHALL hiển thị đầy đủ các thông tin: tên công ty (`tenCongTy`), mã số thuế (`maSoThue`), thời gian đánh giá (`taoLuc`), sản phẩm gặp vấn đề (`sanPham`), số sao (`soSao`), và nội dung góp ý (`noiDung`).

3. WHEN He_Thong hiển thị một DanhGia_Xau, THE He_Thong SHALL đếm số YeuCauHoTro có cùng `uid` với người đánh giá và có `taoLuc` trong khoảng 30 ngày trước thời điểm `taoLuc` của DanhGia_Xau đó, rồi hiển thị số đếm này cạnh mỗi mục.

4. WHEN Admin nhấn vào một mục DanhGia_Xau, THE He_Thong SHALL hiển thị danh sách các YeuCauHoTro liên quan trong khoảng thời gian 30 ngày đó, bao gồm: tiêu đề lỗi, trạng thái, tên KTV xử lý, và thời gian tạo.

5. IF collection `DanhGia` không có DanhGia_Xau nào trong khoảng thời gian được chọn, THEN THE He_Thong SHALL hiển thị thông báo "Không có đánh giá xấu trong khoảng thời gian này".

6. WHEN Admin chọn bộ lọc thời gian (7 ngày hoặc 30 ngày), THE He_Thong SHALL lọc danh sách DanhGia_Xau theo khoảng thời gian tương ứng tính từ thời điểm hiện tại.

---

### Requirement 2: Kiểm soát quyền đánh giá theo gói đăng ký

**User Story:** Là Admin, tôi muốn chỉ cho phép doanh nghiệp đánh giá những sản phẩm họ đã đăng ký trong GoiDangKy, để dữ liệu đánh giá phản ánh đúng trải nghiệm thực tế.

#### Acceptance Criteria

1. WHEN KhachHang mở màn hình đánh giá sản phẩm, THE He_Thong SHALL truy vấn GoiDangKy theo `maSoThue` của KhachHang và chỉ hiển thị danh sách sản phẩm có trong `sanPhamDangKy` của gói đang hoạt động (`trangThai = "HoatDong"`).

2. IF KhachHang không có GoiDangKy nào với `trangThai = "HoatDong"`, THEN THE He_Thong SHALL hiển thị thông báo "Doanh nghiệp của bạn chưa có gói đăng ký đang hoạt động" và ẩn toàn bộ danh sách sản phẩm có thể đánh giá.

3. WHEN KhachHang cố gắng gửi DanhGia cho một sản phẩm không có trong `sanPhamDangKy` của GoiDangKy đang hoạt động, THE He_Thong SHALL từ chối lưu DanhGia và hiển thị thông báo "Bạn không có quyền đánh giá sản phẩm này".

4. THE He_Thong SHALL kiểm tra quyền đánh giá ở cả phía client (ẩn UI) và phía server (Firestore Security Rules), đảm bảo không thể bypass bằng cách gọi API trực tiếp.

5. WHERE KhachHang có GoiDungThu, THE He_Thong SHALL cho phép KhachHang đánh giá các sản phẩm trong `sanPhamDangKy` của GoiDungThu tương tự như GoiChinhThuc, nhưng gắn thêm field `loaiGoi = "DungThu"` vào document DanhGia khi lưu.

---

### Requirement 3: Gói dùng thử sản phẩm và chuyển lead kinh doanh

**User Story:** Là Admin, tôi muốn cho phép doanh nghiệp dùng thử sản phẩm và tự động chuyển thông tin lead sang bộ phận kinh doanh khi họ đánh giá tốt, để tăng tỷ lệ chuyển đổi hợp đồng.

#### Acceptance Criteria

1. THE He_Thong SHALL hỗ trợ field `loaiGoi` trong GoiDangKy với hai giá trị hợp lệ: `"ChinhThuc"` và `"DungThu"`.

2. WHEN Admin tạo hoặc cập nhật GoiDangKy, THE He_Thong SHALL cho phép Admin chỉ định `loaiGoi` là `"ChinhThuc"` hoặc `"DungThu"` cùng với danh sách `sanPhamDangKy` tương ứng.

3. WHEN KhachHang có GoiDungThu gửi DanhGia với `soSao >= 4` cho một sản phẩm trong `sanPhamDangKy` của GoiDungThu, THE He_Thong SHALL tự động tạo một document Lead trong collection `LeadKinhDoanh` với các thông tin: `maSoThue`, `tenCongTy`, `sanPham`, `soSao`, `noiDung`, `taoLuc`, `trangThaiLead = "Moi"`.

4. WHEN một Lead mới được tạo trong `LeadKinhDoanh`, THE He_Thong SHALL gửi Firebase Cloud Messaging notification đến tất cả người dùng có `kinhDoanh = true` trong NguoiDung, với nội dung: tên công ty, sản phẩm dùng thử, và số sao đánh giá.

5. WHEN Admin truy cập màn hình quản lý Lead, THE He_Thong SHALL hiển thị danh sách Lead từ collection `LeadKinhDoanh` sắp xếp theo `taoLuc` giảm dần, bao gồm: tên công ty, MST, sản phẩm, số sao, trạng thái lead, và thời gian tạo.

6. WHEN Admin cập nhật trạng thái một Lead, THE He_Thong SHALL cho phép chuyển `trangThaiLead` sang một trong các giá trị: `"Moi"`, `"DangTuVan"`, `"DaDangKy"`, `"TuChoi"`.

7. IF KhachHang có GoiDungThu gửi DanhGia với `soSao <= 2`, THEN THE He_Thong SHALL KHÔNG tạo Lead và SHALL lưu DanhGia như bình thường để phục vụ thống kê đánh giá xấu (Requirement 1).

---

### Requirement 4: Ghi chú tiến độ hỗ trợ (KTV Handoff)

**User Story:** Là KTV, tôi muốn ghi lại tiến độ xử lý ticket và đọc ghi chú của KTV trước, để tiếp tục hỗ trợ khách hàng đúng từ bước đã dừng mà không cần hỏi lại từ đầu.

#### Acceptance Criteria

1. THE He_Thong SHALL thêm field `lichSuHoTro` kiểu `List<Map>` vào document YeuCauHoTro, mỗi phần tử gồm: `ktvUid` (String), `ktvTen` (String), `noiDung` (String), `thoiDiem` (Timestamp).

2. WHEN KTV đang xử lý một YeuCauHoTro có `trangThai = "DangXuLy"`, THE He_Thong SHALL hiển thị ô nhập liệu "Ghi chú tiến độ" trong màn hình chi tiết ticket (KtvTicketDetailActivity).

3. WHEN KTV nhập nội dung ghi chú và nhấn "Lưu ghi chú", THE He_Thong SHALL append một GhiChuTienDo mới vào `lichSuHoTro` của YeuCauHoTro tương ứng, với `ktvUid` và `ktvTen` lấy từ tài khoản KTV đang đăng nhập, `thoiDiem` là thời điểm hiện tại.

4. IF nội dung ghi chú rỗng hoặc chỉ chứa khoảng trắng, THEN THE He_Thong SHALL không lưu và hiển thị thông báo "Vui lòng nhập nội dung ghi chú".

5. WHEN KTV mở màn hình chi tiết một YeuCauHoTro, THE He_Thong SHALL hiển thị toàn bộ `lichSuHoTro` theo thứ tự thời gian tăng dần, mỗi mục hiển thị: tên KTV, thời điểm ghi, và nội dung ghi chú.

6. WHEN một KTV mới được phân công vào YeuCauHoTro (field `ktvUid` thay đổi), THE He_Thong SHALL hiển thị badge "Có ghi chú từ KTV trước" trên card ticket trong màn hình danh sách ticket của KTV mới, nếu `lichSuHoTro` không rỗng.

7. WHILE YeuCauHoTro có `trangThai = "DaXuLy"`, THE He_Thong SHALL hiển thị `lichSuHoTro` ở chế độ chỉ đọc và ẩn ô nhập liệu ghi chú mới.

8. THE He_Thong SHALL giới hạn độ dài mỗi GhiChuTienDo tối đa 1000 ký tự; IF KTV nhập quá 1000 ký tự, THEN THE He_Thong SHALL hiển thị cảnh báo và không cho phép lưu.
