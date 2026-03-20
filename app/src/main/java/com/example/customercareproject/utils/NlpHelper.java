package com.example.customercareproject.utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * NlpHelper dùng Gemini API để phân tích feedback khách hàng:
 * - Gắn tag chủ đề (#UI, #Lỗi, ...)
 * - Phân tích cảm xúc (HaiLong / TrungBinh / KhongHaiLong)
 * - Xác định mức ưu tiên (Cao / TrungBinh / Thap)
 * - Tóm tắt insight ngắn gọn
 *
 * Fallback về keyword matching nếu API lỗi.
 */
public class NlpHelper {

    public static final String TAG_UI          = "#UI";
    public static final String TAG_FEATURE     = "#TinhNang";
    public static final String TAG_PERFORMANCE = "#HieuSuat";
    public static final String TAG_BUG         = "#Loi";
    public static final String TAG_UX          = "#TraiNghiem";
    public static final String TAG_DATA        = "#DuLieu";
    public static final String TAG_LOGIN       = "#DangNhap";
    public static final String TAG_REPORT      = "#BaoCao";
    public static final String TAG_SYNC        = "#DongBo";

    public static final String PRIORITY_CAO        = "Cao";
    public static final String PRIORITY_TRUNG_BINH = "TrungBinh";
    public static final String PRIORITY_THAP       = "Thap";

    private static final String GEMINI_API_KEY = "AIzaSyBvPzRyR5yuB9FonQL_7cAb6DCjZAlZNgY";
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    public interface GeminiCallback {
        void onResult(List<String> tags, String camXuc, String uuTien, String tomTat);
        void onError(String error);
    }

    /**
     * Phân tích feedback bằng Gemini.
     * Kết quả trả về qua callback trên background thread — post lên UI thread nếu cần.
     */
    public static void phanTichGemini(String noiDung, int soSao, String sanPham, GeminiCallback callback) {
        String prompt = xayDungPrompt(noiDung, soSao, sanPham);

        JSONObject part = new JSONObject();
        JSONObject content = new JSONObject();
        JSONArray parts = new JSONArray();
        JSONArray contents = new JSONArray();
        JSONObject body = new JSONObject();

        try {
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            body.put("contents", contents);
        } catch (Exception e) {
            callback.onError(e.getMessage());
            return;
        }

        Request request = new Request.Builder()
                .url(GEMINI_URL)
                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .build();

        HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@androidx.annotation.NonNull Call call, @androidx.annotation.NonNull IOException e) {
                // Fallback về keyword matching
                callback.onResult(
                        phanTichTag(noiDung),
                        phanTichCamXuc(soSao),
                        phanTichUuTien(noiDung),
                        ""
                );
            }

            @Override
            public void onResponse(@androidx.annotation.NonNull Call call, @androidx.annotation.NonNull Response response) throws IOException {
                try {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    String text = json
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    // Parse JSON trả về từ Gemini
                    // Tìm block JSON trong response
                    int start = text.indexOf('{');
                    int end = text.lastIndexOf('}');
                    if (start >= 0 && end > start) {
                        JSONObject result = new JSONObject(text.substring(start, end + 1));

                        List<String> tags = new ArrayList<>();
                        JSONArray tagsArr = result.optJSONArray("tags");
                        if (tagsArr != null) {
                            for (int i = 0; i < tagsArr.length(); i++) {
                                tags.add(tagsArr.getString(i));
                            }
                        }
                        if (tags.isEmpty()) tags = phanTichTag(noiDung);

                        String camXuc = result.optString("camXuc", phanTichCamXuc(soSao));
                        String uuTien = result.optString("uuTien", phanTichUuTien(noiDung));
                        String tomTat = result.optString("tomTat", "");

                        callback.onResult(tags, camXuc, uuTien, tomTat);
                    } else {
                        // Fallback
                        callback.onResult(phanTichTag(noiDung), phanTichCamXuc(soSao), phanTichUuTien(noiDung), "");
                    }
                } catch (Exception e) {
                    callback.onResult(phanTichTag(noiDung), phanTichCamXuc(soSao), phanTichUuTien(noiDung), "");
                }
            }
        });
    }

    private static String xayDungPrompt(String noiDung, int soSao, String sanPham) {
        return "Bạn là chuyên gia phân tích feedback phần mềm. Phân tích đánh giá sau và trả về JSON.\n\n"
                + "Sản phẩm: " + sanPham + "\n"
                + "Số sao: " + soSao + "/5\n"
                + "Nội dung: " + (noiDung.isEmpty() ? "(không có nội dung)" : noiDung) + "\n\n"
                + "Trả về JSON với format sau (chỉ JSON, không giải thích thêm):\n"
                + "{\n"
                + "  \"tags\": [\"#UI\", \"#Loi\"],\n"
                + "  \"camXuc\": \"HaiLong\" | \"TrungBinh\" | \"KhongHaiLong\",\n"
                + "  \"uuTien\": \"Cao\" | \"TrungBinh\" | \"Thap\",\n"
                + "  \"tomTat\": \"Tóm tắt ngắn gọn 1 câu bằng tiếng Việt\"\n"
                + "}\n\n"
                + "Tags hợp lệ: #UI, #TinhNang, #HieuSuat, #Loi, #TraiNghiem, #DuLieu, #DangNhap, #BaoCao, #DongBo\n"
                + "camXuc: HaiLong nếu soSao >= 4, TrungBinh nếu soSao = 3, KhongHaiLong nếu soSao <= 2\n"
                + "uuTien: Cao nếu có lỗi nghiêm trọng/mất dữ liệu, Thap nếu chỉ góp ý nhỏ, còn lại TrungBinh";
    }

    // ---- Fallback keyword matching (giữ lại để dùng khi offline) ----

    private static final String[] UI_KW = {
        "giao dien", "mau sac", "font", "chu", "nut", "button", "icon", "man hinh",
        "layout", "thiet ke", "hien thi", "kho nhin", "xau", "dep", "bo cuc", "theme"
    };
    private static final String[] FEATURE_KW = {
        "chuc nang", "tinh nang", "khong hoat dong", "khong lam duoc", "thieu",
        "can them", "muon co", "nen co", "cai thien", "nang cap", "them", "bo sung"
    };
    private static final String[] PERFORMANCE_KW = {
        "cham", "lag", "do", "treo", "toc do", "lau", "mai khong", "mat nhieu thoi gian",
        "nhanh hon", "toi uu", "hieu suat", "loading", "tai trang"
    };
    private static final String[] BUG_KW = {
        "loi", "bug", "crash", "sap", "khong mo duoc", "bao loi", "error",
        "exception", "khong luu duoc", "mat du lieu", "sai", "khong dung"
    };
    private static final String[] DATA_KW = {
        "du lieu", "database", "luu tru", "mat du lieu", "sai so lieu",
        "khong dong bo", "dong bo", "xuat file", "import", "export"
    };
    private static final String[] LOGIN_KW = {
        "dang nhap", "dang ky", "mat khau", "tai khoan", "xac thuc", "otp",
        "khong vao duoc", "bi khoa", "quen mat khau"
    };
    private static final String[] REPORT_KW = {
        "bao cao", "in", "xuat", "pdf", "excel", "thong ke", "bieu do", "chart"
    };
    private static final String[] HIGH_PRIORITY_KW = {
        "khan cap", "gap", "ngay", "khong dung duoc", "mat du lieu", "sap hoan toan",
        "khong the", "nghiem trong", "quan trong", "anh huong lon", "toan bo"
    };
    private static final String[] LOW_PRIORITY_KW = {
        "nho", "khong quan trong", "tuy", "neu co the", "gop y them", "y kien"
    };

    public static List<String> phanTichTag(String noiDung) {
        List<String> tags = new ArrayList<>();
        String lower = normalize(noiDung);
        if (containsAny(lower, UI_KW))          tags.add(TAG_UI);
        if (containsAny(lower, FEATURE_KW))     tags.add(TAG_FEATURE);
        if (containsAny(lower, PERFORMANCE_KW)) tags.add(TAG_PERFORMANCE);
        if (containsAny(lower, BUG_KW))         tags.add(TAG_BUG);
        if (containsAny(lower, DATA_KW))        tags.add(TAG_DATA);
        if (containsAny(lower, LOGIN_KW))       tags.add(TAG_LOGIN);
        if (containsAny(lower, REPORT_KW))      tags.add(TAG_REPORT);
        if (tags.isEmpty()) tags.add(TAG_UX);
        return tags;
    }

    public static String phanTichUuTien(String noiDung) {
        String lower = normalize(noiDung);
        if (containsAny(lower, HIGH_PRIORITY_KW)) return PRIORITY_CAO;
        if (containsAny(lower, LOW_PRIORITY_KW))  return PRIORITY_THAP;
        return PRIORITY_TRUNG_BINH;
    }

    public static String phanTichCamXuc(int soSao) {
        if (soSao >= 4) return "HaiLong";
        if (soSao == 3) return "TrungBinh";
        return "KhongHaiLong";
    }

    private static String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "a")
                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "e")
                .replaceAll("[ÌÍỊỈĨ]", "i")
                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "o")
                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "u")
                .replaceAll("[ỲÝỴỶỸ]", "y")
                .replaceAll("[Đ]", "d");
    }

    private static boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}
