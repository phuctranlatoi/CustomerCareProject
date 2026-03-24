package com.example.customercareproject.utils;

import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Tạo Stringee Access Token phía client (chỉ dùng cho dev/demo).
 * Production nên generate token từ server backend.
 */
public class StringeeTokenHelper {

    // API SID và Secret từ Stringee Dashboard
    private static final String API_KEY_SID = "SK.0.30Msz99urXRfbOCEhKUymc3NJZyFOpo";
    private static final String API_KEY_SECRET = "N0dzYThPdkphYUNTSlBJSWM2anI5Y0NFQzJybnhyOHE=";

    /**
     * Tạo JWT token cho Stringee với userId là Firebase UID
     */
    public static String generateToken(String userId) {
        try {
            // Header
            JSONObject header = new JSONObject();
            header.put("typ", "JWT");
            header.put("alg", "HS256");
            header.put("cty", "stringee-api;v=1");

            // Payload
            long now = System.currentTimeMillis() / 1000;
            JSONObject payload = new JSONObject();
            payload.put("jti", API_KEY_SID + "-" + now);
            payload.put("iss", API_KEY_SID);
            payload.put("exp", now + 86400); // hết hạn sau 24 giờ
            payload.put("userId", userId);

            String headerEncoded = base64UrlEncode(header.toString().getBytes(StandardCharsets.UTF_8));
            String payloadEncoded = base64UrlEncode(payload.toString().getBytes(StandardCharsets.UTF_8));
            String signingInput = headerEncoded + "." + payloadEncoded;

            // Ký HMAC-SHA256 - dùng secret string trực tiếp (không decode base64)
            byte[] secretBytes = API_KEY_SECRET.getBytes(StandardCharsets.UTF_8);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] signature = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
            String signatureEncoded = base64UrlEncode(signature);

            return signingInput + "." + signatureEncoded;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.encodeToString(data, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
}
