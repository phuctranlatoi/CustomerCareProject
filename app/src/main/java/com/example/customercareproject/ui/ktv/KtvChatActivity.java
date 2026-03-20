package com.example.customercareproject.ui.ktv;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.ui.call.VoiceCallActivity;
import com.example.customercareproject.utils.StringeeTokenHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KtvChatActivity extends AppCompatActivity {

    private String ticketId, tenKhachHang, khachHangUid = "", tenKtv = "KTV";
    private FirebaseUser user;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration chatListener;
    private RecyclerView rvChat;
    private final List<Map<String, Object>> danhSachTin = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_khach_hang);

        ticketId = getIntent().getStringExtra("ticketId");
        tenKhachHang = getIntent().getStringExtra("tenKhachHang");

        if (ticketId == null || ticketId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ticket", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle("Chat với " + (tenKhachHang != null ? tenKhachHang : "Khách hàng"));
        toolbar.setNavigationOnClickListener(v -> finish());

        rvChat = findViewById(R.id.rvChat);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        chatAdapter = new ChatAdapter(new ArrayList<>(), user.getUid());
        rvChat.setAdapter(chatAdapter);

        // Lấy tên KTV và khachHangUid
        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(doc -> { if (doc.getString("hoTen") != null) tenKtv = doc.getString("hoTen"); });
        db.collection("YeuCauHoTro").document(ticketId).get()
                .addOnSuccessListener(doc -> { if (doc.exists() && doc.getString("uid") != null) khachHangUid = doc.getString("uid"); });

        TextInputEditText edtTinNhan = findViewById(R.id.edtTinNhan);
        ImageButton btnGui = findViewById(R.id.btnGui);
        ImageButton btnGoiThoai = findViewById(R.id.btnGoiThoai);

        btnGui.setOnClickListener(v -> guiTin(edtTinNhan));
        edtTinNhan.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { guiTin(edtTinNhan); return true; }
            return false;
        });

        btnGoiThoai.setOnClickListener(v -> batDauGoiThoai());

        langNgheChat();
    }

    private void batDauGoiThoai() {
        if (khachHangUid == null || khachHangUid.isEmpty()) {
            Toast.makeText(this, "Chưa lấy được thông tin khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = StringeeTokenHelper.generateToken(user.getUid());
        if (token == null) {
            Toast.makeText(this, "Không thể tạo token gọi điện", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_ID, khachHangUid);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_NAME, tenKhachHang != null ? tenKhachHang : "Khách hàng");
        intent.putExtra(VoiceCallActivity.EXTRA_ACCESS_TOKEN, token);
        //intent.putExtra(VoiceCallActivity.EXTRA_IS_INCOMING, false);
        startActivity(intent);
    }

    private void guiTin(TextInputEditText edt) {
        String nd = edt.getText() != null ? edt.getText().toString().trim() : "";
        if (nd.isEmpty()) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("nguoiGuiUid", user.getUid());
        msg.put("nguoiGuiTen", tenKtv);
        msg.put("vaiTro", "KTV");
        msg.put("noiDung", nd);
        msg.put("ktvUid", user.getUid());
        msg.put("khachHangUid", khachHangUid);
        msg.put("ticketId", ticketId);
        msg.put("thoiGian", com.google.firebase.firestore.FieldValue.serverTimestamp());

        edt.setText("");
        db.collection("TinNhan").document(ticketId)
                .collection("messages")
                .add(msg)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void langNgheChat() {
        chatListener = db.collection("TinNhan").document(ticketId)
                .collection("messages")
                .orderBy("thoiGian", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    danhSachTin.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                        Map<String, Object> msg = new HashMap<>(doc.getData());
                        msg.put("_id", doc.getId());
                        Object ts = msg.get("thoiGian");
                        if (ts instanceof com.google.firebase.Timestamp) {
                            msg.put("thoiGian", ((com.google.firebase.Timestamp) ts).toDate().getTime());
                        }
                        danhSachTin.add(msg);
                    }
                    chatAdapter.capNhatRaw(danhSachTin);
                    if (!danhSachTin.isEmpty())
                        rvChat.scrollToPosition(chatAdapter.getItemCount() - 1);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) chatListener.remove();
    }
}
