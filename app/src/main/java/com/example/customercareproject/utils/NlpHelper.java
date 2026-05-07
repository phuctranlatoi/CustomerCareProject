package com.example.customercareproject.utils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NlpHelper {

    public static final String PRIORITY_CAO        = "Cao";
    public static final String PRIORITY_TRUNG_BINH = "TrungBinh";
    public static final String PRIORITY_THAP       = "Thap";

    private static final String GROQ_API_KEY = "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";
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    // -----------------------------------------------------------------------
    // PHÂN TÍCH FEEDBACK đơn lẻ — dùng khi khách gửi đánh giá
    // -----------------------------------------------------------------------

    public interface GeminiCallback {
        void onResult(List<String> tags, String camXuc, String uuTien, String tomTat);
        void onError(String error);
    }

    public static void phanTichGemini(String noiDung, int soSao, String sanPham, GeminiCallback callback) {
        String prompt = "Phân tích đánh giá phần mềm sau và trả về JSON duy nhất.\n"
                + "Sản phẩm: " + sanPham + " | Số sao: " + soSao + "/5\n"
                + "Nội dung: " + (noiDung.isEmpty() ? "(trống)" : noiDung) + "\n"
                + "Trả về JSON: {\"tags\":[\"tên chủ đề ngắn gọn\"],\"camXuc\":\"HaiLong\",\"uuTien\":\"TrungBinh\",\"tomTat\":\"...\"}\n"
                + "tags: tự đặt tên chủ đề phù hợp nội dung (ví dụ: Giao diện, Tốc độ, Đăng nhập, Lỗi thanh toán)\n"
                + "camXuc: HaiLong(>=4sao) TrungBinh(3sao) KhongHaiLong(<=2sao)\n"
                + "uuTien: Cao(lỗi nghiêm trọng) TrungBinh(bình thường) Thap(góp ý nhỏ)";

        goiAI(prompt, text -> {
            try {
                int s = text.indexOf('{'), e = text.lastIndexOf('}');
                if (s >= 0 && e > s) {
                    JSONObject json = new JSONObject(text.substring(s, e + 1));
                    List<String> tags = new ArrayList<>();
                    JSONArray arr = json.optJSONArray("tags");
                    if (arr != null) for (int i = 0; i < arr.length(); i++) tags.add(arr.getString(i));
                    if (tags.isEmpty()) tags = phanTichTagFallback(noiDung);
                    callback.onResult(tags,
                            json.optString("camXuc", phanTichCamXuc(soSao)),
                            json.optString("uuTien", phanTichUuTien(noiDung)),
                            json.optString("tomTat", ""));
                } else {
                    callback.onResult(phanTichTagFallback(noiDung), phanTichCamXuc(soSao), phanTichUuTien(noiDung), "");
                }
            } catch (Exception ex) {
                callback.onResult(phanTichTagFallback(noiDung), phanTichCamXuc(soSao), phanTichUuTien(noiDung), "");
            }
        }, err -> callback.onResult(phanTichTagFallback(noiDung), phanTichCamXuc(soSao), phanTichUuTien(noiDung), ""));
    }

    // -----------------------------------------------------------------------
    // PHÂN TÍCH TỔNG HỢP — admin bấm "Phân tích AI"
    // AI tự gom cụm, tự đặt tên chủ đề, trả danh sách đánh giá gốc theo cụm
    // -----------------------------------------------------------------------

    /**
     * cumDe: mỗi phần tử có:
     *   chuDe      (String)       — tên chủ đề AI tự đặt
     *   soLuong    (int)          — số phản hồi trong cụm
     *   uuTien     (String)       — Cao / TrungBinh / Thap
     *   danhGia    (List<String>) — các câu đánh giá gốc thuộc cụm
     */
    public interface InsightCallback {
        void onResult(String insight, List<Map<String, Object>> cumDe);
        void onError(String error);
    }

    public static void phanTichTongHop(InsightCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        Timestamp tuNgay = new Timestamp(cal.getTime());

        // allItems: text gửi AI  |  rawNoiDung: câu gốc để map lại
        List<String> allItems = new ArrayList<>();
        final int[] done = {0};

        db.collection("DanhGia").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String nd = doc.getString("noiDung");
                        String sp = doc.getString("sanPham");
                        Long ss = doc.getLong("soSao");
                        if (nd != null && !nd.isEmpty())
                            allItems.add("[DanhGia][" + sp + "][" + ss + "sao] " + nd);
                    }
                    done[0]++;
                    if (done[0] == 2) xuLyPhanTich(allItems, db, callback);
                })
                .addOnFailureListener(e -> { done[0]++; if (done[0] == 2) xuLyPhanTich(allItems, db, callback); });

        db.collection("YeuCauHoTro").whereGreaterThan("taoLuc", tuNgay).get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) {
                        String tieuDe = doc.getString("tieuDeLoi");
                        String moTa = doc.getString("moTaVanDe");
                        String sp = doc.getString("sanPham");
                        String text = (tieuDe != null ? tieuDe : "")
                                + (moTa != null && !moTa.isEmpty() ? ": " + moTa : "");
                        if (!text.trim().isEmpty())
                            allItems.add("[HoTro][" + sp + "] " + text.trim());
                    }
                    done[0]++;
                    if (done[0] == 2) xuLyPhanTich(allItems, db, callback);
                })
                .addOnFailureListener(e -> { done[0]++; if (done[0] == 2) xuLyPhanTich(allItems, db, callback); });
    }

    private static void xuLyPhanTich(List<String> allItems, FirebaseFirestore db, InsightCallback callback) {
        if (allItems.isEmpty()) {
            callback.onError("Chưa có dữ liệu đánh giá hoặc yêu cầu hỗ trợ trong 30 ngày qua.");
            return;
        }

        List<String> sample = allItems.size() > 50 ? allItems.subList(0, 50) : allItems;

        String prompt = "Bạn là chuyên gia phân tích phần mềm. Dưới đây là phản hồi từ khách hàng:\n\n"
                + String.join("\n", sample) + "\n\n"
                + "Nhiệm vụ:\n"
                + "1. Nhận ra các vấn đề GIỐNG NHAU dù khách viết khác nhau, gom thành cụm chủ đề.\n"
                + "2. Tự đặt tên chủ đề ngắn gọn, rõ ràng (ví dụ: 'Ứng dụng bị lag', 'Không đăng nhập được', 'Giao diện khó dùng').\n"
                + "3. Với mỗi cụm, liệt kê các câu phản hồi gốc thuộc cụm đó.\n"
                + "4. Viết phân tích tổng thể dành cho lập trình viên: vấn đề nào cần sửa trước, mức độ ưu tiên, gợi ý kỹ thuật cụ thể.\n\n"
                + "Trả về JSON (chỉ JSON, không giải thích thêm):\n"
                + "{\n"
                + "  \"cumDe\": [\n"
                + "    {\n"
                + "      \"chuDe\": \"Tên chủ đề do AI đặt\",\n"
                + "      \"soLuong\": 5,\n"
                + "      \"uuTien\": \"Cao\",\n"
                + "      \"danhGia\": [\"câu phản hồi gốc 1\", \"câu phản hồi gốc 2\"]\n"
                + "    }\n"
                + "  ],\n"
                + "  \"phanTich\": \"Báo cáo cho dev: [Cao] Vấn đề X → Gợi ý: ... | [TrungBinh] Vấn đề Y → Gợi ý: ...\"\n"
                + "}\n"
                + "uuTien: Cao | TrungBinh | Thap. Tối đa 6 cụm. phanTich tối đa 150 từ.";

        goiAI(prompt, text -> {
            try {
                int s = text.indexOf('{'), e = text.lastIndexOf('}');
                if (s < 0 || e <= s) { callback.onError("AI trả về định dạng không hợp lệ."); return; }

                JSONObject json = new JSONObject(text.substring(s, e + 1));
                String phanTich = json.optString("phanTich", "");
                JSONArray cumArr = json.optJSONArray("cumDe");

                List<Map<String, Object>> cumList = new ArrayList<>();

                if (cumArr != null) {
                    for (int i = 0; i < cumArr.length(); i++) {
                        try {
                            JSONObject cum = cumArr.getJSONObject(i);
                            Map<String, Object> data = new HashMap<>();
                            data.put("chuDe", cum.optString("chuDe", "Vấn đề " + (i + 1)));
                            data.put("soLuong", cum.optInt("soLuong", 1));
                            data.put("uuTien", cum.optString("uuTien", "TrungBinh"));

                            // Lấy danh sách câu đánh giá gốc
                            List<String> danhGiaList = new ArrayList<>();
                            JSONArray dgArr = cum.optJSONArray("danhGia");
                            if (dgArr != null) {
                                for (int j = 0; j < dgArr.length(); j++)
                                    danhGiaList.add(dgArr.getString(j));
                            }
                            data.put("danhGia", danhGiaList);
                            cumList.add(data);
                        } catch (Exception ignored) {}
                    }
                }

                // Lưu vào Firestore (xóa cũ trước)
                db.collection("InsightCumDe").get().addOnSuccessListener(oldSnap -> {
                    com.google.firebase.firestore.WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot old : oldSnap) batch.delete(old.getReference());
                    batch.commit().addOnCompleteListener(task -> {
                        com.google.firebase.firestore.WriteBatch newBatch = db.batch();
                        Timestamp now = Timestamp.now();
                        for (Map<String, Object> data : cumList) {
                            Map<String, Object> toSave = new HashMap<>(data);
                            toSave.put("capNhatLuc", now);
                            newBatch.set(db.collection("InsightCumDe").document(), toSave);
                        }
                        newBatch.commit()
                                .addOnSuccessListener(v -> callback.onResult(phanTich, cumList))
                                .addOnFailureListener(ex -> callback.onResult(phanTich, cumList));
                    });
                }).addOnFailureListener(ex -> callback.onResult(phanTich, cumList));

            } catch (Exception ex) {
                callback.onError("Lỗi xử lý: " + ex.getMessage());
            }
        }, callback::onError);
    }

    // -----------------------------------------------------------------------
    // CORE: Gọi Groq AI
    // -----------------------------------------------------------------------

    private static void goiAI(String prompt, java.util.function.Consumer<String> onResult,
                               java.util.function.Consumer<String> onError) {
        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);
            JSONArray messages = new JSONArray();
            messages.put(message);
            JSONObject body = new JSONObject();
            body.put("model", "llama-3.1-8b-instant");
            body.put("messages", messages);
            body.put("max_tokens", 2000);

            Request request = new Request.Builder()
                    .url(GROQ_URL)
                    .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .build();

            HTTP.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(Call call, IOException e) { onError.accept(e.getMessage()); }
                @Override public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String resp = response.body().string();
                        JSONObject json = new JSONObject(resp);
                        if (json.has("error")) { onError.accept(json.getJSONObject("error").optString("message")); return; }
                        String text = json.getJSONArray("choices").getJSONObject(0)
                                .getJSONObject("message").getString("content");
                        onResult.accept(text);
                    } catch (Exception e) { onError.accept("Parse error: " + e.getMessage()); }
                }
            });
        } catch (Exception e) { onError.accept("Build error: " + e.getMessage()); }
    }

    // -----------------------------------------------------------------------
    // FALLBACK: Keyword matching (dùng khi AI lỗi)
    // -----------------------------------------------------------------------

    private static final String[] UI_KW = {"giao dien","mau sac","font","nut","button","icon","man hinh","layout","thiet ke","hien thi","kho nhin","xau","dep"};
    private static final String[] FEATURE_KW = {"chuc nang","tinh nang","khong hoat dong","thieu","can them","muon co","nen co","cai thien","nang cap"};
    private static final String[] PERFORMANCE_KW = {"cham","lag","do","treo","toc do","lau","nhanh hon","toi uu","loading"};
    private static final String[] BUG_KW = {"loi","bug","crash","sap","khong mo duoc","bao loi","error","khong luu duoc","mat du lieu","sai"};
    private static final String[] DATA_KW = {"du lieu","luu tru","mat du lieu","sai so lieu","khong dong bo","dong bo","xuat file"};
    private static final String[] LOGIN_KW = {"dang nhap","dang ky","mat khau","tai khoan","xac thuc","otp","khong vao duoc"};
    private static final String[] HIGH_PRIORITY_KW = {"khan cap","gap","ngay","khong dung duoc","mat du lieu","nghiem trong","quan trong","toan bo"};
    private static final String[] LOW_PRIORITY_KW = {"nho","khong quan trong","tuy","neu co the","gop y them"};

    public static List<String> phanTichTagFallback(String noiDung) {
        List<String> tags = new ArrayList<>();
        String lower = normalize(noiDung);
        if (containsAny(lower, UI_KW))          tags.add("Giao diện");
        if (containsAny(lower, FEATURE_KW))     tags.add("Tính năng");
        if (containsAny(lower, PERFORMANCE_KW)) tags.add("Hiệu suất");
        if (containsAny(lower, BUG_KW))         tags.add("Lỗi");
        if (containsAny(lower, DATA_KW))        tags.add("Dữ liệu");
        if (containsAny(lower, LOGIN_KW))       tags.add("Đăng nhập");
        if (tags.isEmpty()) tags.add("Trải nghiệm");
        return tags;
    }

    // Giữ tên cũ để không break DanhGiaFormFragment nếu đang dùng
    public static List<String> phanTichTag(String noiDung) {
        return phanTichTagFallback(noiDung);
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
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a").replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i").replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u").replaceAll("[ỳýỵỷỹ]", "y").replaceAll("[đ]", "d");
    }

    private static boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }
}
