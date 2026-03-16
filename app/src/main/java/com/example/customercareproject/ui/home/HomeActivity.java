package com.example.customercareproject.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customercareproject.R;
import com.example.customercareproject.model.SanPham;
import com.example.customercareproject.ui.LoginActivity;
import com.example.customercareproject.ui.danhgia.DanhGiaActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

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

        TextView tvChaoMung = findViewById(R.id.tvChaoMung);
        TextView tvEmail = findViewById(R.id.tvEmail);
        ImageButton btnLogout = findViewById(R.id.btnLogout);
        RecyclerView rvSanPham = findViewById(R.id.rvSanPham);

        // Lấy tên từ Firestore
        FirebaseFirestore.getInstance().collection("NguoiDung")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String hoTen = doc.getString("hoTen");
                    tvChaoMung.setText("Xin chào, " + (hoTen != null ? hoTen : "bạn") + "!");
                });
        tvEmail.setText(user.getEmail());

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        SanPhamAdapter adapter = new SanPhamAdapter(SanPham.DANH_SACH, tenSanPham -> {
            Intent intent = new Intent(this, DanhGiaActivity.class);
            intent.putExtra("sanPham", tenSanPham);
            startActivity(intent);
        });

        rvSanPham.setLayoutManager(new LinearLayoutManager(this));
        rvSanPham.setAdapter(adapter);
    }
}
