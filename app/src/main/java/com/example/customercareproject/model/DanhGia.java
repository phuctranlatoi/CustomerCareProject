package com.example.customercareproject.model;

import com.google.firebase.Timestamp;

public class DanhGia {
    private String id;
    private String uid;
    private String hoTen;
    private String sanPham;       // ECUS5, E-INVOICE, ...
    private String loaiDanhGia;   // "GiaoDien" | "ChucNang"
    private String buocDanhGia;   // Bước/tính năng được đánh giá
    private int soSao;            // 1-5
    private String noiDung;       // Nội dung góp ý
    private java.util.List<String> tags;  // NLP tags: #UI, #TinhNang, ...
    private String uuTien;        // Cao | TrungBinh | Thap
    private String camXuc;        // HaiLong | TrungBinh | KhongHaiLong
    private String loaiGoi;       // ChinhThuc | DungThu | null
    private String maSoThue;      // MST công ty của người đánh giá
    private String tenCongTy;     // Tên công ty
    private Timestamp taoLuc;

    public DanhGia() {}

    public DanhGia(String uid, String hoTen, String sanPham, String loaiDanhGia,
                   String buocDanhGia, int soSao, String noiDung) {
        this.uid = uid;
        this.hoTen = hoTen;
        this.sanPham = sanPham;
        this.loaiDanhGia = loaiDanhGia;
        this.buocDanhGia = buocDanhGia;
        this.soSao = soSao;
        this.noiDung = noiDung;
        this.taoLuc = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public String getSanPham() { return sanPham; }
    public void setSanPham(String sanPham) { this.sanPham = sanPham; }
    public String getLoaiDanhGia() { return loaiDanhGia; }
    public void setLoaiDanhGia(String loaiDanhGia) { this.loaiDanhGia = loaiDanhGia; }
    public String getBuocDanhGia() { return buocDanhGia; }
    public void setBuocDanhGia(String buocDanhGia) { this.buocDanhGia = buocDanhGia; }
    public int getSoSao() { return soSao; }
    public void setSoSao(int soSao) { this.soSao = soSao; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public java.util.List<String> getTags() { return tags; }
    public void setTags(java.util.List<String> tags) { this.tags = tags; }
    public String getUuTien() { return uuTien; }
    public void setUuTien(String uuTien) { this.uuTien = uuTien; }
    public String getCamXuc() { return camXuc; }
    public void setCamXuc(String camXuc) { this.camXuc = camXuc; }
    public String getLoaiGoi() { return loaiGoi; }
    public void setLoaiGoi(String loaiGoi) { this.loaiGoi = loaiGoi; }
    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }
    public String getTenCongTy() { return tenCongTy; }
    public void setTenCongTy(String tenCongTy) { this.tenCongTy = tenCongTy; }
    public Timestamp getTaoLuc() { return taoLuc; }
    public void setTaoLuc(Timestamp taoLuc) { this.taoLuc = taoLuc; }
}
