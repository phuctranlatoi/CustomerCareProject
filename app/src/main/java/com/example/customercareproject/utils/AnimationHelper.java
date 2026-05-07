package com.example.customercareproject.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

/**
 * AnimationHelper provides centralized animation utilities for consistent motion across the app.
 * All animations follow Material Design 3 Motion guidelines with Material Motion easing curves.
 * 
 * Animation Durations:
 * - FAST: 100ms (micro-interactions like button press)
 * - STANDARD: 200ms (button presses, ripples)
 * - MEDIUM: 300ms (screen transitions)
 * - SLOW: 400ms (complex animations)
 * 
 * Easing Curve: Material Motion standard curve (cubic-bezier(0.4, 0.0, 0.2, 1))
 * This is approximated using FastOutSlowInInterpolator in Android.
 */
public class AnimationHelper {
    
    // Animation duration constants (in milliseconds)
    public static final int DURATION_FAST = 100;
    public static final int DURATION_STANDARD = 200;
    public static final int DURATION_MEDIUM = 300;
    public static final int DURATION_SLOW = 400;
    
    // Material Motion easing curve - standard curve (cubic-bezier(0.4, 0.0, 0.2, 1))
    // FastOutSlowInInterpolator is the closest Android equivalent
    private static final Interpolator MATERIAL_MOTION_EASING = new FastOutSlowInInterpolator();
    
    // Deceleration curve for slide-up animations
    private static final Interpolator DECELERATION_EASING = new DecelerateInterpolator();
    
    // Standard interpolator for general animations
    private static final Interpolator STANDARD_EASING = new AccelerateDecelerateInterpolator();
    
    // Scale values for press animations
    private static final float SCALE_PRESSED = 0.98f;
    private static final float SCALE_NORMAL = 1.0f;
    
    // Stagger delay for list items (in milliseconds)
    private static final int STAGGER_DELAY = 50;
    
    /**
     * Fade in animation - animates view from transparent to opaque.
     * 
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     */
    public static void fadeIn(View view, int duration) {
        if (view == null) return;
        
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(MATERIAL_MOTION_EASING)
            .setListener(null)
            .start();
    }
    
    /**
     * Fade in animation with default MEDIUM duration (300ms).
     * 
     * @param view The view to animate
     */
    public static void fadeIn(View view) {
        fadeIn(view, DURATION_MEDIUM);
    }
    
