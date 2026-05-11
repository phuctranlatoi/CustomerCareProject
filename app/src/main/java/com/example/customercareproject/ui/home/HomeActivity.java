package com.example.customercareproject.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;

import com.example.customercareproject.R;
import com.example.customercareproject.model.GoiDangKy;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.ui.LoginActivity;
import com.example.customercareproject.ui.components.InitialAvatarView;
import com.example.customercareproject.ui.danhgia.DanhGiaActivity;
import com.example.customercareproject.ui.danhgiaktv.DanhGiaKTVActivity;
import com.example.customercareproject.ui.loi.ChatKhachHangActivity;
import com.example.customercareproject.ui.loi.LichSuChatActivity;
import com.example.customercareproject.utils.AnimationHelper;
import com.example.customercareproject.utils.ResponsiveLayoutHelper;
import com.example.customercareproject.ui.flutter.FlutterEngineManager;
import io.flutter.embedding.android.FlutterFragment;
import androidx.fragment.app.FragmentManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;

public class HomeActivity extends AppCompatActivity {

    private CardView cardTicketActive;
    private CardView cardGoiHetHan;
    private TextView tvGoiHetHanMsg;
    private TextView tvTicketActiveTieuDe, tvTicketActiveKtv, tvDaCho;
    private Button btnChatNow;
    private String activeTicketId = null;
    private String activeHoTen = null;
    private String maSoThueUser = null;  // Mã số thuế của user
    private boolean daDanhGiaPopup = false;
    private InitialAvatarView avatarProfile;

    private java.util.List<String> danhSachSanPhamHienTai = new java.util.ArrayList<>();

    private Handler daChoHandler;
    private Runnable daChoRunnable;

    private com.google.firebase.firestore.ListenerRegistration ticketListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Apply fade-in animation to content
        View contentLayout = findViewById(R.id.contentLayout);
        AnimationHelper.fadeIn(contentLayout);

        TextView tvChaoMung = findViewById(R.id.tvChaoMung);
        RecyclerView rvSanPham = findViewById(R.id.rvSanPham);

        cardTicketActive = findViewById(R.id.cardTicketActive);
        cardGoiHetHan = findViewById(R.id.cardGoiHetHan);
        tvGoiHetHanMsg = findViewById(R.id.tvGoiHetHanMsg);
        tvTicketActiveTieuDe = findViewById(R.id.tvTicketActiveTieuDe);
        tvTicketActiveKtv = findViewById(R.id.tvTicketActiveKtv);
        tvDaCho = findViewById(R.id.tvDaCho);
        btnChatNow = findViewById(R.id.btnChatNow);

        avatarProfile = findViewById(R.id.avatarProfile);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    tvChaoMung.setText(hoTen != null ? hoTen : "bạn");
                    activeHoTen = hoTen;
                    maSoThueUser = doc.getString("maSoThue");
                    if (avatarProfile != null) avatarProfile.setName(hoTen != null ? hoTen : "?");

