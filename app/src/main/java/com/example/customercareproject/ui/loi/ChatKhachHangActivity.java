package com.example.customercareproject.ui.loi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.TinNhan;
import com.example.customercareproject.ui.ktv.ChatAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatKhachHangActivity extends AppCompatActivity {

    private String ticketId, hoTen;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private ChatAdapter chatAdapter;
    private ListenerRegistration chatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_khach_hang);

        ticketId = getIntent().getStringExtra("ticketId");
        hoTen = getIntent().getStringExtra("hoTen");

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Chat hỗ trợ");
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView rvChat = findViewById(R.id.rvChat);
        chatAdapter = new ChatAdapter(new ArrayList<>(), user != null ? user.getUid() : "");
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        TextInputEditText edtTinNhan = findViewById(R.id.edtTinNhan);
        Button btnGui = findViewById(R.id.btnGui);

        btnGui.setOnClickListener(v -> {
            String nd = edtTinNhan.getText() != null ? edtTinNhan.getText().toString().trim() : "";
            if (nd.isEmpty() || user == null) return;
            TinNhan tin = new TinNhan(ticketId, user.getUid(), hoTen != null ? hoTen : "Khách hàng",
                    "KhachHang", nd);
            db.collection("TinNhan").add(tin)
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi tin", Toast.LENGTH_SHORT).show());
            edtTinNhan.setText("");
        });

        chatListener = db.collection("TinNhan")
                .whereEqualTo("ticketId", ticketId)
                .orderBy("thoiGian", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;
                    List<TinNhan> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) {
                        TinNhan t = doc.toObject(TinNhan.class);
                        t.setId(doc.getId());
                        list.add(t);
                    }
                    chatAdapter.capNhat(list);
                    if (!list.isEmpty()) rvChat.scrollToPosition(list.size() - 1);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) chatListener.remove();
    }
}
