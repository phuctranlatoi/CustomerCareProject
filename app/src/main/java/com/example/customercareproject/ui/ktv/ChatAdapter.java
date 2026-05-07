package com.example.customercareproject.ui.ktv;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.customercareproject.R;
import com.example.customercareproject.utils.AnimationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_TEXT = 0;
    private static final int VIEW_TYPE_IMAGE_ME = 1;
    private static final int VIEW_TYPE_IMAGE_OTHER = 2;
    private static final int VIEW_TYPE_TYPING_INDICATOR = 3;
    private static final int VIEW_TYPE_DATE_SEPARATOR = 10;

    private List<Map<String, Object>> danhSach = new ArrayList<>();
    private final String myUid;
    private final Set<String> animatedMessageIds = new HashSet<>();
    private boolean showTypingIndicator = false;

    public ChatAdapter(List<Map<String, Object>> danhSach, String myUid) {
        this.danhSach = new ArrayList<>(danhSach);
        this.myUid = myUid;
    }

    /** Cập nhật toàn bộ danh sách từ RTDB */
    public void capNhatRaw(List<Map<String, Object>> newList) {
        this.danhSach = insertDateSeparators(new ArrayList<>(newList));
        notifyDataSetChanged();
    }

    private boolean isDateSeparator(Map<String, Object> item) {
        return Boolean.TRUE.equals(item.get("_dateSeparator"));
    }

    public static List<Map<String, Object>> insertDateSeparators(List<Map<String, Object>> messages) {
        List<Map<String, Object>> result = new ArrayList<>();
        String lastDate = null;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar yesterday = java.util.Calendar.getInstance();
        yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1);

        for (Map<String, Object> msg : messages) {
            Object thoiGianObj = msg.get("thoiGian");
            if (thoiGianObj != null) {
                long ts = thoiGianObj instanceof Long ? (Long) thoiGianObj : ((Number) thoiGianObj).longValue();
                cal.setTimeInMillis(ts);
                String dateKey = cal.get(java.util.Calendar.YEAR) + "-" + cal.get(java.util.Calendar.DAY_OF_YEAR);

                if (!dateKey.equals(lastDate)) {
                    lastDate = dateKey;
                    // Determine display text
                    String dateText;
                    if (cal.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR)
                            && cal.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR)) {
                        dateText = "Hôm nay";
                    } else if (cal.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR)
                            && cal.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR)) {
                        dateText = "Hôm qua";
                    } else {
                        dateText = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(new java.util.Date(ts));
                    }

                    // Create separator item
                    java.util.Map<String, Object> separator = new java.util.HashMap<>();
                    separator.put("_dateSeparator", true);
                    separator.put("_dateText", dateText);
                    result.add(separator);
                }
            }
            result.add(msg);
        }
        return result;
    }

    /** Hiển thị hoặc ẩn typing indicator */
    public void setTypingIndicatorVisible(boolean visible) {
        if (this.showTypingIndicator != visible) {
            this.showTypingIndicator = visible;
            if (visible) {
                notifyItemInserted(getItemCount() - 1);
            } else {
                notifyItemRemoved(getItemCount());
            }
        }
    }

    /** Xóa tất cả animations khi adapter bị detach */
    public void clearAnimations() {
        animatedMessageIds.clear();
    }

    @Override
    public int getItemViewType(int position) {
        // Check if this is the typing indicator position
        if (showTypingIndicator && position == danhSach.size()) {
            return VIEW_TYPE_TYPING_INDICATOR;
        }

        Map<String, Object> msg = danhSach.get(position);
        if (isDateSeparator(msg)) return VIEW_TYPE_DATE_SEPARATOR;

        String loaiTin = (String) msg.get("loaiTin");
        if ("anh".equals(loaiTin)) {
            String nguoiGuiUid = (String) msg.get("nguoiGuiUid");
            if (myUid.equals(nguoiGuiUid)) {
                return VIEW_TYPE_IMAGE_ME;
            } else {
                return VIEW_TYPE_IMAGE_OTHER;
            }
        }
        return VIEW_TYPE_TEXT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_IMAGE_ME || viewType == VIEW_TYPE_IMAGE_OTHER) {
            View v = inflater.inflate(R.layout.item_chat_image, parent, false);
            return new ImageViewHolder(v);
        } else if (viewType == VIEW_TYPE_TYPING_INDICATOR) {
            View v = inflater.inflate(R.layout.item_typing_indicator, parent, false);
            return new TypingIndicatorViewHolder(v);
        } else if (viewType == VIEW_TYPE_DATE_SEPARATOR) {
            View v = inflater.inflate(R.layout.item_chat_date_separator, parent, false);
            return new DateSeparatorViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_chat, parent, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Handle typing indicator
        if (holder instanceof TypingIndicatorViewHolder) {
            TypingIndicatorViewHolder typingHolder = (TypingIndicatorViewHolder) holder;
            typingHolder.startTypingAnimation();
            
            // Apply slide-in animation for typing indicator
            AnimationHelper.slideUp(holder.itemView, AnimationHelper.DURATION_STANDARD);
            return;
        }

        // Handle date separator
        if (holder instanceof DateSeparatorViewHolder) {
            DateSeparatorViewHolder sepHolder = (DateSeparatorViewHolder) holder;
            String dateText = (String) danhSach.get(position).get("_dateText");
            sepHolder.tvDateSeparator.setText(dateText != null ? dateText : "");
            return;
        }

        Map<String, Object> msg = danhSach.get(position);

        String nguoiGuiUid = (String) msg.get("nguoiGuiUid");
        String nguoiGuiTen = (String) msg.get("nguoiGuiTen");
        Object thoiGianObj = msg.get("thoiGian");
        String messageId = (String) msg.get("_id");

        String time = "";
        if (thoiGianObj != null) {
            long ts = thoiGianObj instanceof Long ? (Long) thoiGianObj : ((Number) thoiGianObj).longValue();
            time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(ts));
        }

        boolean isMe = myUid.equals(nguoiGuiUid);

        // Apply slide-in animation for new messages with stagger effect
        if (messageId != null && !animatedMessageIds.contains(messageId)) {
            animatedMessageIds.add(messageId);
            // Apply stagger effect: 50ms delay per position from the end
            int staggerDelay = Math.max(0, (getItemCount() - position - 1)) * 50;
            AnimationHelper.animateListItem(holder.itemView, staggerDelay / 50);
        }

        if (holder instanceof ImageViewHolder) {
            ImageViewHolder imgHolder = (ImageViewHolder) holder;
            String anhUrl = (String) msg.get("anhUrl");
            final String finalTime = time;

            if (isMe) {
                imgHolder.layoutKtvImg.setVisibility(View.VISIBLE);
                imgHolder.layoutKhImg.setVisibility(View.GONE);
                imgHolder.tvThoiGianKtvImg.setText(finalTime);
                Glide.with(holder.itemView.getContext())
                        .load(anhUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(imgHolder.ivAnhKtv);
                imgHolder.ivAnhKtv.setOnClickListener(v -> openImage(v, anhUrl));
            } else {
                imgHolder.layoutKhImg.setVisibility(View.VISIBLE);
                imgHolder.layoutKtvImg.setVisibility(View.GONE);
                imgHolder.tvThoiGianKhImg.setText(finalTime);
                Glide.with(holder.itemView.getContext())
                        .load(anhUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(imgHolder.ivAnhKh);
                imgHolder.ivAnhKh.setOnClickListener(v -> openImage(v, anhUrl));
            }
        } else if (holder instanceof ViewHolder) {
            ViewHolder textHolder = (ViewHolder) holder;
            String noiDung = (String) msg.get("noiDung");

            if (isMe) {
                textHolder.layoutKtv.setVisibility(View.VISIBLE);
                textHolder.layoutKh.setVisibility(View.GONE);
                textHolder.tvNoiDungKtv.setText(noiDung);
                textHolder.tvThoiGianKtv.setText(time);
            } else {
                textHolder.layoutKh.setVisibility(View.VISIBLE);
                textHolder.layoutKtv.setVisibility(View.GONE);
                textHolder.tvTenNguoiGui.setText(nguoiGuiTen != null ? nguoiGuiTen : "");
                textHolder.tvNoiDungKh.setText(noiDung);
                textHolder.tvThoiGianKh.setText(time);
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        
        // Stop typing animation when view is recycled
        if (holder instanceof TypingIndicatorViewHolder) {
            ((TypingIndicatorViewHolder) holder).stopTypingAnimation();
        }
        
        // Cancel any ongoing animations
        AnimationHelper.cancelAnimations(holder.itemView);
        AnimationHelper.resetView(holder.itemView);
    }

    private void openImage(View view, String anhUrl) {
        if (anhUrl == null || anhUrl.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(anhUrl));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        view.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() { 
        return danhSach.size() + (showTypingIndicator ? 1 : 0); 
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutKtv, layoutKh;
        TextView tvNoiDungKtv, tvThoiGianKtv;
        TextView tvTenNguoiGui, tvNoiDungKh, tvThoiGianKh;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutKtv = itemView.findViewById(R.id.layoutKtv);
            layoutKh = itemView.findViewById(R.id.layoutKh);
            tvNoiDungKtv = itemView.findViewById(R.id.tvNoiDungKtv);
            tvThoiGianKtv = itemView.findViewById(R.id.tvThoiGianKtv);
            tvTenNguoiGui = itemView.findViewById(R.id.tvTenNguoiGui);
            tvNoiDungKh = itemView.findViewById(R.id.tvNoiDungKh);
            tvThoiGianKh = itemView.findViewById(R.id.tvThoiGianKh);
        }
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutKtvImg, layoutKhImg;
        ImageView ivAnhKtv, ivAnhKh;
        TextView tvThoiGianKtvImg, tvThoiGianKhImg;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutKtvImg = itemView.findViewById(R.id.layoutKtvImg);
            layoutKhImg = itemView.findViewById(R.id.layoutKhImg);
            ivAnhKtv = itemView.findViewById(R.id.ivAnhKtv);
            ivAnhKh = itemView.findViewById(R.id.ivAnhKh);
            tvThoiGianKtvImg = itemView.findViewById(R.id.tvThoiGianKtvImg);
            tvThoiGianKhImg = itemView.findViewById(R.id.tvThoiGianKhImg);
        }
    }

    static class DateSeparatorViewHolder extends RecyclerView.ViewHolder {
        TextView tvDateSeparator;
        DateSeparatorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDateSeparator = itemView.findViewById(R.id.tvDateSeparator);
        }
    }

    static class TypingIndicatorViewHolder extends RecyclerView.ViewHolder {
        View dot1, dot2, dot3;
        ValueAnimator typingAnimator;

        TypingIndicatorViewHolder(@NonNull View itemView) {
            super(itemView);
            dot1 = itemView.findViewById(R.id.dot1);
            dot2 = itemView.findViewById(R.id.dot2);
            dot3 = itemView.findViewById(R.id.dot3);
        }

        void startTypingAnimation() {
            if (typingAnimator != null && typingAnimator.isRunning()) {
                return;
            }

            // Create pulsing animation for typing dots
            typingAnimator = ValueAnimator.ofFloat(0.3f, 1.0f);
            typingAnimator.setDuration(600);
            typingAnimator.setRepeatCount(ValueAnimator.INFINITE);
            typingAnimator.setRepeatMode(ValueAnimator.REVERSE);

            typingAnimator.addUpdateListener(animation -> {
                float alpha = (float) animation.getAnimatedValue();
                long elapsed = animation.getCurrentPlayTime();
                
                // Stagger the dots with 200ms delay
                if (elapsed >= 0) {
                    dot1.setAlpha(alpha);
                }
                if (elapsed >= 200) {
                    dot2.setAlpha(alpha);
                }
                if (elapsed >= 400) {
                    dot3.setAlpha(alpha);
                }
            });

            typingAnimator.start();
        }

        void stopTypingAnimation() {
            if (typingAnimator != null) {
                typingAnimator.cancel();
                typingAnimator = null;
            }
        }
    }
}