                    // Load sản phẩm theo gói đăng ký
                    taiSanPhamTheoCongTy(rvSanPham);
                });

        // Avatar click → bottom sheet profile menu
        if (avatarProfile != null) {
            avatarProfile.setOnClickListener(v -> {
                BottomSheetDialog sheet = new BottomSheetDialog(this);
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_profile_menu, null);
                sheetView.findViewById(R.id.itemTaiKhoan).setOnClickListener(v2 -> {
                    startActivity(new Intent(this, com.example.customercareproject.ui.ProfileActivity.class));
                    sheet.dismiss();
                });
                sheetView.findViewById(R.id.itemLichSu).setOnClickListener(v2 -> {
                    startActivity(new Intent(this, LichSuChatActivity.class));
                    sheet.dismiss();
                });
                sheetView.findViewById(R.id.itemDangXuat).setOnClickListener(v2 -> {
                    com.example.customercareproject.utils.StringeeManager.getInstance().reset();
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    sheet.dismiss();
                });
                sheet.setContentView(sheetView);
                sheet.show();
            });

            // Long-press → UI Showcase (moved from btnProfile)
            avatarProfile.setOnLongClickListener(v -> {
                startActivity(new Intent(this, com.example.customercareproject.ui.UIShowcaseActivity.class));
                return true;
            });
        }

        btnChatNow.setOnClickListener(v -> {
            if (activeTicketId != null) {
                Intent intent = new Intent(this, ChatKhachHangActivity.class);
                intent.putExtra("ticketId", activeTicketId);
                intent.putExtra("hoTen", activeHoTen);
                startActivity(intent);
            }
        });

        ExtendedFloatingActionButton fabYeuCau = findViewById(R.id.fabYeuCau);
        if (fabYeuCau != null) {
            fabYeuCau.setOnClickListener(v -> {
                if (danhSachSanPhamHienTai == null || danhSachSanPhamHienTai.isEmpty()) {
                    android.widget.Toast.makeText(this, "Chưa có sản phẩm đăng ký", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] tenSanPham = danhSachSanPhamHienTai.toArray(new String[0]);
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Chọn sản phẩm cần hỗ trợ")
                        .setItems(tenSanPham, (dialog, which) -> {
                            Intent intent = new Intent(this, com.example.customercareproject.ui.loi.YeuCauHoTroActivity.class);
                            intent.putExtra("sanPham", tenSanPham[which]);
                            startActivity(intent);
                        })
                        .show();
            });
        }

        // Shrink/extend FAB on scroll
        RecyclerView rvSanPhamScroll = findViewById(R.id.rvSanPham);
        if (rvSanPhamScroll != null && fabYeuCau != null) {
            rvSanPhamScroll.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                    if (dy > 0) fabYeuCau.shrink();
                    else if (dy < 0) fabYeuCau.extend();
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        batDauLangNgheTicket();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ticketListener != null) {
            ticketListener.remove();
            ticketListener = null;
        }
    }

    private void batDauLangNgheTicket() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        ticketListener = FirebaseFirestore.getInstance().collection("YeuCauHoTro")
                .whereEqualTo("uid", user.getUid())
                .orderBy("taoLuc", Query.Direction.DESCENDING)
                .limit(5) // Chỉ cần kiểm tra các ticket gần đây nhất
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    boolean foundActive = false;
                    long now = System.currentTimeMillis();
                    long durationLimit = 24L * 60 * 60 * 1000; // 24h

                    for (QueryDocumentSnapshot doc : snap) {
                        String ts = doc.getString("trangThai");
                        Timestamp taoLuc = doc.getTimestamp("taoLuc");

                        // 1. Logic cho Ticket đang hoạt động (Popup chat ở header)
                        if (!foundActive && taoLuc != null && (now - taoLuc.toDate().getTime() < durationLimit)) {
                            if ("ChoXuLy".equals(ts) || "DangXuLy".equals(ts) || "HangCho".equals(ts)) {
                                foundActive = true;
                                activeTicketId = doc.getId();

                                String tieuDe = doc.getString("tieuDeLoi");
                                String ktvTen = doc.getString("ktvTen");
                                tvTicketActiveTieuDe.setText(tieuDe != null ? tieuDe : "Đang hỗ trợ...");

                                if ("DangXuLy".equals(ts)) {
                                    tvTicketActiveKtv.setText("KTV đang xử lý");
                                    anDaChoVaHuyHandler();
                                } else {
                                    tvTicketActiveKtv.setText(ktvTen != null ? "KTV: " + ktvTen : "Đang tìm KTV...");
                                    capNhatDaCho(taoLuc);
                                }
                                cardTicketActive.setVisibility(View.VISIBLE);
                            }
                        }

                        // 2. Logic cho Ticket đã xử lý xong -> Hiện popup đánh giá (Chỉ hiện 1 lần)
                        if (!daDanhGiaPopup && "DaXuLy".equals(ts)) {
                            Boolean daDG = doc.getBoolean("daDanhGiaKtv");
                            String ktvUid = doc.getString("ktvUid");
                            if ((daDG == null || !daDG) && ktvUid != null && !ktvUid.isEmpty()) {
                                daDanhGiaPopup = true;
                                Intent intent = new Intent(this, DanhGiaKTVActivity.class);
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_TICKET_ID, doc.getId());
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_UID, ktvUid);
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_KTV_TEN, doc.getString("ktvTen"));
                                intent.putExtra(DanhGiaKTVActivity.EXTRA_SAN_PHAM, doc.getString("sanPham"));
                                startActivity(intent);
                            }
                        }
                    }

                    // Nếu sau khi duyệt hết list mà không thấy ticket nào active thì mới ẩn
                    if (!foundActive) {
                        activeTicketId = null;
                        cardTicketActive.setVisibility(View.GONE);
                        anDaChoVaHuyHandler();
                    }
                });
    }

    private void taiSanPhamTheoCongTy(RecyclerView rvSanPham) {
        if (maSoThueUser == null || maSoThueUser.isEmpty()) {
            hienThiSanPham(rvSanPham, Arrays.asList(SanPham.DANH_SACH));
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("GoiDangKy")
                .whereEqualTo("maSoThue", maSoThueUser)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        hienThiSanPham(rvSanPham, Arrays.asList(SanPham.DANH_SACH));
                        return;
                    }

                    GoiDangKy goi = snap.getDocuments().get(0).toObject(GoiDangKy.class);
                    if (goi == null || goi.getSanPhamDangKy() == null || goi.getSanPhamDangKy().isEmpty()) {
                        hienThiSanPham(rvSanPham, Arrays.asList(SanPham.DANH_SACH));
                        return;
                    }

                    if (!GoiDangKy.TRANG_THAI_HOAT_DONG.equals(goi.getTrangThai())) {
                        String msg = "HetHan".equals(goi.getTrangThai())
                                ? "⚠️ Gói đăng ký đã hết hạn. Vui lòng liên hệ để gia hạn."
                                : "⚠️ Gói đăng ký đang tạm dừng. Vui lòng liên hệ để được hỗ trợ.";
                        tvGoiHetHanMsg.setText(msg);
                        cardGoiHetHan.setVisibility(View.VISIBLE);
                        hienThiSanPham(rvSanPham, java.util.Collections.emptyList());
                        return;
                    }

                    cardGoiHetHan.setVisibility(View.GONE);
                    hienThiSanPham(rvSanPham, goi.getSanPhamDangKy());
                })
                .addOnFailureListener(e -> hienThiSanPham(rvSanPham, Arrays.asList(SanPham.DANH_SACH)));
    }

    private void hienThiSanPham(RecyclerView rvSanPham, java.util.List<String> danhSachSanPham) {
        danhSachSanPhamHienTai = danhSachSanPham;
        SanPhamAdapter adapter = new SanPhamAdapter(danhSachSanPham, tenSanPham -> {
            Intent intent = new Intent(this, DanhGiaActivity.class);
            intent.putExtra("sanPham", tenSanPham);
            startActivity(intent);
        });

        int columnCount = 2;
        GridLayoutManager layoutManager = new GridLayoutManager(this, columnCount);
        rvSanPham.setLayoutManager(layoutManager);
        rvSanPham.setAdapter(adapter);
    }

    private void capNhatDaCho(Timestamp taoLuc) {
        anDaChoVaHuyHandler();
        daChoHandler = new Handler(Looper.getMainLooper());
        daChoRunnable = new Runnable() {
            @Override
            public void run() {
                if (taoLuc == null) return;
                long elapsed = (System.currentTimeMillis() - taoLuc.toDate().getTime()) / 60000;
                tvDaCho.setText("Đã chờ: " + elapsed + "m");
                tvDaCho.setVisibility(View.VISIBLE);
                daChoHandler.postDelayed(this, 60000);
            }
        };
        daChoHandler.post(daChoRunnable);
    }

    private void anDaChoVaHuyHandler() {
        if (daChoHandler != null && daChoRunnable != null) {
            daChoHandler.removeCallbacks(daChoRunnable);
        }
        daChoHandler = null;
        daChoRunnable = null;
        if (tvDaCho != null) {
            tvDaCho.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        anDaChoVaHuyHandler();
    }
}