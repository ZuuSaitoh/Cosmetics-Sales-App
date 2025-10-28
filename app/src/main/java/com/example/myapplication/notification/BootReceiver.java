package com.example.myapplication.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * BroadcastReceiver để restore cart notification sau khi device restart
 * Được trigger khi BOOT_COMPLETED broadcast được gửi
 */
public class BootReceiver extends BroadcastReceiver {
    
    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_CART_ITEM_COUNT = "cart_item_count";
    private static final String KEY_USER_ID = "user_id";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Device boot completed - Restoring cart notification");
            restoreCartNotification(context);
        }
    }
    
    /**
     * Restore cart notification sau khi device restart
     */
    private void restoreCartNotification(Context context) {
        try {
            // Lấy thông tin cart đã lưu từ SharedPreferences
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            int cartItemCount = prefs.getInt(KEY_CART_ITEM_COUNT, 0);
            String userId = prefs.getString(KEY_USER_ID, null);
            
            // Nếu có items trong cart và user đã login
            if (cartItemCount > 0 && userId != null && !userId.isEmpty()) {
                Log.d(TAG, "Restoring cart notification - Items: " + cartItemCount + ", User: " + userId);
                
                // Show notification
                NotificationHelper helper = new NotificationHelper(context);
                helper.showCartBadgeNotification(userId, cartItemCount);
                
                Log.d(TAG, "Cart notification restored successfully");
            } else {
                Log.d(TAG, "No cart items to restore or user not logged in");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error restoring cart notification", e);
        }
    }
    
    /**
     * Helper method để lưu cart state vào SharedPreferences
     * Gọi method này khi cart thay đổi để persist state
     */
    public static void saveCartState(Context context, String userId, int cartItemCount) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(KEY_CART_ITEM_COUNT, cartItemCount);
            editor.putString(KEY_USER_ID, userId);
            editor.apply();
            
            Log.d(TAG, "Cart state saved - Items: " + cartItemCount + ", User: " + userId);
        } catch (Exception e) {
            Log.e(TAG, "Error saving cart state", e);
        }
    }
    
    /**
     * Clear cart state từ SharedPreferences
     */
    public static void clearCartState(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(KEY_CART_ITEM_COUNT);
            editor.remove(KEY_USER_ID);
            editor.apply();
            
            Log.d(TAG, "Cart state cleared");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing cart state", e);
        }
    }
}

