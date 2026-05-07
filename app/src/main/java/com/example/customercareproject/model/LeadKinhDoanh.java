package com.example.customercareproject.model;

import com.google.firebase.Timestamp;

public class LeadKinhDoanh {
    public static final String TRANG_THAI_MOI       = "Moi";
    public static final String TRANG_THAI_DANG_TU_VAN = "DangTuVan";
    public static final String TRANG_THAI_DA_DANG_KY  = "DaDangKy";
    public static final String TRANG_THAI_TU_CHOI     = "TuChoi";

    private String id;
    private String maSoThue;
    private String tenCongTy;
    private String sanPham;
    private int soSao;
    private String noiDung;
    private String trangThaiLead;  // Moi | DangTuVan | DaDangKy | TuChoi
    private Timestamp taoLuc;
    private Timestamp capNhatLuc;

    public LeadKinhDoanh() {}

    public LeadKinhDoanh(String maSoThue, String tenCongTy, String sanPham,
                         int soSao, String noiDung) {
        this.maSoThue = maSoThue;
        this.tenCongTy = tenCongTy;
        this.sanPham = sanPham;
        this.soSao = soSao;
        this.noiDung = noiDung;
        this.trangThaiLead = TRANG_THAI_MOI;
        this.taoLuc = Timestamp.now();
        this.capNhatLuc = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMaSoThue() { return maSoThue; }
    public void setMaSoThue(String maSoThue) { this.maSoThue = maSoThue; }
    public String getTenCongTy() { return tenCongTy; }
    public void setTenCongTy(String tenCongTy) { this.tenCongTy = tenCongTy; }
    public String getSanPham() { return sanPham; }
    public void setSanPham(String sanPham) { this.sanPham = sanPham; }
    public int getSoSao() { return soSao; }
    public void setSoSao(int soSao) { this.soSao = soSao; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public String getTrangThaiLead() { return trangThaiLead; }
    public void setTrangThaiLead(String trangThaiLead) { this.trangThaiLead = trangThaiLead; }
    public Timestamp getTaoLuc() { return taoLuc; }
    public void setTaoLuc(Timestamp taoLuc) { this.taoLuc = taoLuc; }
    public Timestamp getCapNhatLuc() { return capNhatLuc; }
    public void setCapNhatLuc(Timestamp capNhatLuc) { this.capNhatLuc = capNhatLuc; }
}
