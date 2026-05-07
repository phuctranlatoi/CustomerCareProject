package com.example.customercareproject.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.utils.AnimationHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Material3TextField is a custom text field component that extends TextInputLayout with enhanced features:
 * - Floating label animation (built-in with TextInputLayout)
 * - Inline error message display with red color and shake animation
 * - Character counter support
 * - Password strength indicator for password fields
 * 
 * This component provides consistent text field styling and validation across the app,
 * following Material Design 3 guidelines.
 * 
 * Usage in XML:
 * <com.example.customercareproject.ui.components.Material3TextField
 *     android:id="@+id/txtEmail"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     android:hint="Email"
 *     app:counterEnabled="true"
 *     app:counterMaxLength="50"
 *     app:showPasswordStrength="false" />
 * 
 * Usage in Java:
 * Material3TextField textField = findViewById(R.id.txtEmail);
 * textField.setError("Invalid email format"); // Shows error with shake animation
 * textField.clearError(); // Clears error
 * String text = textField.getText(); // Gets text value
 */
public class Material3TextField extends TextInputLayout {
    
    private TextInputEditText editText;
    private boolean showPasswordStrength = false;
    private LinearLayout passwordStrengthContainer;
    private ProgressBar passwordStrengthBar;
    private TextView passwordStrengthLabel;
    
    // Password strength levels
    private enum PasswordStrength {
        WEAK(0, "Yếu", R.color.error),      // Red
        MEDIUM(1, "Trung bình", R.color.warning), // Orange
        STRONG(2, "Mạnh", R.color.success);   // Green
        
        private final int level;
        private final String label;
        private final int colorRes;
        
        PasswordStrength(int level, String label, int colorRes) {
            this.level = level;
            this.label = label;
            this.colorRes = colorRes;
        }
        
        public int getLevel() {
            return level;
        }
        
        public String getLabel() {
            return label;
        }
        
        public int getColorRes() {
            return colorRes;
        }
    }
    
    public Material3TextField(@NonNull Context context) {
        super(context);
        init(context, null);
    }
    
    public Material3TextField(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public Material3TextField(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    /**
     * Initialize the text field with Material Design 3 styling and features.
     */
    private void init(Context context, AttributeSet attrs) {
        // Create and add EditText
        editText = new TextInputEditText(context);
        editText.setLayoutParams(new LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ));
        addView(editText);
        
        // Apply Material Design 3 styling
        applyMaterial3Styling();
        
        // Parse custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Material3TextField);
            try {
                showPasswordStrength = a.getBoolean(R.styleable.Material3TextField_showPasswordStrength, false);
                
                // Apply hint if provided
                if (a.hasValue(R.styleable.Material3TextField_android_hint)) {
                    setHint(a.getString(R.styleable.Material3TextField_android_hint));
                }
                
                // Apply input type if provided
                if (a.hasValue(R.styleable.Material3TextField_android_inputType)) {
                    int inputType = a.getInt(R.styleable.Material3TextField_android_inputType, InputType.TYPE_CLASS_TEXT);
                    editText.setInputType(inputType);
                }
            } finally {
                a.recycle();
            }
            
            // Parse standard Android attributes
            TypedArray androidAttrs = context.obtainStyledAttributes(attrs, new int[]{
                android.R.attr.inputType,
                android.R.attr.minLines,
                android.R.attr.maxLength,
                android.R.attr.gravity,
                android.R.attr.enabled
            });
            try {
                // Input type
                int inputType = androidAttrs.getInt(0, InputType.TYPE_CLASS_TEXT);
                editText.setInputType(inputType);
                
                // Min lines
                int minLines = androidAttrs.getInt(1, -1);
                if (minLines > 0) {
                    editText.setMinLines(minLines);
                }
                
                // Max length
                int maxLength = androidAttrs.getInt(2, -1);
                if (maxLength > 0) {
                    editText.setFilters(new android.text.InputFilter[]{
                        new android.text.InputFilter.LengthFilter(maxLength)
                    });
                }
                
                // Gravity
                int gravity = androidAttrs.getInt(3, -1);
                if (gravity != -1) {
                    editText.setGravity(gravity);
                }
                
                // Enabled state
                boolean enabled = androidAttrs.getBoolean(4, true);
                editText.setEnabled(enabled);
                setEnabled(enabled);
            } finally {
                androidAttrs.recycle();
            }
        }
        
        // Setup password strength indicator if enabled
        if (showPasswordStrength) {
            setupPasswordStrengthIndicator();
        }
        
        // Setup error handling with shake animation
        setupErrorHandling();
    }
    
