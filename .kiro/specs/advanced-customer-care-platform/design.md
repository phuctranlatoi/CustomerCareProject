# Design Document: Advanced Customer Care Platform

## Overview

Tài liệu thiết kế cho 5 tính năng nâng cao của nền tảng CSKH B2B Android (Java/Firebase):
1. **Escalation tự động** — tự động tăng ưu tiên và cảnh báo khi ticket quá hạn SLA
2. **Export báo cáo PDF/CSV** — xuất báo cáo thống kê ra file
3. **Audit Log** — ghi lại mọi thay đổi quan trọng trong hệ thống
4. **Chatbot AI sơ bộ** — bot tự trả lời FAQ từ knowledge base khi tạo ticket
5. **NPS Survey định kỳ** — khảo sát Net Promoter Score hàng tháng

> **Trạng thái online KTV**: Giữ nguyên hành vi hiện tại — KTV vào app → set `trangThai = "Ran"` qua Firebase RTDB, thoát app → tự động set `"Offline"` qua `onDisconnect`. Không cần quản lý ca làm việc.

---

## Architecture Overview

```
Android Client (Java)
    ├── UI Layer          — Activities, Fragments, Adapters
    ├── Model Layer       — AuditLog, NpsSurvey (models mới)
    └── Utils Layer       — ReportExporter, AuditLogger, EscalationChecker (utils mới)

Firebase Backend (Spark Plan — không dùng Cloud Functions)
    └── Firestore         — AuditLog, NpsSurvey collections (read/write trực tiếp từ app)
```

> **Không dùng Cloud Functions** — toàn bộ logic chạy trên Android client:
> - **Escalation**: `WorkManager` PeriodicWorkRequest chạy mỗi 15 phút trên thiết bị KTV/Admin
> - **Chatbot**: Chạy trực tiếp trong `YeuCauHoTroActivity` sau khi tạo ticket thành công
> - **NPS Survey**: `WorkManager` kiểm tra khi mở app, hiển thị dialog nếu đến kỳ

---

## Feature 1: Escalation Tự Động

### Data Model

**Thêm fields vào `YeuCauHoTro` (Firestore):**
```
daEscalate: Boolean (default: false)
thoiGianEscalate: Timestamp
thoiGianXuLy: Long (phút, set khi đóng ticket)
lichSuEscalation: List<Map> [
  { thoiDiem: Timestamp, uuTienTruoc: String, uuTienSau: String, lyDo: String }
]
```

### Cloud Function: `scheduledEscalation` → **WorkManager (Android)**

Thay vì Cloud Function, dùng `WorkManager` chạy trên thiết bị của KTV/Admin:

```java
// workers/EscalationWorker.java
public class EscalationWorker extends Worker {
    @Override
    public Result doWork() {
        // Query Firestore: trangThai in [HangCho, ChoXuLy, DangXuLy]
        //                  AND daEscalate == false
        // Với mỗi ticket: tính elapsed, so ngưỡng SLA
        // Nếu quá hạn: update daEscalate=true, tăng uuTien, append lichSuEscalation
        return Result.success();
    }
}

// Đăng ký trong KtvDashboardActivity / AdminDashboardActivity khi mở app:
PeriodicWorkRequest escalationWork =
    new PeriodicWorkRequest.Builder(EscalationWorker.class, 15, TimeUnit.MINUTES)
        .build();
WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "escalation_check",
    ExistingPeriodicWorkPolicy.KEEP,
    escalationWork
);
```

**Lưu ý**: WorkManager chỉ chạy khi app đang active hoặc ở background gần đây. Đây là trade-off chấp nhận được cho Spark plan — escalation vẫn hoạt động khi có người dùng đang dùng app.

### Android Client Changes

- **`TicketAdapter.java`**: Thêm badge "⚠ Quá hạn" màu đỏ khi `daEscalate = true`
- **`AdminTicketsFragment.java`**: Sort ticket escalated lên đầu; ghi nhận `thoiGianXuLy` khi đóng ticket
- **`KtvDashboardActivity.java`**: Hiển thị badge escalation trên ticket list

### SLA Constants

