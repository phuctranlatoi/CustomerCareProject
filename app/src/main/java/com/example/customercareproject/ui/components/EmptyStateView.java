package com.example.customercareproject.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.example.customercareproject.R;
import com.example.customercareproject.utils.AnimationHelper;

/**
 * EmptyStateView is a custom view for displaying empty states with illustration, text, and action button.
 * 
 * Features:
 * - Configurable illustration (ImageView)
 * - Title TextView with proper MD3 typography style (TitleLarge)
 * - Description TextView with proper MD3 typography style (BodyMedium)
 * - Action button with click listener interface (uses Material3Button)
 * - Fade-in animation when view is displayed
 * 
 * This component provides a consistent empty state experience across the app,
 * following Material Design 3 guidelines.
 * 
 * Usage in XML:
 * <com.example.customercareproject.ui.components.EmptyStateView
 *     android:id="@+id/emptyState"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     app:emptyStateIllustration="@drawable/ic_empty_tickets"
 *     app:emptyStateTitle="@string/no_tickets_title"
 *     app:emptyStateDescription="@string/no_tickets_description"
 *     app:emptyStateActionText="@string/create_ticket"
 *     app:showActionButton="true" />
 * 
 * Usage in Java:
 * EmptyStateView emptyState = findViewById(R.id.emptyState);
 * emptyState.setIllustration(R.drawable.ic_empty_tickets);
 * emptyState.setTitle("No Tickets");
 * emptyState.setDescription("You don't have any tickets yet");
 * emptyState.setActionText("Create Ticket");
 * emptyState.setOnActionClickListener(() -> {
 *     // Handle action button click
 * });
 * emptyState.show(); // Show with fade-in animation
 */
public class EmptyStateView extends LinearLayout {
    
    /**
     * Listener interface for action button clicks.
     */
    public interface OnActionClickListener {
        void onActionClick();
    }
    
    // UI Components
    private ImageView ivIllustration;
    private TextView tvTitle;
    private TextView tvDescription;
    private Material3Button btnAction;
    
    // State
    private OnActionClickListener actionClickListener;
    private boolean showActionButton = true;
    
    public EmptyStateView(@NonNull Context context) {
        super(context);
        init(context, null);
    }
    
    public EmptyStateView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public EmptyStateView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }
    
    /**
     * Initialize the empty state view with default values and custom attributes.
     */
    private void init(Context context, AttributeSet attrs) {
        // Set orientation and gravity
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        
        // Inflate layout
        LayoutInflater.from(context).inflate(R.layout.view_empty_state, this, true);
        
        // Find views
        ivIllustration = findViewById(R.id.ivIllustration);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        btnAction = findViewById(R.id.btnAction);
        
        // Setup action button click listener
        btnAction.setOnClickListener(v -> {
            if (actionClickListener != null) {
                actionClickListener.onActionClick();
            }
        });
        
        // Parse custom attributes if provided
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EmptyStateView);
            try {
                // Illustration
                Drawable illustration = a.getDrawable(R.styleable.EmptyStateView_emptyStateIllustration);
                if (illustration != null) {
                    ivIllustration.setImageDrawable(illustration);
                }
                
                // Title
                String title = a.getString(R.styleable.EmptyStateView_emptyStateTitle);
                if (title != null) {
                    tvTitle.setText(title);
                }
                
                // Description
                String description = a.getString(R.styleable.EmptyStateView_emptyStateDescription);
                if (description != null) {
                    tvDescription.setText(description);
                }
                
                // Action button text
                String actionText = a.getString(R.styleable.EmptyStateView_emptyStateActionText);
                if (actionText != null) {
                    btnAction.setText(actionText);
                }
                
                // Show action button
                showActionButton = a.getBoolean(R.styleable.EmptyStateView_showActionButton, true);
                btnAction.setVisibility(showActionButton ? VISIBLE : GONE);
                
            } finally {
                a.recycle();
            }
        }
        
        // Initially hide the view (will be shown with animation)
        setVisibility(GONE);
        setAlpha(0f);
    }
    
    /**
     * Show the empty state view with fade-in animation.
     */
    public void show() {
        if (getVisibility() == VISIBLE) {
            return;
        }
        
        setVisibility(VISIBLE);
        AnimationHelper.fadeIn(this, AnimationHelper.DURATION_MEDIUM);
    }
    
    /**
     * Hide the empty state view with fade-out animation.
     */
    public void hide() {
        if (getVisibility() == GONE) {
            return;
        }
        
        AnimationHelper.fadeOut(this, AnimationHelper.DURATION_MEDIUM);
    }
    
    /**
     * Set the illustration drawable.
     * 
     * @param drawable Illustration drawable
     */
    public void setIllustration(Drawable drawable) {
        ivIllustration.setImageDrawable(drawable);
    }
    
    /**
     * Set the illustration drawable from resource ID.
     * 
     * @param resId Drawable resource ID
     */
    public void setIllustration(@DrawableRes int resId) {
        ivIllustration.setImageResource(resId);
    }
    
    /**
     * Set the title text.
     * 
     * @param title Title text
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }
    
    /**
     * Set the title text from resource ID.
     * 
     * @param resId String resource ID
     */
    public void setTitle(@StringRes int resId) {
        tvTitle.setText(resId);
    }
    
    /**
     * Set the description text.
     * 
     * @param description Description text
     */
    public void setDescription(String description) {
        tvDescription.setText(description);
    }
    
    /**
     * Set the description text from resource ID.
     * 
     * @param resId String resource ID
     */
    public void setDescription(@StringRes int resId) {
        tvDescription.setText(resId);
    }
    
    /**
     * Set the action button text.
     * 
     * @param text Action button text
     */
    public void setActionText(String text) {
        btnAction.setText(text);
    }
    
    /**
     * Set the action button text from resource ID.
     * 
     * @param resId String resource ID
     */
    public void setActionText(@StringRes int resId) {
        btnAction.setText(resId);
    }
    
    /**
     * Set whether to show the action button.
     * 
     * @param show true to show, false to hide
     */
    public void setShowActionButton(boolean show) {
        this.showActionButton = show;
        btnAction.setVisibility(show ? VISIBLE : GONE);
    }
    
    /**
     * Check if action button is shown.
     * 
     * @return true if shown, false otherwise
     */
    public boolean isActionButtonShown() {
        return showActionButton;
    }
    
    /**
     * Set the action button click listener.
     * 
     * @param listener Click listener
     */
    public void setOnActionClickListener(OnActionClickListener listener) {
        this.actionClickListener = listener;
    }
    
    /**
     * Get the illustration ImageView for advanced customization.
     * 
     * @return ImageView instance
     */
    public ImageView getIllustrationView() {
        return ivIllustration;
    }
    
    /**
     * Get the title TextView for advanced customization.
     * 
     * @return TextView instance
     */
    public TextView getTitleView() {
        return tvTitle;
    }
    
    /**
     * Get the description TextView for advanced customization.
     * 
     * @return TextView instance
     */
    public TextView getDescriptionView() {
        return tvDescription;
    }
    
    /**
     * Get the action button for advanced customization.
     * 
     * @return Material3Button instance
     */
    public Material3Button getActionButton() {
        return btnAction;
    }
}
