package com.example.customercareproject.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

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

    // Lưu các cuộc gọi đến theo callId
    private final Map<String, StringeeCall> incomingCallsMap = new HashMap<>();

    private StringeeManager() {}

    public static StringeeManager getInstance() {
        if (instance == null) instance = new StringeeManager();
        return instance;
    }

    public void init(Context context, String userId) {
        appContext = context.getApplicationContext();
        if (client != null) return; // đã init rồi

        String token = StringeeTokenHelper.generateToken(userId);
        if (token == null) return;

        client = new StringeeClient(appContext);
        client.setConnectionListener(new StringeeConnectionListener() {
            @Override
            public void onConnectionConnected(StringeeClient c, boolean isReconnecting) {}

            @Override
            public void onConnectionDisconnected(StringeeClient c, boolean isReconnecting) {}

            @Override
            public void onIncomingCall(StringeeCall call) {
                incomingCallsMap.put(call.getCallId(), call);
                // Lookup tên người gọi từ Firestore
                String callerUid = call.getFrom();
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("NguoiDung")
                        .document(callerUid)
                        .get()
                        .addOnSuccessListener(doc -> {
                            String tenNguoiGoi = doc.getString("hoTen");
                            if (tenNguoiGoi == null || tenNguoiGoi.isEmpty()) {
                                tenNguoiGoi = callerUid;
                            }
                            Intent intent = new Intent(appContext, IncomingCallActivity.class);
                            intent.putExtra(IncomingCallActivity.EXTRA_CALL_ID, call.getCallId());
                            intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, tenNguoiGoi);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            // Nếu lỗi thì vẫn mở với UID
                            Intent intent = new Intent(appContext, IncomingCallActivity.class);
                            intent.putExtra(IncomingCallActivity.EXTRA_CALL_ID, call.getCallId());
                            intent.putExtra(IncomingCallActivity.EXTRA_CALLER_NAME, callerUid);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(intent);
                        });
            }

            @Override
            public void onIncomingCall2(StringeeCall2 call2) {}

            @Override
            public void onConnectionError(StringeeClient c, StringeeError error) {}

            @Override
            public void onRequestNewToken(StringeeClient c) {
                // Refresh token khi hết hạn
                String newToken = StringeeTokenHelper.generateToken(c.getUserId());
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

    public StringeeClient getClient() { return client; }

    public StringeeCall getIncomingCall(String callId) {
        return incomingCallsMap.get(callId);
    }

    public void removeCall(String callId) {
        incomingCallsMap.remove(callId);
    }

    public void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }
    }
}
