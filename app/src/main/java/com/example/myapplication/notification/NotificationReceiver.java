package com.example.myapplication.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Notification;

/**
 * BroadcastReceiver để xử lý các actions từ notification
 * - Notification clicked
 * - Notification dismissed
 * - View cart action
 */
public class NotificationReceiver extends BroadcastReceiver {
    
    private static final String TAG = "NotificationReceiver";
    
    // Action constants
    public static final String ACTION_NOTIFICATION_DISMISSED = "com.example.myapplication.NOTIFICATION_DISMISSED";
    public static final String ACTION_NOTIFICATION_CLICKED = "com.example.myapplication.NOTIFICATION_CLICKED";
    public static final String ACTION_VIEW_CART = "com.example.myapplication.VIEW_CART";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);
        
        switch (action) {
            case ACTION_NOTIFICATION_DISMISSED:
                handleNotificationDismissed(context, intent);
                break;
                
            case ACTION_NOTIFICATION_CLICKED:
                handleNotificationClicked(context, intent);
                break;
                
            case ACTION_VIEW_CART:
                handleViewCart(context, intent);
                break;
                
            default:
                Log.w(TAG, "Unknown action: " + action);
                break;
        }
    }
    
    /**
     * Xử lý khi user dismiss notification
     */
    private void handleNotificationDismissed(Context context, Intent intent) {
        String notificationType = intent.getStringExtra("notification_type");
        int notificationId = intent.getIntExtra("notification_id", -1);
        
        Log.d(TAG, "Notification dismissed - Type: " + notificationType + ", ID: " + notificationId);
        
        // Clear badge nếu là cart notification
        if ("CART".equals(notificationType)) {
            NotificationHelper helper = new NotificationHelper(context);
            helper.updateAppBadge(0);
        }
        
        // Có thể log analytics event ở đây
        // Analytics.logEvent("notification_dismissed", bundle);
    }
    
    /**
     * Xử lý khi user click vào notification
     */
    private void handleNotificationClicked(Context context, Intent intent) {
        int notificationId = intent.getIntExtra("notification_id", -1);
        
        Log.d(TAG, "Notification clicked - ID: " + notificationId);
        
        // Mark notification as read trong database
        if (notificationId > 0) {
            markNotificationAsRead(context, notificationId);
        }
        
        // Có thể log analytics event
        // Analytics.logEvent("notification_clicked", bundle);
    }
    
    /**
     * Xử lý khi user click "View Cart" action
     */
    private void handleViewCart(Context context, Intent intent) {
        Log.d(TAG, "View Cart action clicked");
        
        // Clear cart notification badge
        NotificationHelper helper = new NotificationHelper(context);
        helper.clearCartNotification();
        
        // Có thể log analytics event
        // Analytics.logEvent("notification_view_cart_clicked", bundle);
    }
    
    /**
     * Đánh dấu notification là đã đọc trong database
     */
    private void markNotificationAsRead(Context context, int notificationId) {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(context);
                db.notificationDao().markAsRead(notificationId);
                Log.d(TAG, "Marked notification as read: " + notificationId);
            } catch (Exception e) {
                Log.e(TAG, "Error marking notification as read", e);
            }
        }).start();
    }
}

