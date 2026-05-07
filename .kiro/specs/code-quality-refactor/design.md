# Design: Code Quality & Feature Enhancement

## Kiến trúc hiện tại

```
┌─────────────────────────────────────────────────────────┐
│                    Android App (Client)                  │
│                                                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────────────────┐  │
│  │  Auth    │  │  UI      │  │  Utils               │  │
│  │ Login    │  │ Customer │  │ SmartRouter          │  │
│  │ Register │  │ KTV      │  │ NlpHelper (Groq AI)  │  │
│  │ Forgot   │  │ Admin    │  │ StringeeManager      │  │
│  └──────────┘  └──────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────┘
         │                              │
         ▼                              ▼
┌─────────────────┐          ┌──────────────────────┐
│  Firebase       │          │  External Services   │
│  Auth           │          │  Stringee (VoIP)     │
│  Firestore      │          │  Groq API (AI/NLP)   │
│  Realtime DB    │          └──────────────────────┘
│  Storage        │
│  Cloud Functions│
│  FCM            │
└─────────────────┘
```

## Cấu trúc Firestore Collections

```
NguoiDung/{uid}
  - uid, hoTen, email, soDienThoai
  - vaiTro: KhachHang | KTV | Admin
  - trangThai: HoatDong | Khoa | Ran | DangBan | Offline
  - maSoThue, tenCongTy
  - soTicketDangXuLy, tongTicketDaXuLy
  - chuyenMon: List<String>

YeuCauHoTro/{ticketId}
  - uid, hoTen, email, soDienThoai
  - sanPham, loiId, tieuDeLoi, moTaVanDe
  - trangThai: HangCho | ChoXuLy | DangXuLy | DaXuLy
  - ktvUid, ktvTen, phanHoiKyThuat
  - uuTien: Cao | TrungBinh | Thap
  - daDanhGiaKtv: boolean
  - lichSuHoTro: List<Map>
  - taoLuc, capNhatLuc, thoiGianChoXuLy

TinNhan/{ticketId}/messages/{msgId}
  - ticketId, nguoiGuiUid, nguoiGuiTen, vaiTroNguoiGui
  - noiDung, loaiTin: van_ban | anh
  - anhUrl, thoiGian

DanhGia/{id}
  - uid, hoTen, sanPham, loaiDanhGia
  - soSao, noiDung, tags, camXuc, uuTien
  - loaiGoi, maSoThue, tenCongTy

GoiDangKy/{maSoThue}
  - maSoThue, tenCongTy, trangThai
  - sanPhamChinhThuc, sanPhamDungThu, sanPhamDangKy
  - ngayDangKy, ngayHetHan

LoiPhatSinh/{id}
  - sanPham, tieuDe, moTa, cachGiaiQuyet, coHuongDan

TemplateTrLoi/{id}
  - sanPham, tieuDe, noiDung

LeadKinhDoanh/{id}
  - maSoThue, tenCongTy, sanPham, soSao, noiDung
  - trangThaiLead: Moi | DangTuVan | DaDangKy | TuChoi

InsightCumDe/{id}
  - sanPham, chuDe, soLuong, tomTat, taoLuc
```

---

## Thiết kế cải thiện

### 1. Bảo mật — Chuyển API Key ra khỏi source code

**Vấn đề:** `NlpHelper.java` hardcode `GROQ_API_KEY` trong source.

**Giải pháp:** Dùng Firebase Remote Config để lưu key, fetch khi app khởi động.

```java
// Thay vì:
private static final String GROQ_API_KEY = "gsk_...hardcoded...";

// Dùng:
FirebaseRemoteConfig.getInstance().getString("groq_api_key")
```

**Hoặc tốt hơn:** Proxy qua Cloud Function — client gọi Cloud Function, Function gọi Groq. Key chỉ tồn tại trên server.

```
Client → callGroqAnalysis (Cloud Function) → Groq API
```

---

### 2. Firestore Security Rules

Viết rules phân quyền rõ ràng:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // NguoiDung: chỉ đọc được của mình, admin đọc tất cả
    match /NguoiDung/{uid} {
      allow read: if request.auth.uid == uid || isAdmin();
      allow write: if request.auth.uid == uid || isAdmin();
    }

    // YeuCauHoTro: khách đọc ticket của mình, KTV đọc ticket được assign
    match /YeuCauHoTro/{ticketId} {
      allow read: if isOwner(resource.data.uid) || isAssignedKtv(resource.data.ktvUid) || isAdmin();
      allow create: if isAuthenticated() && isKhachHang();
      allow update: if isAssignedKtv(resource.data.ktvUid) || isAdmin();
    }

    // TinNhan: chỉ người trong ticket mới đọc/ghi được
    match /TinNhan/{ticketId}/messages/{msgId} {
      allow read, write: if isInTicket(ticketId);
    }

    function isAdmin() { ... }
    function isKhachHang() { ... }
    function isAssignedKtv(ktvUid) { return request.auth.uid == ktvUid; }
    function isOwner(uid) { return request.auth.uid == uid; }
    function isInTicket(ticketId) { ... }
  }
}
```

---

### 3. Memory Leak Fix — ListenerRegistration Cleanup

**Vấn đề:** Nhiều Activity giữ `ListenerRegistration` nhưng không remove trong `onDestroy`.

**Pattern chuẩn:**

```java
public class ChatKhachHangActivity extends AppCompatActivity {
    private final List<ListenerRegistration> listeners = new ArrayList<>();

