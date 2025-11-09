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
    private android.widget.Button btnMarkAllRead;
    
    private NotificationAdapter adapter;
    private AuthManager authManager;
    private NotificationService notificationService;
    private Long currentUserId;
    
    // Toast instance để tránh "Toast already killed"
    private android.widget.Toast currentToast;
    
    // Danh sách TẤT CẢ notifications từ API
    private List<Notification> allNotifications = new ArrayList<>();
    
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
    
    @Override
    public void onResume() {
        super.onResume();
        // Tự động refresh notifications mỗi khi user quay lại tab này
        if (currentUserId != null) {
            Log.d(TAG, "🔄 onResume - Auto refreshing notifications");
            loadNotificationsFromAPI();
        }
    }
    
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_notifications);
        emptyStateView = view.findViewById(R.id.text_empty);
        unreadCountTextView = view.findViewById(R.id.text_unread_count);
        progressBar = view.findViewById(R.id.progress_bar);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
        
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new NotificationAdapter(this);
            recyclerView.setAdapter(adapter);
        }
        
        // Setup SwipeRefreshLayout để pull-to-refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Log.d(TAG, "Refreshing all notifications...");
                loadNotificationsFromAPI();
            });
        }
        
        // Setup button "Đánh dấu tất cả đã đọc"
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> {
                Log.d(TAG, "Mark all read button clicked");
                
                // Kiểm tra xem có notification chưa đọc không
                boolean hasUnread = false;
                if (allNotifications != null) {
                    for (Notification n : allNotifications) {
                        if (!n.isRead()) {
                            hasUnread = true;
                            break;
                        }
                    }
                }
                
                if (hasUnread) {
                    markAllNotificationsAsRead();
                } else {
                    showToast("Tất cả thông báo đã được đọc");
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
                                // Hiển thị TẤT CẢ notifications
                                displayAllNotifications();
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
     * Hiển thị TẤT CẢ notifications (không filter theo type)
     */
    private void displayAllNotifications() {
        if (allNotifications == null || allNotifications.isEmpty()) {
            showEmptyState("Bạn chưa có thông báo nào");
            return;
        }
        
        Log.d(TAG, "=== DISPLAYING ALL NOTIFICATIONS ===");
        Log.d(TAG, "Total count: " + allNotifications.size());

        allNotifications.sort((n1, n2) ->
                Integer.compare(n2.getNotificationId(), n1.getNotificationId())
        );
        // Hiển thị TẤT CẢ notifications
        showNotifications(allNotifications);
        updateUnreadCount(allNotifications);
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
        
        // Đánh dấu notification là đã đọc
        if (!notification.isRead()) {
            markNotificationAsRead(notification);
        }
        
        // Handle navigation based on notification type
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
    
    /**
     * Đánh dấu TẤT CẢ notifications của user là đã đọc
     * API: PUT /notifications/mark-all-as-read/{userID}
     */
    private void markAllNotificationsAsRead() {
        if (currentUserId == null) {
            showToast("Không thể đánh dấu: User ID không hợp lệ");
            return;
        }
        
        Log.d(TAG, "Marking all notifications as read for user: " + currentUserId);
        
        // Gọi API mark-all-as-read
        Call<okhttp3.ResponseBody> call = notificationService.markAllNotificationsAsRead(currentUserId);
        
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, 
                                 @NonNull Response<okhttp3.ResponseBody> response) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Marked all notifications as read");
                    
                    // Update tất cả local notifications
                    if (allNotifications != null) {
                        for (Notification n : allNotifications) {
                            n.setRead(true);
                        }
                    }
                    
                    // Refresh UI
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                showToast("Đã đánh dấu tất cả là đã đọc");
                                displayAllNotifications();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Failed to mark all as read - HTTP: " + response.code());
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                showToast("Lỗi đánh dấu đã đọc");
                            }
                        });
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, 
                                @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "API call failed - mark all as read", t);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded()) {
                                showToast("Lỗi kết nối");
                            }
                        });
                    }
                }
            }
        });
    }
    
    /**
     * Đánh dấu notification là đã đọc
     * API: PUT /notifications/mark-as-read/{notificationID}
     */
    private void markNotificationAsRead(Notification notification) {
        if (notification == null || notification.getNotificationId() <= 0) {
            return;
        }
        
        Long notificationId = (long) notification.getNotificationId();
        
        Log.d(TAG, "Marking notification as read - ID: " + notificationId);
        
        // Gọi API mark-as-read
        Call<okhttp3.ResponseBody> call = notificationService.markNotificationAsRead(notificationId);
        
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, 
                                 @NonNull Response<okhttp3.ResponseBody> response) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                
                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Marked notification " + notificationId + " as read");
                    
                    // Update local notification object
                    notification.setRead(true);
                    
                    // Refresh UI
                    if (getActivity() != null && isAdded()) {
                        getActivity().runOnUiThread(() -> {
                            if (isAdded() && adapter != null) {
                                adapter.notifyDataSetChanged();
                                displayAllNotifications();
                            }
                        });
                    }
                } else {
                    Log.e(TAG, "Failed to mark as read - HTTP: " + response.code());
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, 
                                @NonNull Throwable t) {
                if (isAdded()) {
                    Log.e(TAG, "API call failed - mark as read", t);
                }
            }
        });
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
                                    
                                    Log.d(TAG, "Reloading all notifications after delete");
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
}
