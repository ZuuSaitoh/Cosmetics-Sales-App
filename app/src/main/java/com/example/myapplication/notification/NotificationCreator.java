package com.example.myapplication.notification;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.NotificationService;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.NotificationDTO;
import com.example.myapplication.network.dto.NotificationRequest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class để tạo notifications cho user
 * Sử dụng API: POST /notifications/create
 * 
 * Các loại notifications:
 * - ORDER: Cập nhật trạng thái đơn hàng
 * - PAYMENT: Thanh toán và hoàn tiền
 * - SYSTEM: Hàng về kho, nhắc nhở đánh giá
 */
public class NotificationCreator {
    
    private static final String TAG = "NotificationCreator";
    
    private final Context context;
    private final NotificationService notificationService;
    private final AuthManager authManager;
    
    // Notification Types
    public static final String TYPE_CART = "CART";
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_PAYMENT = "PAYMENT";
    public static final String TYPE_SYSTEM = "SYSTEM";
    
    // Order Status
    public static final String ORDER_CONFIRMED = "CONFIRMED";
    public static final String ORDER_SHIPPING = "SHIPPING";
    public static final String ORDER_DELIVERED = "DELIVERED";
    public static final String ORDER_CANCELLED = "CANCELLED";
    
    public NotificationCreator(Context context) {
        this.context = context.getApplicationContext();
        this.notificationService = ApiClient.getRetrofit(context).create(NotificationService.class);
        this.authManager = new AuthManager(context);
    }
    
    /**
     * Interface để handle callback khi tạo notification
     */
    public interface OnNotificationCreatedListener {
        void onSuccess(NotificationDTO notification);
        void onError(String error);
    }
    
    // ============================================
    // 1. NOTIFICATIONS GIỎ HÀNG
    // ============================================
    
