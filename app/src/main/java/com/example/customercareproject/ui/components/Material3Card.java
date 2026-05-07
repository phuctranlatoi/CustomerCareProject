package com.example.customercareproject.ui.components;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.utils.AnimationHelper;
import com.google.android.material.card.MaterialCardView;

/**
 * Material3Card is a custom card component that extends MaterialCardView with enhanced features:
 * - Hover effect with elevation animation
 * - Press animation (scale to 0.98)
 * - Corner radius size presets (small, medium, large)
 * - Automatic ripple effect
 * 
 * This component provides consistent card styling and animations across the app,
 * following Material Design 3 guidelines.
 * 
 * Usage in XML:
 * <com.example.customercareproject.ui.components.Material3Card
 *     android:id="@+id/card"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:cornerRadiusSize="medium"
 *     app:enableHoverEffect="true" />
 */
public class Material3Card extends MaterialCardView {
    
    private static final float HOVER_ELEVATION_DP = 8f;
    private static final float DEFAULT_ELEVATION_DP = 2f;
    private static final long HOVER_ANIMATION_DURATION = 150L;
    
    private boolean enableHoverEffect = false;
    private float originalElevation;
    private float hoverElevation;
    
    public Material3Card(@NonNull Context context) {
        super(context);
        init(context, null);
    }
    
    public Material3Card(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public Material3Card(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    /**
     * Initialize the card with Material Design 3 styling and animations.
     */
    private void init(Context context, AttributeSet attrs) {
        // Apply Material Design 3 defaults
        applyMaterial3Defaults();
        
        // Parse custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Material3Card);
            try {
                // Corner radius size
                int cornerRadiusSize = a.getInt(R.styleable.Material3Card_cornerRadiusSize, 1); // medium = 1
                setCornerRadiusSize(cornerRadiusSize);
                
                // Hover effect
                enableHoverEffect = a.getBoolean(R.styleable.Material3Card_enableHoverEffect, false);
                
            } finally {
                a.recycle();
            }
        }
        
        // Setup elevations
        originalElevation = getCardElevation();
        hoverElevation = dpToPx(HOVER_ELEVATION_DP);
        
        // Setup hover and press effects
        if (enableHoverEffect) {
            setupHoverEffect();
        }
        setupPressAnimation();
    }
    
    /**
     * Apply Material Design 3 default styling.
     */
    private void applyMaterial3Defaults() {
        // Set default elevation
        setCardElevation(dpToPx(DEFAULT_ELEVATION_DP));
        
        // Set default background color from theme
        setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.surface_container_low));
        
        // Enable ripple effect
        setClickable(true);
        setFocusable(true);
        
        // Set default corner radius
        setRadius(getResources().getDimension(R.dimen.shape_corner_medium));
    }
    
    /**
     * Set corner radius based on Material Design 3 size presets.
     * 
     * @param size 0 = small, 1 = medium, 2 = large
     */
    public void setCornerRadiusSize(int size) {
        float radius;
        switch (size) {
            case 0: // small
                radius = getResources().getDimension(R.dimen.shape_corner_small);
                break;
            case 2: // large
                radius = getResources().getDimension(R.dimen.shape_corner_large);
                break;
            case 1: // medium
            default:
                radius = getResources().getDimension(R.dimen.shape_corner_medium);
                break;
        }
        setRadius(radius);
    }
    
    /**
     * Setup hover effect with elevation animation.
     */
    private void setupHoverEffect() {
        setOnHoverListener(new OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        animateElevation(hoverElevation);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        animateElevation(originalElevation);
                        break;
                }
                return false;
            }
        });
    }
    
    /**
     * Setup press animation that scales the card to 0.98 when pressed.
     */
    private void setupPressAnimation() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!isEnabled()) {
                    return false;
                }
                
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Scale down when pressed
                        AnimationHelper.scalePress(Material3Card.this);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Scale back to normal when released
                        AnimationHelper.scaleRelease(Material3Card.this);
                        break;
                }
                
                // Return false to allow click event to propagate
                return false;
            }
        });
    }
    
    /**
     * Animate elevation change.
     * 
     * @param targetElevation Target elevation in pixels
     */
    private void animateElevation(float targetElevation) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "cardElevation", getCardElevation(), targetElevation);
        animator.setDuration(HOVER_ANIMATION_DURATION);
        animator.start();
    }
    
    /**
     * Convert dp to pixels.
     * 
     * @param dp Value in dp
     * @return Value in pixels
     */
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
    
    /**
     * Set whether hover effect is enabled.
     * 
     * @param enabled true to enable hover effect, false to disable
     */
    public void setHoverEffectEnabled(boolean enabled) {
        this.enableHoverEffect = enabled;
        if (enabled) {
            setupHoverEffect();
        } else {
            setOnHoverListener(null);
        }
    }
    
    /**
     * Check if hover effect is enabled.
     * 
     * @return true if enabled, false otherwise
     */
    public boolean isHoverEffectEnabled() {
        return enableHoverEffect;
    }
}