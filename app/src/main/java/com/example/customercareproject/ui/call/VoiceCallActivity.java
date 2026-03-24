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
import com.stringee.StringeeClient;
import com.stringee.call.StringeeCall;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;

import org.json.JSONObject;

/**
 * Màn hình gọi đi (outgoing call).
 * Luôn dùng StringeeManager client (đã kết nối với userId của người dùng hiện tại).
 * Nếu chưa kết nối thì kết nối lại với token mới trước khi gọi.
 */
public class VoiceCallActivity extends AppCompatActivity {

    public static final String EXTRA_CALLEE_ID   = "calleeId";
    public static final String EXTRA_CALLEE_NAME = "calleeName";
    // EXTRA_ACCESS_TOKEN giữ lại để tương thích nhưng không dùng nữa
    public static final String EXTRA_ACCESS_TOKEN = "accessToken";
    public static final String EXTRA_CALLER_UID  = "callerUid"; // uid của người gọi

    private static final int REQUEST_RECORD_AUDIO = 101;

    private StringeeClient stringeeClient;
    private StringeeCall   stringeeCall;
    private AudioManager   audioManager;

    private TextView    tvCalleeName, tvCallStatus;
    private ImageButton btnHangup, btnMute, btnSpeaker;

    private boolean isMuted   = false;
    private boolean isSpeaker = false;
    private boolean callMade  = false; // tránh gọi 2 lần

    private String calleeId, calleeName, callerUid;

    private final Handler  handler = new Handler(Looper.getMainLooper());
    private int            callSeconds = 0;
    private Runnable       timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);

        calleeId   = getIntent().getStringExtra(EXTRA_CALLEE_ID);
        calleeName = getIntent().getStringExtra(EXTRA_CALLEE_NAME);
        callerUid  = getIntent().getStringExtra(EXTRA_CALLER_UID);

        tvCalleeName = findViewById(R.id.tvCalleeName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        btnHangup    = findViewById(R.id.btnHangup);
        btnMute      = findViewById(R.id.btnMute);
        btnSpeaker   = findViewById(R.id.btnSpeaker);

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
            initAndCall();
        }
    }

    private void initAndCall() {
        String uid = callerUid;
        if (uid == null || uid.isEmpty()) {
            com.google.firebase.auth.FirebaseUser u =
                    com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (u != null) uid = u.getUid();
        }
        if (uid == null) {
            Toast.makeText(this, "Không xác định được người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvCallStatus.setText("Đang kết nối Stringee...");
        final String finalUid = uid;

        // Luôn dùng StringeeManager, đảm bảo client sẵn sàng trước khi gọi
        StringeeManager.getInstance().ensureConnected(finalUid, () -> {
            stringeeClient = StringeeManager.getInstance().getClient();
            if (stringeeClient == null) {
                Toast.makeText(this, "Không thể kết nối Stringee", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            makeCall();
        });
    }

    private void makeCall() {
        if (callMade) return;
        callMade = true;

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);

        // from = userId của client đang kết nối, to = calleeId (userId của người nhận)
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
                            tvCallStatus.setText("Đang gọi...");
                            break;
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
                        // Media 2 chiều đã kết nối thành công
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
            public void onSuccess() {
                runOnUiThread(() -> tvCallStatus.setText("Đang đổ chuông..."));
            }

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
            stringeeCall = null;
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
                int m = callSeconds / 60, s = callSeconds % 60;
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
            initAndCall();
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
        // StringeeManager client dùng chung, không disconnect ở đây
    }
}
