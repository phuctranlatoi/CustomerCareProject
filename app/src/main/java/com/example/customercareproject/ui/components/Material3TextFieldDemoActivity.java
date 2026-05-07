package com.example.customercareproject.ui.components;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.customercareproject.R;

/**
 * Demo activity showcasing Material3TextField component features:
 * - Basic text field with validation
 * - Email field with email validation
 * - Password field with strength indicator
 * - Phone number field with formatting
 * - Multi-line text field with character counter
 */
public class Material3TextFieldDemoActivity extends AppCompatActivity {
    
    private Material3TextField txtName;
    private Material3TextField txtEmail;
    private Material3TextField txtPassword;
    private Material3TextField txtPhone;
    private Material3TextField txtDescription;
    private Button btnSubmit;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material3_textfield_demo);
        
        // Initialize views
        initViews();
        
        // Setup validation
        setupValidation();
        
        // Setup submit button
        setupSubmitButton();
    }
    
    /**
     * Initialize all views.
     */
    private void initViews() {
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtPhone = findViewById(R.id.txtPhone);
        txtDescription = findViewById(R.id.txtDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
    }
    
    /**
     * Setup validation for all text fields.
     */
    private void setupValidation() {
        // Name validation
        txtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateName(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Email validation
        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmail(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Password validation
        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePassword(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Phone validation
        txtPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhone(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Description validation
        txtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateDescription(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    /**
     * Validate name field.
     */
    private boolean validateName(String name) {
        if (name.isEmpty()) {
            txtName.setError("Tên không được để trống");
            return false;
        } else if (name.length() < 2) {
            txtName.setError("Tên phải có ít nhất 2 ký tự");
            return false;
        } else {
            txtName.clearError();
            return true;
        }
    }
    
    /**
     * Validate email field.
     */
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            txtEmail.setError("Email không được để trống");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtEmail.setError("Định dạng email không hợp lệ");
            return false;
        } else {
            txtEmail.clearError();
            return true;
        }
    }
    
    /**
     * Validate password field.
     */
    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            txtPassword.setError("Mật khẩu không được để trống");
            return false;
        } else if (password.length() < 6) {
            txtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        } else {
            txtPassword.clearError();
            return true;
        }
    }
    
    /**
     * Validate phone field.
     */
    private boolean validatePhone(String phone) {
        if (phone.isEmpty()) {
            txtPhone.setError("Số điện thoại không được để trống");
            return false;
        } else if (phone.length() < 10) {
            txtPhone.setError("Số điện thoại phải có ít nhất 10 số");
            return false;
        } else {
            txtPhone.clearError();
            return true;
        }
    }
    
    /**
     * Validate description field.
     */
    private boolean validateDescription(String description) {
        if (description.isEmpty()) {
            txtDescription.setError("Mô tả không được để trống");
            return false;
        } else if (description.length() < 10) {
            txtDescription.setError("Mô tả phải có ít nhất 10 ký tự");
            return false;
        } else {
            txtDescription.clearError();
            return true;
        }
    }
    
    /**
     * Setup submit button click listener.
     */
    private void setupSubmitButton() {
        btnSubmit.setOnClickListener(v -> {
            // Validate all fields
            boolean isNameValid = validateName(txtName.getText());
            boolean isEmailValid = validateEmail(txtEmail.getText());
            boolean isPasswordValid = validatePassword(txtPassword.getText());
            boolean isPhoneValid = validatePhone(txtPhone.getText());
            boolean isDescriptionValid = validateDescription(txtDescription.getText());
            
            if (isNameValid && isEmailValid && isPasswordValid && isPhoneValid && isDescriptionValid) {
                // All fields are valid
                Toast.makeText(this, "Form submitted successfully!", Toast.LENGTH_SHORT).show();
                
                // Clear all fields
                txtName.setText("");
                txtEmail.setText("");
                txtPassword.setText("");
                txtPhone.setText("");
                txtDescription.setText("");
            } else {
                // Show error message
                Toast.makeText(this, "Please fix the errors before submitting", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
