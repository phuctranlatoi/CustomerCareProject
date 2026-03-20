package com.example.customercareproject.model;

import com.google.firebase.Timestamp;

public class YeuCauHoTro {
    private String id;
    private String uid;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String sanPham;
    private String loiId;           // ID lỗi liên quan (nếu có)
    private String tieuDeLoi;
    private String moTaVanDe;       // Mô tả thêm của khách hàng
    private String trangThai;       // "ChoXuLy" | "DangXuLy" | "DaXuLy" | "HangCho"
    private String ktvUid;          // UID của KTV được phân công
    private String ktvTen;          // Tên KTV
    private String phanHoiKyThuat;  // Phản hồi từ kỹ thuật viên
    private String uuTien;          // "Cao" | "TrungBinh" | "Thap"
    private Timestamp taoLuc;
    private Timestamp capNhatLuc;
    private Timestamp thoiGianChoXuLy; // Thoi diem het 30s chuyen hang cho

    public YeuCauHoTro() {}

    public YeuCauHoTro(String uid, String hoTen, String email, String soDienThoai,
                       String sanPham, String loiId, String tieuDeLoi, String moTaVanDe) {
        this.uid = uid;
        this.hoTen = hoTen;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.sanPham = sanPham;
        this.loiId = loiId;
        this.tieuDeLoi = tieuDeLoi;
        this.moTaVanDe = moTaVanDe;
        this.trangThai = "ChoXuLy";
        this.uuTien = "TrungBinh";
        this.taoLuc = Timestamp.now();
        this.capNhatLuc = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getSanPham() { return sanPham; }
    public void setSanPham(String sanPham) { this.sanPham = sanPham; }
    public String getLoiId() { return loiId; }
    public void setLoiId(String loiId) { this.loiId = loiId; }
    public String getTieuDeLoi() { return tieuDeLoi; }
    public void setTieuDeLoi(String tieuDeLoi) { this.tieuDeLoi = tieuDeLoi; }
    public String getMoTaVanDe() { return moTaVanDe; }
    public void setMoTaVanDe(String moTaVanDe) { this.moTaVanDe = moTaVanDe; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getKtvUid() { return ktvUid; }
    public void setKtvUid(String ktvUid) { this.ktvUid = ktvUid; }
    public String getKtvTen() { return ktvTen; }
    public void setKtvTen(String ktvTen) { this.ktvTen = ktvTen; }
    public String getPhanHoiKyThuat() { return phanHoiKyThuat; }
    public void setPhanHoiKyThuat(String phanHoiKyThuat) { this.phanHoiKyThuat = phanHoiKyThuat; }
    public String getUuTien() { return uuTien; }
    public void setUuTien(String uuTien) { this.uuTien = uuTien; }
    public Timestamp getTaoLuc() { return taoLuc; }
    public void setTaoLuc(Timestamp taoLuc) { this.taoLuc = taoLuc; }
    public Timestamp getCapNhatLuc() { return capNhatLuc; }
    public void setCapNhatLuc(Timestamp capNhatLuc) { this.capNhatLuc = capNhatLuc; }
    public Timestamp getThoiGianChoXuLy() { return thoiGianChoXuLy; }
    public void setThoiGianChoXuLy(Timestamp thoiGianChoXuLy) { this.thoiGianChoXuLy = thoiGianChoXuLy; }
}
