package com.example.customercareproject.ui.ktv;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.GhiChuTienDo;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.utils.SmartRouter;
import com.example.customercareproject.ui.components.Material3TextField;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class KtvTicketDetailActivity extends AppCompatActivity {

    private String ticketId;
    private boolean readOnly = false;
    private String khachHangUid = "";
    private FirebaseFirestore db;
    private FirebaseUser user;
    private YeuCauHoTro ticketHienTai;
    private ListenerRegistration ticketListener;
    private boolean daCapNhatTrangThai = false;

    private RecyclerView rvGhiChu;
    private TextView tvGhiChuTrong;
    private View layoutNhapGhiChu;
    private com.example.customercareproject.ui.components.Material3TextField edtGhiChu;
    private com.google.android.material.button.MaterialButton btnLuuGhiChu;
    private GhiChuTienDoAdapter ghiChuAdapter;
    private final List<GhiChuTienDo> danhSachGhiChu = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ktv_ticket_detail);

        ticketId = getIntent().getStringExtra("ticketId");
        readOnly = getIntent().getBooleanExtra("readOnly", false);
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        com.google.android.material.button.MaterialButton btnNhanTin = findViewById(R.id.btnNhanTin);
        com.google.android.material.button.MaterialButton btnDongTicket = findViewById(R.id.btnDongTicket);

        btnNhanTin.setOnClickListener(v -> {
            Intent intent = new Intent(this, KtvChatActivity.class);
            intent.putExtra("ticketId", ticketId);
            if (ticketHienTai != null) intent.putExtra("tenKhachHang", ticketHienTai.getHoTen());
            startActivity(intent);
        });
        btnDongTicket.setOnClickListener(v -> dongTicket());

        com.google.android.material.button.MaterialButton btnGoiDienDetail = findViewById(R.id.btnGoiDienDetail);
        btnGoiDienDetail.setOnClickListener(v -> batDauGoiDien());

        // Setup ghi chú tiến độ
        rvGhiChu = findViewById(R.id.rvGhiChu);
        tvGhiChuTrong = findViewById(R.id.tvGhiChuTrong);
        layoutNhapGhiChu = findViewById(R.id.layoutNhapGhiChu);
        edtGhiChu = findViewById(R.id.edtGhiChu);
        btnLuuGhiChu = findViewById(R.id.btnLuuGhiChu);

        com.google.android.material.chip.Chip chipDangKiemTra = findViewById(R.id.chipDangKiemTra);
        com.google.android.material.chip.Chip chipChoPhanhoi = findViewById(R.id.chipChoPhanhoi);
        com.google.android.material.chip.Chip chipDaLienHe = findViewById(R.id.chipDaLienHe);
        com.google.android.material.chip.Chip chipDaGiaiQuyet = findViewById(R.id.chipDaGiaiQuyet);

        if (chipDangKiemTra != null) chipDangKiemTra.setOnClickListener(v -> edtGhiChu.setText("Đang kiểm tra"));
        if (chipChoPhanhoi != null) chipChoPhanhoi.setOnClickListener(v -> edtGhiChu.setText("Chờ phản hồi khách"));
        if (chipDaLienHe != null) chipDaLienHe.setOnClickListener(v -> edtGhiChu.setText("Đã liên hệ điện thoại"));
        if (chipDaGiaiQuyet != null) chipDaGiaiQuyet.setOnClickListener(v -> edtGhiChu.setText("Đã giải quyết"));

        ghiChuAdapter = new GhiChuTienDoAdapter(danhSachGhiChu);
        rvGhiChu.setLayoutManager(new LinearLayoutManager(this));
        rvGhiChu.setAdapter(ghiChuAdapter);

        btnLuuGhiChu.setOnClickListener(v -> luuGhiChu());

        // Nút xem lịch sử note toàn bộ của KH trên sản phẩm này
        com.google.android.material.button.MaterialButton btnXemLichSuNote = findViewById(R.id.btnXemLichSuNote);
        if (btnXemLichSuNote != null) {
            btnXemLichSuNote.setOnClickListener(v -> xemLichSuNoteKhachHang());
        }

        if (readOnly) {
            layoutNhapGhiChu.setVisibility(View.GONE);
            btnDongTicket.setVisibility(View.GONE);
            daCapNhatTrangThai = true; // skip auto status update
        }

        taiChiTietTicket();
    }

    private void taiChiTietTicket() {
        ticketListener = db.collection("YeuCauHoTro").document(ticketId)
                .addSnapshotListener((doc, e) -> {
                    if (doc == null || !doc.exists()) return;
                    ticketHienTai = doc.toObject(YeuCauHoTro.class);
                    if (ticketHienTai == null) return;
                    ticketHienTai.setId(doc.getId());
                    if (ticketHienTai.getUid() != null) khachHangUid = ticketHienTai.getUid();

                    ((TextView) findViewById(R.id.tvTieuDe)).setText(ticketHienTai.getTieuDeLoi());
                    ((TextView) findViewById(R.id.tvSanPham)).setText(ticketHienTai.getSanPham());
                    ((TextView) findViewById(R.id.tvMoTa)).setText(ticketHienTai.getMoTaVanDe());
                    ((TextView) findViewById(R.id.tvHoTen)).setText("Họ tên: " + ticketHienTai.getHoTen());
                    ((TextView) findViewById(R.id.tvSdt)).setText("SĐT: " + ticketHienTai.getSoDienThoai());
                    ((TextView) findViewById(R.id.tvEmailKh)).setText("Email: " + ticketHienTai.getEmail());

                    TextView tvUuTien = findViewById(R.id.tvUuTienDetail);
                    String uu = ticketHienTai.getUuTien();
                    if ("Cao".equals(uu)) {
                        tvUuTien.setText("Khẩn cấp");
                        tvUuTien.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                    } else if ("Thap".equals(uu)) {
                        tvUuTien.setText("Ưu tiên thấp");
                        tvUuTien.setTextColor(android.graphics.Color.parseColor("#388E3C"));
                    } else {
                        tvUuTien.setText("Bình thường");
                        tvUuTien.setTextColor(android.graphics.Color.parseColor("#F57F17"));
                    }

                    // Cập nhật danh sách ghi chú
                    capNhatDanhSachGhiChu(ticketHienTai.getLichSuHoTro());

                    boolean daXuLy = "DaXuLy".equals(ticketHienTai.getTrangThai());
                    if (daXuLy) {
                        findViewById(R.id.btnDongTicket).setVisibility(View.GONE);
                        layoutNhapGhiChu.setVisibility(View.GONE); // read-only khi đã xử lý
                    } else {
                        layoutNhapGhiChu.setVisibility(View.VISIBLE);
                    }

                    if (!daCapNhatTrangThai &&
                            ("ChoXuLy".equals(ticketHienTai.getTrangThai()) || "HangCho".equals(ticketHienTai.getTrangThai()))) {
                        daCapNhatTrangThai = true;
                        hienDialogNhanTicket();
                    }
                });
    }

    private void hienDialogNhanTicket() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Nhận ticket này?")
            .setMessage("Bạn có muốn nhận và bắt đầu xử lý ticket này không?")
            .setPositiveButton("Nhận", (d, w) -> {
                db.collection("NguoiDung").document(user.getUid()).get()
                    .addOnSuccessListener(ktvDoc -> {
                        String tenKtv = ktvDoc.getString("hoTen");
                        db.collection("YeuCauHoTro").document(ticketId)
                            .update("trangThai", "DangXuLy",
                                    "capNhatLuc", Timestamp.now(),
                                    "ktvUid", user.getUid(),
                                    "ktvTen", tenKtv != null ? tenKtv : "");
                    });
            })
            .setNegativeButton("Xem thôi", (d, w) -> {
                if (layoutNhapGhiChu != null) layoutNhapGhiChu.setVisibility(View.GONE);
                View btnDong = findViewById(R.id.btnDongTicket);
                if (btnDong != null) btnDong.setVisibility(View.GONE);
            })
            .setCancelable(false)
            .show();
    }

    @SuppressWarnings("unchecked")
    private void capNhatDanhSachGhiChu(List<Map<String, Object>> lichSu) {
        danhSachGhiChu.clear();
        if (lichSu != null) {
            for (Map<String, Object> map : lichSu) {
                GhiChuTienDo g = GhiChuTienDo.fromMap(map);
                if (g != null) danhSachGhiChu.add(g);
            }
            // Sắp xếp gần nhất lên đầu
            danhSachGhiChu.sort((a, b) -> {
                if (a.getThoiDiem() == null && b.getThoiDiem() == null) return 0;
                if (a.getThoiDiem() == null) return 1;
                if (b.getThoiDiem() == null) return -1;
                return b.getThoiDiem().compareTo(a.getThoiDiem());
            });
        }
        ghiChuAdapter.notifyDataSetChanged();
        tvGhiChuTrong.setVisibility(danhSachGhiChu.isEmpty() ? View.VISIBLE : View.GONE);
        rvGhiChu.setVisibility(danhSachGhiChu.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void luuGhiChu() {
        String noiDung = edtGhiChu.getText() != null ? edtGhiChu.getText().toString().trim() : "";
        if (noiDung.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung ghi chú", Toast.LENGTH_SHORT).show();
            return;
        }
        if (noiDung.length() > 1000) {
            Toast.makeText(this, "Ghi chú không được vượt quá 1000 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        btnLuuGhiChu.setEnabled(false);
        btnLuuGhiChu.setText("Đang lưu...");

        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(ktvDoc -> {
                    String tenKtv = ktvDoc.getString("hoTen");
                    GhiChuTienDo ghiChu = new GhiChuTienDo(
                            user.getUid(),
                            tenKtv != null ? tenKtv : "",
                            noiDung,
                            Timestamp.now()
                    );
                    
                    // Use arrayUnion to add to existing array or create new array if field doesn't exist
                    db.collection("YeuCauHoTro").document(ticketId)
                            .update(
                                    "lichSuHoTro", FieldValue.arrayUnion(ghiChu.toMap()),
                                    "capNhatLuc", Timestamp.now()
                            )
                            .addOnSuccessListener(v -> {
                                edtGhiChu.setText("");
                                Toast.makeText(this, "Đã lưu ghi chú", Toast.LENGTH_SHORT).show();
                                resetButtonState();
                            })
                            .addOnFailureListener(ex -> {
                                // If field doesn't exist, create it with set merge
                                java.util.List<Map<String, Object>> newList = new java.util.ArrayList<>();
                                newList.add(ghiChu.toMap());
                                
                                db.collection("YeuCauHoTro").document(ticketId)
                                        .update("lichSuHoTro", newList, "capNhatLuc", Timestamp.now())
                                        .addOnSuccessListener(v2 -> {
                                            edtGhiChu.setText("");
                                            Toast.makeText(this, "Đã lưu ghi chú", Toast.LENGTH_SHORT).show();
                                            resetButtonState();
                                        })
                                        .addOnFailureListener(ex2 -> {
                                            Toast.makeText(this, "Lỗi lưu ghi chú: " + ex2.getMessage(), Toast.LENGTH_SHORT).show();
                                            resetButtonState();
                                        });
                            });
                })
                .addOnFailureListener(ex -> {
                    Toast.makeText(this, "Lỗi lấy thông tin KTV: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                    resetButtonState();
                });
    }
    
    private void resetButtonState() {
        if (btnLuuGhiChu != null) {
            btnLuuGhiChu.setEnabled(true);
            btnLuuGhiChu.setText("Lưu ghi chú");
        }
    }

    private void batDauGoiDien() {
        if (khachHangUid == null || khachHangUid.isEmpty()) {
            Toast.makeText(this, "Chưa lấy được thông tin khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, com.example.customercareproject.ui.call.VoiceCallActivity.class);
        intent.putExtra(com.example.customercareproject.ui.call.VoiceCallActivity.EXTRA_CALLEE_ID, khachHangUid);
        intent.putExtra(com.example.customercareproject.ui.call.VoiceCallActivity.EXTRA_CALLEE_NAME,
                ticketHienTai != null ? ticketHienTai.getHoTen() : "Khách hàng");
        intent.putExtra(com.example.customercareproject.ui.call.VoiceCallActivity.EXTRA_CALLER_UID, user.getUid());
        startActivity(intent);
    }

    private void dongTicket() {
        if (ticketHienTai == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Xác nhận đóng ticket")
                .setMessage("Bạn có chắc muốn đóng ticket này?")
                .setPositiveButton("Đóng ticket", (dialog, which) -> {
                    db.collection("YeuCauHoTro").document(ticketId)
                            .update("trangThai", "DaXuLy",
                                    "capNhatLuc", Timestamp.now(),
                                    "ktvTen", ticketHienTai.getKtvTen() != null ? ticketHienTai.getKtvTen() : "")
                            .addOnSuccessListener(v -> {
                                SmartRouter.giamTicketKtv(user.getUid());
                                Toast.makeText(this, "Ticket đã đóng!", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Xem lịch sử note của tất cả KTV cho khách hàng này trên cùng sản phẩm.
     * Gom nhóm theo: KTV → Ticket (đợt hỗ trợ) → Danh sách note chi tiết
     */
    private void xemLichSuNoteKhachHang() {
        if (ticketHienTai == null || khachHangUid == null || khachHangUid.isEmpty()) {
            Toast.makeText(this, "Chưa có thông tin khách hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        String sanPham = ticketHienTai.getSanPham();
        if (sanPham == null || sanPham.isEmpty()) {
            Toast.makeText(this, "Không xác định được sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Đang tải lịch sử...", Toast.LENGTH_SHORT).show();

        db.collection("YeuCauHoTro")
            .whereEqualTo("uid", khachHangUid)
            .whereEqualTo("sanPham", sanPham)
            .get()
            .addOnSuccessListener(snap -> {
                if (snap.isEmpty()) {
                    new AlertDialog.Builder(this)
                        .setTitle("Lịch sử Note - " + sanPham)
                        .setMessage("Chưa có lịch sử note nào cho khách hàng này trên sản phẩm " + sanPham)
                        .setPositiveButton("Đóng", null)
                        .show();
                    return;
                }

                // Thu thập tất cả note, gom theo KTV → đợt hỗ trợ (ticketId)
                // Key: ktvTen, Value: list of SessionGroup
                LinkedHashMap<String, List<SessionGroup>> ktvGroupMap = new LinkedHashMap<>();
                SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat sdfFull = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

                for (QueryDocumentSnapshot doc : snap) {
                    YeuCauHoTro yc = doc.toObject(YeuCauHoTro.class);
                    yc.setId(doc.getId());
                    List<Map<String, Object>> lichSu = yc.getLichSuHoTro();
                    if (lichSu == null || lichSu.isEmpty()) continue;

                    // Nhóm note theo ktvTen trong từng ticket
                    LinkedHashMap<String, List<GhiChuTienDo>> notesByKtvInTicket = new LinkedHashMap<>();
                    for (Map<String, Object> map : lichSu) {
                        GhiChuTienDo g = GhiChuTienDo.fromMap(map);
                        if (g == null) continue;
                        String ktvKey = g.getKtvTen() != null ? g.getKtvTen() : "(Không rõ)";
                        notesByKtvInTicket.computeIfAbsent(ktvKey, k -> new ArrayList<>()).add(g);
                    }

                    // Tạo SessionGroup cho mỗi KTV trong ticket này
                    String tieuDeTicket = yc.getTieuDeLoi() != null ? yc.getTieuDeLoi() : "(Không tiêu đề)";
                    String trangThaiTicket = yc.getTrangThai() != null ? yc.getTrangThai() : "";
                    String ngayTao = yc.getTaoLuc() != null ? sdfDate.format(yc.getTaoLuc().toDate()) : "";

                    for (Map.Entry<String, List<GhiChuTienDo>> entry : notesByKtvInTicket.entrySet()) {
                        String ktvTen = entry.getKey();
                        List<GhiChuTienDo> notes = entry.getValue();
                        SessionGroup session = new SessionGroup();
                        session.ticketId = doc.getId();
                        session.tieuDe = tieuDeTicket;
                        session.trangThai = trangThaiTicket;
                        session.ngayTao = ngayTao;
                        session.notes = notes;

                        ktvGroupMap.computeIfAbsent(ktvTen, k -> new ArrayList<>()).add(session);
                    }
                }

                if (ktvGroupMap.isEmpty()) {
                    new AlertDialog.Builder(this)
                        .setTitle("Lịch sử Note - " + sanPham)
                        .setMessage("Chưa có note nào được ghi cho khách hàng này.")
                        .setPositiveButton("Đóng", null)
                        .show();
                    return;
                }

                // Hiển thị danh sách nhóm KTV
                hienThiDanhSachKtvGroup(sanPham, ktvGroupMap, sdfFull);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Lỗi tải lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    /** Hiển thị dialog cấp 1: danh sách KTV → mỗi KTV có các đợt hỗ trợ */
    private void hienThiDanhSachKtvGroup(String sanPham, 
            LinkedHashMap<String, List<SessionGroup>> ktvGroupMap, SimpleDateFormat sdfFull) {
        
        // Build danh sách items cho dialog
        List<String> ktvNames = new ArrayList<>(ktvGroupMap.keySet());
        String[] items = new String[ktvNames.size()];
        for (int i = 0; i < ktvNames.size(); i++) {
            String ktvTen = ktvNames.get(i);
            List<SessionGroup> sessions = ktvGroupMap.get(ktvTen);
            int totalNotes = 0;
            for (SessionGroup s : sessions) totalNotes += s.notes.size();
            items[i] = "👤 " + ktvTen + " — " + sessions.size() + " đợt HT, " + totalNotes + " note";
        }

        new AlertDialog.Builder(this)
            .setTitle("📋 Lịch sử Note - " + sanPham)
            .setItems(items, (dialog, which) -> {
                String ktvTen = ktvNames.get(which);
                List<SessionGroup> sessions = ktvGroupMap.get(ktvTen);
                hienThiDanhSachSession(ktvTen, sessions, sdfFull);
            })
            .setPositiveButton("Đóng", null)
            .show();
    }

    /** Hiển thị dialog cấp 2: danh sách đợt hỗ trợ (ticket) của 1 KTV */
    private void hienThiDanhSachSession(String ktvTen, List<SessionGroup> sessions, 
            SimpleDateFormat sdfFull) {
        
        String[] items = new String[sessions.size()];
        for (int i = 0; i < sessions.size(); i++) {
            SessionGroup s = sessions.get(i);
            String trangThaiEmoji = "DaXuLy".equals(s.trangThai) ? "✅" 
                : "DangXuLy".equals(s.trangThai) ? "🔄" : "⏳";
            items[i] = trangThaiEmoji + " " + s.tieuDe + "\n    📅 " + s.ngayTao 
                + " — " + s.notes.size() + " note";
        }

        new AlertDialog.Builder(this)
            .setTitle("👤 " + ktvTen + " — Các đợt hỗ trợ")
            .setItems(items, (dialog, which) -> {
                SessionGroup session = sessions.get(which);
                hienThiChiTietNote(ktvTen, session, sdfFull);
            })
            .setPositiveButton("Quay lại", null)
            .show();
    }

    /** Hiển thị dialog cấp 3: chi tiết từng note trong 1 đợt hỗ trợ */
    private void hienThiChiTietNote(String ktvTen, SessionGroup session, SimpleDateFormat sdfFull) {
        StringBuilder sb = new StringBuilder();
        sb.append("📌 Ticket: ").append(session.tieuDe).append("\n");
        sb.append("📅 Ngày tạo: ").append(session.ngayTao).append("\n");
        sb.append("──────────────────\n\n");

        for (int i = 0; i < session.notes.size(); i++) {
            GhiChuTienDo note = session.notes.get(i);
            sb.append("#").append(i + 1);
            if (note.getThoiDiem() != null) {
                sb.append(" — ").append(sdfFull.format(note.getThoiDiem().toDate()));
            }
            sb.append("\n");
            sb.append(note.getNoiDung() != null ? note.getNoiDung() : "(trống)");
            sb.append("\n\n");
        }

        new AlertDialog.Builder(this)
            .setTitle("👤 " + ktvTen + " — Chi tiết note")
            .setMessage(sb.toString().trim())
            .setPositiveButton("Quay lại", null)
            .show();
    }

    /** Helper class để group note theo đợt hỗ trợ (ticket) */
    private static class SessionGroup {
        String ticketId;
        String tieuDe;
        String trangThai;
        String ngayTao;
        List<GhiChuTienDo> notes;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketListener != null) ticketListener.remove();
    }
}
