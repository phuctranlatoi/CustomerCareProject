package com.example.customercareproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.MainActivity;
import com.example.customercareproject.R;
import com.example.customercareproject.ui.components.Material3Button;
import com.example.customercareproject.ui.components.Material3TextField;
import com.example.customercareproject.utils.AnimationHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private Material3TextField txtEmail, txtPassword;
    private Material3Button btnLogin, btnGoogleLogin;
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
        
        // Apply fade-in animation when screen loads
        LinearLayout loginContainer = findViewById(R.id.loginContainer);
        AnimationHelper.fadeIn(loginContainer);
    }

    private void setupViews() {

        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvRegister = findViewById(R.id.tvRegister);

        // Setup Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Setup inline email validation
        setupEmailValidation();

        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogleLogin.setOnClickListener(v -> googleSignInLauncher.launch(googleSignInClient.getSignInIntent()));
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
        tvRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
    
    /**
     * Setup inline email validation with real-time feedback
     * Only validates when user leaves the field (onFocusChange)
     */
    private void setupEmailValidation() {
        // Clear error when user starts typing
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
                // Don't validate here - too aggressive
            }
        });
        
        // Validate only when user leaves the field
        txtEmail.getEditText().setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = txtEmail.getText().trim();
                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    txtEmail.setError("Email không hợp lệ");
                }
            }
        });
    }

    private void loginWithEmail() {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();

        // Validate email
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Email không hợp lệ");
            return;
        }
        
        // Validate password
        if (password.length() < 6) {
            txtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }
        
        // Show loading state
        btnLogin.setLoading(true);
        
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = authResult.getUser();
                    if (user == null) {
                        btnLogin.setLoading(false);
                        return;
                    }

                    // Kiem tra role truoc khi enforce email verification
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("NguoiDung").document(user.getUid()).get()
                            .addOnSuccessListener(doc -> {
                                btnLogin.setLoading(false);
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
                            .addOnFailureListener(e -> {
                                btnLogin.setLoading(false);
                                goToMain();
                            });
                })
                .addOnFailureListener(e -> {
                    btnLogin.setLoading(false);
                    Toast.makeText(this, "Đăng nhập thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
