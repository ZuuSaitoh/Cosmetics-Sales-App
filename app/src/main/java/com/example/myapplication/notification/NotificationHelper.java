package com.example.myapplication.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.myapplication.ActivityCart;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Notification;

import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Helper class để quản lý và hiển thị notifications
 * Xử lý Notification Channels, build và show notifications
 */
public class NotificationHelper {
    
    // ============ NOTIFICATION CHANNEL IDs ============
    public static final String CHANNEL_ID_CART = "cart_notification_channel";
    public static final String CHANNEL_ID_ORDER = "order_notification_channel";
    public static final String CHANNEL_ID_PROMOTION = "promotion_notification_channel";
    public static final String CHANNEL_ID_SYSTEM = "system_notification_channel";
    
    // ============ NOTIFICATION IDs ============
    public static final int NOTIFICATION_ID_CART = 1001;
    public static final int NOTIFICATION_ID_ORDER = 1002;
    public static final int NOTIFICATION_ID_PROMOTION = 1003;
    public static final int NOTIFICATION_ID_SYSTEM = 1004;
    
    // ============ REQUEST CODES cho PendingIntent ============
    private static final int REQUEST_CODE_CART = 100;
    private static final int REQUEST_CODE_ORDER = 101;
    private static final int REQUEST_CODE_PROMOTION = 102;
    
    private Context context;
    private NotificationManagerCompat notificationManager;
    
