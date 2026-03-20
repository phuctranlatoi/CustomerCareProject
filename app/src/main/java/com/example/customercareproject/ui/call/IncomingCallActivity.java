package com.example.customercareproject.ui.call;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
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

    public static final String EXTRA_CALL_ID = "callId";
    public static final String EXTRA_CALLER_NAME = "callerName";

    private static final int REQUEST_RECORD_AUDIO = 102;

    private StringeeCall stringeeCall;
    private AudioManager audioManager;
    private Ringtone ringtone;

    private TextView tvCallerName, tvCallStatus;
    private ImageButton btnAnswer, btnReject;

    private String callId, callerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        callId = getIntent().getStringExtra(EXTRA_CALL_ID);
        callerName = getIntent().getStringExtra(EXTRA_CALLER_NAME);

        tvCallerName = findViewById(R.id.tvCallerName);
        tvCallStatus = findViewById(R.id.tvCallStatus);
        btnAnswer = findViewById(R.id.btnAnswer);
        btnReject = findViewById(R.id.btnReject);

        tvCallerName.setText(callerName != null ? callerName : "Cuộc gọi đến");
        tvCallStatus.setText("Cuộc gọi đến...");

        // Lấy call từ StringeeManager
        stringeeCall = StringeeManager.getInstance().getIncomingCall(callId);
        if (stringeeCall == null) {
            finish();
            return;
        }

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Đổ chuông
        batDauChuong();

        // Đăng ký listener để theo dõi trạng thái
        stringeeCall.setCallListener(new StringeeCall.StringeeCallListener() {
            @Override
            public void onSignalingStateChange(StringeeCall call, StringeeCall.SignalingState state,
                                               String reason, int sipCode, String sipReason) {
                runOnUiThread(() -> {
                    if (state == StringeeCall.SignalingState.ENDED
                            || state == StringeeCall.SignalingState.BUSY) {
                        dungChuong();
                        finish();
                    }
                });
            }

            @Override
            public void onError(StringeeCall call, int code, String description) {
                runOnUiThread(() -> {
                    dungChuong();
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
            public void onMediaStateChange(StringeeCall call, StringeeCall.MediaState state) {}

            @Override
            public void onLocalStream(StringeeCall call) {}

            @Override
            public void onRemoteStream(StringeeCall call) {}

            @Override
            public void onCallInfo(StringeeCall call, JSONObject info) {}
        });

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

    private void ngheMaxy() {
        dungChuong();
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(false);

        stringeeCall.ringing(new StatusListener() {
            @Override public void onSuccess() {}
            @Override public void onError(StringeeError error) {}
        });

        stringeeCall.answer(new StatusListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> tvCallStatus.setText("Đang nghe..."));
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
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        }
    }
}
