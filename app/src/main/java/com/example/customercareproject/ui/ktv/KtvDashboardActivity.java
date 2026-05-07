package com.example.customercareproject.ui.ktv;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.NguoiDung;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.ui.LoginActivity;
import com.google.android.material.tabs.TabLayout;
import com.example.customercareproject.utils.FirebaseHelper;
import com.example.customercareproject.utils.SmartRouter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KtvDashboardActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "ticket_channel";
    private FirebaseFirestore db;
    private FirebaseUser user;
    private DatabaseReference statusRef; // RTDB: trạng thái online real-time
    private TicketAdapter adapter;
    private ListenerRegistration ticketListener;
    private ListenerRegistration autoStatusListener;
    private String filterTrangThai = "ChoXuLy";
    private TextView tvSoTicket;
    private com.google.android.material.chip.Chip tvTrangThaiKtv;
    private com.example.customercareproject.ui.components.InitialAvatarView avatarKtv;
    private com.google.android.material.search.SearchBar searchBar;
    private int prevChoXuLyCount = -1;
    // Tránh set Offline khi chỉ chuyển sang màn hình khác trong app
    private boolean dangXuatThucSu = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ktv_dashboard);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { startActivity(new Intent(this, LoginActivity.class)); finish(); return; }

        taoNotificationChannel();

        tvSoTicket = findViewById(R.id.tvSoTicket);
        tvTrangThaiKtv = findViewById(R.id.tvTrangThaiKtv);
        avatarKtv = findViewById(R.id.avatarKtv);
        searchBar = findViewById(R.id.searchBar);
        TextView tvTenKtv = findViewById(R.id.tvTenKtv);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        RecyclerView rvTickets = findViewById(R.id.rvTickets);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // RTDB: set trạng thái online, tự set Offline khi mất kết nối
        try {
            statusRef = FirebaseHelper.getStatusRef(user.getUid());
            batStatusRTDB(NguoiDung.TRANG_THAI_RAN);
        } catch (Exception e) {
            // RTDB chưa enable hoặc URL sai — chỉ update Firestore
            db.collection("NguoiDung").document(user.getUid()).update("trangThai", NguoiDung.TRANG_THAI_RAN);
            capNhatBadge(NguoiDung.TRANG_THAI_RAN);
        }

        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    tvTenKtv.setText(hoTen);
                    if (avatarKtv != null) avatarKtv.setName(hoTen);
                    TextView tvTongDaXuLy = findViewById(R.id.tvTongDaXuLy);
                    if (tvTongDaXuLy != null) {
                        Object tong = doc.get("tongTicketDaXuLy");
                        tvTongDaXuLy.setText(tong instanceof Number ? String.valueOf(((Number)tong).intValue()) : "0");
                    }
                });

        btnLogout.setOnClickListener(v -> dangXuat());

        tvTrangThaiKtv.setOnClickListener(v -> moDialogChonTrangThai());
        
        if (searchBar != null) {
            searchBar.setOnClickListener(v -> android.widget.Toast.makeText(this, "Tính năng tìm kiếm sẽ sớm ra mắt", android.widget.Toast.LENGTH_SHORT).show());
        }

        adapter = new TicketAdapter(new ArrayList<>(), ticket -> {
            Intent intent = new Intent(this, KtvTicketDetailActivity.class);
            intent.putExtra("ticketId", ticket.getId());
            startActivity(intent);
        });
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Chờ xử lý"));
        tabLayout.addTab(tabLayout.newTab().setText("Hàng chờ"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang xử lý"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã xử lý"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: filterTrangThai = "ChoXuLy"; break;
                    case 1: filterTrangThai = "HangCho"; break;
                    case 2: filterTrangThai = "DangXuLy"; break;
                    case 3: filterTrangThai = "DaXuLy"; break;
                }
                taiTickets();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        taiTickets();
        batAutoStatus();
    }

    private void batStatusRTDB(String trangThai) {
        // Đồng bộ sang Firestore để SmartRouter đọc được
        db.collection("NguoiDung").document(user.getUid()).update("trangThai", trangThai);
        capNhatBadge(trangThai);

        // Khi KTV vừa chuyển sang Rảnh → ChatKhachHangActivity của khách tự detect và assign
        if (NguoiDung.TRANG_THAI_RAN.equals(trangThai)) {
            // Không cần làm gì thêm — phía khách đang lắng nghe realtime
        }

        if (statusRef == null) return;
        try {
            Map<String, Object> onlineData = new HashMap<>();
            onlineData.put("trangThai", trangThai);
            onlineData.put("lastSeen", System.currentTimeMillis());

            Map<String, Object> offlineData = new HashMap<>();
            offlineData.put("trangThai", NguoiDung.TRANG_THAI_OFFLINE);
            offlineData.put("lastSeen", System.currentTimeMillis());

            statusRef.onDisconnect().setValue(offlineData);
            statusRef.setValue(onlineData);
        } catch (Exception e) {
            // RTDB không khả dụng, bỏ qua
        }
    }

    private void batAutoStatus() {
        autoStatusListener = db.collection("YeuCauHoTro")
                .whereEqualTo("ktvUid", user.getUid())
                .whereIn("trangThai", java.util.Arrays.asList("ChoXuLy", "DangXuLy"))
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    String newStatus = snapshot.size() > 0 ? NguoiDung.TRANG_THAI_BAN : NguoiDung.TRANG_THAI_RAN;
                    batStatusRTDB(newStatus);
                });

        // Detect ticket mới -> notification
        db.collection("YeuCauHoTro")
                .whereEqualTo("ktvUid", user.getUid())
                .whereEqualTo("trangThai", "ChoXuLy")
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    int count = snapshot.size();
                    if (prevChoXuLyCount >= 0 && count > prevChoXuLyCount)
                        guiThongBao("Ticket mới!", "Bạn có " + count + " ticket chờ xử lý.");
                    prevChoXuLyCount = count;
                });
    }

    private void capNhatBadge(String trangThai) {
        if (tvTrangThaiKtv == null) return;
        switch (trangThai) {
            case NguoiDung.TRANG_THAI_RAN:
                tvTrangThaiKtv.setText("● Rảnh");
                tvTrangThaiKtv.setTextColor(0xFF10B981);
                tvTrangThaiKtv.setChipBackgroundColorResource(R.color.success_container);
                break;
            case NguoiDung.TRANG_THAI_BAN:
                tvTrangThaiKtv.setText("● Đang bận");
                tvTrangThaiKtv.setTextColor(0xFFEF4444);
                tvTrangThaiKtv.setChipBackgroundColorResource(R.color.error_container);
                break;
            default:
                tvTrangThaiKtv.setText("● Offline");
                tvTrangThaiKtv.setTextColor(0xFF6B7280);
                tvTrangThaiKtv.setChipBackgroundColorResource(R.color.surface_variant);
        }
    }

    private void moDialogChonTrangThai() {
        String[] options = {"Rảnh", "Bận", "Offline"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Chọn trạng thái")
                .setItems(options, (dialog, which) -> {
                    String ts = NguoiDung.TRANG_THAI_RAN;
                    if (which == 1) ts = NguoiDung.TRANG_THAI_BAN;
                    else if (which == 2) ts = NguoiDung.TRANG_THAI_OFFLINE;
                    batStatusRTDB(ts);
                })
                .show();
    }

    private void taiTickets() {
        if (ticketListener != null) ticketListener.remove();
        ticketListener = db.collection("YeuCauHoTro")
                .whereEqualTo("ktvUid", user.getUid())
                .whereEqualTo("trangThai", filterTrangThai)
                .addSnapshotListener((snapshot, e) -> {
                    if (snapshot == null) return;
                    List<YeuCauHoTro> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        YeuCauHoTro t = doc.toObject(YeuCauHoTro.class);
                        t.setId(doc.getId());
                        list.add(t);
                    }
                    adapter.capNhat(list);
                    tvSoTicket.setText(list.size() + " ticket");
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        dangXuatThucSu = false;
        if (user != null) {
            try { batStatusRTDB(NguoiDung.TRANG_THAI_RAN); } catch (Exception ignored) {}
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Chỉ set Offline khi app thực sự vào background (không phải khi chuyển màn hình trong app)
        // isFinishing() = true khi back ra, isChangingConfigurations() = true khi xoay màn hình
        // Không set Offline ở đây nữa — để RTDB onDisconnect tự xử lý khi mất kết nối
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Không set Offline ở đây nữa - tránh set Offline khi chuyển sang màn hình chat/detail
    }

    private void dangXuat() {
        dangXuatThucSu = true;
        try {
            if (statusRef != null) {
                Map<String, Object> offlineData = new HashMap<>();
                offlineData.put("trangThai", NguoiDung.TRANG_THAI_OFFLINE);
                statusRef.setValue(offlineData);
                statusRef.onDisconnect().cancel();
            }
        } catch (Exception ignored) {}
        db.collection("NguoiDung").document(user.getUid()).update("trangThai", NguoiDung.TRANG_THAI_OFFLINE);
        com.example.customercareproject.utils.StringeeManager.getInstance().reset();
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void taoNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Ticket", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(ch);
        }
    }

    private void guiThongBao(String title, String content) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title).setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) System.currentTimeMillis(), b.build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketListener != null) ticketListener.remove();
        if (autoStatusListener != null) autoStatusListener.remove();
    }
}
