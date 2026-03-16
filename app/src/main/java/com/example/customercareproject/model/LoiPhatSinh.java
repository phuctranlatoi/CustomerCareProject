package com.example.customercareproject.model;

import com.google.firebase.Timestamp;

public class LoiPhatSinh {
    private String id;
    private String sanPham;
    private String tieuDe;
    private String moTa;
    private String cachGiaiQuyet;   // Hướng dẫn tự xử lý (nếu có)
    private boolean coHuongDan;     // true = có hướng dẫn tự xử lý
    private Timestamp taoLuc;

    public LoiPhatSinh() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSanPham() { return sanPham; }
    public void setSanPham(String sanPham) { this.sanPham = sanPham; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public String getCachGiaiQuyet() { return cachGiaiQuyet; }
    public void setCachGiaiQuyet(String cachGiaiQuyet) { this.cachGiaiQuyet = cachGiaiQuyet; }
    public boolean isCoHuongDan() { return coHuongDan; }
    public void setCoHuongDan(boolean coHuongDan) { this.coHuongDan = coHuongDan; }
    public Timestamp getTaoLuc() { return taoLuc; }
    public void setTaoLuc(Timestamp taoLuc) { this.taoLuc = taoLuc; }
}