```java
// SlaHelper.java
public static int getSlaThreshold(String uuTien) {
    switch (uuTien) {
        case "Cao":       return 15;
        case "TrungBinh": return 30;
        case "Thap":      return 60;
        default:          return 30;
    }
}
```

---

## Feature 2: Export Báo Cáo PDF/CSV

### Libraries

- **PDF**: `com.itextpdf:itext7-core:7.2.5` (hoặc `com.github.barteksc:android-pdf-viewer` cho viewer)
- **CSV**: Không cần thư viện — dùng `StringBuilder` + `FileOutputStream`
- **File storage**: `MediaStore` API (Android 10+), `Environment.DIRECTORY_DOWNLOADS` (Android < 10)

### ReportExporter Utility

```java
// utils/ReportExporter.java
public class ReportExporter {
    // Xuất CSV tickets
    public static void exportTicketsCsv(Context ctx, List<YeuCauHoTro> tickets,
                                         OnExportComplete callback);
    // Xuất CSV KTV summary
    public static void exportKtvCsv(Context ctx, List<KtvSummary> ktvList,
                                     OnExportComplete callback);
    // Xuất PDF báo cáo tổng hợp
    public static void exportReportPdf(Context ctx, ReportData data,
                                        OnExportComplete callback);

    interface OnExportComplete {
        void onSuccess(Uri fileUri, String fileName);
        void onError(Exception e);
    }
}
```

### CSV Format (Tickets)

```
ticketId,tieuDeLoi,sanPham,trangThai,uuTien,ktvTen,taoLuc,thoiGianXuLy,daDanhGiaKtv,daEscalate
```

### PDF Layout

```
[Header] BÁO CÁO HIỆU SUẤT CSKH
[Period] Từ: dd/MM/yyyy — Đến: dd/MM/yyyy
[Summary] Tổng ticket | Đúng SLA | Điểm TB KTV
[Table]   KTV | Ticket | Điểm | % Đúng SLA
[Chart]   Phân bổ cảm xúc (text-based bar)
```

### UI Changes

- **`AdminThongKeFragment`**: Thêm nút "Xuất báo cáo" → BottomSheet chọn PDF/CSV
- **`AdminKtvReviewsFragment`**: Thêm nút "Xuất CSV"
- **`ProgressDialog`**: Hiển thị khi đang tạo file

---

## Feature 3: Audit Log

### Data Model

**Collection `AuditLog` (Firestore):**
```
uid: String
hoTen: String
vaiTro: String
hanhDong: String  // TAO_KTV, XOA_KTV, KHOA_TAI_KHOAN, MO_KHOA_TAI_KHOAN,
                  // CAP_NHAT_GOI_DANG_KY, CAP_NHAT_LEAD, DONG_TICKET
doiTuong: String  // NguoiDung, GoiDangKy, LeadKinhDoanh, YeuCauHoTro
doiTuongId: String
giaTriCu: Map<String, Object>
giaTriMoi: Map<String, Object>
thoiDiem: Timestamp
```

### AuditLogger Utility

```java
// utils/AuditLogger.java
public class AuditLogger {
    public static void log(String hanhDong, String doiTuong, String doiTuongId,
                           Map<String, Object> giaTriCu, Map<String, Object> giaTriMoi);
}
```

Gọi `AuditLogger.log()` tại các điểm:
- `AdminUsersFragment`: tạo/xóa/khóa/mở khóa KTV
- `AdminGoiDangKyFragment`: cập nhật trạng thái gói
- `AdminLeadFragment`: cập nhật trạng thái lead
- `KtvTicketDetailActivity`: đóng ticket

### New Model

```java
// model/AuditLog.java
public class AuditLog {
    String id, uid, hoTen, vaiTro, hanhDong, doiTuong, doiTuongId;
    Map<String, Object> giaTriCu, giaTriMoi;
    Timestamp thoiDiem;
}
```

### UI: AdminAuditLogFragment