    /**
     * Constructor
     */
    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.notificationManager = NotificationManagerCompat.from(context);
    }
    
    // ============ NOTIFICATION CHANNEL SETUP ============
    
    /**
     * Tạo tất cả notification channels (gọi 1 lần khi app khởi động)
     * Chỉ cần thiết cho Android 8.0 (API 26) trở lên
     */
    public void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel cho Cart notifications
            NotificationChannel cartChannel = new NotificationChannel(
                    CHANNEL_ID_CART,
                    "Thông báo giỏ hàng",
                    NotificationManager.IMPORTANCE_HIGH
            );
            cartChannel.setDescription("Thông báo về các sản phẩm trong giỏ hàng của bạn");
            cartChannel.enableLights(true);
            cartChannel.setLightColor(Color.BLUE);
            cartChannel.enableVibration(true);
            cartChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            cartChannel.setShowBadge(true);
            
            // Channel cho Order notifications
            NotificationChannel orderChannel = new NotificationChannel(
                    CHANNEL_ID_ORDER,
                    "Thông báo đơn hàng",
                    NotificationManager.IMPORTANCE_HIGH
            );
            orderChannel.setDescription("Thông báo về trạng thái đơn hàng của bạn");
            orderChannel.enableLights(true);
            orderChannel.setLightColor(Color.GREEN);
            orderChannel.enableVibration(true);
            orderChannel.setShowBadge(true);
            
            // Channel cho Promotion notifications
            NotificationChannel promotionChannel = new NotificationChannel(
                    CHANNEL_ID_PROMOTION,
                    "Thông báo khuyến mãi",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            promotionChannel.setDescription("Thông báo về các chương trình khuyến mãi");
            promotionChannel.enableLights(true);
            promotionChannel.setLightColor(Color.RED);
            promotionChannel.setShowBadge(true);
            
            // Channel cho System notifications
            NotificationChannel systemChannel = new NotificationChannel(
                    CHANNEL_ID_SYSTEM,
                    "Thông báo hệ thống",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            systemChannel.setDescription("Thông báo hệ thống và cập nhật ứng dụng");
            systemChannel.setShowBadge(true);
            
            // Register channels với NotificationManager
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(cartChannel);
                manager.createNotificationChannel(orderChannel);
                manager.createNotificationChannel(promotionChannel);
                manager.createNotificationChannel(systemChannel);
            }
        }
    }
    
    // ============ CART BADGE NOTIFICATION ============
    
    /**
     * Hiển thị notification badge cho giỏ hàng
     * Đây là method chính để hiển thị cart badge khi app đóng
     * 
     * @param userId ID của user
     * @param itemCount Số lượng items trong giỏ hàng
     */
    public void showCartBadgeNotification(String userId, int itemCount) {
        if (itemCount <= 0) {
            // Nếu giỏ hàng trống, clear notification
            clearCartNotification();
            return;
        }
        
        // Tạo message
        String title = "Giỏ hàng của bạn";
        String message = itemCount == 1 
                ? "Bạn có 1 sản phẩm trong giỏ hàng" 
                : "Bạn có " + itemCount + " sản phẩm trong giỏ hàng";
        
        // Build notification
        NotificationCompat.Builder builder = buildCartNotification(title, message, itemCount);
        
        // Show notification
        showNotification(NOTIFICATION_ID_CART, builder);
        
        // Update badge trên app icon
        updateAppBadge(itemCount);
        
        // Lưu notification vào database
        saveNotificationToDatabase(userId, title, message, Notification.NotificationType.CART);
    }
    
    /**
     * Build Cart Notification
     */
    private NotificationCompat.Builder buildCartNotification(String title, String message, int itemCount) {
        // Tạo Intent mở CartActivity khi click notification
        Intent intent = new Intent(context, ActivityCart.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                REQUEST_CODE_CART,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Tạo delete intent (khi user dismiss notification)
        Intent deleteIntent = new Intent(context, NotificationReceiver.class);
        deleteIntent.setAction("com.example.myapplication.NOTIFICATION_DISMISSED");
        deleteIntent.putExtra("notification_type", "CART");
        
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_CART,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Sound URI
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        // Build notification
        return new NotificationCompat.Builder(context, CHANNEL_ID_CART)
                .setSmallIcon(R.drawable.ic_shopping_cart) // Cần tạo icon này
                .setContentTitle(title)
                .setContentText(message)
                .setNumber(itemCount) // Hiển thị badge number
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true) // Tự động xóa khi click
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setLights(Color.BLUE, 1000, 500)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                // Action buttons (optional)
                .addAction(R.drawable.ic_shopping_cart, "Xem giỏ hàng", pendingIntent);
    }
    
    // ============ ORDER NOTIFICATION ============
    
    /**
     * Hiển thị notification cho đơn hàng
     */
    public void showOrderNotification(String userId, String title, String message, String orderId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_ORDER)
                .setSmallIcon(R.drawable.ic_shopping_bag) // Cần tạo icon này
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        
        showNotification(NOTIFICATION_ID_ORDER, builder);
        saveNotificationToDatabase(userId, title, message, Notification.NotificationType.ORDER);
    }
    
    // ============ PROMOTION NOTIFICATION ============
    
    /**
     * Hiển thị notification cho khuyến mãi
     */
    public void showPromotionNotification(String userId, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_PROMOTION)
                .setSmallIcon(R.drawable.ic_promotion) // Cần tạo icon này
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        
        showNotification(NOTIFICATION_ID_PROMOTION, builder);
        saveNotificationToDatabase(userId, title, message, Notification.NotificationType.PROMOTION);
    }
    
    // ============ HELPER METHODS ============
    
    /**
     * Hiển thị notification
     */
    private void showNotification(int notificationId, NotificationCompat.Builder builder) {
        try {
            notificationManager.notify(notificationId, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
            // User chưa grant notification permission (Android 13+)
        }
    }
    
    /**
     * Clear cart notification
     */
    public void clearCartNotification() {
        notificationManager.cancel(NOTIFICATION_ID_CART);
        updateAppBadge(0); // Clear badge
    }
    
    /**
     * Clear tất cả notifications
     */
    public void clearAllNotifications() {
        notificationManager.cancelAll();
        updateAppBadge(0);
    }
    
    /**
     * Update badge number trên app icon
     */
    public void updateAppBadge(int count) {
        try {
            ShortcutBadger.applyCount(context, count);
        } catch (Exception e) {
            e.printStackTrace();
            // Một số launchers không support badge
        }
    }
    
    /**
     * Lưu notification vào database
     */
    private void saveNotificationToDatabase(String userId, String title, String message, String type) {
        // Chạy async để không block UI thread
        new Thread(() -> {
            try {
                Notification notification = new Notification(userId, title, message, type);
                AppDatabase db = AppDatabase.getInstance(context);
                db.notificationDao().insertNotification(notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Kiểm tra notification permission (Android 13+)
     */
    public boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }
}

