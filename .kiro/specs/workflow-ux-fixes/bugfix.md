# Bugfix Requirements Document

## Introduction

Dự án Android Customer Care B2B hiện có 6 vấn đề quy trình UX khiến người dùng (khách hàng và KTV) không thể thực hiện các tác vụ cốt lõi một cách trực quan. Các lỗi này bao gồm: FAB "Yêu cầu hỗ trợ" không điều hướng đúng, luồng tạo ticket bị nhầm lẫn với luồng đánh giá sản phẩm, màn hình chờ KTV không có timeout/hủy, KTV tự động nhận ticket khi mở mà không xác nhận, danh sách lịch sử ticket không có filter/search, và luồng đánh giá sản phẩm bị tách rời khỏi luồng hỗ trợ. Các vấn đề này ảnh hưởng trực tiếp đến khả năng sử dụng của ứng dụng trong môi trường B2B.

---

## Bug Analysis

### Current Behavior (Defect)

**Bug 1 – FAB "Yêu cầu hỗ trợ" không điều hướng**

1.1 WHEN người dùng nhấn FAB "Yêu cầu hỗ trợ" trên HomeActivity THEN hệ thống chỉ hiển thị Toast "Chọn sản phẩm cần hỗ trợ bên dưới" mà không điều hướng đến bất kỳ màn hình nào

1.2 WHEN người dùng nhấn vào một sản phẩm trong grid trên HomeActivity THEN hệ thống mở DanhGiaActivity (màn hình đánh giá sản phẩm) thay vì màn hình tạo yêu cầu hỗ trợ

**Bug 2 – Luồng tạo ticket bị nhầm lẫn**

2.1 WHEN người dùng muốn tạo yêu cầu hỗ trợ kỹ thuật từ HomeActivity THEN hệ thống không cung cấp đường điều hướng rõ ràng nào đến YeuCauHoTroActivity

2.2 WHEN người dùng nhấn vào sản phẩm trong grid với mục đích tạo ticket hỗ trợ THEN hệ thống mở DanhGiaActivity (đánh giá sản phẩm) thay vì YeuCauHoTroActivity, gây nhầm lẫn về mục đích của thao tác

**Bug 3 – ChatKhachHangActivity quét KTV không có timeout và không có nút hủy**

3.1 WHEN ticket ở trạng thái HangCho và `batDauQuetKtv()` được gọi THEN hệ thống lắng nghe realtime vô hạn mà không có cơ chế timeout

3.2 WHEN người dùng đã chờ KTV trong thời gian dài THEN hệ thống không hiển thị thông báo cụ thể về thời gian đã chờ thực tế

3.3 WHEN người dùng muốn hủy yêu cầu hỗ trợ trong khi đang chờ KTV THEN hệ thống không cung cấp nút "Hủy yêu cầu" trên màn hình ChatKhachHangActivity

**Bug 4 – KtvTicketDetailActivity tự động set trạng thái DangXuLy khi mở**

4.1 WHEN KTV mở KtvTicketDetailActivity cho một ticket ở trạng thái ChoXuLy hoặc HangCho THEN hệ thống tự động cập nhật trangThai thành "DangXuLy" ngay lập tức mà không có bước xác nhận

4.2 WHEN KTV mở nhầm ticket không thuộc phạm vi xử lý của mình THEN hệ thống đã thay đổi trạng thái ticket không thể hoàn tác, buộc KTV phải xử lý ticket đó

**Bug 5 – LichSuChatActivity hiển thị tất cả ticket không có filter**

5.1 WHEN người dùng mở LichSuChatActivity THEN hệ thống hiển thị tất cả ticket theo thứ tự thời gian giảm dần mà không có tùy chọn lọc theo trạng thái

5.2 WHEN người dùng có nhiều ticket và muốn tìm một ticket cụ thể THEN hệ thống không cung cấp chức năng tìm kiếm theo từ khóa hoặc lọc theo trạng thái (đang xử lý / đã xử lý / hàng chờ)

**Bug 6 – Luồng đánh giá sản phẩm bị tách rời sau khi đóng ticket**

6.1 WHEN ticket chuyển sang trạng thái DaXuLy và khách hàng đã đánh giá KTV THEN hệ thống kết thúc luồng mà không gợi ý hoặc điều hướng đến màn hình đánh giá sản phẩm liên quan

6.2 WHEN khách hàng hoàn thành đánh giá KTV trong DanhGiaKTVActivity THEN hệ thống gọi `finish()` và quay về màn hình trước mà không đề xuất đánh giá sản phẩm đã được hỗ trợ

---

### Expected Behavior (Correct)

**Bug 1 – FAB "Yêu cầu hỗ trợ" điều hướng đúng**

1.1 WHEN người dùng nhấn FAB "Yêu cầu hỗ trợ" trên HomeActivity THEN hệ thống SHALL hiển thị một bottom sheet hoặc dialog cho phép người dùng chọn sản phẩm cần hỗ trợ, sau đó điều hướng đến YeuCauHoTroActivity với sản phẩm đã chọn

1.2 WHEN người dùng nhấn vào một sản phẩm trong grid trên HomeActivity THEN hệ thống SHALL hiển thị menu lựa chọn hành động (đánh giá sản phẩm hoặc tạo yêu cầu hỗ trợ) hoặc điều hướng theo luồng được thiết kế rõ ràng

**Bug 2 – Luồng tạo ticket rõ ràng**

2.1 WHEN người dùng muốn tạo yêu cầu hỗ trợ kỹ thuật từ HomeActivity THEN hệ thống SHALL cung cấp ít nhất một đường điều hướng rõ ràng đến YeuCauHoTroActivity (qua FAB hoặc click sản phẩm)

