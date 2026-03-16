package com.example.customercareproject.model;

public class TinNhan {
    private String id;
    private String ticketId;
    private String nguoiGuiUid;
    private String nguoiGuiTen;
    private String vaiTroNguoiGui;  // KhachHang | KTV
    private String noiDung;
    private long thoiGian;          // System.currentTimeMillis()

    public TinNhan() {}

    public TinNhan(String ticketId, String nguoiGuiUid, String nguoiGuiTen,
                   String vaiTroNguoiGui, String noiDung) {
        this.ticketId = ticketId;
        this.nguoiGuiUid = nguoiGuiUid;
        this.nguoiGuiTen = nguoiGuiTen;
        this.vaiTroNguoiGui = vaiTroNguoiGui;
        this.noiDung = noiDung;
        this.thoiGian = System.currentTimeMillis();
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
    public long getThoiGian() { return thoiGian; }
    public void setThoiGian(long thoiGian) { this.thoiGian = thoiGian; }
}
