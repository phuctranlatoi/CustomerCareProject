package com.example.customercareproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.model.NguoiDung;
import com.example.customercareproject.ui.LoginActivity;
import com.example.customercareproject.ui.admin.AdminDashboardActivity;
import com.example.customercareproject.ui.home.HomeActivity;
import com.example.customercareproject.ui.ktv.KtvDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Đọc vaiTro từ Firestore để route đúng dashboard
        FirebaseFirestore.getInstance().collection("NguoiDung")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    String vaiTro = doc.getString("vaiTro");
                    if (vaiTro == null) vaiTro = NguoiDung.VAI_TRO_KHACH_HANG;

                    Intent intent;
                    switch (vaiTro) {
                        case NguoiDung.VAI_TRO_KTV:
                            intent = new Intent(this, KtvDashboardActivity.class);
                            break;
                        case NguoiDung.VAI_TRO_ADMIN:
                            intent = new Intent(this, AdminDashboardActivity.class);
                            break;
                        default:
                            intent = new Intent(this, HomeActivity.class);
                            break;
                    }
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                });
    }
}
