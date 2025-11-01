package com.example.myapplication.notification;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.myapplication.network.dto.NotificationDTO;

/**
 * VÍ DỤ CÁCH SỬ DỤNG NotificationCreator
 * 
 * File này chỉ để tham khảo, không được gọi trực tiếp.
 * Copy các methods vào Activities/Services tương ứng.
 */
public class NotificationUsageExamples {
    
    private static final String TAG = "NotificationExamples";
    
    // ============================================
    // EXAMPLE 1: TẠO NOTIFICATION KHI XÁC NHẬN ĐƠN HÀNG
    // Sử dụng trong: OrderConfirmationActivity, CheckoutActivity
    // ============================================
    
    public static void exampleOrderConfirmed(Context context, Long userId, String orderId) {
        NotificationCreator creator = new NotificationCreator(context);
        
        creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Notification created for order confirmed: " + orderId);
                // Optional: Show toast or update UI
                Toast.makeText(context, "Đã gửi thông báo xác nhận đơn hàng", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Failed to create notification: " + error);
                // Handle error silently - không cần show error cho user
            }
        });
    }
    
    // ============================================
    // EXAMPLE 2: TẠO NOTIFICATION KHI GIAO HÀNG
    // Sử dụng trong: OrderTrackingService, AdminOrderUpdateActivity
    // ============================================
    
    public static void exampleOrderShipping(Context context, Long userId, String orderId) {
        NotificationCreator creator = new NotificationCreator(context);
        
        String estimatedTime = "15:00 hôm nay";  // Lấy từ delivery API
        
        creator.notifyOrderShipping(userId, orderId, estimatedTime, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Notification created for order shipping: " + orderId);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
            }
        });
    }
    
    // ============================================
    // EXAMPLE 3: TẠO NOTIFICATION KHI GIAO THÀNH CÔNG
    // Sử dụng trong: OrderTrackingService, DeliveryConfirmationActivity
    // ============================================
    
    public static void exampleOrderDelivered(Context context, Long userId, String orderId) {
        NotificationCreator creator = new NotificationCreator(context);
        
        creator.notifyOrderDelivered(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Order delivered notification sent");
                // Sau khi giao thành công, có thể schedule notification nhắc đánh giá sau 1-2 ngày
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
            }
        });
    }
    
    // ============================================
    // EXAMPLE 4: TẠO NOTIFICATION KHI THANH TOÁN THÀNH CÔNG
    // Sử dụng trong: PaymentActivity, PaymentCallback
    // ============================================
    
    public static void examplePaymentSuccess(Context context, Long userId, String orderId, double amount) {
        NotificationCreator creator = new NotificationCreator(context);
        
        creator.notifyPaymentSuccess(userId, orderId, amount, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Payment success notification sent");
                Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
                // Vẫn show payment success cho user
                Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // ============================================
    // EXAMPLE 5: TẠO NOTIFICATION KHI THANH TOÁN THẤT BẠI
    // Sử dụng trong: PaymentActivity, PaymentCallback
    // ============================================
    
    public static void examplePaymentFailed(Context context, Long userId, String orderId) {
        NotificationCreator creator = new NotificationCreator(context);
        
        String reason = "Số dư không đủ";  // Lấy từ payment gateway response
        
        creator.notifyPaymentFailed(userId, orderId, reason, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Payment failed notification sent");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
            }
        });
    }
    
    // ============================================
    // EXAMPLE 6: TẠO NOTIFICATION KHI HOÀN TIỀN
    // Sử dụng trong: RefundActivity, AdminRefundProcessActivity
    // ============================================
    
    public static void exampleRefundProcessed(Context context, Long userId, String orderId, double amount) {
        NotificationCreator creator = new NotificationCreator(context);
        
        creator.notifyRefundProcessed(userId, orderId, amount, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Refund notification sent");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
            }
        });
    }
    
    // ============================================
    // EXAMPLE 7: TẠO NOTIFICATION KHI SẢN PHẨM CÓ HÀNG TRỞ LẠI
    // Sử dụng trong: ProductStockUpdateService, AdminProductActivity
    // ============================================
    
    public static void exampleBackInStock(Context context, Long userId, String productId, String productName) {
        NotificationCreator creator = new NotificationCreator(context);
        
        creator.notifyBackInStock(userId, productId, productName, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Back-in-stock notification sent to user: " + userId);
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
            }
        });
    }
    
    // ============================================
    // EXAMPLE 8: TẠO NOTIFICATION NHẮC NHỞ ĐÁNH GIÁ
    // Sử dụng trong: ScheduledJobService (chạy sau 2-3 ngày khi giao hàng)
    // ============================================
    
    public static void exampleReviewReminder(Context context, Long userId, String orderId, 
                                            String productId, String productName) {
        NotificationCreator creator = new NotificationCreator(context);
        
        creator.notifyReviewReminder(userId, orderId, productId, productName, 
            new NotificationCreator.OnNotificationCreatedListener() {
                @Override
                public void onSuccess(NotificationDTO notification) {
                    Log.d(TAG, "✅ Review reminder sent");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error: " + error);
                }
            });
    }
    
    // ============================================
    // EXAMPLE 9: SỬ DỤNG CONVENIENCE METHOD (Tự động lấy userId)
    // Sử dụng khi đã có context và user đang login
    // ============================================
    
    public static void exampleCurrentUser(Context context) {
        NotificationCreator creator = new NotificationCreator(context);
        
        String message = "Thông báo custom cho user hiện tại";
        String dataPayload = "{\"custom\":\"data\"}";
        
        creator.notifyCurrentUser(message, NotificationCreator.TYPE_SYSTEM, dataPayload, 
            new NotificationCreator.OnNotificationCreatedListener() {
                @Override
                public void onSuccess(NotificationDTO notification) {
                    Log.d(TAG, "✅ Notification sent to current user");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Error: " + error);
                }
            });
    }
    
    // ============================================
    // EXAMPLE 10: BATCH NOTIFICATIONS
    // Gửi nhiều notifications cùng lúc
    // ============================================
    
    public static void exampleBatchNotifications(Context context, Long userId, String orderId) {
        NotificationCreator creator = new NotificationCreator(context);
        
        // 1. Xác nhận đơn hàng
        creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Order confirmed notification sent");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
            }
        });
        
        // 2. Thanh toán thành công (nếu có)
        double amount = 500000;
        creator.notifyPaymentSuccess(userId, orderId, amount, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Payment success notification sent");
            }
            
            @Override
            public void onError(String error) {
                Log.e(TAG, "❌ Error: " + error);
            }
        });
    }
}

