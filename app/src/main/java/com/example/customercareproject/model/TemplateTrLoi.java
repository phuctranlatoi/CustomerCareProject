package com.example.customercareproject.model;

import com.google.firebase.Timestamp;

public class TemplateTrLoi {

    private String id;
    private String tieuDe;
    private String noiDung;
    private String sanPham;
    private Timestamp taoLuc;

    /** Constructor rỗng — bắt buộc cho Firestore deserialization */
    public TemplateTrLoi() {}

    /** Constructor đầy đủ (không có id và taoLuc — Firestore tự sinh) */
    public TemplateTrLoi(String tieuDe, String noiDung, String sanPham) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.sanPham = sanPham;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getSanPham() { return sanPham; }
    public void setSanPham(String sanPham) { this.sanPham = sanPham; }

    public Timestamp getTaoLuc() { return taoLuc; }
    public void setTaoLuc(Timestamp taoLuc) { this.taoLuc = taoLuc; }
}
