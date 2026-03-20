package com.example.customercareproject.ui.call;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.utils.StringeeManager;
import com.example.customercareproject.utils.StringeeTokenHelper;
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.call.StringeeCall2;
import com.stringee.common.SocketAddress;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;
import com.stringee.listener.StringeeConnectionListener;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class VoiceCallActivity extends AppCompatActivity {

    public static final String EXTRA_CALLEE_ID = "calleeId";
    public static final String EXTRA_CALLEE_NAME = "calleeName";
    public static final String EXTRA_ACCESS_TOKEN = "accessToken";
    private static final int REQUEST_RECORD_AUDIO = 101;

    private StringeeClient stringeeClient;
    private StringeeCall stringeeCall;
    private AudioManager audioManager;

    private TextView tvCalleeName, tvCallStatus;
    private ImageButton btnHangup, btnMute, btnSpeaker;

    private boolean isMuted = false;
    private boolean isSpeaker = false;

    private String calleeId, calleeName, accessToken;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int callSeconds = 0;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        calleeId = getIntent().getStringExtra(EXTRA_CALLEE_ID);
        calleeName = getIntent().getStringExtra(EXTRA_CALLEE_NAME);
        accessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);

        tvCalleeName = findViewById(R.id.tvCalleeName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        btnHangup = findViewById(R.id.btnHangup);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        tvCalleeName.setText(calleeName != null ? calleeName : calleeId);
        tvCallStatus.setText("Đang kết nối...");

        btnHangup.setOnClickListener(v -> hangup());
        btnMute.setOnClickListener(v -> toggleMute());
        btnSpeaker.setOnClickListener(v -> toggleSpeaker());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        } else {
            initStringee();
        }
    }

    private void initStringee() {
        // Dùng client từ StringeeManager (đã kết nối sẵn)
        stringeeClient = StringeeManager.getInstance().getClient();
        if (stringeeClient != null && stringeeClient.isConnected()) {
            makeCall();
        } else {
            // Fallback: tạo client mới nếu chưa có
            stringeeClient = new StringeeClient(this);
            stringeeClient.setConnectionListener(new StringeeConnectionListener() {
                @Override
                public void onConnectionConnected(StringeeClient client, boolean isReconnecting) {
                    runOnUiThread(() -> makeCall());
                }

                @Override
                public void onConnectionDisconnected(StringeeClient client, boolean isReconnecting) {
                    runOnUiThread(() -> tvCallStatus.setText("Mất kết nối"));
                }

                @Override
                public void onIncomingCall(StringeeCall call) {}

                @Override
                public void onIncomingCall2(StringeeCall2 call2) {}

                @Override
                public void onConnectionError(StringeeClient client, StringeeError error) {
                    runOnUiThread(() -> {
                        Toast.makeText(VoiceCallActivity.this,
                                "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onRequestNewToken(StringeeClient client) {
                    runOnUiThread(() -> {
                        Toast.makeText(VoiceCallActivity.this, "Token hết hạn", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onCustomMessage(String from, JSONObject msg) {}

                @Override
                public void onTopicMessage(String from, JSONObject msg) {}
            });

            List<SocketAddress> socketList = new ArrayList<>();
            socketList.add(new SocketAddress("v1.stringee.com", 9879));
            socketList.add(new SocketAddress("v2.stringee.com", 9879));
            stringeeClient.setHost(socketList);
            stringeeClient.connect(accessToken);
        }
    }

    private void makeCall() {
        // Chế độ âm thanh khi gọi
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);

        stringeeCall = new StringeeCall(stringeeClient,
                stringeeClient.getUserId(), calleeId);
        stringeeCall.setVideoCall(false);

        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall call, StringeeCall.SignalingState state,
                                               String reason, int sipCode, String sipReason) {
                runOnUiThread(() -> {
                    switch (state) {
                        case CALLING:
                        case RINGING:
                            tvCallStatus.setText("Đang đổ chuông...");
                            break;
                        case ANSWERED:
                            tvCallStatus.setText("Đang kết nối media...");
                            break;
                        case BUSY:
                            tvCallStatus.setText("Máy bận");
                            handler.postDelayed(() -> finish(), 2000);
                            break;
                        case ENDED:
                            stopTimer();
                            tvCallStatus.setText("Cuộc gọi kết thúc");
                            handler.postDelayed(() -> finish(), 1500);
                            break;
                    }
                });
            }

            @Override
            public void onError(StringeeCall call, int code, String description) {
                runOnUiThread(() -> {
                    Toast.makeText(VoiceCallActivity.this,
                            "Lỗi: " + description, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall call,
                                                 StringeeCall.SignalingState state,
                                                 String description) {}

            @Override
            public void onMediaStateChange(StringeeCall call, StringeeCall.MediaState state) {
                runOnUiThread(() -> {
                    if (state == StringeeCall.MediaState.CONNECTED) {
                        startTimer();
                    } else if (state == StringeeCall.MediaState.DISCONNECTED) {
                        tvCallStatus.setText("Media ngắt kết nối");
                    }
                });
            }

            @Override
            public void onLocalStream(StringeeCall call) {}

            @Override
            public void onRemoteStream(StringeeCall call) {}

            @Override
            public void onCallInfo(StringeeCall call, JSONObject info) {}
        });

        stringeeCall.makeCall(new StatusListener() {
            @Override
            public void onSuccess() {}

            @Override
            public void onError(StringeeError error) {
                runOnUiThread(() -> {
                    Toast.makeText(VoiceCallActivity.this,
                            "Không thể gọi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void hangup() {
        stopTimer();
        if (stringeeCall != null) {
            stringeeCall.hangup(new StatusListener() {
                @Override public void onSuccess() {}
                @Override public void onError(StringeeError error) {}
            });
        }
        restoreAudio();
        finish();
    }

    private void toggleMute() {
        if (stringeeCall == null) return;
        isMuted = !isMuted;
        stringeeCall.mute(isMuted);
        btnMute.setImageResource(isMuted
                ? android.R.drawable.ic_lock_silent_mode
                : android.R.drawable.ic_lock_silent_mode_off);
        Toast.makeText(this, isMuted ? "Đã tắt mic" : "Đã bật mic", Toast.LENGTH_SHORT).show();
    }

    private void toggleSpeaker() {
        isSpeaker = !isSpeaker;
        audioManager.setSpeakerphoneOn(isSpeaker);
        btnSpeaker.setAlpha(isSpeaker ? 1.0f : 0.5f);
        Toast.makeText(this, isSpeaker ? "Loa ngoài" : "Loa trong", Toast.LENGTH_SHORT).show();
    }

    private void restoreAudio() {
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }

    private void startTimer() {
        callSeconds = 0;
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                callSeconds++;
                int m = callSeconds / 60;
                int s = callSeconds % 60;
                tvCallStatus.setText(String.format("%02d:%02d", m, s));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(timerRunnable);
    }

    private void stopTimer() {
        if (timerRunnable != null) handler.removeCallbacks(timerRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initStringee();
        } else {
            Toast.makeText(this, "Cần quyền microphone để gọi điện", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        if (stringeeCall != null) {
            stringeeCall.hangup(new StatusListener() {
                @Override public void onSuccess() {}
                @Override public void onError(StringeeError error) {}
            });
        }
        restoreAudio();
        // Không disconnect client dùng chung từ StringeeManager
    }
}
