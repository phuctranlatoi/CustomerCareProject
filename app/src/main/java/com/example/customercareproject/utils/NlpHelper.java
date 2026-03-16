package com.example.customercareproject.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * NLP đơn giản dùng keyword matching để:
 * 1. Gắn tag chủ đề cho feedback
 * 2. Xác định mức độ ưu tiên
 */
public class NlpHelper {

    public static final String TAG_UI = "#UI";
    public static final String TAG_FEATURE = "#TinhNang";
    public static final String TAG_PERFORMANCE = "#HieuSuat";
    public static final String TAG_BUG = "#Loi";
    public static final String TAG_UX = "#TraiNghiem";
    public static final String TAG_DATA = "#DuLieu";

    public static final String PRIORITY_CAO = "Cao";
    public static final String PRIORITY_TRUNG_BINH = "TrungBinh";
    public static final String PRIORITY_THAP = "Thap";

    private static final String[][] UI_KEYWORDS = {
        {"giao diện", "màu sắc", "font", "chữ", "nút", "button", "icon", "màn hình",
         "layout", "thiết kế", "hiển thị", "khó nhìn", "xấu", "đẹp", "bố cục"}
    };

    private static final String[][] FEATURE_KEYWORDS = {
        {"chức năng", "tính năng", "không hoạt động", "không làm được", "thiếu",
         "cần thêm", "muốn có", "nên có", "cải thiện", "nâng cấp", "thêm"}
    };

    private static final String[][] PERFORMANCE_KEYWORDS = {
        {"chậm", "lag", "đơ", "treo", "tốc độ", "lâu", "mãi không", "mất nhiều thời gian",
         "nhanh hơn", "tối ưu", "hiệu suất"}
    };

    private static final String[][] BUG_KEYWORDS = {
        {"lỗi", "bug", "crash", "sập", "không mở được", "báo lỗi", "error",
         "exception", "không lưu được", "mất dữ liệu", "sai"}
    };

    private static final String[][] HIGH_PRIORITY_KEYWORDS = {
        {"khẩn cấp", "gấp", "ngay", "không dùng được", "mất dữ liệu", "sập hoàn toàn",
         "không thể", "nghiêm trọng", "quan trọng", "ảnh hưởng lớn", "toàn bộ"}
    };

    private static final String[][] LOW_PRIORITY_KEYWORDS = {
        {"nhỏ", "không quan trọng", "tùy", "nếu có thể", "góp ý thêm", "ý kiến"}
    };

    public static List<String> phanTichTag(String noiDung) {
        List<String> tags = new ArrayList<>();
        String lower = noiDung.toLowerCase();

        if (containsAny(lower, UI_KEYWORDS[0])) tags.add(TAG_UI);
        if (containsAny(lower, FEATURE_KEYWORDS[0])) tags.add(TAG_FEATURE);
        if (containsAny(lower, PERFORMANCE_KEYWORDS[0])) tags.add(TAG_PERFORMANCE);
        if (containsAny(lower, BUG_KEYWORDS[0])) tags.add(TAG_BUG);

        if (tags.isEmpty()) tags.add(TAG_UX);
        return tags;
    }

    public static String phanTichUuTien(String noiDung) {
        String lower = noiDung.toLowerCase();
        if (containsAny(lower, HIGH_PRIORITY_KEYWORDS[0])) return PRIORITY_CAO;
        if (containsAny(lower, LOW_PRIORITY_KEYWORDS[0])) return PRIORITY_THAP;
        return PRIORITY_TRUNG_BINH;
    }

    public static String phanTichCamXuc(int soSao) {
        if (soSao >= 4) return "HaiLong";
        if (soSao == 3) return "TrungBinh";
        return "KhongHaiLong";
    }

    private static boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
