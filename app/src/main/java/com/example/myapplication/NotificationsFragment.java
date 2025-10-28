package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.model.Notification;
import com.example.myapplication.notification.NotificationAdapter;

import java.util.List;

/**
 * Fragment để hiển thị danh sách notifications
 */
public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {
    
    private RecyclerView recyclerView;
    private View emptyStateView;  // Changed from TextView to View (LinearLayout in XML)
    private TextView unreadCountTextView;
    
    private NotificationAdapter adapter;
    private AppDatabase database;
    private AuthManager authManager;
    private String currentUserId;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            authManager = new AuthManager(requireContext());
            database = AppDatabase.getInstance(requireContext());
            
            initViews(view);
            
            Long userId = authManager.getUserId();
            if (userId == null) {
                showEmptyState();
                return;
            }
            currentUserId = String.valueOf(userId);
            
            loadNotifications();
        } catch (Exception e) {
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khi tải thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_notifications);
        emptyStateView = view.findViewById(R.id.text_empty);  // LinearLayout in XML
        unreadCountTextView = view.findViewById(R.id.text_unread_count);
        
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new NotificationAdapter(this);
            recyclerView.setAdapter(adapter);
        }
    }
    
    private void loadNotifications() {
        try {
            // Observe notifications với LiveData
            LiveData<List<Notification>> notificationsLiveData = 
                database.notificationDao().getAllNotifications(currentUserId);
            
            notificationsLiveData.observe(getViewLifecycleOwner(), notifications -> {
                try {
                    if (notifications == null || notifications.isEmpty()) {
                        showEmptyState();
                    } else {
                        showNotifications(notifications);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showEmptyState();
                }
            });
            
            // Update unread count
            updateUnreadCount();
        } catch (Exception e) {
            e.printStackTrace();
            showEmptyState();
        }
    }
    
    private void showNotifications(List<Notification> notifications) {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.GONE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }
        if (adapter != null) {
            adapter.setNotifications(notifications);
        }
    }
    
    private void showEmptyState() {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.VISIBLE);
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (unreadCountTextView != null) {
            unreadCountTextView.setVisibility(View.GONE);
        }
    }
    
    private void updateUnreadCount() {
        if (currentUserId == null) return;
        
        LiveData<Integer> unreadCountLiveData = 
            database.notificationDao().getUnreadCount(currentUserId);
        
        unreadCountLiveData.observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0 && unreadCountTextView != null) {
                unreadCountTextView.setVisibility(View.VISIBLE);
                unreadCountTextView.setText(count + " thông báo chưa đọc");
            } else if (unreadCountTextView != null) {
                unreadCountTextView.setVisibility(View.GONE);
            }
        });
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
                Toast.makeText(requireContext(), "Mở giỏ hàng...", Toast.LENGTH_SHORT).show();
                // Navigate to cart
                break;
                
            case Notification.NotificationType.ORDER:
                Toast.makeText(requireContext(), "Xem chi tiết đơn hàng...", Toast.LENGTH_SHORT).show();
                break;
                
            case Notification.NotificationType.PROMOTION:
                Toast.makeText(requireContext(), "Xem khuyến mãi...", Toast.LENGTH_SHORT).show();
                break;
                
            default:
                Toast.makeText(requireContext(), notification.getMessage(), Toast.LENGTH_SHORT).show();
                break;
        }
    }
    
    @Override
    public void onNotificationDelete(Notification notification) {
        new Thread(() -> {
            database.notificationDao().deleteNotification(notification);
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(), "Đã xóa thông báo", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }
}