    private void addListener(ListenerRegistration reg) {
        listeners.add(reg);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (ListenerRegistration reg : listeners) {
            if (reg != null) reg.remove();
        }
        listeners.clear();
    }
}
```

---

### 4. Pagination cho Admin Queries

**Vấn đề:** Admin load toàn bộ collection.

**Giải pháp:** Dùng Firestore cursor-based pagination.

```java
// Load trang đầu
Query query = db.collection("YeuCauHoTro")
    .orderBy("taoLuc", Query.Direction.DESCENDING)
    .limit(PAGE_SIZE);

// Load trang tiếp theo
query = query.startAfter(lastDocument);
```

---

### 5. Image Compression trước khi upload

```java
// Trước khi upload lên Firebase Storage
Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
ByteArrayOutputStream baos = new ByteArrayOutputStream();
bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // 70% quality
byte[] data = baos.toByteArray();
```

---

### 6. Loading States & Empty States

Mỗi màn hình cần có 3 trạng thái UI:
- **Loading**: ProgressBar / Shimmer effect
- **Empty**: Illustration + message "Chưa có dữ liệu"
- **Error**: Icon lỗi + nút "Thử lại"

```xml
<!-- ViewFlipper với 3 child views -->
<ViewFlipper>
    <ProgressBar /> <!-- index 0: loading -->
    <RecyclerView /> <!-- index 1: content -->
    <LinearLayout> <!-- index 2: empty/error -->
        <ImageView android:src="@drawable/ic_empty" />
        <TextView android:text="Chưa có dữ liệu" />
        <Button android:text="Thử lại" />
    </LinearLayout>
</ViewFlipper>
```

---

### 7. FCM Push Notifications

**Luồng thông báo:**

```
Ticket được assign → Cloud Function → FCM → Khách hàng nhận thông báo
KTV gửi tin nhắn → Cloud Function → FCM → Khách hàng nhận thông báo
Ticket quá hạn → Cloud Function → FCM → Admin nhận cảnh báo
```

**Cloud Function mới:**

```javascript
exports.onTicketAssigned = functions.firestore
  .document("YeuCauHoTro/{ticketId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    // Khi ticket vừa được assign KTV
    if (!before.ktvUid && after.ktvUid) {
      await sendFcmToUser(after.uid, {
        title: "Yêu cầu của bạn đã được tiếp nhận",
        body: `KTV ${after.ktvTen} đang xử lý yêu cầu của bạn`
      });
    }
  });
```

---

### 8. Input Validation

```java
public class ValidationHelper {
    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^(0|\\+84)[0-9]{9}$");
    }

    public static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("[<>\"'&]", "");
    }
}
```

---

### 9. Tính năng mới: Template Auto-suggest

Khi KTV bắt đầu gõ trong chat, hiển thị gợi ý template phù hợp:

```java
editText.addTextChangedListener(new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, ...) {
        if (s.length() >= 2) {
            filterTemplates(s.toString());
        }
    }
});

private void filterTemplates(String query) {
    List<TemplateTrLoi> filtered = templates.stream()
        .filter(t -> t.getTieuDe().toLowerCase().contains(query.toLowerCase())
                  || t.getNoiDung().toLowerCase().contains(query.toLowerCase()))
        .collect(Collectors.toList());
    templateSuggestAdapter.updateData(filtered);
    rvTemplateSuggest.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
}
```

---

### 10. Escalation tự động

Cloud Function mới — chạy mỗi 5 phút, kiểm tra ticket quá hạn:

```javascript
exports.autoEscalate = functions.pubsub
  .schedule("every 5 minutes")
  .onRun(async () => {
    const threshold = new Date(Date.now() - 30 * 60 * 1000); // 30 phút
    const overdueSnap = await db.collection("YeuCauHoTro")
      .where("trangThai", "in", ["HangCho", "ChoXuLy"])
      .where("taoLuc", "<", threshold)
      .get();

    for (const doc of overdueSnap.docs) {
      await doc.ref.update({ uuTien: "Cao", daEscalate: true });
      await sendFcmToAdmins({
        title: "⚠️ Ticket quá hạn",
        body: `Ticket #${doc.id} chưa được xử lý sau 30 phút`
      });
    }
  });
```

---

## Thứ tự ưu tiên triển khai

| Nhóm | Mức độ | Lý do |
|------|--------|-------|
| Bảo mật (API key, Security Rules) | 🔴 Critical | Rủi ro bảo mật ngay lập tức |
| Memory leak cleanup | 🟠 High | Ảnh hưởng trực tiếp đến UX |
| Input validation | 🟠 High | Ngăn crash và data corruption |
| Loading/Empty states | 🟡 Medium | UX cơ bản |
| FCM notifications | 🟡 Medium | Tính năng quan trọng còn thiếu |
| Pagination | 🟡 Medium | Scalability |
| Image compression | 🟢 Low | Tối ưu bandwidth |
| Template auto-suggest | 🟢 Low | Enhancement |
| Escalation tự động | 🟢 Low | Enhancement |
| Export báo cáo | 🟢 Low | Nice-to-have |
