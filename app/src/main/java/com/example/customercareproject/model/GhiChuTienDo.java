package com.example.customercareproject.model;

import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class GhiChuTienDo {
    private String ktvUid;
    private String ktvTen;
    private String noiDung;      // tối đa 1000 ký tự
    private Timestamp thoiDiem;

    public GhiChuTienDo() {}

    public GhiChuTienDo(String ktvUid, String ktvTen, String noiDung, Timestamp thoiDiem) {
        this.ktvUid = ktvUid;
        this.ktvTen = ktvTen;
        this.noiDung = noiDung;
        this.thoiDiem = thoiDiem;
    }

    public String getKtvUid() { return ktvUid; }
    public void setKtvUid(String ktvUid) { this.ktvUid = ktvUid; }
    public String getKtvTen() { return ktvTen; }
    public void setKtvTen(String ktvTen) { this.ktvTen = ktvTen; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public Timestamp getThoiDiem() { return thoiDiem; }
    public void setThoiDiem(Timestamp thoiDiem) { this.thoiDiem = thoiDiem; }

    /** Chuyển sang Map để dùng với FieldValue.arrayUnion() */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("ktvUid", ktvUid);
        map.put("ktvTen", ktvTen);
        map.put("noiDung", noiDung);
        map.put("thoiDiem", thoiDiem != null ? thoiDiem : Timestamp.now());
        return map;
    }

    /** Tạo GhiChuTienDo từ Map (khi đọc từ Firestore) */
    public static GhiChuTienDo fromMap(Map<String, Object> map) {
        if (map == null) return null;
        GhiChuTienDo g = new GhiChuTienDo();
        g.ktvUid = (String) map.get("ktvUid");
        g.ktvTen = (String) map.get("ktvTen");
        g.noiDung = (String) map.get("noiDung");
        Object t = map.get("thoiDiem");
        if (t instanceof Timestamp) g.thoiDiem = (Timestamp) t;
        return g;
    }
}
