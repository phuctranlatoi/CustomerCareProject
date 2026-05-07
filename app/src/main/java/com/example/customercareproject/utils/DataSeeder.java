package com.example.customercareproject.utils;

import com.example.customercareproject.model.GoiDangKy;
import com.example.customercareproject.model.SanPham;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seed dữ liệu mẫu vào Firestore
 * Chạy 1 lần khi app khởi động lần đầu
 */
public class DataSeeder {

    /**
     * Seed dữ liệu GoiDangKy với tên sản phẩm khớp SanPham.DANH_SACH
     * Chỉ seed nếu collection rỗng
     */
    public static void seedGoiDangKy(FirebaseFirestore db, Runnable onComplete) {
        db.collection("GoiDangKy").limit(1).get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        if (onComplete != null) onComplete.run();
                        return;
                    }

                    // Dùng đúng tên từ SanPham.DANH_SACH để filter hoạt động chính xác
                    List<Map<String, Object>> danhSachCongTy = Arrays.asList(
                            taoGoiDangKy(
                                    "0123456787",
                                    "Công ty TNHH Công Nghệ ABC",
                                    Arrays.asList(SanPham.ECUS5, SanPham.EINVOICE, SanPham.ETAX),
                                    Arrays.asList(SanPham.CLOUDOFFICE) // đang dùng thử CLOUDOFFICE
                            ),
                            taoGoiDangKy(
                                    "0234567897",
                                    "Công ty Cổ phần Giải Pháp XYZ",
                                    Arrays.asList(SanPham.EINVOICE, SanPham.EBH),
                                    new java.util.ArrayList<>()
                            ),
                            taoGoiDangKy(
                                    "0345678903",
                                    "Tập đoàn Thương Mại DEF",
                                    Arrays.asList(SanPham.TRUEPOS, SanPham.EINVOICE),
                                    Arrays.asList(SanPham.ETAX) // dùng thử ETAX
                            ),
                            taoGoiDangKy(
                                    "0456789018",
                                    "Công ty TNHH Sản Xuất GHI",
                                    Arrays.asList(SanPham.ECUS5, SanPham.ETAX, SanPham.EBH),
                                    new java.util.ArrayList<>()
                            ),
                            taoGoiDangKy(
                                    "0567890121",
                                    "Công ty Cổ phần Dịch Vụ JKL",
                                    new java.util.ArrayList<>(),
                                    Arrays.asList(SanPham.CLOUDOFFICE) // chỉ dùng thử
                            ),
                            taoGoiDangKy(
                                    "0678901231",
                                    "Công ty TNHH Phần Mềm MNO",
                                    Arrays.asList(SanPham.ECUS5, SanPham.EINVOICE, SanPham.ETAX, SanPham.EBH),
                                    Arrays.asList(SanPham.CLOUDOFFICE, SanPham.TRUEPOS) // dùng thử 2 SP
                            ),
                            taoGoiDangKy(
                                    "0789012341",
                                    "Tập đoàn Bán Lẻ PQR",
                                    Arrays.asList(SanPham.TRUEPOS),
                                    Arrays.asList(SanPham.EINVOICE) // dùng thử E-INVOICE
                            ),
                            taoGoiDangKy(
                                    "0890123458",
                                    "Công ty Cổ phần Logistics STU",
                                    Arrays.asList(SanPham.ECUS5, SanPham.ETAX),
                                    new java.util.ArrayList<>()
                            )
                    );

                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (Map<String, Object> congTy : danhSachCongTy) {
                        String mst = (String) congTy.get("maSoThue");
                        // Dùng MST làm document ID để dễ query
                        batch.set(db.collection("GoiDangKy").document(mst != null ? mst : db.collection("GoiDangKy").document().getId()), congTy);
                    }

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                android.util.Log.d("DataSeeder", "✓ Đã seed " + danhSachCongTy.size() + " công ty");
                                if (onComplete != null) onComplete.run();
                            })
                            .addOnFailureListener(e -> {
                                android.util.Log.e("DataSeeder", "✗ Lỗi seed data: " + e.getMessage());
                                if (onComplete != null) onComplete.run();
                            });
                });
    }

    private static Map<String, Object> taoGoiDangKy(String maSoThue, String tenCongTy,
                                                      List<String> sanPhamChinhThuc,
                                                      List<String> sanPhamDungThu) {
        // Tính union để lưu vào sanPhamDangKy (backward compat)
        List<String> union = new ArrayList<>(sanPhamChinhThuc);
        for (String sp : sanPhamDungThu) {
            if (!union.contains(sp)) union.add(sp);
        }

        Map<String, Object> goi = new HashMap<>();
        goi.put("maSoThue", maSoThue);
        goi.put("tenCongTy", tenCongTy);
        goi.put("sanPhamChinhThuc", sanPhamChinhThuc);
        goi.put("sanPhamDungThu", sanPhamDungThu);
        goi.put("sanPhamDangKy", union);
        goi.put("trangThai", GoiDangKy.TRANG_THAI_HOAT_DONG);
        goi.put("ngayDangKy", Timestamp.now());
        goi.put("ngayHetHan", null);
        return goi;
    }

    /**
     * Đảm bảo tài khoản admin (theo email) có document NguoiDung với vaiTro=Admin.
     * Dùng khi admin đăng nhập lần đầu hoặc document bị mất.
     */
    public static void ensureAdminDocument(FirebaseFirestore db,
                                            String uid, String email, String hoTen) {
        if (uid == null || uid.isEmpty()) return;
        db.collection("NguoiDung").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("uid", uid);
                        data.put("hoTen", hoTen != null ? hoTen : "Admin");
                        data.put("email", email != null ? email : "");
                        data.put("vaiTro", "Admin");
                        data.put("trangThai", "HoatDong");
                        data.put("taoLuc", com.google.firebase.Timestamp.now());
                        db.collection("NguoiDung").document(uid).set(data)
                                .addOnSuccessListener(v ->
                                        android.util.Log.d("DataSeeder", "✓ Đã tạo document Admin cho: " + email))
                                .addOnFailureListener(e ->
                                        android.util.Log.e("DataSeeder", "✗ Lỗi tạo document Admin: " + e.getMessage()));
                    } else {
                        // Document tồn tại nhưng vaiTro có thể sai — đảm bảo là Admin
                        String vaiTro = doc.getString("vaiTro");
                        if (!"Admin".equals(vaiTro)) {
                            db.collection("NguoiDung").document(uid)
                                    .update("vaiTro", "Admin")
                                    .addOnSuccessListener(v ->
                                            android.util.Log.d("DataSeeder", "✓ Đã cập nhật vaiTro=Admin cho: " + email));
                        }
                    }
                });
    }

    public static void initDatabase(FirebaseFirestore db) {
        seedGoiDangKy(db, null);
    }

    /**
     * Gọi khi user đăng nhập — đảm bảo document NguoiDung tồn tại.
     * Nếu chưa có document → tạo mới với vaiTro=KhachHang (user thường).
     * Nếu đã có → không thay đổi gì.
     * Admin phải được set thủ công qua Firebase Console hoặc qua AdminUsersFragment.
     */
    public static void ensureUserDocument(FirebaseFirestore db, String uid, String email,
                                           String hoTen, Runnable onComplete) {
        if (uid == null || uid.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        db.collection("NguoiDung").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        java.util.Map<String, Object> data = new java.util.HashMap<>();
                        data.put("uid", uid);
                        data.put("hoTen", hoTen != null ? hoTen : "");
                        data.put("email", email != null ? email : "");
                        data.put("vaiTro", "KhachHang");
                        data.put("trangThai", "HoatDong");
                        data.put("taoLuc", com.google.firebase.Timestamp.now());
                        db.collection("NguoiDung").document(uid).set(data)
                                .addOnCompleteListener(t -> { if (onComplete != null) onComplete.run(); });
                    } else {
                        if (onComplete != null) onComplete.run();
                    }
                })
                .addOnFailureListener(e -> { if (onComplete != null) onComplete.run(); });
    }

    public static void reseedGoiDangKy(FirebaseFirestore db, Runnable onComplete) {
        db.collection("GoiDangKy").get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        seedGoiDangKy(db, onComplete);
                        return;
                    }
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snap) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnCompleteListener(task -> seedGoiDangKy(db, onComplete));
                });
    }
}
