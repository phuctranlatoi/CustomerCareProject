package com.example.customercareproject.ui.loi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.customercareproject.R;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.utils.NlpHelper;
import com.example.customercareproject.utils.SmartRouter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class YeuCauHoTroActivity extends AppCompatActivity {

    private TextInputEditText edtHoTen, edtSoDienThoai, edtEmail, edtMoTaVanDe;
    private String sanPham, loiId, tieuDeLoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yeu_cau_ho_tro);

        sanPham = getIntent().getStringExtra("sanPham");
        loiId = getIntent().getStringExtra("loiId");
        tieuDeLoi = getIntent().getStringExtra("tieuDeLoi");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        ((TextView) findViewById(R.id.tvTieuDeLoi)).setText(tieuDeLoi);
        ((TextView) findViewById(R.id.tvSanPhamLoi)).setText(sanPham);

        edtHoTen = findViewById(R.id.edtHoTen);
        edtSoDienThoai = findViewById(R.id.edtSoDienThoai);
        edtEmail = findViewById(R.id.edtEmail);
        edtMoTaVanDe = findViewById(R.id.edtMoTaVanDe);

        // Điền sẵn thông tin từ tài khoản
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            edtEmail.setText(user.getEmail());
            FirebaseFirestore.getInstance().collection("NguoiDung")
                    .document(user.getUid()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            edtHoTen.setText(doc.getString("hoTen"));
                            edtSoDienThoai.setText(doc.getString("soDienThoai"));
                        }
                    });
        }

        ((com.google.android.material.button.MaterialButton) findViewById(R.id.btnGuiYeuCau)).setOnClickListener(v -> guiYeuCau());
    }

    private void guiYeuCau() {
        String hoTen = edtHoTen.getText() != null ? edtHoTen.getText().toString().trim() : "";
        String sdt = edtSoDienThoai.getText() != null ? edtSoDienThoai.getText().toString().trim() : "";
        String email = edtEmail.getText() != null ? edtEmail.getText().toString().trim() : "";
        String moTa = edtMoTaVanDe.getText() != null ? edtMoTaVanDe.getText().toString().trim() : "";

        if (hoTen.isEmpty()) { edtHoTen.setError("Vui lòng nhập họ tên"); return; }
        if (sdt.isEmpty()) { edtSoDienThoai.setError("Vui lòng nhập số điện thoại"); return; }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : "";

        YeuCauHoTro yeuCau = new YeuCauHoTro(uid, hoTen, email, sdt, sanPham,
                loiId != null ? loiId : "", tieuDeLoi, moTa);

        String uuTien = NlpHelper.phanTichUuTien(moTa + " " + tieuDeLoi);
        yeuCau.setUuTien(uuTien);

        // Thử tìm KTV rảnh ngay. Nếu không có → lưu HangCho, Cloud Function tự assign sau.
        SmartRouter.timKtvRanh(sanPham, (ktvUid, ktvTen) -> {
            yeuCau.setKtvUid(ktvUid);
            yeuCau.setKtvTen(ktvTen);
            // Có KTV → trangThai = ChoXuLy
            luuYeuCau(yeuCau, ktvUid);
        }, () -> {
            // Không có KTV → trangThai = HangCho, server sẽ assign
            yeuCau.setTrangThai("HangCho");
            luuYeuCau(yeuCau, null);
        });
    }

    private void luuYeuCau(YeuCauHoTro yeuCau, String ktvUid) {
        FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .add(yeuCau)
                .addOnSuccessListener(ref -> {
                    if (ktvUid != null) {
                        SmartRouter.tangTicketKtv(ktvUid);
                    } else {
                        // Đánh dấu thời điểm vào hàng chờ để Cloud Function sort theo thứ tự
                        ref.update("thoiGianChoXuLy", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    }
                    String msg = ktvUid != null
                            ? "Yêu cầu đã gửi! KTV " + yeuCau.getKtvTen() + " sẽ hỗ trợ bạn."
                            : "Yêu cầu đã gửi! Đang tìm kỹ thuật viên, bạn sẽ được hỗ trợ sớm nhất.";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    android.content.Intent chatIntent = new android.content.Intent(this, ChatKhachHangActivity.class);
                    chatIntent.putExtra("ticketId", ref.getId());
                    chatIntent.putExtra("hoTen", yeuCau.getHoTen());
                    startActivity(chatIntent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
