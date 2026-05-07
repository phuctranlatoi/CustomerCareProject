package com.example.customercareproject.ui.components;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import androidx.appcompat.widget.AppCompatTextView;

public class InitialAvatarView extends AppCompatTextView {

    private static final int[] AVATAR_COLORS = {
        0xFF1565C0, 0xFF2E7D32, 0xFF6A1B9A, 0xFFE65100,
        0xFF00695C, 0xFFC62828, 0xFF4527A0, 0xFF00838F
    };

    public InitialAvatarView(Context context) {
        super(context);
        init();
    }

    public InitialAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InitialAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setTextColor(0xFFFFFFFF);
        setTypeface(getTypeface(), android.graphics.Typeface.BOLD);
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            setText("?");
            applyBackground(0xFF9E9E9E);
            return;
        }
        String initial = String.valueOf(name.trim().charAt(0)).toUpperCase();
        setText(initial);
        int colorIndex = Math.abs(name.hashCode()) % AVATAR_COLORS.length;
        applyBackground(AVATAR_COLORS[colorIndex]);
    }

    public void setSize(int dp) {
        int px = (int) TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(px, px);
        } else {
            params.width = px;
            params.height = px;
        }
        setLayoutParams(params);
    }

    private void applyBackground(int color) {
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        setBackground(bg);
    }
}
