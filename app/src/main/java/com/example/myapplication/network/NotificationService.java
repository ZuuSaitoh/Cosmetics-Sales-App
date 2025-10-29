package com.example.myapplication.network;

import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.NotificationDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Retrofit Service interface cho Notification APIs
 */
public interface NotificationService {
    
    /**
     * Lấy tất cả notifications của một user
     * GET /notifications/get-all-notifications-by-user-id/{userID}
     * 
     * @param userId ID của user
     * @return Call chứa ApiResponse với danh sách NotificationDTO
     */
    @GET("notifications/get-all-notifications-by-user-id/{userID}")
    Call<ApiResponse<List<NotificationDTO>>> getAllNotificationsByUserId(@Path("userID") Long userId);
    
    /**
     * Xóa một notification
     * DELETE /notifications/delete-notification/{notificationID}
     * 
     * @param notificationId ID của notification cần xóa
     * @return Call chứa ApiResponse (có thể chứa message hoặc null result)
     */
    @DELETE("notifications/delete-notification/{notificationID}")
    Call<ApiResponse<Void>> deleteNotification(@Path("notificationID") Long notificationId);
}