```
Layout: fragment_admin_audit_log.xml
  - ChipGroup: 7 ngày / 30 ngày
  - Spinner: lọc theo hanhDong
  - RecyclerView: danh sách AuditLog (20 mục/trang)
  - Button "Tải thêm" (pagination)

Item layout: item_audit_log.xml
  - Icon hành động (theo hanhDong)
  - TextView: hoTen + vaiTro
  - TextView: mô tả hành động
  - TextView: thời gian dd/MM/yyyy HH:mm
```

---

## Feature 4: Chatbot AI Sơ Bộ

### Flow — Client-side (không dùng Cloud Function)

```
KhachHang tạo ticket (YeuCauHoTroActivity)
    → Lưu YeuCauHoTro vào Firestore thành công
    → [MỚI] Gọi ChatbotHelper.sendBotReply(ticketId, tieuDeLoi, sanPham)
    → Query LoiPhatSinh where sanPham == ticket.sanPham (từ Firestore)
    → Filter: tieuDe hoặc moTa contains keyword từ tieuDeLoi (case-insensitive)
    → IF có kết quả:
        Ghi TinNhan Bot: "Tôi tìm thấy [N] bài viết..."
    → ELSE:
        Ghi TinNhan Bot: "Yêu cầu đã ghi nhận, KTV sẽ hỗ trợ sớm."
    → Giới hạn: tối đa 3 tin nhắn bot/ticket
```

Chạy trong `YeuCauHoTroActivity` sau callback `onSuccess` của Firestore add, trên background thread (`Executors.newSingleThreadExecutor()`).

### TinNhan Model Extension

```java
// Thêm vào TinNhan.java (đã có loaiTin, anhUrl)
String vaiTroNguoiGui;  // "KhachHang", "KTV", "Bot"
String nguoiGuiTen;     // "Bot CSKH"
```

### ChatAdapter Changes

- Thêm `VIEW_TYPE_BOT = 3`
- Item layout `item_chat_bot.xml`: bubble màu xanh lá nhạt + badge "Bot"
- Khi `vaiTroNguoiGui == "Bot"` → dùng bot viewtype

### ChatbotHelper Utility (thay Cloud Function)

```java
// utils/ChatbotHelper.java
public class ChatbotHelper {
    public static void sendBotReply(String ticketId, String tieuDeLoi, String sanPham) {
        // Chạy trên background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("LoiPhatSinh")
              .whereEqualTo("sanPham", sanPham)
              .get()
              .addOnSuccessListener(snapshot -> {
                  String[] keywords = tieuDeLoi.toLowerCase().split("\\s+");
                  List<String> matched = new ArrayList<>();
                  for (DocumentSnapshot doc : snapshot.getDocuments()) {
                      String text = (doc.getString("tieuDe") + " "
                                   + doc.getString("moTa")).toLowerCase();
                      for (String kw : keywords) {
                          if (text.contains(kw)) { matched.add(doc.getString("tieuDe")); break; }
                      }
                      if (matched.size() >= 3) break;
                  }
                  String botMsg = matched.isEmpty()
                      ? "Yêu cầu đã ghi nhận. KTV sẽ hỗ trợ bạn sớm nhất."
                      : "Tôi tìm thấy " + matched.size() + " bài viết có thể giúp bạn:\n"
                        + String.join("\n", matched)
                        + "\nNếu chưa giải quyết được, KTV sẽ hỗ trợ bạn sớm.";
                  // Ghi TinNhan vào Firestore
                  Map<String, Object> tin = new HashMap<>();
                  tin.put("noiDung", botMsg);
                  tin.put("vaiTroNguoiGui", "Bot");
                  tin.put("nguoiGuiTen", "Bot CSKH");
                  tin.put("thoiGian", FieldValue.serverTimestamp());
                  db.collection("TinNhan").document(ticketId)
                    .collection("messages").add(tin);
              });
        });
    }
}
```

---

## Feature 5: NPS Survey Định Kỳ

### Data Model

**Collection `NpsSurvey` (Firestore):**
```
uid: String
maSoThue: String
tenCongTy: String
diemNps: Integer  // 0-10
lyDo: String
thangKhaoSat: String  // "2026-05"
taoLuc: Timestamp
```

### Cloud Function: `scheduledNpsSurvey` → **WorkManager + App Launch Check (Android)**

Thay vì Cloud Function, kiểm tra khi KhachHang mở app:

