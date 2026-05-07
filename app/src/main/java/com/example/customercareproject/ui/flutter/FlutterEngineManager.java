package com.example.customercareproject.ui.flutter;

import android.content.Context;
import android.content.Intent;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodChannel;

/**
 * Manager để khởi tạo và cache Flutter Engine.
 * Gọi FlutterEngineManager.init(context) một lần trong Application.onCreate()
 * để Flutter engine được warm-up sẵn, tránh chờ khi mở màn hình Flutter.
 */
public class FlutterEngineManager {

    public static final String ENGINE_ID = "customer_care_engine";
    public static final String CHANNEL = "com.customercareproject/bridge";

    private static MethodChannel methodChannel;

    /**
     * Khởi tạo Flutter engine với entry point mặc định (homeScreen).
     * Nên gọi trong Application.onCreate() để warm-up trước.
     */
    public static void init(Context context) {
        if (FlutterEngineCache.getInstance().get(ENGINE_ID) != null) return;

        FlutterEngine engine = new FlutterEngine(context);
        engine.getDartExecutor().executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        );
        FlutterEngineCache.getInstance().put(ENGINE_ID, engine);

        // Khởi tạo MethodChannel để giao tiếp Java ↔ Flutter
        methodChannel = new MethodChannel(
            engine.getDartExecutor().getBinaryMessenger(),
            CHANNEL
        );

        // Xử lý các method call từ Flutter sang Android
        methodChannel.setMethodCallHandler((call, result) -> {
            switch (call.method) {
                case "navigate":
                    // Flutter yêu cầu navigate trong Android
                    String route = call.argument("route");
                    handleNavigate(context, route, call.arguments, result);
                    break;
                case "close":
                    result.success(null);
                    break;
                case "getAuthToken":
                    // Trả về Firebase token cho Flutter (nếu cần)
                    result.success(null);
                    break;
                default:
                    result.notImplemented();
            }
        });
    }

    /**
     * Mở màn hình Flutter Chat.
     */
    public static Intent chatIntent(Context context, String chatId, String customerName) {
        return FlutterActivity
            .withCachedEngine(ENGINE_ID)
            .build(context)
            .putExtra("route", "/chat")
            .putExtra("chatId", chatId)
            .putExtra("customerName", customerName);
    }

    /**
     * Mở màn hình Flutter Home Dashboard.
     */
    public static Intent homeIntent(Context context) {
        return FlutterActivity
            .withCachedEngine(ENGINE_ID)
            .build(context)
            .putExtra("route", "/home");
    }

    /**
     * Mở màn hình Flutter Ticket List.
     */
    public static Intent ticketListIntent(Context context) {
        return FlutterActivity
            .withCachedEngine(ENGINE_ID)
            .build(context)
            .putExtra("route", "/tickets");
    }

    /**
     * Mở màn hình Flutter Profile.
     */
    public static Intent profileIntent(Context context) {
        return FlutterActivity
            .withCachedEngine(ENGINE_ID)
            .build(context)
            .putExtra("route", "/profile");
    }

    /**
     * Gửi tin nhắn mới từ Android sang màn hình Chat Flutter.
     */
    public static void sendNewMessage(String content, String senderName) {
        if (methodChannel == null) return;
        java.util.Map<String, String> args = new java.util.HashMap<>();
        args.put("content", content);
        args.put("senderName", senderName);
        methodChannel.invokeMethod("newMessage", args);
    }

    /**
     * Xử lý navigation request từ Flutter
     */
    private static void handleNavigate(Context context, String route, Object args, MethodChannel.Result result) {
        // TODO: Implement navigation logic based on route
        // Ví dụ: mở màn hình Android tương ứng
        result.success(null);
    }

    /**
     * Lấy MethodChannel để giao tiếp trực tiếp
     */
    public static MethodChannel getMethodChannel() {
        return methodChannel;
    }
}
