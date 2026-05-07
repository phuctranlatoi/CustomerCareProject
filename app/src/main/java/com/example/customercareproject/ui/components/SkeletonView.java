package com.example.customercareproject.ui.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.customercareproject.R;
import com.google.android.material.color.MaterialColors;

/**
 * SkeletonView is a custom view for skeleton loading states with shimmer effect.
 * 
 * Features:
 * - Shimmer effect animation with gradient
 * - Configurable shapes (rectangle, circle)
 * - Customizable shimmer colors and animation speed
 * - Automatic start/stop of animation based on visibility
 * 
 * This component provides loading state placeholders with shimmer effects,
 * improving perceived performance while data is being fetched.
 * 
 * Usage in XML:
 * <com.example.customercareproject.ui.components.SkeletonView
 *     android:id="@+id/skeletonAvatar"
 *     android:layout_width="48dp"
 *     android:layout_height="48dp"
 *     app:skeletonShape="circle"
 *     app:shimmerDuration="1500" />
 * 
 * <com.example.customercareproject.ui.components.SkeletonView
 *     android:id="@+id/skeletonText"
 *     android:layout_width="match_parent"
 *     android:layout_height="16dp"
 *     app:skeletonShape="rectangle"
 *     app:shimmerDuration="1500" />
 * 
 * Usage in Java:
 * SkeletonView skeleton = findViewById(R.id.skeletonAvatar);
 * skeleton.setSkeletonShape(SkeletonView.Shape.CIRCLE);
 * skeleton.startShimmer();
 * 
 * // When data is loaded:
 * skeleton.stopShimmer();
 * skeleton.setVisibility(View.GONE);
 */
public class SkeletonView extends View {
    
    /**
     * Shape options for the skeleton view.
     */
    public enum Shape {
        RECTANGLE(0),
        CIRCLE(1);
        
        private final int value;
        
        Shape(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static Shape fromValue(int value) {
            for (Shape shape : values()) {
                if (shape.value == value) {
                    return shape;
                }
            }
            return RECTANGLE; // Default
        }
    }
    
    // Default values
    private static final int DEFAULT_SHIMMER_DURATION = 1500; // 1.5 seconds
    private static final float DEFAULT_SHIMMER_ANGLE = 20f; // Degrees
    private static final float SHIMMER_WIDTH_RATIO = 0.5f; // Shimmer width as ratio of view width
    
    // Shape and appearance
    private Shape skeletonShape = Shape.RECTANGLE;
    private int baseColor;
    private int shimmerColor;
    private int highlightColor;
    
    // Animation
    private ValueAnimator shimmerAnimator;
    private float shimmerTranslation = 0f;
    private int shimmerDuration = DEFAULT_SHIMMER_DURATION;
    private float shimmerAngle = DEFAULT_SHIMMER_ANGLE;
    private boolean isShimmerRunning = false;
    
    // Drawing
    private Paint paint;
    private RectF rectF;
    private LinearGradient shimmerGradient;
    
    public SkeletonView(@NonNull Context context) {
        super(context);
        init(context, null);
    }
    
    public SkeletonView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public SkeletonView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    /**
     * Initialize the skeleton view with default values and custom attributes.
     */
    private void init(Context context, AttributeSet attrs) {
        // Initialize paint
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        
        // Initialize rect for drawing
        rectF = new RectF();
        
        // Get default colors from theme
        baseColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurfaceVariant, Color.LTGRAY);
        shimmerColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.WHITE);
        highlightColor = adjustAlpha(shimmerColor, 0.7f);
        
