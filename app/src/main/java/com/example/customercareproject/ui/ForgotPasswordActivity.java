package com.example.customercareproject.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;
import com.example.customercareproject.ui.components.Material3Button;
import com.example.customercareproject.ui.components.Material3TextField;
import com.example.customercareproject.utils.AnimationHelper;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private Material3TextField txtEmail;
    private Material3Button btnGuiEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        setupViews();
        
        // Apply fade-in animation when screen loads
        LinearLayout forgotPasswordContainer = findViewById(R.id.forgotPasswordContainer);
        AnimationHelper.fadeIn(forgotPasswordContainer);
    }

    private void setupViews() {
        txtEmail = findViewById(R.id.txtEmail);
        btnGuiEmail = findViewById(R.id.btnGuiEmail);
        TextView tvQuayLai = findViewById(R.id.tvQuayLai);

        // Setup inline email validation
        setupEmailValidation();

        btnGuiEmail.setOnClickListener(v -> guiEmailDatLaiMatKhau());
        tvQuayLai.setOnClickListener(v -> finish());
    }

    /**
     * Setup inline email validation with real-time feedback
     */
    private void setupEmailValidation() {
        txtEmail.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Clear error when user starts typing
                if (txtEmail.getError() != null) {
                    txtEmail.clearError();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Validate email format after user finishes typing
                String email = s.toString().trim();
                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    txtEmail.setError("Email không hợp lệ");
                }
            }
        });
    }

    private void guiEmailDatLaiMatKhau() {
        String email = txtEmail.getText().trim();

        // Validate email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Vui lòng nhập email hợp lệ");
            return;
        }

        // Show loading state
        btnGuiEmail.setLoading(true);

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    btnGuiEmail.setLoading(false);
                    Toast.makeText(this,
                            "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư!",
                            Toast.LENGTH_LONG).show();
                    // Optionally navigate back to login after success
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnGuiEmail.setLoading(false);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
