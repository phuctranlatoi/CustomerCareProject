# Implementation Plan: Enterprise Customer Care Improvements

## Overview

Triển khai 4 tính năng mở rộng hệ thống CSKH B2B: thống kê đánh giá xấu, kiểm soát quyền đánh giá theo gói, gói dùng thử + lead kinh doanh, và KTV Handoff ghi chú tiến độ. Ngôn ngữ: Java (Android) + Node.js (Cloud Functions).

## Tasks

- [x] 1. Cập nhật Model Layer
  - [x] 1.1 Thêm `loaiGoi` vào `GoiDangKy.java`
  - [x] 1.2 Thêm `loaiGoi`, `maSoThue`, `tenCongTy` vào `DanhGia.java`
  - [x] 1.3 Thêm `lichSuHoTro` vào `YeuCauHoTro.java`
  - [x] 1.4 Tạo `model/GhiChuTienDo.java`
  - [x] 1.5 Tạo `model/LeadKinhDoanh.java`

- [x] 2. Kiểm soát quyền đánh giá theo gói
  - [x] 2.1 Sửa `DanhGiaActivity.java` để truyền `loaiGoi` xuống fragment
  - [x] 2.2 Sửa `DanhGiaFormFragment.java`: truy vấn `GoiDangKy` trước khi render

- [x] 3. Checkpoint – Model và quyền đánh giá hoạt động đúng

- [x] 4. KTV Handoff – Ghi chú tiến độ
  - [x] 4.1 Tạo `layout/item_ghi_chu_tien_do.xml`
  - [x] 4.2 Tạo `ui/ktv/GhiChuTienDoAdapter.java`
  - [x] 4.3 Sửa `KtvTicketDetailActivity.java`: thêm section ghi chú tiến độ
  - [x] 4.4 Sửa `TicketAdapter.java`: thêm badge "Có ghi chú từ KTV trước"

- [x] 5. Admin – Thống kê đánh giá xấu
  - [x] 5.1 Tạo `layout/item_danh_gia_xau.xml`
  - [x] 5.2 Tạo `layout/fragment_admin_danh_gia_xau.xml`
  - [x] 5.3 Tạo `ui/admin/DanhGiaXauAdapter.java`
  - [x] 5.4 Tạo `ui/admin/AdminDanhGiaXauFragment.java`
  - [x] 5.5 Tích hợp vào `AdminPhanTichFragment`

- [x] 6. Checkpoint – KTV Handoff và Admin thống kê hoạt động đúng

- [x] 7. Gói dùng thử + Lead kinh doanh
  - [x] 7.1 Tạo `layout/item_lead.xml`
  - [x] 7.2 Tạo `layout/fragment_admin_lead.xml`
  - [x] 7.3 Tạo `ui/admin/LeadAdapter.java`
  - [x] 7.4 Tạo `ui/admin/AdminLeadFragment.java`

- [x] 8. Cloud Function – Tạo Lead tự động
  - [x] 8.1 Thêm trigger `onDanhGiaTot` vào `functions/index.js`
  - [x] 8.2 Thêm logic gửi FCM notification trong `onDanhGiaTot`

- [x] 9. Final Checkpoint – Toàn bộ tính năng hoạt động đúng

## Notes

- Spec này đã được implement đầy đủ (tất cả task bắt buộc hoàn thành).
- Property-based tests (junit-quickcheck) là optional, chưa implement.
- Firestore Security Rules cần deploy riêng lên Firebase Console.
- Cloud Functions cần deploy lên Firebase sau khi code xong.
