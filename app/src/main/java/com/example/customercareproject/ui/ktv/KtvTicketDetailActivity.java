package com.example.customercareproject.ui.ktv;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.List;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketListener != null) ticketListener.remove();
    }
}