        // Parse custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SkeletonView);
            try {
                int shapeValue = a.getInt(R.styleable.SkeletonView_skeletonShape, Shape.RECTANGLE.getValue());
                skeletonShape = Shape.fromValue(shapeValue);
                
                shimmerDuration = a.getInt(R.styleable.SkeletonView_shimmerDuration, DEFAULT_SHIMMER_DURATION);
                
                baseColor = a.getColor(R.styleable.SkeletonView_baseColor, baseColor);
                shimmerColor = a.getColor(R.styleable.SkeletonView_shimmerColor, shimmerColor);
                highlightColor = adjustAlpha(shimmerColor, 0.7f);
                
                boolean autoStart = a.getBoolean(R.styleable.SkeletonView_autoStart, true);
                if (autoStart) {
                    // Start shimmer after view is laid out
                    post(this::startShimmer);
                }
            } finally {
                a.recycle();
            }
        }
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateShimmerGradient();
    }
    
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) {
            return;
        }
        
        // Draw base shape
        paint.setShader(null);
        paint.setColor(baseColor);
        
        if (skeletonShape == Shape.CIRCLE) {
            float radius = Math.min(width, height) / 2f;
            canvas.drawCircle(width / 2f, height / 2f, radius, paint);
        } else {
            float cornerRadius = getResources().getDimension(R.dimen.shape_corner_small);
            rectF.set(0, 0, width, height);
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        }
        
        // Draw shimmer effect if running
        if (isShimmerRunning && shimmerGradient != null) {
            canvas.save();
            
            // Translate canvas for shimmer animation
            canvas.translate(shimmerTranslation, 0);
            
            // Draw shimmer gradient
            paint.setShader(shimmerGradient);
            
            if (skeletonShape == Shape.CIRCLE) {
                float radius = Math.min(width, height) / 2f;
                canvas.drawCircle(width / 2f, height / 2f, radius, paint);
            } else {
                float cornerRadius = getResources().getDimension(R.dimen.shape_corner_small);
                rectF.set(0, 0, width, height);
                canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
            }
            
            canvas.restore();
        }
    }
    
    /**
     * Update the shimmer gradient based on current view size.
     */
    private void updateShimmerGradient() {
        int width = getWidth();
        int height = getHeight();
        
        if (width == 0 || height == 0) {
            return;
        }
        
        // Calculate shimmer width
        float shimmerWidth = width * SHIMMER_WIDTH_RATIO;
        
        // Create gradient with three colors: transparent -> highlight -> transparent
        shimmerGradient = new LinearGradient(
            -shimmerWidth, 0,
            shimmerWidth, 0,
            new int[]{Color.TRANSPARENT, highlightColor, shimmerColor, highlightColor, Color.TRANSPARENT},
            new float[]{0f, 0.2f, 0.5f, 0.8f, 1f},
            Shader.TileMode.CLAMP
        );
    }
    
    /**
     * Start the shimmer animation.
     */
    public void startShimmer() {
        if (isShimmerRunning) {
            return;
        }
        
        int width = getWidth();
        if (width == 0) {
            // View not laid out yet, try again after layout
            post(this::startShimmer);
            return;
        }
        
        isShimmerRunning = true;
        
        // Calculate animation range
        float shimmerWidth = width * SHIMMER_WIDTH_RATIO;
        float startX = -shimmerWidth * 2;
        float endX = width + shimmerWidth * 2;
        
        // Create animator
        shimmerAnimator = ValueAnimator.ofFloat(startX, endX);
        shimmerAnimator.setDuration(shimmerDuration);
        shimmerAnimator.setInterpolator(new LinearInterpolator());
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        
        shimmerAnimator.addUpdateListener(animation -> {
            shimmerTranslation = (float) animation.getAnimatedValue();
            invalidate();
        });
        
        shimmerAnimator.start();
    }
    
    /**
     * Stop the shimmer animation.
     */
    public void stopShimmer() {
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
        isShimmerRunning = false;
        shimmerTranslation = 0f;
        invalidate();
    }
    
    /**
     * Check if shimmer animation is running.
     * 
     * @return true if shimmer is running, false otherwise
     */
    public boolean isShimmerRunning() {
        return isShimmerRunning;
    }
    
    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        
        // Auto-start/stop shimmer based on visibility
        if (visibility == VISIBLE) {
            if (!isShimmerRunning) {
                startShimmer();
            }
        } else {
            if (isShimmerRunning) {
                stopShimmer();
            }
        }
    }
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getVisibility() == VISIBLE && !isShimmerRunning) {
            startShimmer();
        }
    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopShimmer();
    }
    
    /**
     * Adjust color alpha.
     * 
     * @param color Original color
     * @param factor Alpha factor (0.0 - 1.0)
     * @return Color with adjusted alpha
     */
    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
    
    // ========================================
    // Public API
    // ========================================
    
    /**
     * Set the skeleton shape.
     * 
     * @param shape Shape (RECTANGLE or CIRCLE)
     */
    public void setSkeletonShape(Shape shape) {
        if (this.skeletonShape != shape) {
            this.skeletonShape = shape;
            invalidate();
        }
    }
    
    /**
     * Get the current skeleton shape.
     * 
     * @return Current shape
     */
    public Shape getSkeletonShape() {
        return skeletonShape;
    }
    
    /**
     * Set the base color (background color of skeleton).
     * 
     * @param color Base color
     */
    public void setBaseColor(int color) {
        if (this.baseColor != color) {
            this.baseColor = color;
            invalidate();
        }
    }
    
    /**
     * Get the base color.
     * 
     * @return Base color
     */
    public int getBaseColor() {
        return baseColor;
    }
    
    /**
     * Set the shimmer color (highlight color of shimmer effect).
     * 
     * @param color Shimmer color
     */
    public void setShimmerColor(int color) {
        if (this.shimmerColor != color) {
            this.shimmerColor = color;
            this.highlightColor = adjustAlpha(color, 0.7f);
            updateShimmerGradient();
            invalidate();
        }
    }
    
    /**
     * Get the shimmer color.
     * 
     * @return Shimmer color
     */
    public int getShimmerColor() {
        return shimmerColor;
    }
    
    /**
     * Set the shimmer animation duration.
     * 
     * @param duration Duration in milliseconds
     */
    public void setShimmerDuration(int duration) {
        if (this.shimmerDuration != duration) {
            this.shimmerDuration = duration;
            
            // Restart animation if running
            if (isShimmerRunning) {
                stopShimmer();
                startShimmer();
            }
        }
    }
    
    /**
     * Get the shimmer animation duration.
     * 
     * @return Duration in milliseconds
     */
    public int getShimmerDuration() {
        return shimmerDuration;
    }
}
