package com.example.customercareproject.ui.ktv;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.NguoiDung;
import com.example.customercareproject.model.YeuCauHoTro;
import com.example.customercareproject.ui.LoginActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class KtvDashboardActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseUser user;
    private TicketAdapter adapter;
    private ListenerRegistration ticketListener;
    private String filterTrangThai = "ChoXuLy";
    private TextView tvSoTicket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ktv_dashboard);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { startActivity(new Intent(this, LoginActivity.class)); finish(); return; }

        tvSoTicket = findViewById(R.id.tvSoTicket);
        TextView tvTenKtv = findViewById(R.id.tvTenKtv);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        Spinner spinnerTrangThai = findViewById(R.id.spinnerTrangThai);
        RecyclerView rvTickets = findViewById(R.id.rvTickets);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        db.collection("NguoiDung").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    tvTenKtv.setText("KTV: " + doc.getString("hoTen"));
                    // Cập nhật trạng thái KTV
                    String trangThai = doc.getString("trangThai");
                    String[] options = {NguoiDung.TRANG_THAI_RAN, NguoiDung.TRANG_THAI_BAN, NguoiDung.TRANG_THAI_OFFLINE};
                    String[] labels = {"Rảnh", "Đang bận", "Offline"};
                    ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
                    spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerTrangThai.setAdapter(spAdapter);
                    // Set current selection
                    for (int i = 0; i < options.length; i++) {
                        if (options[i].equals(trangThai)) { spinnerTrangThai.setSelection(i); break; }
                    }
                    spinnerTrangThai.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                            db.collection("NguoiDung").document(user.getUid())
                                    .update("trangThai", options[position]);
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });
                });

        btnLogout.setOnClickListener(v -> {
            db.collection("NguoiDung").document(user.getUid()).update("trangThai", NguoiDung.TRANG_THAI_OFFLINE);
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        adapter = new TicketAdapter(new ArrayList<>(), ticket -> {
            Intent intent = new Intent(this, KtvTicketDetailActivity.class);
            intent.putExtra("ticketId", ticket.getId());
            startActivity(intent);
        });
        rvTickets.setLayoutManager(new LinearLayoutManager(this));
        rvTickets.setAdapter(adapter);

        // Tabs
        tabLayout.addTab(tabLayout.newTab().setText("Chờ xử lý"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang xử lý"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã xử lý"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0: filterTrangThai = "ChoXuLy"; break;
                    case 1: filterTrangThai = "DangXuLy"; break;
                    case 2: filterTrangThai = "DaXuLy"; break;
                }
                taiTickets();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        taiTickets();
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
                    tvSoTicket.setText(list.size() + " ticket " + filterTrangThai.toLowerCase());
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ticketListener != null) ticketListener.remove();
    }
}
