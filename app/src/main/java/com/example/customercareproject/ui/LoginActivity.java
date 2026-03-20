package com.example.customercareproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.MainActivity;
import com.example.customercareproject.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText edtEmail, edtPassword;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    android.util.Log.d("GoogleLogin", "Got account: " + account.getEmail() + ", idToken null? " + (account.getIdToken() == null));
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    android.util.Log.e("GoogleLogin", "ApiException code: " + e.getStatusCode(), e);
                    Toast.makeText(this, "Google sign-in thất bại (code " + e.getStatusCode() + "): " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMain();
            return;
        }

        setContentView(R.layout.activity_login);
        setupViews();
    }

    private void setupViews() {

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // Setup Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogleLogin.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void loginWithEmail() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không hợp lệ");
            return;
        }
        if (password.length() < 6) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) return;

                    // Kiem tra role truoc khi enforce email verification
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("NguoiDung").document(user.getUid()).get()
                            .addOnSuccessListener(doc -> {
                                // String vaiTro = doc.getString("vaiTro");
                                // boolean isAdminOrKtv = "Admin".equals(vaiTro) || "KTV".equals(vaiTro);
                                // if (!isAdminOrKtv && !user.isEmailVerified()) {
                                //     mAuth.signOut();
                                //     Toast.makeText(this, "Vui lòng xác thực email trước khi đăng nhập!", Toast.LENGTH_LONG).show();
                                // } else {
                                //     goToMain();
                                // }
                                goToMain();
                            })
                            .addOnFailureListener(e -> goToMain());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void firebaseAuthWithGoogle(String idToken) {
        if (idToken == null) {
            Toast.makeText(this, "Lỗi: không lấy được ID Token từ Google", Toast.LENGTH_LONG).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> goToMain())
                .addOnFailureListener(e -> {
                    android.util.Log.e("GoogleLogin", "signInWithCredential failed", e);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
