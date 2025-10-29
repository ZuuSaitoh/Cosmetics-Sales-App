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
    
    private NotificationAdapter adapter;
    private AuthManager authManager;
    private NotificationService notificationService;
    private Long currentUserId;
    
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
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<NotificationDTO>> apiResponse = response.body();
                        
                        Log.d(TAG, "API Response - Code: " + apiResponse.getCode());
                        Log.d(TAG, "API Response - Message: " + apiResponse.getMessage());
                        
                        // Kiểm tra code == 9999 (success)
                        if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                            List<NotificationDTO> notificationDTOs = apiResponse.getResult();
                            
                            Log.d(TAG, "Received " + notificationDTOs.size() + " notification DTOs");
                            
                            // Convert DTO sang Entity
                            List<Notification> notifications = NotificationMapper.toEntityList(notificationDTOs);
                            
                            Log.d(TAG, "Converted to " + notifications.size() + " notifications");
                            
                            if (notifications.isEmpty()) {
                                showEmptyState("Bạn chưa có thông báo nào");
                            } else {
                                showNotifications(notifications);
                                updateUnreadCount(notifications);
                            }
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? 
                                apiResponse.getMessage() : "Lỗi không xác định";
                            Log.e(TAG, "API Error: " + errorMsg);
                            showEmptyState("Lỗi: " + errorMsg);
                            Toast.makeText(getContext(), "Lỗi: " + errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response not successful - Code: " + response.code());
                        showEmptyState("Không thể tải thông báo");
                        Toast.makeText(getContext(), 
                            "Lỗi tải thông báo: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response", e);
                    showEmptyState("Lỗi xử lý dữ liệu");
                    Toast.makeText(getContext(), 
                        "Lỗi: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
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
            Toast.makeText(requireContext(), notification.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        
        switch (type) {
            case Notification.NotificationType.CART:
                Toast.makeText(requireContext(), "Mở giỏ hàng...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to cart activity
                // Intent intent = new Intent(requireContext(), ActivityCart.class);
                // startActivity(intent);
                break;
                
            case Notification.NotificationType.ORDER:
                Toast.makeText(requireContext(), "Xem chi tiết đơn hàng...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to order details
                break;
                
            case Notification.NotificationType.PROMOTION:
                Toast.makeText(requireContext(), "Xem khuyến mãi...", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to promotions
                break;
                
            default:
                Toast.makeText(requireContext(), notification.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Lỗi: ID thông báo không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Long notificationId = (long) notification.getNotificationId();
        
        Log.d(TAG, "Deleting notification ID: " + notificationId);
        
        // Hiển thị loading (optional - có thể dùng ProgressDialog)
        // showLoading(true);
        
        // Gọi API delete
        Call<ApiResponse<Void>> call = notificationService.deleteNotification(notificationId);
        
        call.enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Void>> call, 
                                 @NonNull Response<ApiResponse<Void>> response) {
                // showLoading(false);
                
                if (!isAdded() || getContext() == null) {
                    return;
                }
                
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Void> apiResponse = response.body();
                        
                        Log.d(TAG, "Delete API Response - Code: " + apiResponse.getCode());
                        Log.d(TAG, "Delete API Response - Message: " + apiResponse.getMessage());
                        
                        // Kiểm tra code == 9999 (success)
                        if (apiResponse.getCode() == 9999) {
                            Toast.makeText(getContext(), 
                                "Đã xóa thông báo", 
                                Toast.LENGTH_SHORT).show();
                            
                            // Refresh danh sách notifications
                            loadNotificationsFromAPI();
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? 
                                apiResponse.getMessage() : "Không thể xóa thông báo";
                            Log.e(TAG, "Delete API Error: " + errorMsg);
                            Toast.makeText(getContext(), 
                                "Lỗi: " + errorMsg, 
                                Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Delete response not successful - Code: " + response.code());
                        Toast.makeText(getContext(), 
                            "Lỗi xóa thông báo: " + response.code(), 
                            Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing delete response", e);
                    Toast.makeText(getContext(), 
                        "Lỗi: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<Void>> call, 
                                @NonNull Throwable t) {
                // showLoading(false);
                
                if (!isAdded() || getContext() == null) {
                    return;
                }
                
                Log.e(TAG, "Delete API call failed", t);
                Toast.makeText(getContext(), 
                    "Lỗi kết nối: " + t.getMessage(), 
                    Toast.LENGTH_LONG).show();
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
