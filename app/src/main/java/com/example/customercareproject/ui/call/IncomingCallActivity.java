package com.example.customercareproject.ui.call;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
import com.stringee.call.StringeeCall;
import com.stringee.exception.StringeeError;
import com.stringee.listener.StatusListener;

import org.json.JSONObject;

public class IncomingCallActivity extends AppCompatActivity {

    public static final String EXTRA_CALL_ID    = "callId";
    public static final String EXTRA_CALLER_NAME = "callerName";

    private static final int REQUEST_RECORD_AUDIO = 102;

    private StringeeCall stringeeCall;
    private AudioManager audioManager;
    private Ringtone ringtone;

    private TextView tvCallerName, tvCallStatus;
    private ImageButton btnAnswer, btnReject;

    private String callId, callerName;
    private boolean answered = false;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private int callSeconds = 0;
    private Runnable timerRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        callId     = getIntent().getStringExtra(EXTRA_CALL_ID);
        callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);

        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        btnAnswer    = findViewById(R.id.btnAnswer);
        btnReject    = findViewById(R.id.btnReject);

        tvCallerName.setText(callerName != null ? callerName : "Cuộc gọi đến");
        tvCallStatus.setText("Cuộc gọi đến...");

        stringeeCall = StringeeManager.getInstance().getIncomingCall(callId);
        if (stringeeCall == null) {
            finish();
            return;
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setupCallListener();

        batDauChuong();

        btnAnswer.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
            } else {
                ngheMaxy();
            }
        });

        btnReject.setOnClickListener(v -> tuChoiCuocGoi());
    }

    private void setupCallListener() {
        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall call, StringeeCall.SignalingState state,
                                               String reason, int sipCode, String sipReason) {
                runOnUiThread(() -> {
                    switch (state) {
                        case ANSWERED:
                            tvCallStatus.setText("Đang kết nối...");
                            break;
                        case ENDED:
                        case BUSY:
                            dungChuong();
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
                    dungChuong();
                    Toast.makeText(IncomingCallActivity.this,
                            "Lỗi: " + description, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onHandledOnAnotherDevice(StringeeCall call,
                                                 StringeeCall.SignalingState state,
                                                 String description) {
                runOnUiThread(() -> {
                    dungChuong();
                    finish();
                });
            }

            @Override
            public void onMediaStateChange(StringeeCall call, StringeeCall.MediaState state) {
                runOnUiThread(() -> {
                    if (state == StringeeCall.MediaState.CONNECTED) {
                        // Media 2 chiều đã kết nối
                        tvCallStatus.setText("Đang nghe...");
                        startTimer();
                        btnAnswer.setEnabled(false);
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
    }

    private void ngheMaxy() {
        if (answered) return;
        answered = true;

        dungChuong();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);

        // Thứ tự bắt buộc: ringing → answer
        stringeeCall.ringing(new StatusListener() {
            @Override public void onSuccess() {
                stringeeCall.answer(new StatusListener() {
                    @Override
                    public void onSuccess() {
                        runOnUiThread(() -> tvCallStatus.setText("Đang kết nối media..."));
                    }

                    @Override
                    public void onError(StringeeError error) {
                        runOnUiThread(() -> {
                            Toast.makeText(IncomingCallActivity.this,
                                    "Lỗi nghe máy: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                });
            }

            @Override
            public void onError(StringeeError error) {
                // ringing thất bại vẫn thử answer
                stringeeCall.answer(new StatusListener() {
                    @Override public void onSuccess() {
                        runOnUiThread(() -> tvCallStatus.setText("Đang kết nối media..."));
                    }
                    @Override public void onError(StringeeError e) {
                        runOnUiThread(() -> finish());
                    }
                });
            }
        });
    }

    private void tuChoiCuocGoi() {
        dungChuong();
        if (stringeeCall != null) {
            stringeeCall.reject(new StatusListener() {
                @Override public void onSuccess() {}
                @Override public void onError(StringeeError error) {}
            });
        }
        StringeeManager.getInstance().removeCall(callId);
        finish();
    }

    private void batDauChuong() {
        try {
            Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            ringtone = RingtoneManager.getRingtone(this, ringtoneUri);
            if (ringtone != null) ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void dungChuong() {
        if (ringtone != null && ringtone.isPlaying()) ringtone.stop();
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
            ngheMaxy();
        } else {
            Toast.makeText(this, "Cần quyền microphone", Toast.LENGTH_SHORT).show();
            tuChoiCuocGoi();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dungChuong();
        stopTimer();
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }
}
