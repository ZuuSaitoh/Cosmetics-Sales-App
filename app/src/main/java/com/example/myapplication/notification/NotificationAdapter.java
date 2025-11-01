package com.example.myapplication.notification;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter cho RecyclerView hiển thị danh sách notifications
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    
    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    
    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onNotificationDelete(Notification notification);
    }
    
    public NotificationAdapter(OnNotificationClickListener listener) {
        this.listener = listener;
    }
    
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }
    
    @Override
    public int getItemCount() {
        return notifications.size();
    }
    
    class NotificationViewHolder extends RecyclerView.ViewHolder {
        
        private ImageView iconImageView;
        private TextView titleTextView;
        private TextView messageTextView;
        private TextView timeTextView;
        private View unreadIndicator;
        private ImageButton btnDelete;
        
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            
            iconImageView = itemView.findViewById(R.id.icon_notification);
            titleTextView = itemView.findViewById(R.id.text_title);
            messageTextView = itemView.findViewById(R.id.text_message);
            timeTextView = itemView.findViewById(R.id.text_time);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
        
        public void bind(Notification notification) {
            // LOG chi tiết notification data
            android.util.Log.d("NotificationAdapter", 
                "Binding notification - ID: " + notification.getNotificationId() + 
                ", Title: " + notification.getTitle() + 
                ", Message: " + notification.getMessage() + 
                ", Type: " + notification.getNotificationType());
            
            // Set title
            String title = notification.getTitle();
            if (title == null || title.isEmpty()) {
                title = getDefaultTitle(notification.getNotificationType());
            }
            titleTextView.setText(title);
            
            // Set message
            messageTextView.setText(notification.getMessage());
            
            // Set time
            Date createdDate = new Date(notification.getCreatedAt());
            timeTextView.setText(getRelativeTime(notification.getCreatedAt()));
            
            // Set icon based on type
            setNotificationIcon(notification.getNotificationType());
            
            // Style based on read/unread status
            if (notification.isRead()) {
                unreadIndicator.setVisibility(View.GONE);
                titleTextView.setTypeface(null, Typeface.NORMAL);
                messageTextView.setTypeface(null, Typeface.NORMAL);
                itemView.setBackgroundColor(Color.TRANSPARENT);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
                titleTextView.setTypeface(null, Typeface.BOLD);
                messageTextView.setTypeface(null, Typeface.BOLD);
                itemView.setBackgroundColor(Color.parseColor("#F0F8FF")); // Light blue for unread
            }
            
            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
            
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    android.util.Log.d("NotificationAdapter", 
                        "Delete button clicked - Notification ID: " + notification.getNotificationId() + 
                        ", Title: " + notification.getTitle());
                    listener.onNotificationDelete(notification);
                }
            });
        }
        
        private void setNotificationIcon(String type) {
            int iconRes;
            switch (type != null ? type : "") {
                case Notification.NotificationType.CART:
                    iconRes = R.drawable.ic_shopping_cart;
                    break;
                case Notification.NotificationType.ORDER:
                    iconRes = R.drawable.ic_shopping_bag;
                    break;
                case Notification.NotificationType.PROMOTION:
                    iconRes = R.drawable.ic_promotion;
                    break;
                default:
                    iconRes = R.drawable.baseline_circle_notifications_24;
                    break;
            }
            iconImageView.setImageResource(iconRes);
        }
        
        private String getDefaultTitle(String type) {
            switch (type != null ? type : "") {
                case Notification.NotificationType.CART:
                    return "Giỏ hàng";
                case Notification.NotificationType.ORDER:
                    return "Đơn hàng";
                case Notification.NotificationType.PROMOTION:
                    return "Khuyến mãi";
                case Notification.NotificationType.SYSTEM:
                    return "Hệ thống";
                default:
                    return "Thông báo";
            }
        }
        
        /**
         * Convert timestamp to relative time (e.g., "2 giờ trước", "Hôm qua")
         */
        private String getRelativeTime(long timestamp) {
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            // Convert to seconds
            long seconds = diff / 1000;
            
            if (seconds < 60) {
                return "Vừa xong";
            } else if (seconds < 3600) {
                long minutes = seconds / 60;
                return minutes + " phút trước";
            } else if (seconds < 86400) {
                long hours = seconds / 3600;
                return hours + " giờ trước";
            } else if (seconds < 172800) {
                return "Hôm qua";
            } else {
                return dateFormat.format(new Date(timestamp));
            }
        }
    }
}

