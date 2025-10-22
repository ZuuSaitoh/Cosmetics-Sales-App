package com.example.myapplication.notification;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Notification;

import java.util.List;

/**
 * Activity để hiển thị danh sách notifications
 */
public class NotificationListActivity extends AppCompatActivity implements NotificationAdapter.OnNotificationClickListener {
    
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private TextView unreadCountTextView;
    private ImageButton btnBack;
    private ImageButton btnMarkAllRead;
    
    private NotificationAdapter adapter;
    private AppDatabase database;
    private AuthManager authManager;
    private String currentUserId;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_list);
        
        authManager = new AuthManager(this);
        database = AppDatabase.getInstance(this);
        
        Long userId = authManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông báo", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        currentUserId = String.valueOf(userId);
        
        initViews();
        setupClickListeners();
        loadNotifications();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_notifications);
        emptyTextView = findViewById(R.id.text_empty);
        unreadCountTextView = findViewById(R.id.text_unread_count);
        btnBack = findViewById(R.id.btn_back);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this);
        recyclerView.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
    }
    
    private void loadNotifications() {
        // Observe notifications với LiveData
        LiveData<List<Notification>> notificationsLiveData = 
            database.notificationDao().getAllNotifications(currentUserId);
        
        notificationsLiveData.observe(this, notifications -> {
            if (notifications == null || notifications.isEmpty()) {
                emptyTextView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                unreadCountTextView.setVisibility(View.GONE);
            } else {
                emptyTextView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.setNotifications(notifications);
                
                // Update unread count
                updateUnreadCount();
            }
        });
    }
    
    private void updateUnreadCount() {
        LiveData<Integer> unreadCountLiveData = 
            database.notificationDao().getUnreadCount(currentUserId);
        
        unreadCountLiveData.observe(this, count -> {
            if (count != null && count > 0) {
                unreadCountTextView.setVisibility(View.VISIBLE);
                unreadCountTextView.setText(count + " thông báo chưa đọc");
            } else {
                unreadCountTextView.setVisibility(View.GONE);
            }
        });
    }
    
    private void markAllAsRead() {
        new Thread(() -> {
            database.notificationDao().markAllAsRead(currentUserId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
    
    @Override
    public void onNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.isRead()) {
            new Thread(() -> {
                database.notificationDao().markAsRead(notification.getNotificationId());
            }).start();
        }
        
        // Handle click based on notification type
        switch (notification.getNotificationType()) {
            case Notification.NotificationType.CART:
                // Navigate to cart
                Toast.makeText(this, "Mở giỏ hàng...", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(this, CartActivity.class);
                // startActivity(intent);
                break;
                
            case Notification.NotificationType.ORDER:
                // Navigate to order details
                Toast.makeText(this, "Xem chi tiết đơn hàng...", Toast.LENGTH_SHORT).show();
                break;
                
            case Notification.NotificationType.PROMOTION:
                // Navigate to promotions
                Toast.makeText(this, "Xem khuyến mãi...", Toast.LENGTH_SHORT).show();
                break;
                
            default:
                Toast.makeText(this, notification.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    @Override
    public void onNotificationDelete(Notification notification) {
        new Thread(() -> {
            database.notificationDao().deleteNotification(notification);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}

