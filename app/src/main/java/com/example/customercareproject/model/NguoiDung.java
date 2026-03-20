package com.example.customercareproject.model;

import com.google.firebase.Timestamp;
import java.util.List;

public class NguoiDung {
    // vaiTro constants
    public static final String VAI_TRO_KHACH_HANG = "KhachHang";
    public static final String VAI_TRO_KTV = "KTV";
    public static final String VAI_TRO_ADMIN = "Admin";

    // trangThai KTV
    public static final String TRANG_THAI_RAN = "Ran";
    public static final String TRANG_THAI_BAN = "DangBan";
    public static final String TRANG_THAI_OFFLINE = "Offline";

    private String uid;
    private String hoTen;
    private String email;
    private String soDienThoai;
    private String vaiTro;
    private String trangThai;           // HoatDong | Khoa (user) | Ran/DangBan/Offline (KTV)
    private int soTicketDangXuLy;       // KTV: số ticket đang xử lý (load balancing)
    private List<String> chuyenMon;     // KTV: danh sách sản phẩm chuyên môn
    private int tongTicketDaXuLy;       // KTV: tổng ticket đã xử lý (performance)
    private Timestamp taoLuc;

    public NguoiDung() {}

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }
    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public int getSoTicketDangXuLy() { return soTicketDangXuLy; }
    public void setSoTicketDangXuLy(int soTicketDangXuLy) { this.soTicketDangXuLy = soTicketDangXuLy; }
    public List<String> getChuyenMon() { return chuyenMon; }
    public void setChuyenMon(List<String> chuyenMon) { this.chuyenMon = chuyenMon; }
    public int getTongTicketDaXuLy() { return tongTicketDaXuLy; }
    public void setTongTicketDaXuLy(int tongTicketDaXuLy) { this.tongTicketDaXuLy = tongTicketDaXuLy; }
    public Timestamp getTaoLuc() { return taoLuc; }
    public void setTaoLuc(Timestamp taoLuc) { this.taoLuc = taoLuc; }
}