    /**
     * Fade out animation - animates view from opaque to transparent.
     * 
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     */
    public static void fadeOut(View view, int duration) {
        if (view == null) return;
        
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(MATERIAL_MOTION_EASING)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            })
            .start();
    }
    
    /**
     * Fade out animation with default MEDIUM duration (300ms).
     * 
     * @param view The view to animate
     */
    public static void fadeOut(View view) {
        fadeOut(view, DURATION_MEDIUM);
    }
    
    /**
     * Scale press animation - scales view down to 0.98 when pressed.
     * Use this for button press micro-interactions.
     * 
     * @param view The view to animate
     */
    public static void scalePress(View view) {
        if (view == null) return;
        
        view.animate()
            .scaleX(SCALE_PRESSED)
            .scaleY(SCALE_PRESSED)
            .setDuration(DURATION_FAST)
            .setInterpolator(MATERIAL_MOTION_EASING)
            .start();
    }
    
    /**
     * Scale release animation - scales view back to normal size (1.0).
     * Use this after scalePress when button is released.
     * 
     * @param view The view to animate
     */
    public static void scaleRelease(View view) {
        if (view == null) return;
        
        view.animate()
            .scaleX(SCALE_NORMAL)
            .scaleY(SCALE_NORMAL)
            .setDuration(DURATION_FAST)
            .setInterpolator(MATERIAL_MOTION_EASING)
            .start();
    }
    
    /**
     * Slide up animation - slides view up from below with deceleration curve.
     * Commonly used for bottom sheets and dialogs.
     * 
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     */
    public static void slideUp(View view, int duration) {
        if (view == null) return;
        
        view.setVisibility(View.VISIBLE);
        view.setTranslationY(view.getHeight());
        view.setAlpha(0f);
        
        view.animate()
            .translationY(0)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DECELERATION_EASING)
            .setListener(null)
            .start();
    }
    
    /**
     * Slide up animation with default MEDIUM duration (300ms).
     * 
     * @param view The view to animate
     */
    public static void slideUp(View view) {
        slideUp(view, DURATION_MEDIUM);
    }
    
    /**
     * Slide down animation - slides view down and fades out.
     * Commonly used for dismissing bottom sheets and dialogs.
     * 
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     */
    public static void slideDown(View view, int duration) {
        if (view == null) return;
        
        view.animate()
            .translationY(view.getHeight())
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DECELERATION_EASING)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                    view.setTranslationY(0); // Reset for next use
                }
            })
            .start();
    }
    
    /**
     * Slide down animation with default MEDIUM duration (300ms).
     * 
     * @param view The view to animate
     */
    public static void slideDown(View view) {
        slideDown(view, DURATION_MEDIUM);
    }
    
    /**
     * Animate list item with fade-in and stagger effect.
     * Each item appears with a delay based on its position (50ms per item).
     * 
     * @param view The list item view to animate
     * @param position The position of the item in the list (0-based)
     */
    public static void animateListItem(View view, int position) {
        if (view == null) return;
        
        // Set initial state
        view.setAlpha(0f);
        view.setTranslationY(50f);
        
        // Calculate stagger delay based on position
        long delay = position * STAGGER_DELAY;
        
        // Animate with delay
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(DURATION_MEDIUM)
            .setStartDelay(delay)
            .setInterpolator(MATERIAL_MOTION_EASING)
            .setListener(null)
            .start();
    }
    
    /**
     * Setup shared element transition for Material Container Transform.
     * Call this in the Activity's onCreate() before setContentView().
     * 
     * This enables smooth transitions between screens with shared elements,
     * following Material Design 3 guidelines.
     * 
     * @param activity The activity to setup shared element transitions for
     */
    public static void setupSharedElementTransition(Activity activity) {
        if (activity == null) return;
        
        // Set exit and enter shared element callbacks for Material Container Transform
        activity.setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        activity.getWindow().setSharedElementsUseOverlay(false);
    }
    
    /**
     * Shake animation for form validation errors.
     * Shakes the view horizontally to indicate an error.
     * 
     * @param view The view to shake
     */
    public static void shake(View view) {
        if (view == null) return;
        
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 25, -25, 25, -25, 15, -15, 6, -6, 0);
        animator.setDuration(DURATION_SLOW);
        animator.start();
    }
    
    /**
     * Bounce animation for success states.
     * Scales the view up and down to create a bounce effect.
     * 
     * @param view The view to bounce
     */
    public static void bounce(View view) {
        if (view == null) return;
        
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(DURATION_FAST)
            .setInterpolator(MATERIAL_MOTION_EASING)
            .withEndAction(() -> {
                view.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(DURATION_STANDARD)
                    .setInterpolator(MATERIAL_MOTION_EASING)
                    .start();
            })
            .start();
    }
    
    /**
     * Rotate animation - rotates view by specified degrees.
     * 
     * @param view The view to rotate
     * @param degrees The degrees to rotate (positive = clockwise, negative = counter-clockwise)
     * @param duration Animation duration in milliseconds
     */
    public static void rotate(View view, float degrees, int duration) {
        if (view == null) return;
        
        view.animate()
            .rotation(degrees)
            .setDuration(duration)
            .setInterpolator(MATERIAL_MOTION_EASING)
            .start();
    }
    
    /**
     * Pulse animation - creates a pulsing effect by scaling up and down repeatedly.
     * Useful for drawing attention to important elements.
     * 
     * @param view The view to pulse
     * @param repeatCount Number of times to repeat (-1 for infinite)
     */
    public static void pulse(View view, int repeatCount) {
        if (view == null) return;
        
        ValueAnimator scaleUp = ValueAnimator.ofFloat(1.0f, 1.1f);
        scaleUp.setDuration(DURATION_STANDARD);
        scaleUp.setRepeatCount(repeatCount);
        scaleUp.setRepeatMode(ValueAnimator.REVERSE);
        scaleUp.setInterpolator(STANDARD_EASING);
        
        scaleUp.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            view.setScaleX(scale);
            view.setScaleY(scale);
        });
        
        scaleUp.start();
    }
    
    /**
     * Cancel all animations on a view.
     * 
     * @param view The view to cancel animations on
     */
    public static void cancelAnimations(View view) {
        if (view == null) return;
        view.animate().cancel();
    }
    
    /**
     * Reset view to default state (alpha = 1, scale = 1, translation = 0).
     * 
     * @param view The view to reset
     */
    public static void resetView(View view) {
        if (view == null) return;
        
        view.setAlpha(1f);
        view.setScaleX(1f);
        view.setScaleY(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
        view.setRotation(0f);
    }
}
