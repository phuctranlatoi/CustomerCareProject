package com.example.customercareproject.model;

public class SanPham {
    public static final String ECUS5 = "ECUS5 VNACCS";
    public static final String EINVOICE = "E-INVOICE";
    public static final String ETAX = "ETAX";
    public static final String EBH = "EBH (Bảo Hiểm)";
    public static final String CLOUDOFFICE = "CLOUDOFFICE";
    public static final String TRUEPOS = "TRUEPOS";

    public static final String[] DANH_SACH = {ECUS5, EINVOICE, ETAX, EBH, CLOUDOFFICE, TRUEPOS};

    public static int getIcon(String tenSanPham) {
        // Trả về index để dùng màu/icon tương ứng
        switch (tenSanPham) {
            case ECUS5: return 0;
            case EINVOICE: return 1;
            case ETAX: return 2;
            case EBH: return 3;
            case CLOUDOFFICE: return 4;
            case TRUEPOS: return 5;
            default: return 0;
        }
    }
}
