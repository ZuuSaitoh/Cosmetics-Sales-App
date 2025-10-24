package com.example.myapplication;

import android.app.Application;
import android.util.Log;

import com.example.myapplication.notification.NotificationHelper;

/**
 * Application class - Được khởi tạo khi app start
 * Dùng để setup các components cần thiết cho toàn app
 */
public class MyApplication extends Application {
    
    private static final String TAG = "MyApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        Log.d(TAG, "Application onCreate - Initializing...");
        
        // Khởi tạo Notification Channels
        initializeNotificationChannels();
        
        // Có thể thêm các initialization khác ở đây
        // - Firebase
        // - Analytics
        // - Crash reporting
        // - etc.
    }
    
    /**
     * Khởi tạo Notification Channels (Android 8.0+)
     */
    private void initializeNotificationChannels() {
        try {
            NotificationHelper notificationHelper = new NotificationHelper(this);
            notificationHelper.createNotificationChannels();
            Log.d(TAG, "Notification channels created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating notification channels", e);
        }
    }
}

