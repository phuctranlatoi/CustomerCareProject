package com.example.customercareproject.ui.loi;

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
import com.example.customercareproject.ui.danhgiaktv.DanhGiaKTVActivity;
import com.example.customercareproject.ui.ktv.ChatAdapter;
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

public class ChatKhachHangActivity extends AppCompatActivity {

    private String ticketId, hoTen, ktvUid = "", ktvTen = "", sanPham = "";
    private boolean daDanhGia = false;
    private ListenerRegistration ticketListener;
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
        hoTen = getIntent().getStringExtra("hoTen");

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
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Chat hỗ trợ");
        toolbar.setNavigationOnClickListener(v -> finish());

        rvChat = findViewById(R.id.rvChat);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        chatAdapter = new ChatAdapter(new ArrayList<>(), user.getUid());
        rvChat.setAdapter(chatAdapter);

        // Lấy ktvUid từ ticket và lắng nghe trạng thái đóng ticket
        db.collection("YeuCauHoTro").document(ticketId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String ktv = doc.getString("ktvUid");
                        if (ktv != null) ktvUid = ktv;
                        String ten = doc.getString("ktvTen");
                        if (ten != null) ktvTen = ten;
                        String sp = doc.getString("sanPham");
                        if (sp != null) sanPham = sp;
                        if (hoTen == null || hoTen.isEmpty()) hoTen = doc.getString("hoTen");
                        Boolean daDG = doc.getBoolean("daDanhGiaKtv");
                        if (daDG != null) daDanhGia = daDG;
                    }
                });

        // Lắng nghe khi ticket chuyển sang DaXuLy → hỏi đánh giá
        ticketListener = db.collection("YeuCauHoTro").document(ticketId)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null || !snap.exists()) return;
                    String trangThai = snap.getString("trangThai");
                    Boolean daDG = snap.getBoolean("daDanhGiaKtv");
                    if ("DaXuLy".equals(trangThai) && (daDG == null || !daDG) && !daDanhGia) {
                        daDanhGia = true; // tránh mở nhiều lần
                        String ktv = snap.getString("ktvUid");
                        String ten = snap.getString("ktvTen");
                        String sp  = snap.getString("sanPham");
                        if (ktv != null) ktvUid = ktv;
                        if (ten != null) ktvTen = ten;
                        if (sp  != null) sanPham = sp;
                        moManHinhDanhGia();
                    }
                });

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

    private void moManHinhDanhGia() {
        Intent intent = new Intent(this, DanhGiaKTVActivity.class);
        intent.putExtra(DanhGiaKTVActivity.EXTRA_TICKET_ID, ticketId);
        intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_UID, ktvUid);
        intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_TEN, ktvTen);
        intent.putExtra(DanhGiaKTVActivity.EXTRA_SAN_PHAM, sanPham);
        startActivity(intent);
    }

    private void batDauGoiThoai() {
        if (ktvUid == null || ktvUid.isEmpty()) {
            Toast.makeText(this, "Chưa có KTV xử lý ticket này", Toast.LENGTH_SHORT).show();
            return;
        }
        String token = StringeeTokenHelper.generateToken(user.getUid());
        if (token == null) {
            Toast.makeText(this, "Không thể tạo token gọi điện", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_ID, ktvUid);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_NAME, "KTV hỗ trợ");
        intent.putExtra(VoiceCallActivity.EXTRA_ACCESS_TOKEN, token);
        //intent.putExtra(VoiceCallActivity.EXTRA_IS_INCOMING, false);
        startActivity(intent);
    }

    private void guiTin(TextInputEditText edt) {
        String nd = edt.getText() != null ? edt.getText().toString().trim() : "";
        if (nd.isEmpty()) return;

        String ten = (hoTen != null && !hoTen.isEmpty()) ? hoTen : "Khách hàng";
        Map<String, Object> msg = new HashMap<>();
        msg.put("nguoiGuiUid", user.getUid());
        msg.put("nguoiGuiTen", ten);
        msg.put("vaiTro", "KhachHang");
        msg.put("noiDung", nd);
        msg.put("ktvUid", ktvUid);
        msg.put("khachHangUid", user.getUid());
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
                        // Chuyển Timestamp sang long để ChatAdapter đọc được
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
        if (ticketListener != null) ticketListener.remove();
    }
}
