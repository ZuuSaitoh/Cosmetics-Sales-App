package com.example.myapplication.network;

import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.BroadcastNotificationRequest;
import com.example.myapplication.network.dto.NotificationDTO;
import com.example.myapplication.network.dto.NotificationRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
     * Lấy danh sách notifications CHƯA ĐỌC của một user
     * GET /notifications/get-unread-notifications/{userID}
     * 
     * Response:
     * {
     *   "code": 9999,
     *   "message": "Success",
     *   "result": [
     *     {
     *       "notificationID": 6,
     *       "user": { "userID": 9, ... },
     *       "message": "...",
     *       "isRead": false,
     *       "createdAt": "2025-11-05T19:18:58.807"
     *     }
     *   ]
     * }
     * 
     * @param userId ID của user
     * @return Call chứa ApiResponse với danh sách NotificationDTO chưa đọc
     */
    @GET("notifications/get-unread-notifications/{userID}")
    Call<ApiResponse<List<NotificationDTO>>> getUnreadNotificationsByUserId(@Path("userID") Long userId);
    
    /**
     * Xóa một notification
     * DELETE /notifications/delete-notification/{notificationID}
     * 
     * Backend có thể trả về ApiResponse hoặc String
     * Dùng ResponseBody để handle cả 2 cases
     * 
     * @param notificationId ID của notification cần xóa
     * @return Call chứa ResponseBody (raw response)
     */
    @DELETE("notifications/delete-notification/{notificationID}")
    Call<okhttp3.ResponseBody> deleteNotification(@Path("notificationID") Long notificationId);
    
    /**
     * Đánh dấu một notification là đã đọc
     * PUT /notifications/mark-as-read/{notificationID}
     * 
     * @param notificationId ID của notification cần đánh dấu
     * @return Call chứa ResponseBody (raw response)
     */
    @retrofit2.http.PUT("notifications/mark-as-read/{notificationID}")
    Call<okhttp3.ResponseBody> markNotificationAsRead(@Path("notificationID") Long notificationId);
    
    /**
     * Đánh dấu tất cả notifications của user là đã đọc
     * PUT /notifications/mark-all-as-read/{userID}
     * 
     * @param userId ID của user
     * @return Call chứa ResponseBody (raw response)
     */
    @retrofit2.http.PUT("notifications/mark-all-as-read/{userID}")
    Call<okhttp3.ResponseBody> markAllNotificationsAsRead(@Path("userID") Long userId);
    
    /**
     * Tạo notification mới cho user
     * POST /notifications/create
     * 
     * Request body:
     * {
     *   "userId": 7,
     *   "message": "Đơn hàng #12345 của bạn đã được xác nhận",
     *   "notificationType": "ORDER",
     *   "dataPayload": "{\"orderId\":12345,\"status\":\"CONFIRMED\"}"
     * }
     * 
     * @param request NotificationRequest object chứa thông tin notification
     * @return Call chứa ApiResponse với NotificationDTO đã tạo
     */
    @POST("notifications/create")
    Call<ApiResponse<NotificationDTO>> createNotification(@Body NotificationRequest request);
    
    /**
     * Gửi notification cho TẤT CẢ users
     * POST /notifications/send-notification-to-all
     * 
     * Request body:
     * {
     *   "message": "🔥 FLASH SALE SỐC! Giảm 50% tất cả sản phẩm"
     * }
     * 
     * Response:
     * {
     *   "code": 0,
     *   "message": "string",
     *   "result": "string"
     * }
     * 
     * @param request BroadcastNotificationRequest chỉ chứa message
     * @return Call chứa ApiResponse với result message
     */
    @POST("notifications/send-notification-to-all")
    Call<ApiResponse<String>> sendNotificationToAll(@Body BroadcastNotificationRequest request);
}


