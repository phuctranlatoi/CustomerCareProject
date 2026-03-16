package com.example.customercareproject.ui;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private com.google.android.material.textfield.TextInputEditText edtEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        edtEmail = findViewById(R.id.edtEmail);
        Button btnGuiEmail = findViewById(R.id.btnGuiEmail);
        TextView tvQuayLai = findViewById(R.id.tvQuayLai);

        btnGuiEmail.setOnClickListener(v -> guiEmailDatLaiMatKhau());
        tvQuayLai.setOnClickListener(v -> finish());
    }

    private void guiEmailDatLaiMatKhau() {
        String email = edtEmail.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Vui lòng nhập email hợp lệ");
            return;
        }

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this,
                                "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư!",
                                Toast.LENGTH_LONG).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