    /**
     * Apply Material Design 3 styling to the text field.
     */
    private void applyMaterial3Styling() {
        // Set box style to outlined (Material Design 3 default)
        setBoxBackgroundMode(BOX_BACKGROUND_OUTLINE);
        
        // Set corner radius to small (8dp) following MD3 shape system
        float cornerRadius = getResources().getDimension(R.dimen.shape_corner_small);
        setBoxCornerRadii(cornerRadius, cornerRadius, cornerRadius, cornerRadius);
        
        // Set box stroke color from theme
        int primaryColor = ContextCompat.getColor(getContext(), R.color.primary);
        setBoxStrokeColor(primaryColor);
        
        // Set error color
        int errorColor = ContextCompat.getColor(getContext(), R.color.error);
        setErrorTextColor(ColorStateList.valueOf(errorColor));
        
        // Set error icon tint if available (API 21+)
        try {
            setErrorIconTintList(ColorStateList.valueOf(errorColor));
        } catch (NoSuchMethodError e) {
            // Method not available in this version, skip
        }
        
        // Enable floating label animation (default behavior)
        setHintEnabled(true);
        setHintAnimationEnabled(true);
    }
    
    /**
     * Setup password strength indicator for password fields.
     */
    private void setupPasswordStrengthIndicator() {
        // Create container for password strength indicator
        passwordStrengthContainer = new LinearLayout(getContext());
        passwordStrengthContainer.setOrientation(LinearLayout.HORIZONTAL);
        passwordStrengthContainer.setPadding(
            dpToPx(4),
            dpToPx(4),
            dpToPx(4),
            0
        );
        
        // Create progress bar for visual strength indicator
        passwordStrengthBar = new ProgressBar(
            getContext(),
            null,
            android.R.attr.progressBarStyleHorizontal
        );
        passwordStrengthBar.setMax(2); // 0 = weak, 1 = medium, 2 = strong
        passwordStrengthBar.setProgress(0);
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
            0,
            dpToPx(4),
            1.0f
        );
        barParams.setMarginEnd(dpToPx(8));
        passwordStrengthBar.setLayoutParams(barParams);
        
        // Create label for strength text
        passwordStrengthLabel = new TextView(getContext());
        passwordStrengthLabel.setTextSize(12);
        passwordStrengthLabel.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        
        // Add views to container
        passwordStrengthContainer.addView(passwordStrengthBar);
        passwordStrengthContainer.addView(passwordStrengthLabel);
        
        // Add container to text field
        addView(passwordStrengthContainer);
        
        // Initially hide the indicator
        passwordStrengthContainer.setVisibility(View.GONE);
        
        // Add text watcher to update strength indicator
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    
    /**
     * Setup error handling with shake animation.
     */
    private void setupErrorHandling() {
        // Error handling is done through setError() method
        // Shake animation will be triggered when error is set
    }
    
    /**
     * Update password strength indicator based on password text.
     * 
     * Password strength criteria:
     * - Weak: < 6 characters
     * - Medium: 6-8 characters OR has letters and numbers
     * - Strong: > 8 characters AND has letters, numbers, and special characters
     * 
     * @param password The password text
     */
    private void updatePasswordStrength(String password) {
        if (password.isEmpty()) {
            passwordStrengthContainer.setVisibility(View.GONE);
            return;
        }
        
        passwordStrengthContainer.setVisibility(View.VISIBLE);
        
        PasswordStrength strength = calculatePasswordStrength(password);
        
        // Update progress bar
        passwordStrengthBar.setProgress(strength.getLevel());
        int color = ContextCompat.getColor(getContext(), strength.getColorRes());
        passwordStrengthBar.setProgressTintList(ColorStateList.valueOf(color));
        
        // Update label
        passwordStrengthLabel.setText(strength.getLabel());
        passwordStrengthLabel.setTextColor(color);
    }
    
