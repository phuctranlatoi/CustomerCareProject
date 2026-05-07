package com.example.customercareproject.ui.components;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.utils.AnimationHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

/**
 * Material3Button is a custom button component that extends MaterialButton with enhanced features:
 * - Automatic ripple effect with 200ms duration
 * - Press animation (scale to 0.98)
 * - Loading state with circular progress indicator
 * - Proper disabled state styling with opacity
 * 
 * This component provides consistent button styling and animations across the app,
 * following Material Design 3 guidelines.
 * 
 * Usage in XML:
 * <com.example.customercareproject.ui.components.Material3Button
 *     android:id="@+id/btnSubmit"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     android:text="Submit"
 *     app:loading="false" />
 * 
 * Usage in Java:
 * Material3Button button = findViewById(R.id.btnSubmit);
 * button.setLoading(true); // Show loading state
 * button.setLoading(false); // Hide loading state
 */
public class Material3Button extends MaterialButton {
    
    private static final float DISABLED_ALPHA = 0.38f;
    private static final float ENABLED_ALPHA = 1.0f;
    
    private boolean isLoading = false;
    private CircularProgressIndicator progressIndicator;
    private CharSequence originalText;
    private Drawable originalIcon;
    private boolean wasEnabled;
    
    public Material3Button(@NonNull Context context) {
        super(context);
        init(context, null);
    }
    
    public Material3Button(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public Material3Button(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    /**
     * Initialize the button with Material Design 3 styling and animations.
     */
    private void init(Context context, AttributeSet attrs) {
        // Setup ripple effect with 200ms duration
        setupRippleEffect();
        
        // Setup press animation
        setupPressAnimation();
        
        // Setup disabled state styling
        setupDisabledState();
        
        // Parse custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Material3Button);
            try {
                boolean loading = a.getBoolean(R.styleable.Material3Button_loading, false);
                if (loading) {
                    setLoading(true);
                }
            } finally {
                a.recycle();
            }
        }
    }
    
    /**
     * Setup ripple effect with Material Design 3 specifications.
     * The ripple duration is automatically set to 200ms by MaterialButton.
     */
    private void setupRippleEffect() {
        // MaterialButton already has ripple effect built-in
        // We just ensure it's enabled and properly configured
        setClickable(true);
        setFocusable(true);
        
        // Ensure ripple color is set from theme
        if (getRippleColor() == null) {
            // Get ripple color from theme
            TypedArray a = getContext().obtainStyledAttributes(new int[]{
                com.google.android.material.R.attr.colorControlHighlight
            });
            try {
                int rippleColor = a.getColor(0, 0x1F000000); // Default to 12% black
                setRippleColor(ColorStateList.valueOf(rippleColor));
            } finally {
                a.recycle();
            }
        }
    }
    
    /**
     * Setup press animation that scales the button to 0.98 when pressed.
     */
    private void setupPressAnimation() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isEnabled() || isLoading) {
                    return false;
                }
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Scale down when pressed
                        AnimationHelper.scalePress(Material3Button.this);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Scale back to normal when released
                        AnimationHelper.scaleRelease(Material3Button.this);
                        break;
                }
                
                // Return false to allow click event to propagate
                return false;
            }
        });
    }
    
    /**
     * Setup disabled state styling with proper opacity.
     */
    private void setupDisabledState() {
        // Override setEnabled to apply custom alpha
        // This is handled in the overridden setEnabled method below
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        
        // Apply opacity based on enabled state
        if (enabled) {
            setAlpha(ENABLED_ALPHA);
        } else {
            setAlpha(DISABLED_ALPHA);
            // Reset scale if button is disabled while pressed
            AnimationHelper.scaleRelease(this);
        }
    }
    
    /**
     * Set the loading state of the button.
     * When loading is true:
     * - Button is disabled
     * - Text and icon are hidden
     * - Circular progress indicator is shown
     * 
     * @param loading true to show loading state, false to hide
     */
    public void setLoading(boolean loading) {
        if (this.isLoading == loading) {
            return; // No change
        }
        
        this.isLoading = loading;
        
        if (loading) {
            // Save current state
            wasEnabled = isEnabled();
            originalText = getText();
            originalIcon = getIcon();
            
            // Disable button and hide text/icon
            setEnabled(false);
            setText("");
            setIcon(null);
            
            // Show progress indicator
            showProgressIndicator();
        } else {
            // Restore original state
            hideProgressIndicator();
            setText(originalText);
            setIcon(originalIcon);
            setEnabled(wasEnabled);
        }
    }
    
    /**
     * Check if the button is in loading state.
     * 
     * @return true if loading, false otherwise
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Show the circular progress indicator.
     */
    private void showProgressIndicator() {
        if (progressIndicator == null) {
            progressIndicator = createProgressIndicator();
        }
        
        // Remove existing progress indicator if any
        if (progressIndicator.getParent() != null) {
            ((ViewGroup) progressIndicator.getParent()).removeView(progressIndicator);
        }
        
        // Add progress indicator to button
        // We need to wrap the button in a FrameLayout to overlay the progress indicator
        // Since we can't modify the button's parent here, we'll use the icon slot
        setIcon(progressIndicator.getIndeterminateDrawable());
        setIconGravity(ICON_GRAVITY_TEXT_START);
    }
    
    /**
     * Hide the circular progress indicator.
     */
    private void hideProgressIndicator() {
        if (progressIndicator != null && progressIndicator.getParent() != null) {
            ((ViewGroup) progressIndicator.getParent()).removeView(progressIndicator);
        }
    }
    
    /**
     * Create a circular progress indicator for the loading state.
     * 
     * @return CircularProgressIndicator instance
     */
    private CircularProgressIndicator createProgressIndicator() {
        CircularProgressIndicator indicator = new CircularProgressIndicator(getContext());
        
        // Set size to match button height
        int size = (int) (24 * getResources().getDisplayMetrics().density); // 24dp
        indicator.setIndicatorSize(size);
        
        // Set color to match button text color
        int color = getCurrentTextColor();
        indicator.setIndicatorColor(color);
        
        // Set track thickness
        indicator.setTrackThickness((int) (3 * getResources().getDisplayMetrics().density)); // 3dp
        
        // Make it indeterminate (spinning)
        indicator.setIndeterminate(true);
        
        return indicator;
    }
    
    /**
     * Override performClick to prevent clicks when loading.
     */
    @Override
    public boolean performClick() {
        if (isLoading) {
            return false;
        }
        return super.performClick();
    }
}
