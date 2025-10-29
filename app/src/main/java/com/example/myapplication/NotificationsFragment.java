package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Notification;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.NotificationService;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.NotificationDTO;
import com.example.myapplication.network.dto.NotificationMapper;
import com.example.myapplication.notification.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment để hiển thị danh sách notifications từ API
 * Sử dụng API endpoint: GET /notifications/get-all-notifications-by-user-id/{userID}
 */
public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {
    
    private static final String TAG = "NotificationsFragment";
    
    private RecyclerView recyclerView;
    private View emptyStateView;
    private TextView unreadCountTextView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private com.google.android.material.tabs.TabLayout tabLayout;
    
    private NotificationAdapter adapter;
    private AuthManager authManager;
    private NotificationService notificationService;
    private Long currentUserId;
    
    // Toast instance để tránh "Toast already killed"
    private android.widget.Toast currentToast;
    
    // Danh sách notifications từ API
    private List<Notification> allNotifications = new ArrayList<>();
    
    // Tab hiện tại: 0 = Khuyến mãi, 1 = Của bạn
    private int currentTab = 0;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notifications, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            // Khởi tạo AuthManager và NotificationService
            authManager = new AuthManager(requireContext());
            notificationService = ApiClient.getRetrofit(requireContext()).create(NotificationService.class);
            
            // Khởi tạo views
            initViews(view);
            
            // Lấy userId từ AuthManager
            currentUserId = authManager.getUserId();
            
            Log.d(TAG, "Current UserID: " + currentUserId);
            
            if (currentUserId == null) {
                Log.w(TAG, "UserID is null - user not logged in");
                showEmptyState("Vui lòng đăng nhập để xem thông báo");
                return;
            }
            
            // Tải notifications từ API
            loadNotificationsFromAPI();
            
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Lỗi khi khởi tạo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_notifications);
        emptyStateView = view.findViewById(R.id.text_empty);
        unreadCountTextView = view.findViewById(R.id.text_unread_count);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        tabLayout = view.findViewById(R.id.tab_layout);
        
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new NotificationAdapter(this);
            recyclerView.setAdapter(adapter);
        }
        
        // Setup SwipeRefreshLayout để pull-to-refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Refreshing notifications...");
                loadNotificationsFromAPI();
            });
        }
        
        // Setup TabLayout
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                    currentTab = tab.getPosition();
                    Log.d(TAG, "Tab selected: " + currentTab + " (" + tab.getText() + ")");
                    filterAndDisplayNotifications();
                }
                
                @Override
                public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                }
                
                @Override
                public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
                }
            });
        }
    }
    
    /**
     * Tải notifications từ API
     * Endpoint: GET /notifications/get-all-notifications-by-user-id/{userID}
     */
    private void loadNotificationsFromAPI() {
        if (currentUserId == null) {
            Log.w(TAG, "Cannot load notifications - userId is null");
            showEmptyState("Vui lòng đăng nhập để xem thông báo");
            return;
        }
        
        Log.d(TAG, "Loading notifications for userId: " + currentUserId);
        
        // Hiển thị loading
        showLoading(true);
        
        // Gọi API - Sử dụng NotificationDTO
        Call<ApiResponse<List<NotificationDTO>>> call = notificationService.getAllNotificationsByUserId(currentUserId);
        
        call.enqueue(new Callback<ApiResponse<List<NotificationDTO>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<NotificationDTO>>> call, 
                                 @NonNull Response<ApiResponse<List<NotificationDTO>>> response) {
                // Ẩn loading
                showLoading(false);
                
                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "Fragment not attached - ignoring response");
                    return;
                }
                
                try {
                    // LOG RAW RESPONSE để debug
                    Log.d(TAG, "========== LOAD NOTIFICATIONS RESPONSE ==========");
                    Log.d(TAG, "HTTP Status: " + response.code());
                    Log.d(TAG, "Response successful: " + response.isSuccessful());
                    
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<NotificationDTO>> apiResponse = response.body();
                        
                        Log.d(TAG, "API Response - Code: " + apiResponse.getCode());
                        Log.d(TAG, "API Response - Message: " + apiResponse.getMessage());
                        
                        // Kiểm tra code == 9999 (success)
                        if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                            List<NotificationDTO> notificationDTOs = apiResponse.getResult();
                            
                            Log.d(TAG, "Received " + notificationDTOs.size() + " notification DTOs");
                            
                            // LOG chi tiết TOÀN BỘ DTO để debug
                            for (int i = 0; i < Math.min(5, notificationDTOs.size()); i++) {
                                NotificationDTO dto = notificationDTOs.get(i);
                                Log.d(TAG, "  ──────────────────────────────────────");
                                Log.d(TAG, "  DTO[" + i + "] FULL DATA:");
                                Log.d(TAG, "    notificationId (Long): " + dto.getNotificationId());
                                Log.d(TAG, "    userId (Long): " + dto.getUserId());
                                Log.d(TAG, "    title: " + dto.getTitle());
                                Log.d(TAG, "    message: " + dto.getMessage());
                                Log.d(TAG, "    type: " + dto.getNotificationType());
                                Log.d(TAG, "    isRead: " + dto.getIsRead());
                                Log.d(TAG, "    createdAt: " + dto.getCreatedAt());
                                Log.d(TAG, "    Full DTO: " + dto.toString());
                            }
                            Log.d(TAG, "  ──────────────────────────────────────");
                            
                            // Convert DTO sang Entity
                            List<Notification> notifications = NotificationMapper.toEntityList(notificationDTOs);
                            
                            Log.d(TAG, "Converted to " + notifications.size() + " notifications");
                            
                            // LOG chi tiết IDs sau convert
                            for (int i = 0; i < Math.min(3, notifications.size()); i++) {
                                Notification n = notifications.get(i);
                                Log.d(TAG, "  Entity[" + i + "] - Local notificationId (int): " + n.getNotificationId());
                            }
                            
                            // Lưu toàn bộ notifications
                            allNotifications = notifications;
                            
                            if (notifications.isEmpty()) {
                                showEmptyState("Bạn chưa có thông báo nào");
                            } else {
                                // Filter và hiển thị theo tab hiện tại
                                filterAndDisplayNotifications();
                            }
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? 
                                apiResponse.getMessage() : "Lỗi không xác định";
                            Log.e(TAG, "API Error: " + errorMsg);
                            showEmptyState("Lỗi: " + errorMsg);
                            showToast("Lỗi: " + errorMsg);
                        }
                    } else {
                        Log.e(TAG, "Response not successful - Code: " + response.code());
                        showEmptyState("Không thể tải thông báo");
                        showToast("Lỗi tải thông báo: " + response.code());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response", e);
                    showEmptyState("Lỗi xử lý dữ liệu");
                    showToast("Lỗi: " + e.getMessage());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<NotificationDTO>>> call, 
                                @NonNull Throwable t) {
                // Ẩn loading
                showLoading(false);
                
                if (!isAdded() || getContext() == null) {
                    return;
                }
                
                Log.e(TAG, "API call failed", t);
                showEmptyState("Không thể kết nối đến server");
                showToast("Lỗi kết nối: " + t.getMessage(), android.widget.Toast.LENGTH_LONG);
            }
        });
    }
    
    /**
     * Hiển thị/ẩn loading indicator
     */
    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(isLoading);
        }
    }
    
    /**
     * Filter và hiển thị notifications theo tab hiện tại
     */
    private void filterAndDisplayNotifications() {
        if (allNotifications == null || allNotifications.isEmpty()) {
            showEmptyState("Bạn chưa có thông báo nào");
            return;
        }
        
        List<Notification> filteredNotifications = new ArrayList<>();
        
        Log.d(TAG, "=== FILTERING NOTIFICATIONS ===");
        Log.d(TAG, "Current Tab: " + (currentTab == 0 ? "Khuyến mãi" : "Của bạn"));
        Log.d(TAG, "Total notifications to filter: " + allNotifications.size());
        
        for (Notification notification : allNotifications) {
            String type = notification.getNotificationType();
            if (type == null) {
                type = "SYSTEM"; // Default
            }
            
            Log.d(TAG, "Notification ID: " + notification.getNotificationId() + 
                  ", Type: " + type + ", Title: " + notification.getTitle());
            
            if (currentTab == 0) {
                // Tab "Khuyến mãi" - Notifications từ admin (PROMOTION, SYSTEM)
                if (type.equals(Notification.NotificationType.PROMOTION) || 
                    type.equals(Notification.NotificationType.SYSTEM)) {
                    filteredNotifications.add(notification);
                    Log.d(TAG, "  ✅ Added to Khuyến mãi tab");
                } else {
                    Log.d(TAG, "  ❌ Filtered out from Khuyến mãi tab");
                }
            } else {
                // Tab "Của bạn" - Notifications cá nhân (CART, ORDER, PAYMENT, ...)
                if (type.equals(Notification.NotificationType.CART) || 
                    type.equals(Notification.NotificationType.ORDER) ||
                    type.equals(Notification.NotificationType.PAYMENT)) {
                    filteredNotifications.add(notification);
                    Log.d(TAG, "  ✅ Added to Của bạn tab - Type: " + type);
                } else {
                    Log.d(TAG, "  ❌ Filtered out from Của bạn tab - Type: " + type);
                }
            }
        }
        
        Log.d(TAG, "=== FILTER RESULT ===");
        Log.d(TAG, "Filtered count: " + filteredNotifications.size());
        Log.d(TAG, "====================");
        
        Log.d(TAG, "Filtered notifications - Tab: " + currentTab + ", Count: " + filteredNotifications.size());
        
        if (filteredNotifications.isEmpty()) {
            String emptyMessage = currentTab == 0 ? 
                "Chưa có thông báo khuyến mãi" : 
                "Bạn chưa có thông báo nào";
            showEmptyState(emptyMessage);
        } else {
            showNotifications(filteredNotifications);
            updateUnreadCount(filteredNotifications);
        }
    }
    
    /**
     * Hiển thị danh sách notifications
     */
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
    
    /**
     * Hiển thị empty state với message tùy chỉnh
     */
    private void showEmptyState(String message) {
        if (emptyStateView != null) {
            emptyStateView.setVisibility(View.VISIBLE);
            // Nếu emptyStateView là TextView thì set text
            if (emptyStateView instanceof TextView) {
                ((TextView) emptyStateView).setText(message);
            }
        }
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }
        if (unreadCountTextView != null) {
            unreadCountTextView.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show toast an toàn, tránh "Toast already killed"
     */
    private void showToast(String message, int duration) {
        if (getContext() == null || !isAdded()) {
            return;
        }
        
        try {
            // Cancel toast cũ nếu đang hiển thị
            if (currentToast != null) {
                currentToast.cancel();
            }
            
            // Tạo toast mới
            currentToast = android.widget.Toast.makeText(getContext(), message, duration);
            currentToast.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast", e);
        }
    }
    
    /**
     * Show toast ngắn (short)
     */
    private void showToast(String message) {
        showToast(message, android.widget.Toast.LENGTH_SHORT);
    }
    
    /**
     * Cập nhật số lượng thông báo chưa đọc
     */
    private void updateUnreadCount(List<Notification> notifications) {
        if (unreadCountTextView == null || notifications == null) {
            return;
        }
        
        int unreadCount = 0;
        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }
        
        Log.d(TAG, "Unread count: " + unreadCount);
        
        if (unreadCount > 0) {
                unreadCountTextView.setVisibility(View.VISIBLE);
            unreadCountTextView.setText(unreadCount + " thông báo chưa đọc");
        } else {
                unreadCountTextView.setVisibility(View.GONE);
            }
    }
    
    @Override
    public void onNotificationClick(Notification notification) {
        Log.d(TAG, "Notification clicked: " + notification.getTitle());
        
        // TODO: Implement mark as read API call if available
        // For now, just handle navigation based on notification type
        
        // Handle click based on notification type
        String type = notification.getNotificationType();
        if (type == null) {
            showToast(notification.getMessage());
            return;
        }
        
        switch (type) {
            case Notification.NotificationType.CART:
                showToast("Mở giỏ hàng...");
                // TODO: Navigate to cart activity
                // Intent intent = new Intent(requireContext(), ActivityCart.class);
                // startActivity(intent);
                break;
                
            case Notification.NotificationType.ORDER:
                showToast("Xem chi tiết đơn hàng...");
                // TODO: Navigate to order details
                break;
                
            case Notification.NotificationType.PROMOTION:
                showToast("Xem khuyến mãi...");
                // TODO: Navigate to promotions
                break;
                
            default:
                showToast(notification.getMessage());
                break;
        }
    }
    
    @Override
    public void onNotificationDelete(Notification notification) {
        Log.d(TAG, "Delete notification requested: " + notification.getNotificationId());
        
        // Xác nhận xóa
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xóa thông báo")
            .setMessage("Bạn có chắc muốn xóa thông báo này?")
            .setPositiveButton("Xóa", (dialog, which) -> {
                deleteNotificationFromAPI(notification);
            })
            .setNegativeButton("Hủy", null)
            .show();
    }
    
    /**
     * Xóa notification thông qua API
     * DELETE /notifications/delete-notification/{notificationID}
     */
    private void deleteNotificationFromAPI(Notification notification) {
        if (notification == null || notification.getNotificationId() <= 0) {
            showToast("Lỗi: ID thông báo không hợp lệ");
            return;
        }
        
        // LOG chi tiết để trace ID conversion
        int entityId = notification.getNotificationId();
        Long apiId = (long) entityId;
        
        Log.d(TAG, "========== DELETE NOTIFICATION ==========");
        Log.d(TAG, "Notification Entity ID (int): " + entityId);
        Log.d(TAG, "Converting to API parameter (Long): " + apiId);
        Log.d(TAG, "Full notification: " + notification.toString());
        Log.d(TAG, "API call: DELETE /notifications/delete-notification/" + apiId);
        Log.d(TAG, "=========================================");
        
        // Hiển thị loading (optional - có thể dùng ProgressDialog)
        // showLoading(true);
        
        // Gọi API delete với apiId
        Call<okhttp3.ResponseBody> call = notificationService.deleteNotification(apiId);
        
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, 
                                 @NonNull Response<okhttp3.ResponseBody> response) {
                // showLoading(false);
                
                // Check fragment state để tránh IllegalStateException
                if (!isAdded() || getContext() == null || getActivity() == null || isDetached()) {
                    Log.w(TAG, "Fragment not in valid state - skipping delete response handling");
                    return;
                }
                
                try {
                    Log.d(TAG, "========== DELETE API RESPONSE ==========");
                    Log.d(TAG, "HTTP Status Code: " + response.code());
                    Log.d(TAG, "Response successful: " + response.isSuccessful());
                    
                    // Đọc raw response body
                    String responseBodyString = null;
                    if (response.body() != null) {
                        try {
                            responseBodyString = response.body().string();
                            Log.d(TAG, "Response Body: " + responseBodyString);
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading response body", e);
                        }
                    }
                    
                    Log.d(TAG, "Deleted notification ID was: " + apiId);
                    Log.d(TAG, "=========================================");
                    
                    // Check HTTP status code - nếu 200-299 = success
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Delete successful! Refreshing notification list...");
                        
                        // Post to UI thread và check fragment state
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    showToast("Đã xóa thông báo");
                                    
                                    Log.d(TAG, "Calling loadNotificationsFromAPI() to refresh list");
                                    loadNotificationsFromAPI();
                                }
                            });
                        }
                    } else {
                        // HTTP error (4xx, 5xx)
                        Log.e(TAG, "Delete HTTP Error - Code: " + response.code());
                        Log.d(TAG, "=========================================");
                        
                        final int errorCode = response.code();
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    showToast("Lỗi xóa thông báo: HTTP " + errorCode);
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception processing delete response", e);
                    Log.e(TAG, "Exception type: " + e.getClass().getName());
                    Log.e(TAG, "Exception message: " + e.getMessage());
                    Log.d(TAG, "=========================================");
                    
                    // Bất kỳ exception nào - nếu HTTP success vẫn coi như delete thành công
                    if (response.isSuccessful()) {
                        Log.d(TAG, "✅ Delete HTTP successful despite exception! Refreshing notification list...");
                        
                        if (getActivity() != null && isAdded()) {
                            getActivity().runOnUiThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    showToast("Đã xóa thông báo");
                                    
                                    Log.d(TAG, "Calling loadNotificationsFromAPI() to refresh list");
                                    loadNotificationsFromAPI();
                                }
                            });
                        }
                    } else {
                        // HTTP không success thì show error
                        String exceptionMsg = e.getMessage() != null ? e.getMessage() : "Unknown error";
                        
                        if (getActivity() != null && isAdded()) {
                            final String errorMessage = exceptionMsg;
                            getActivity().runOnUiThread(() -> {
                                if (isAdded() && getContext() != null) {
                                    showToast("Lỗi: " + errorMessage);
                                }
                            });
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, 
                                @NonNull Throwable t) {
                // showLoading(false);
                
                if (!isAdded() || getContext() == null || getActivity() == null) {
                    Log.w(TAG, "Fragment not in valid state - skipping delete failure handling");
                    return;
                }
                
                Log.e(TAG, "Delete API call failed", t);
                
                final String errorMessage = t.getMessage();
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded() && getContext() != null) {
                            showToast("Lỗi kết nối: " + errorMessage, android.widget.Toast.LENGTH_LONG);
                        }
                    });
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh notifications khi fragment được hiển thị lại
        if (currentUserId != null) {
            Log.d(TAG, "Fragment resumed - refreshing notifications");
            loadNotificationsFromAPI();
        }
    }
}
