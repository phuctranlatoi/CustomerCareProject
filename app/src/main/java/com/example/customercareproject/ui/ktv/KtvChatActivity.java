package com.example.customercareproject.ui.ktv;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.TemplateTrLoi;
import com.example.customercareproject.ui.call.VoiceCallActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
    private String currentSanPham = "";
    private TextInputEditText edtTinNhan;
    private TextView tvSubtitle;
    private FirebaseUser user;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration chatListener;
    private RecyclerView rvChat;
    private final List<Map<String, Object>> danhSachTin = new ArrayList<>();
    private List<TemplateTrLoi> allTemplates = new ArrayList<>();

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

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvSubtitle = findViewById(R.id.tvSubtitle);
        // Set tên khách hàng ngay nếu đã có từ intent
        if (tenKhachHang != null && !tenKhachHang.isEmpty()) {
            tvSubtitle.setText("Khách: " + tenKhachHang);
        }

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
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        if (doc.getString("uid") != null) khachHangUid = doc.getString("uid");
                        // Fallback: lấy tên từ Firestore nếu intent không có
                        if (tenKhachHang == null || tenKhachHang.isEmpty()) {
                            tenKhachHang = doc.getString("hoTen");
                        }
                        if (tenKhachHang != null && !tenKhachHang.isEmpty()) {
                            tvSubtitle.setText("Khách: " + tenKhachHang);
                        }
                        // Lấy sanPham
                        String sp = doc.getString("sanPham");
                        if (sp != null) currentSanPham = sp;
                        // Re-enable template end icon now that currentSanPham is loaded
                        com.google.android.material.textfield.TextInputLayout til = findViewById(R.id.tilTinNhan);
                        if (til != null) til.setEndIconActivated(true);
                        // Load all templates for auto-suggest
                        if (sp != null) {
                            db.collection("TemplateTrLoi")
                                .whereEqualTo("sanPham", sp)
                                .get()
                                .addOnSuccessListener(tSnap -> {
                                    allTemplates.clear();
                                    for (com.google.firebase.firestore.QueryDocumentSnapshot tDoc : tSnap) {
                                        TemplateTrLoi t = tDoc.toObject(TemplateTrLoi.class);
                                        t.setId(tDoc.getId());
                                        allTemplates.add(t);
                                    }
                                });
                        }
                    }
                });

        TextInputEditText edtTinNhan = findViewById(R.id.edtTinNhan);
        this.edtTinNhan = edtTinNhan;
        ImageButton btnGui = findViewById(R.id.btnGui);
        ImageButton btnGoiThoai = findViewById(R.id.btnGoiThoai);
        com.google.android.material.textfield.TextInputLayout tilTinNhan = findViewById(R.id.tilTinNhan);

        btnGui.setOnClickListener(v -> guiTin(edtTinNhan));
        edtTinNhan.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { guiTin(edtTinNhan); return true; }
            return false;
        });

        btnGoiThoai.setOnClickListener(v -> batDauGoiThoai());
        if (tilTinNhan != null) {
            tilTinNhan.setEndIconOnClickListener(v -> hienBottomSheetTemplate());
        }

        langNgheChat();
    }

    private void batDauGoiThoai() {
        if (khachHangUid == null || khachHangUid.isEmpty()) {
            Toast.makeText(this, "Chưa lấy được thông tin khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_ID, khachHangUid);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_NAME, tenKhachHang != null ? tenKhachHang : "Khách hàng");
        intent.putExtra(VoiceCallActivity.EXTRA_CALLER_UID, user.getUid());
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

    private void hienBottomSheetTemplate() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_template, null);
        dialog.setContentView(sheetView);

        RecyclerView rvTemplate = sheetView.findViewById(R.id.rvTemplate);
        TextView tvEmptyTemplate = sheetView.findViewById(R.id.tvEmptyTemplate);
        rvTemplate.setLayoutManager(new LinearLayoutManager(this));

        db.collection("TemplateTrLoi")
                .whereEqualTo("sanPham", currentSanPham)
                // Removed .orderBy("tieuDe") to avoid index requirement
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<TemplateTrLoi> danhSach = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        TemplateTrLoi t = doc.toObject(TemplateTrLoi.class);
                        t.setId(doc.getId());
                        danhSach.add(t);
                    }
                    
                    // Sort in memory instead of in query
                    if (!danhSach.isEmpty()) {
                        java.util.Collections.sort(danhSach, (t1, t2) -> {
                            String title1 = t1.getTieuDe() != null ? t1.getTieuDe() : "";
                            String title2 = t2.getTieuDe() != null ? t2.getTieuDe() : "";
                            return title1.compareTo(title2);
                        });
                    }
                    
                    if (danhSach.isEmpty()) {
                        tvEmptyTemplate.setVisibility(View.VISIBLE);
                        rvTemplate.setVisibility(View.GONE);
                    } else {
                        tvEmptyTemplate.setVisibility(View.GONE);
                        rvTemplate.setVisibility(View.VISIBLE);
                        TemplateAdapter adapter = new TemplateAdapter(danhSach, template -> {
                            if (edtTinNhan != null) edtTinNhan.setText(template.getNoiDung());
                            dialog.dismiss();
                        });
                        rvTemplate.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải template: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        dialog.show();
    }

    private void filterTemplateSuggest(String query) {
        // Feature disabled as layout is simplified
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) chatListener.remove();
    }
}
