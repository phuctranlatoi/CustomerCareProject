package com.example.customercareproject.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Gói đăng ký sản phẩm của 1 công ty (maSoThue là key).
 *
 * Mỗi sản phẩm có thể ở 1 trong 2 trạng thái:
 *   - sanPhamChinhThuc: đã ký hợp đồng chính thức
 *   - sanPhamDungThu:   đang dùng thử, chưa ký hợp đồng
 *
 * sanPhamDangKy = union(sanPhamChinhThuc, sanPhamDungThu) — dùng để kiểm tra quyền truy cập.
 * loaiGoi (cũ) được giữ lại để backward compat nhưng không dùng nữa.
 */
public class GoiDangKy {

    public static final String TRANG_THAI_HOAT_DONG = "HoatDong";
    public static final String TRANG_THAI_HET_HAN   = "HetHan";
    public static final String TRANG_THAI_TAM_DUNG  = "TamDung";

    // Giữ lại để backward compat với code cũ
    public static final String LOAI_GOI_CHINH_THUC = "ChinhThuc";
    public static final String LOAI_GOI_DUNG_THU   = "DungThu";

    private String maSoThue;
    private String tenCongTy;
    private String trangThai;              // HoatDong | HetHan | TamDung
    private List<String> sanPhamChinhThuc; // SP đã ký hợp đồng chính thức
    private List<String> sanPhamDungThu;   // SP đang dùng thử
    private List<String> sanPhamDangKy;    // Union của 2 list trên (backward compat + query)
    private String loaiGoi;                // Deprecated — giữ để không crash Firestore deserialization
    private Timestamp ngayDangKy;
    private Timestamp ngayHetHan;

    public GoiDangKy() {}

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String v) { this.maSoThue = v; }

    public String getTenCongTy() { return tenCongTy; }
    public void setTenCongTy(String v) { this.tenCongTy = v; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String v) { this.trangThai = v; }

    public List<String> getSanPhamChinhThuc() {
        return sanPhamChinhThuc != null ? sanPhamChinhThuc : new ArrayList<>();
    }
    public void setSanPhamChinhThuc(List<String> v) { this.sanPhamChinhThuc = v; }

    public List<String> getSanPhamDungThu() {
        return sanPhamDungThu != null ? sanPhamDungThu : new ArrayList<>();
    }
    public void setSanPhamDungThu(List<String> v) { this.sanPhamDungThu = v; }

    /**
     * Trả về union của chính thức + dùng thử.
     * Nếu Firestore document cũ chỉ có sanPhamDangKy thì dùng field đó.
     */
    public List<String> getSanPhamDangKy() {
        List<String> result = new ArrayList<>();
        if (sanPhamChinhThuc != null) result.addAll(sanPhamChinhThuc);
        if (sanPhamDungThu != null) {
            for (String sp : sanPhamDungThu) {
                if (!result.contains(sp)) result.add(sp);
            }
        }
        // Fallback: document cũ chỉ có sanPhamDangKy
        if (result.isEmpty() && sanPhamDangKy != null) return sanPhamDangKy;
        return result;
    }
    public void setSanPhamDangKy(List<String> v) { this.sanPhamDangKy = v; }

    /** Kiểm tra 1 sản phẩm có phải dùng thử không */
    public boolean isDungThu(String sanPham) {
        return getSanPhamDungThu().contains(sanPham);
    }

    /** Kiểm tra 1 sản phẩm có phải chính thức không */
    public boolean isChinhThuc(String sanPham) {
        return getSanPhamChinhThuc().contains(sanPham);
    }

    /** Trả về loại của 1 sản phẩm cụ thể */
    public String getLoaiCuaSanPham(String sanPham) {
        if (getSanPhamDungThu().contains(sanPham)) return LOAI_GOI_DUNG_THU;
        return LOAI_GOI_CHINH_THUC;
    }

    // Deprecated — giữ để không crash
    public String getLoaiGoi() { return loaiGoi; }
    public void setLoaiGoi(String v) { this.loaiGoi = v; }

    public Timestamp getNgayDangKy() { return ngayDangKy; }
    public void setNgayDangKy(Timestamp v) { this.ngayDangKy = v; }

    public Timestamp getNgayHetHan() { return ngayHetHan; }
    public void setNgayHetHan(Timestamp v) { this.ngayHetHan = v; }
}