```java
// workers/NpsSurveyWorker.java
public class NpsSurveyWorker extends Worker {
    @Override
    public Result doWork() {
        SharedPreferences prefs = getApplicationContext()
            .getSharedPreferences("nps_prefs", Context.MODE_PRIVATE);
        String lastShown = prefs.getString("nps_last_shown", "");
        String currentMonth = new SimpleDateFormat("yyyy-MM").format(new Date());
        long skipUntil = prefs.getLong("nps_skip_until", 0);

        if (!lastShown.equals(currentMonth) && System.currentTimeMillis() > skipUntil) {
            // Gửi broadcast để HomeActivity hiển thị dialog NPS
            Intent intent = new Intent("ACTION_SHOW_NPS_DIALOG");
            getApplicationContext().sendBroadcast(intent);
        }
        return Result.success();
    }
}
```

Đăng ký trong `HomeActivity.onResume()` — chạy một lần khi mở app, không cần schedule định kỳ.

### NPS Dialog (HomeActivity)

```
dialog_nps_survey.xml
  - TextView: "Bạn có sẵn sàng giới thiệu dịch vụ cho đối tác không?"
  - LinearLayout: 11 nút tròn (0-10), màu gradient đỏ→xanh
  - EditText: lý do (optional)
  - Button: Gửi / Để sau
```

### NPS Score Calculation

```java
// AdminThongKeFragment
int promoters  = count(diemNps >= 9);
int detractors = count(diemNps <= 6);
int total      = count(all);
double nps     = (promoters - detractors) * 100.0 / total;
// Range: -100 to +100
```

### SharedPreferences Keys

```java
"nps_skip_until" → long (timestamp, bỏ qua trong 7 ngày)
"nps_last_shown" → String (thangKhaoSat đã hiển thị)
```

---

## New Files Summary

### Android (Java)

| File | Mô tả |
|---|---|
| `model/AuditLog.java` | Model cho AuditLog collection |
| `model/NpsSurvey.java` | Model cho NpsSurvey collection |
| `utils/AuditLogger.java` | Helper ghi AuditLog vào Firestore |
| `utils/ReportExporter.java` | Helper xuất PDF/CSV |
| `utils/SlaHelper.java` | Helper tính ngưỡng SLA |
| `ui/admin/AdminAuditLogFragment.java` | Fragment hiển thị Audit Log |
| `ui/admin/AuditLogAdapter.java` | Adapter cho danh sách Audit Log |

### Layouts (XML)

| File | Mô tả |
|---|---|
| `fragment_admin_audit_log.xml` | Layout fragment Audit Log |
| `item_audit_log.xml` | Layout item trong danh sách |
| `dialog_nps_survey.xml` | Dialog khảo sát NPS |
| `item_chat_bot.xml` | Layout bubble tin nhắn Bot |

### Cloud Functions (Node.js)

Không dùng Cloud Functions (Spark plan). Xem phần WorkManager ở trên.

---

## Correctness Properties

### P1: Escalation Idempotency
- Ticket đã `daEscalate = true` → chạy lại `scheduledEscalation` không thay đổi `uuTien`
- Test: `escalate(ticket)` × 2 → `uuTien` không đổi lần 2

### P2: SLA Threshold Invariant
- `getSlaThreshold("Cao") == 15`, `"TrungBinh" == 30`, `"Thap" == 60`
- Test: với mọi giá trị `uuTien` hợp lệ, threshold luôn đúng

### P3: NPS Score Range
- `diemNps` luôn trong [0, 10]
- Test: generate random diemNps, assert reject khi ngoài range

### P4: NPS Calculation Consistency
- `NPS = (promoters - detractors) / total * 100`
- Test: generate random list diemNps, assert tính toán nhất quán

### P5: AuditLog Timestamp Ordering
- `thoiDiem` trong AuditLog >= `capNhatLuc` của document bị thay đổi
- Test: log action → assert thoiDiem >= capNhatLuc

### P6: Report Data Consistency
- Tổng ticket trong CSV = tổng ticket hiển thị trên màn hình với cùng filter
- Test: generate list tickets, assert count(export) == count(display)