2.2 WHEN người dùng nhấn vào sản phẩm trong grid với mục đích tạo ticket hỗ trợ THEN hệ thống SHALL điều hướng đến YeuCauHoTroActivity với thông tin sản phẩm được truyền qua Intent extra "sanPham"

**Bug 3 – ChatKhachHangActivity có timeout và nút hủy**

3.1 WHEN ticket ở trạng thái HangCho và `batDauQuetKtv()` được gọi THEN hệ thống SHALL tự động hủy lắng nghe và hiển thị thông báo sau khoảng thời gian timeout được định nghĩa (ví dụ: 30 phút)

3.2 WHEN người dùng đã chờ KTV THEN hệ thống SHALL hiển thị thời gian đã chờ thực tế (tính từ `taoLuc` của ticket) và cập nhật định kỳ

3.3 WHEN người dùng muốn hủy yêu cầu hỗ trợ trong khi đang chờ KTV THEN hệ thống SHALL hiển thị nút "Hủy yêu cầu" và khi nhấn SHALL hiển thị dialog xác nhận, sau đó cập nhật trangThai ticket thành "DaHuy" và điều hướng về HomeActivity

**Bug 4 – KtvTicketDetailActivity có bước xác nhận nhận ticket**

4.1 WHEN KTV mở KtvTicketDetailActivity cho một ticket ở trạng thái ChoXuLy hoặc HangCho THEN hệ thống SHALL hiển thị dialog xác nhận "Nhận ticket này?" với nút "Nhận" và "Xem thôi" trước khi thay đổi trạng thái

4.2 WHEN KTV chọn "Xem thôi" trong dialog xác nhận THEN hệ thống SHALL mở màn hình ở chế độ readOnly (không thay đổi trạng thái ticket) và đặt `daCapNhatTrangThai = true` để bỏ qua auto-update

**Bug 5 – LichSuChatActivity có filter theo trạng thái**

5.1 WHEN người dùng mở LichSuChatActivity THEN hệ thống SHALL hiển thị các chip filter theo trạng thái (Tất cả / Đang xử lý / Đã xử lý / Hàng chờ) ở phía trên danh sách

5.2 WHEN người dùng chọn một chip filter THEN hệ thống SHALL lọc danh sách ticket hiển thị theo trạng thái tương ứng và cập nhật RecyclerView

**Bug 6 – Luồng đánh giá sản phẩm được kết nối sau khi đóng ticket**

6.1 WHEN ticket chuyển sang trạng thái DaXuLy và khách hàng đã đánh giá KTV THEN hệ thống SHALL hiển thị dialog hoặc snackbar gợi ý "Bạn có muốn đánh giá sản phẩm [tên sản phẩm] không?" với nút "Đánh giá ngay" và "Bỏ qua"

6.2 WHEN khách hàng hoàn thành đánh giá KTV trong DanhGiaKTVActivity và nhấn "Đánh giá ngay" THEN hệ thống SHALL điều hướng đến DanhGiaActivity với extra "sanPham" được truyền từ thông tin ticket

---

### Unchanged Behavior (Regression Prevention)

3.1 WHEN ticket đã được assign KTV (ktvUid không rỗng) và trạng thái là ChoXuLy hoặc DangXuLy THEN hệ thống SHALL CONTINUE TO hiển thị tên KTV trong subtitle và dừng quét KTV

3.2 WHEN ticket chuyển sang trạng thái DaXuLy THEN hệ thống SHALL CONTINUE TO ẩn thanh nhập tin nhắn, hiển thị "Cuộc trò chuyện đã kết thúc" và tự động mở DanhGiaKTVActivity nếu chưa đánh giá

3.3 WHEN KTV nhấn nút "Đóng ticket" trong KtvTicketDetailActivity THEN hệ thống SHALL CONTINUE TO hiển thị dialog xác nhận và cập nhật trangThai thành "DaXuLy" sau khi xác nhận

3.4 WHEN người dùng nhấn nút lịch sử chat (btnLichSuChat) trên HomeActivity THEN hệ thống SHALL CONTINUE TO điều hướng đến LichSuChatActivity

3.5 WHEN ticket ở trạng thái HangCho và có KTV rảnh được tìm thấy THEN hệ thống SHALL CONTINUE TO thực hiện assign KTV bằng Firestore transaction để tránh race condition

3.6 WHEN người dùng đã có ticket đang xử lý (ChoXuLy/DangXuLy/HangCho) THEN hệ thống SHALL CONTINUE TO hiển thị cardTicketActive trên HomeActivity với thông tin ticket và nút "Chat ngay"

3.7 WHEN người dùng nhấn nút "Chat ngay" trên cardTicketActive THEN hệ thống SHALL CONTINUE TO điều hướng đến ChatKhachHangActivity với ticketId và hoTen tương ứng

3.8 WHEN KTV mở KtvTicketDetailActivity ở chế độ readOnly THEN hệ thống SHALL CONTINUE TO ẩn layoutNhapGhiChu và btnDongTicket, không thay đổi trạng thái ticket

3.9 WHEN người dùng chọn chip filter "Tất cả" trong LichSuChatActivity THEN hệ thống SHALL CONTINUE TO hiển thị toàn bộ danh sách ticket theo thứ tự thời gian giảm dần

3.10 WHEN DanhGiaKTVActivity được mở THEN hệ thống SHALL CONTINUE TO cho phép người dùng đánh giá KTV bằng RatingBar, chip tags và nhận xét tự do, sau đó lưu vào collection DanhGiaKTV và cập nhật điểm trung bình KTV
