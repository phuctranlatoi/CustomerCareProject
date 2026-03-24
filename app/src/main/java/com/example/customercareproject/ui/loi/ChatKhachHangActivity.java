package com.example.customercareproject.ui.loi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.NguoiDung;
import com.example.customercareproject.ui.call.VoiceCallActivity;
import com.example.customercareproject.ui.danhgiaktv.DanhGiaKTVActivity;
import com.example.customercareproject.ui.ktv.ChatAdapter;
import com.example.customercareproject.utils.SmartRouter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatKhachHangActivity extends AppCompatActivity {

    private String ticketId, hoTen, ktvUid = "", ktvTen = "", sanPham = "";
    private boolean daDanhGia = false;
    private boolean cheDoBocDoc = false;
    private boolean dangAssign = false;   // tránh assign 2 lần cùng lúc

    private ListenerRegistration ticketListener;
    private ListenerRegistration ktvScanListener; // lắng nghe KTV rảnh realtime
    private FirebaseUser user;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore db;
    private ListenerRegistration chatListener;
    private RecyclerView rvChat;
    private TextView tvSubtitle;
    private final List<Map<String, Object>> danhSachTin = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_khach_hang);

        ticketId = getIntent().getStringExtra("ticketId");
        hoTen = getIntent().getStringExtra("hoTen");
        String trangThaiIntent = getIntent().getStringExtra("trangThai");
        if ("DaXuLy".equals(trangThaiIntent)) cheDoBocDoc = true;

        if (ticketId == null || ticketId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ticket", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { finish(); return; }

        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        tvSubtitle = findViewById(R.id.tvSubtitle);

        rvChat = findViewById(R.id.rvChat);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);
        chatAdapter = new ChatAdapter(new ArrayList<>(), user.getUid());
        rvChat.setAdapter(chatAdapter);

        // Lắng nghe realtime ticket — xử lý mọi thay đổi trạng thái
        ticketListener = db.collection("YeuCauHoTro").document(ticketId)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null || !snap.exists()) return;

                    String ts = snap.getString("trangThai");
                    String ktv = snap.getString("ktvUid");
                    String ten = snap.getString("ktvTen");
                    String sp  = snap.getString("sanPham");
                    if (ktv != null && !ktv.isEmpty()) ktvUid = ktv;
                    if (ten != null) ktvTen = ten;
                    if (sp  != null) sanPham = sp;
                    if (hoTen == null || hoTen.isEmpty()) hoTen = snap.getString("hoTen");

                    boolean chuaCoKtv = (ktv == null || ktv.isEmpty());

                    if (chuaCoKtv && ("HangCho".equals(ts) || "ChoXuLy".equals(ts))) {
                        // Chưa có KTV → bật quét realtime
                        capNhatSubtitle("🔍 Đang tìm kỹ thuật viên...");
                        batDauQuetKtv();

                    } else if (!chuaCoKtv && ("ChoXuLy".equals(ts) || "DangXuLy".equals(ts))) {
                        // Đã có KTV → dừng quét
                        dungQuetKtv();
                        capNhatSubtitle(ktvTen != null && !ktvTen.isEmpty()
                                ? "KTV: " + ktvTen : "Đang kết nối...");

                    } else if ("DaXuLy".equals(ts)) {
                        dungQuetKtv();
                        if (!cheDoBocDoc) {
                            cheDoBocDoc = true;
                            apDungCheDoDocOnly();
                        }
                        Boolean daDG = snap.getBoolean("daDanhGiaKtv");
                        if ((daDG == null || !daDG) && !daDanhGia) {
                            daDanhGia = true;
                            moManHinhDanhGia();
                        }
                    }
                });

        TextInputEditText edtTinNhan = findViewById(R.id.edtTinNhan);
        ImageButton btnGui = findViewById(R.id.btnGui);
        ImageButton btnGoiThoai = findViewById(R.id.btnGoiThoai);

        if (cheDoBocDoc) apDungCheDoDocOnly();

        btnGui.setOnClickListener(v -> guiTin(edtTinNhan));
        edtTinNhan.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { guiTin(edtTinNhan); return true; }
            return false;
        });
        btnGoiThoai.setOnClickListener(v -> batDauGoiThoai());

        langNgheChat();
    }

    /**
     * Lắng nghe realtime collection NguoiDung — khi có KTV nào chuyển sang "Ran"
     * thì thử assign cho ticket này ngay lập tức.
     */
    private void batDauQuetKtv() {
        if (ktvScanListener != null) return; // đã đang quét rồi

        ktvScanListener = db.collection("NguoiDung")
                .whereEqualTo("vaiTro", NguoiDung.VAI_TRO_KTV)
                .whereEqualTo("trangThai", NguoiDung.TRANG_THAI_RAN)
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null || snap.isEmpty()) return;
                    if (dangAssign) return;

                    // Tìm KTV tốt nhất trong danh sách rảnh hiện tại
                    String bestUid = null, bestTen = null;
                    int minTicket = Integer.MAX_VALUE;

                    // Ưu tiên chuyên môn
                    for (QueryDocumentSnapshot doc : snap) {
                        NguoiDung ktv = doc.toObject(NguoiDung.class);
                        boolean coChuyenMon = ktv.getChuyenMon() != null
                                && ktv.getChuyenMon().contains(sanPham);
                        if (coChuyenMon && ktv.getSoTicketDangXuLy() < minTicket) {
                            minTicket = ktv.getSoTicketDangXuLy();
                            bestUid = doc.getId();
                            bestTen = ktv.getHoTen();
                        }
                    }
                    // Fallback: bất kỳ KTV rảnh ít ticket nhất
                    if (bestUid == null) {
                        minTicket = Integer.MAX_VALUE;
                        for (QueryDocumentSnapshot doc : snap) {
                            NguoiDung ktv = doc.toObject(NguoiDung.class);
                            if (ktv.getSoTicketDangXuLy() < minTicket) {
                                minTicket = ktv.getSoTicketDangXuLy();
                                bestUid = doc.getId();
                                bestTen = ktv.getHoTen();
                            }
                        }
                    }

                    if (bestUid == null) return;

                    // Assign bằng transaction để tránh race condition
                    dangAssign = true;
                    final String finalUid = bestUid;
                    final String finalTen = bestTen;

                    db.runTransaction(tx -> {
                        // Double-check ticket vẫn chưa có KTV
                        com.google.firebase.firestore.DocumentSnapshot ticketDoc =
                                tx.get(db.collection("YeuCauHoTro").document(ticketId));
                        String currentKtv = ticketDoc.getString("ktvUid");
                        if (currentKtv != null && !currentKtv.isEmpty()) return null;

                        tx.update(db.collection("YeuCauHoTro").document(ticketId),
                                "ktvUid", finalUid,
                                "ktvTen", finalTen,
                                "trangThai", "ChoXuLy",
                                "capNhatLuc", Timestamp.now());

                        tx.update(db.collection("NguoiDung").document(finalUid),
                                "soTicketDangXuLy", FieldValue.increment(1));
                        return null;
                    }).addOnSuccessListener(v -> {
                        dangAssign = false;
                        dungQuetKtv();
                    }).addOnFailureListener(ex -> {
                        dangAssign = false; // retry lần sau
                    });
                });
    }

    private void dungQuetKtv() {
        if (ktvScanListener != null) {
            ktvScanListener.remove();
            ktvScanListener = null;
        }
    }

    private void capNhatSubtitle(String text) {
        if (tvSubtitle != null) tvSubtitle.setText(text);
    }

    private void apDungCheDoDocOnly() {
        View inputBar = findViewById(R.id.layoutInputBar);
        if (inputBar != null) inputBar.setVisibility(View.GONE);
        ImageButton btnGoiThoai = findViewById(R.id.btnGoiThoai);
        if (btnGoiThoai != null) btnGoiThoai.setVisibility(View.GONE);
        capNhatSubtitle("Cuộc trò chuyện đã kết thúc");
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
        Intent intent = new Intent(this, VoiceCallActivity.class);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_ID, ktvUid);
        intent.putExtra(VoiceCallActivity.EXTRA_CALLEE_NAME,
                ktvTen != null && !ktvTen.isEmpty() ? ktvTen : "KTV hỗ trợ");
        intent.putExtra(VoiceCallActivity.EXTRA_CALLER_UID, user.getUid());
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
        msg.put("thoiGian", FieldValue.serverTimestamp());

        edt.setText("");
        db.collection("TinNhan").document(ticketId)
                .collection("messages")
                .add(msg)
                .addOnFailureListener(ex ->
                        Toast.makeText(this, "Lỗi gửi: " + ex.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void langNgheChat() {
        chatListener = db.collection("TinNhan").document(ticketId)
                .collection("messages")
                .orderBy("thoiGian", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    danhSachTin.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshots) {
                        Map<String, Object> m = new HashMap<>(doc.getData());
                        m.put("_id", doc.getId());
                        Object ts = m.get("thoiGian");
                        if (ts instanceof com.google.firebase.Timestamp) {
                            m.put("thoiGian", ((com.google.firebase.Timestamp) ts).toDate().getTime());
                        }
                        danhSachTin.add(m);
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
        dungQuetKtv();
    }
}