    /**
     * Calculate password strength based on various criteria.
     * 
     * @param password The password text
     * @return PasswordStrength level
     */
    private PasswordStrength calculatePasswordStrength(String password) {
        if (password.length() < 6) {
            return PasswordStrength.WEAK;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        int criteriaCount = 0;
        if (hasLetter) criteriaCount++;
        if (hasDigit) criteriaCount++;
        if (hasSpecial) criteriaCount++;
        
        if (password.length() > 8 && criteriaCount >= 3) {
            return PasswordStrength.STRONG;
        } else if (password.length() >= 6 && criteriaCount >= 2) {
            return PasswordStrength.MEDIUM;
        } else {
            return PasswordStrength.WEAK;
        }
    }
    
    /**
     * Convert dp to pixels.
     * 
     * @param dp Value in dp
     * @return Value in pixels
     */
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    
    // ========================================
    // Public API
    // ========================================
    
    /**
     * Set error message and trigger shake animation.
     * 
     * @param error Error message to display
     */
    @Override
    public void setError(@Nullable CharSequence error) {
        super.setError(error);
        
        if (error != null && error.length() > 0) {
            // Trigger shake animation
            AnimationHelper.shake(this);
        }
    }
    
    /**
     * Clear error message.
     */
    public void clearError() {
        setError(null);
        setErrorEnabled(false);
    }
    
    /**
     * Get the text value from the edit text.
     * 
     * @return Text value as String
     */
    public String getText() {
        Editable editable = editText.getText();
        return editable != null ? editable.toString() : "";
    }
    
    /**
     * Set the text value in the edit text.
     * 
     * @param text Text to set
     */
    public void setText(String text) {
        editText.setText(text);
    }
    
    /**
     * Get the EditText component for advanced customization.
     * 
     * @return TextInputEditText instance
     */
    public TextInputEditText getEditText() {
        return editText;
    }
    
    /**
     * Set whether to show password strength indicator.
     * Only applicable for password input types.
     * 
     * @param show true to show indicator, false to hide
     */
    public void setShowPasswordStrength(boolean show) {
        if (this.showPasswordStrength != show) {
            this.showPasswordStrength = show;
            
            if (show && passwordStrengthContainer == null) {
                setupPasswordStrengthIndicator();
            } else if (!show && passwordStrengthContainer != null) {
                passwordStrengthContainer.setVisibility(View.GONE);
            }
        }
    }
    
    /**
     * Check if password strength indicator is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isShowPasswordStrength() {
        return showPasswordStrength;
    }
    
    /**
     * Set input type for the edit text.
     * 
     * @param type Input type constant from InputType class
     */
    public void setInputType(int type) {
        editText.setInputType(type);
    }
    
    /**
     * Get current input type.
     * 
     * @return Input type constant
     */
    public int getInputType() {
        return editText.getInputType();
    }
    
    /**
     * Set whether the text field is enabled.
     * 
     * @param enabled true to enable, false to disable
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (editText != null) {
            editText.setEnabled(enabled);
        }
    }
    
    /**
     * Add a text watcher to the edit text.
     * 
     * @param watcher TextWatcher instance
     */
    public void addTextChangedListener(TextWatcher watcher) {
        editText.addTextChangedListener(watcher);
    }
    
    /**
     * Remove a text watcher from the edit text.
     * 
     * @param watcher TextWatcher instance
     */
    public void removeTextChangedListener(TextWatcher watcher) {
        editText.removeTextChangedListener(watcher);
    }
}
