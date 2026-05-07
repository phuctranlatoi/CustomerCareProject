package com.example.customercareproject.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

public class TinNhan {
    private String id;
    private String ticketId;
    private String nguoiGuiUid;
    private String nguoiGuiTen;
    private String vaiTroNguoiGui;  // "KhachHang" | "KTV"
    private String noiDung;

    // 2 mã định danh cuộc trò chuyện
    private String ktvUid;
    private String khachHangUid;

    @ServerTimestamp
    private Timestamp thoiGian;

    private String loaiTin = "van_ban";  // "van_ban" | "anh"
    private String anhUrl;

    public TinNhan() {}

    public TinNhan(String ticketId, String nguoiGuiUid, String nguoiGuiTen,
                   String vaiTroNguoiGui, String noiDung,
                   String ktvUid, String khachHangUid) {
        this.ticketId = ticketId;
        this.nguoiGuiUid = nguoiGuiUid;
        this.nguoiGuiTen = nguoiGuiTen;
        this.vaiTroNguoiGui = vaiTroNguoiGui;
        this.noiDung = noiDung;
        this.ktvUid = ktvUid;
        this.khachHangUid = khachHangUid;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getNguoiGuiUid() { return nguoiGuiUid; }
    public void setNguoiGuiUid(String nguoiGuiUid) { this.nguoiGuiUid = nguoiGuiUid; }
    public String getNguoiGuiTen() { return nguoiGuiTen; }
    public void setNguoiGuiTen(String nguoiGuiTen) { this.nguoiGuiTen = nguoiGuiTen; }
    public String getVaiTroNguoiGui() { return vaiTroNguoiGui; }
    public void setVaiTroNguoiGui(String vaiTroNguoiGui) { this.vaiTroNguoiGui = vaiTroNguoiGui; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public String getKtvUid() { return ktvUid; }
    public void setKtvUid(String ktvUid) { this.ktvUid = ktvUid; }
    public String getKhachHangUid() { return khachHangUid; }
    public void setKhachHangUid(String khachHangUid) { this.khachHangUid = khachHangUid; }
    public Timestamp getThoiGian() { return thoiGian; }
    public void setThoiGian(Timestamp thoiGian) { this.thoiGian = thoiGian; }
    public String getLoaiTin() { return loaiTin; }
    public void setLoaiTin(String loaiTin) { this.loaiTin = loaiTin; }
    public String getAnhUrl() { return anhUrl; }
    public void setAnhUrl(String anhUrl) { this.anhUrl = anhUrl; }
}