    /**
     * Thông báo cập nhật giỏ hàng
     * "Giỏ hàng của bạn có X sản phẩm"
     * Hiển thị ở tab "Của bạn"
     */
    public void notifyCartUpdate(Long userId, int itemCount, OnNotificationCreatedListener listener) {
        String message;
        if (itemCount == 1) {
            message = "Giỏ hàng của bạn có 1 sản phẩm";
        } else {
            message = "Giỏ hàng của bạn có " + itemCount + " sản phẩm";
        }
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("itemCount", itemCount);
            payload.put("action", "VIEW_CART");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_CART, payload.toString(), listener);
    }
    
    /**
     * Thông báo thêm sản phẩm vào giỏ hàng
     * "Bạn vừa thêm [Tên sản phẩm] vào giỏ hàng"
     */
    public void notifyProductAddedToCart(Long userId, String productName, int quantity, int totalItems, OnNotificationCreatedListener listener) {
        String message;
        if (quantity == 1) {
            message = "Bạn vừa thêm \"" + productName + "\" vào giỏ hàng";
        } else {
            message = "Bạn vừa thêm " + quantity + "x \"" + productName + "\" vào giỏ hàng";
        }
        
        if (totalItems > 0) {
            message += ". Giỏ hàng có " + totalItems + " sản phẩm.";
        }
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("productName", productName);
            payload.put("quantity", quantity);
            payload.put("totalItems", totalItems);
            payload.put("action", "VIEW_CART");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_CART, payload.toString(), listener);
    }
    
    // ============================================
    // 2. NOTIFICATIONS CẬP NHẬT TRẠNG THÁI ĐỚN HÀNG
    // ============================================
    
    /**
     * Đơn hàng đã được xác nhận
     * "Đơn hàng #12345 của bạn đã được xác nhận."
     */
    public void notifyOrderConfirmed(Long userId, String orderId, OnNotificationCreatedListener listener) {
        String message = "Đơn hàng #" + orderId + " của bạn đã được xác nhận.";
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("status", ORDER_CONFIRMED);
            payload.put("action", "VIEW_ORDER");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_ORDER, payload.toString(), listener);
    }
    
    /**
     * Đơn hàng đang được giao
     * "Đơn hàng đang được giao (Dự kiến: 15:00 hôm nay)."
     */
    public void notifyOrderShipping(Long userId, String orderId, String estimatedTime, OnNotificationCreatedListener listener) {
        String message = "Đơn hàng #" + orderId + " đang được giao";
        if (estimatedTime != null && !estimatedTime.isEmpty()) {
            message += " (Dự kiến: " + estimatedTime + ")";
        }
        message += ".";
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("status", ORDER_SHIPPING);
            payload.put("estimatedTime", estimatedTime);
            payload.put("action", "TRACK_ORDER");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_ORDER, payload.toString(), listener);
    }
    
    /**
     * Đơn hàng đã giao thành công
     * "Đơn hàng #12345 đã giao thành công."
     */
    public void notifyOrderDelivered(Long userId, String orderId, OnNotificationCreatedListener listener) {
        String message = "Đơn hàng #" + orderId + " đã giao thành công.";
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("status", ORDER_DELIVERED);
            payload.put("action", "REVIEW_ORDER");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_ORDER, payload.toString(), listener);
    }
    
    /**
     * Đơn hàng đã bị hủy
     * "Đơn hàng #12345 đã bị hủy."
     */
    public void notifyOrderCancelled(Long userId, String orderId, String reason, OnNotificationCreatedListener listener) {
        String message = "Đơn hàng #" + orderId + " đã bị hủy";
        if (reason != null && !reason.isEmpty()) {
            message += ": " + reason;
        }
        message += ".";
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("status", ORDER_CANCELLED);
            payload.put("reason", reason);
            payload.put("action", "VIEW_ORDER");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_ORDER, payload.toString(), listener);
    }
    
    // ============================================
    // 3. NOTIFICATIONS THANH TOÁN VÀ HOÀN TIỀN
    // ============================================
    
    /**
     * Giao dịch thanh toán thành công
     * "Giao dịch thanh toán của bạn đã thành công."
     */
    public void notifyPaymentSuccess(Long userId, String orderId, double amount, OnNotificationCreatedListener listener) {
        String message = "Giao dịch thanh toán cho đơn hàng #" + orderId + " đã thành công. Số tiền: " + formatCurrency(amount);
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("amount", amount);
            payload.put("status", "SUCCESS");
            payload.put("action", "VIEW_RECEIPT");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_PAYMENT, payload.toString(), listener);
    }
    
    /**
     * Giao dịch thanh toán thất bại
     * "Giao dịch thanh toán thất bại. Vui lòng thử lại."
     */
    public void notifyPaymentFailed(Long userId, String orderId, String reason, OnNotificationCreatedListener listener) {
        String message = "Giao dịch thanh toán cho đơn hàng #" + orderId + " thất bại";
        if (reason != null && !reason.isEmpty()) {
            message += ": " + reason;
        }
        message += ". Vui lòng thử lại.";
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("status", "FAILED");
            payload.put("reason", reason);
            payload.put("action", "RETRY_PAYMENT");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_PAYMENT, payload.toString(), listener);
    }
    
    /**
     * Yêu cầu hoàn tiền đã được xử lý
     * "Yêu cầu hoàn tiền cho đơn hàng #12345 đã được xử lý."
     */
    public void notifyRefundProcessed(Long userId, String orderId, double amount, OnNotificationCreatedListener listener) {
        String message = "Yêu cầu hoàn tiền cho đơn hàng #" + orderId + " đã được xử lý. Số tiền: " + formatCurrency(amount);
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("amount", amount);
            payload.put("status", "REFUNDED");
            payload.put("action", "VIEW_REFUND");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_PAYMENT, payload.toString(), listener);
    }
    
    // ============================================
    // 4. NOTIFICATIONS HÀNG VỀ KHO
    // ============================================
    
    /**
     * Sản phẩm đã có hàng trở lại
     * "Sản phẩm [Tên sản phẩm] bạn yêu thích đã có hàng trở lại!"
     */
    public void notifyBackInStock(Long userId, String productId, String productName, OnNotificationCreatedListener listener) {
        String message = "Sản phẩm \"" + productName + "\" bạn yêu thích đã có hàng trở lại!";
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("productId", productId);
            payload.put("productName", productName);
            payload.put("action", "VIEW_PRODUCT");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_SYSTEM, payload.toString(), listener);
    }
    
    // ============================================
    // 5. NOTIFICATIONS NHẮC NHỞ ĐÁNH GIÁ
    // ============================================
    
    /**
     * Nhắc nhở đánh giá sản phẩm
     * "Hãy chia sẻ cảm nhận về sản phẩm [Tên sản phẩm] bạn đã mua!"
     */
    public void notifyReviewReminder(Long userId, String orderId, String productId, String productName, OnNotificationCreatedListener listener) {
        String message = "Hãy chia sẻ cảm nhận về sản phẩm \"" + productName + "\" bạn đã mua!";
        
        JSONObject payload = new JSONObject();
        try {
            payload.put("orderId", orderId);
            payload.put("productId", productId);
            payload.put("productName", productName);
            payload.put("action", "WRITE_REVIEW");
        } catch (Exception e) {
            Log.e(TAG, "Error creating payload", e);
        }
        
        createNotification(userId, message, TYPE_SYSTEM, payload.toString(), listener);
    }
    
    // ============================================
    // HELPER METHODS
    // ============================================
    
    /**
     * Method chung để tạo notification
     */
    private void createNotification(Long userId, String message, String notificationType, 
                                   String dataPayload, OnNotificationCreatedListener listener) {
        
        // Validate userId
        if (userId == null || userId <= 0) {
            Log.e(TAG, "Invalid userId: " + userId);
            if (listener != null) {
                listener.onError("User ID không hợp lệ");
            }
            return;
        }
        
        // Validate message
        if (message == null || message.isEmpty()) {
            Log.e(TAG, "Message is empty");
            if (listener != null) {
                listener.onError("Message không được để trống");
            }
            return;
        }
        
        Log.d(TAG, "Creating notification - UserId: " + userId + ", Type: " + notificationType);
        Log.d(TAG, "Message: " + message);
        
        // Tạo request object
        NotificationRequest request = new NotificationRequest(userId, message, notificationType, dataPayload);
        
        // Gọi API
        Call<ApiResponse<NotificationDTO>> call = notificationService.createNotification(request);
        
        call.enqueue(new Callback<ApiResponse<NotificationDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<NotificationDTO>> call, 
                                 @NonNull Response<ApiResponse<NotificationDTO>> response) {
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<NotificationDTO> apiResponse = response.body();
                    
                    if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                        Log.d(TAG, "✅ Notification created successfully - ID: " + 
                              apiResponse.getResult().getNotificationId());
                        
                        if (listener != null) {
                            listener.onSuccess(apiResponse.getResult());
                        }
                    } else {
                        String error = "API Error: " + apiResponse.getMessage();
                        Log.e(TAG, error);
                        if (listener != null) {
                            listener.onError(error);
                        }
                    }
                } else {
                    String error = "HTTP Error: " + response.code();
                    Log.e(TAG, error);
                    if (listener != null) {
                        listener.onError(error);
                    }
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<NotificationDTO>> call, @NonNull Throwable t) {
                String error = "Network Error: " + t.getMessage();
                Log.e(TAG, error, t);
                if (listener != null) {
                    listener.onError(error);
                }
            }
        });
    }
    
    /**
     * Format currency (VND)
     */
    private String formatCurrency(double amount) {
        return String.format("%,.0f đ", amount);
    }
    
    // ============================================
    // CONVENIENCE METHODS - Tự động lấy userId từ AuthManager
    // ============================================
    
    /**
     * Tạo notification cho current logged-in user
     */
    public void notifyCurrentUser(String message, String notificationType, String dataPayload, 
                                 OnNotificationCreatedListener listener) {
        Long userId = authManager.getUserId();
        if (userId == null) {
            Log.e(TAG, "No user logged in");
            if (listener != null) {
                listener.onError("Người dùng chưa đăng nhập");
            }
            return;
        }
        
        createNotification(userId, message, notificationType, dataPayload, listener);
    }
}

