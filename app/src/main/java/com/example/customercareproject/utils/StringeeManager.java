package com.example.customercareproject.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.example.customercareproject.ui.call.IncomingCallActivity;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.SocketAddress;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringeeManager {

    private static StringeeManager instance;
    private StringeeClient client;
    private Context appContext;
    private String currentUserId; 

    private final Map<String, StringeeCall> incomingCallsMap = new HashMap<>();

    private StringeeManager() {}

    public static StringeeManager getInstance() {
        if (instance == null) instance = new StringeeManager();
        return instance;
    }

    public void init(Context context, String userId) {
        appContext = context.getApplicationContext();

        if (client != null && !userId.equals(currentUserId)) {
            reset();
        }

        if (client != null) return;

        currentUserId = userId;
        conectClient(userId);
    }

    private void conectClient(String userId) {
        String token = StringeeTokenHelper.generateToken(userId);
        if (token == null) return;

        client = new StringeeClient(appContext);
        client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient c, boolean isReconnecting) {}

            @Override
            public void onConnectionDisconnected(StringeeClient c, boolean isReconnecting) {
                // Tự reconnect nếu bị ngắt
                if (userId.equals(currentUserId)) {
                    String t = StringeeTokenHelper.generateToken(userId);
                    if (t != null) c.connect(t);
                }
            }

            @Override
            public void onIncomingCall(StringeeCall call) {
                if (!userId.equals(currentUserId)) return;

                incomingCallsMap.put(call.getCallId(), call);

                String callerUid = call.getFrom();
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("NguoiDung")
                        .document(callerUid)
                        .get()
                        .addOnSuccessListener(doc -> {
                            String tenNguoiGoi = doc.getString("hoTen");
                            if (tenNguoiGoi == null || tenNguoiGoi.isEmpty()) tenNguoiGoi = callerUid;
                            moManHinhCuocGoi(call.getCallId(), tenNguoiGoi);
                        })
                        .addOnFailureListener(e -> moManHinhCuocGoi(call.getCallId(), callerUid));
            }

            @Override
            public void onIncomingCall2(StringeeCall2 call2) {}

            @Override
            public void onConnectionError(StringeeClient c, StringeeError error) {}

            @Override
            public void onRequestNewToken(StringeeClient c) {
                // Token hết hạn -> tạo token mới và reconnect
                String newToken = StringeeTokenHelper.generateToken(currentUserId);
                if (newToken != null) c.connect(newToken);
            }

            @Override
            public void onCustomMessage(String from, JSONObject msg) {}

            @Override
            public void onTopicMessage(String from, JSONObject msg) {}
        });

        List<SocketAddress> socketList = new ArrayList<>();
        socketList.add(new SocketAddress("v1.stringee.com", 9879));
        socketList.add(new SocketAddress("v2.stringee.com", 9879));
        client.setHost(socketList);
        client.connect(token);
    }

    /**
     * Đảm bảo client đã kết nối. Nếu chưa, kết nối lại rồi gọi callback.
     */
    public void ensureConnected(String userId, Runnable onReady) {
        if (client != null && client.isConnected()) {
            onReady.run();
            return;
        }
        // Reset và kết nối lại
        if (client != null) {
            try { client.disconnect(); } catch (Exception ignored) {}
            client = null;
        }
        currentUserId = userId;
        String token = StringeeTokenHelper.generateToken(userId);
        if (token == null) return;

        client = new StringeeClient(appContext);
        client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient c, boolean isReconnecting) {
                new Handler(Looper.getMainLooper()).post(onReady);
            }

            @Override
            public void onConnectionDisconnected(StringeeClient c, boolean isReconnecting) {
                if (userId.equals(currentUserId)) {
                    String t = StringeeTokenHelper.generateToken(userId);
                    if (t != null) c.connect(t);
                }
            }

            @Override
            public void onIncomingCall(StringeeCall call) {
                if (!userId.equals(currentUserId)) return;
                incomingCallsMap.put(call.getCallId(), call);
                String callerUid = call.getFrom();
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("NguoiDung").document(callerUid).get()
                        .addOnSuccessListener(doc -> {
                            String ten = doc.getString("hoTen");
                            moManHinhCuocGoi(call.getCallId(), ten != null ? ten : callerUid);
                        })
                        .addOnFailureListener(e -> moManHinhCuocGoi(call.getCallId(), callerUid));
            }

            @Override public void onIncomingCall2(StringeeCall2 call2) {}

            @Override
            public void onConnectionError(StringeeClient c, StringeeError error) {}

            @Override
            public void onRequestNewToken(StringeeClient c) {
                String newToken = StringeeTokenHelper.generateToken(currentUserId);
                if (newToken != null) c.connect(newToken);
            }

            @Override public void onCustomMessage(String from, JSONObject msg) {}
            @Override public void onTopicMessage(String from, JSONObject msg) {}
        });

        List<SocketAddress> socketList = new ArrayList<>();
        socketList.add(new SocketAddress("v1.stringee.com", 9879));
        socketList.add(new SocketAddress("v2.stringee.com", 9879));
        client.setHost(socketList);
        client.connect(token);
    }

    private void moManHinhCuocGoi(String callId, String tenNguoiGoi) {
        Intent intent = new Intent(appContext, IncomingCallActivity.class);
        intent.putExtra(IncomingCallActivity.EXTRA_CALL_ID, callId);
        intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, tenNguoiGoi);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(intent);
    }

    public StringeeClient getClient() { return client; }

    public StringeeCall getIncomingCall(String callId) {
        return incomingCallsMap.get(callId);
    }

    public void removeCall(String callId) {
        incomingCallsMap.remove(callId);
    }

    public void reset() {
        incomingCallsMap.clear();
        if (client != null) {
            try { client.disconnect(); } catch (Exception ignored) {}
            client = null;
        }
        currentUserId = null;
    }

    @Deprecated
    public void disconnect() {
        reset();
    }
}
