# 📱 NOTIFICATION CREATION - INTEGRATION GUIDE

## 📋 Overview

Hệ thống tạo notifications tự động cho user với API `POST /notifications/create`.
Các notifications này sẽ hiển thị ở tab **"Của bạn"** trong trang Notifications.

---

## 🎯 Files Đã Tạo

### 1. **NotificationRequest.java** - DTO cho API request
```
app/src/main/java/com/example/myapplication/network/dto/NotificationRequest.java
```

### 2. **NotificationService.java** - Thêm endpoint `createNotification()`
```
app/src/main/java/com/example/myapplication/network/NotificationService.java
```

### 3. **NotificationCreator.java** - Helper class với template methods
```
app/src/main/java/com/example/myapplication/notification/NotificationCreator.java
```

### 4. **NotificationUsageExamples.java** - Ví dụ cách sử dụng
```
app/src/main/java/com/example/myapplication/notification/NotificationUsageExamples.java
```

---

## 🔧 API Specification

### **Endpoint:** `POST /notifications/create`

### **Request Body:**
```json
{
  "userId": 7,
  "message": "Đơn hàng #12345 của bạn đã được xác nhận",
  "notificationType": "ORDER",
  "dataPayload": "{\"orderId\":\"12345\",\"status\":\"CONFIRMED\",\"action\":\"VIEW_ORDER\"}"
}
```

### **Response:** 200 OK
```json
{
  "code": 9999,
  "message": "Success",
  "result": {
    "notificationID": 15,
    "user": {
      "userID": 7,
      "username": "thaonhi",
      "email": "thaonhi@gmail.com",
      "role": "User"
    },
    "message": "Đơn hàng #12345 của bạn đã được xác nhận",
    "notificationType": "ORDER",
    "isRead": false,
    "createdAt": "2025-10-30T10:30:00.000"
  }
}
```

---

## 📊 Notification Types

| Type | Tab Display | Use Case |
|------|-------------|----------|
| `ORDER` | Của bạn | Cập nhật trạng thái đơn hàng |
| `PAYMENT` | Của bạn | Thanh toán, hoàn tiền |
| `SYSTEM` | Của bạn | Hàng về kho, nhắc đánh giá |

---

## 🚀 Cách Sử Dụng NotificationCreator

### **Basic Usage:**

```java
// 1. Khởi tạo NotificationCreator
NotificationCreator creator = new NotificationCreator(context);

// 2. Gọi method tương ứng
creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
    @Override
    public void onSuccess(NotificationDTO notification) {
        Log.d(TAG, "✅ Notification created: " + notification.getNotificationId());
        // Optional: Update UI hoặc show toast
    }
    
    @Override
    public void onError(String error) {
        Log.e(TAG, "❌ Error: " + error);
        // Handle error - thường không cần show cho user
    }
});
```

---

## 📌 Integration Points

### **1. Order Confirmation (Khi xác nhận đơn hàng)**

**File:** `CheckoutActivity.java` hoặc `OrderConfirmationActivity.java`

**Location:** Sau khi gọi API tạo đơn hàng thành công

```java
// Trong onResponse() của API create order
@Override
public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
    if (response.isSuccessful() && response.body() != null) {
        String orderId = response.body().getOrderId();
        Long userId = authManager.getUserId();
        
        // Tạo notification
        NotificationCreator creator = new NotificationCreator(this);
        creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                Log.d(TAG, "✅ Order confirmed notification sent");
            }
            
            @Override
            public void onError(String error) {
                // Log error nhưng không ảnh hưởng flow chính
                Log.e(TAG, "Failed to send notification: " + error);
            }
        });
        
        // Continue với flow chính (navigate to order success, etc.)
        navigateToOrderSuccess(orderId);
    }
}
```

---

### **2. Order Shipping (Khi giao hàng)**

**File:** `OrderTrackingService.java` hoặc admin update order status

**Location:** Sau khi admin cập nhật status = "SHIPPING"

```java
private void updateOrderStatus(String orderId, String status) {
    // Call API update order status
    orderService.updateStatus(orderId, status).enqueue(new Callback<ApiResponse>() {
        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            if (response.isSuccessful()) {
                // Nếu status = SHIPPING, gửi notification
                if ("SHIPPING".equals(status)) {
                    Long userId = getCurrentUserId(orderId); // Get from order
                    String estimatedTime = "15:00 hôm nay";
                    
                    NotificationCreator creator = new NotificationCreator(getApplicationContext());
                    creator.notifyOrderShipping(userId, orderId, estimatedTime, 
                        new NotificationCreator.OnNotificationCreatedListener() {
                            @Override
                            public void onSuccess(NotificationDTO notification) {
                                Log.d(TAG, "✅ Shipping notification sent");
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error: " + error);
                            }
                        });
                }
            }
        }
        
        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            // Handle failure
        }
    });
}
```

---

### **3. Order Delivered (Khi giao thành công)**

**File:** `OrderTrackingService.java` hoặc `DeliveryConfirmationActivity.java`

**Location:** Sau khi admin/shipper confirm giao hàng thành công

```java
private void confirmDelivery(String orderId) {
    orderService.confirmDelivery(orderId).enqueue(new Callback<ApiResponse>() {
        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            if (response.isSuccessful()) {
                Long userId = getCurrentUserId(orderId);
                
                NotificationCreator creator = new NotificationCreator(getApplicationContext());
                creator.notifyOrderDelivered(userId, orderId, 
                    new NotificationCreator.OnNotificationCreatedListener() {
                        @Override
                        public void onSuccess(NotificationDTO notification) {
                            Log.d(TAG, "✅ Delivery notification sent");
                            
                            // Optional: Schedule review reminder sau 2-3 ngày
                            scheduleReviewReminder(userId, orderId, 2); // 2 days later
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error: " + error);
                        }
                    });
            }
        }
        
        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            // Handle failure
        }
    });
}
```

---

### **4. Payment Success (Khi thanh toán thành công)**

**File:** `PaymentActivity.java` hoặc `PaymentCallbackActivity.java`

**Location:** Sau khi nhận callback thành công từ payment gateway

```java
private void handlePaymentCallback(String orderId, double amount, String status) {
    if ("SUCCESS".equals(status)) {
        Long userId = authManager.getUserId();
        
        NotificationCreator creator = new NotificationCreator(this);
        creator.notifyPaymentSuccess(userId, orderId, amount, 
            new NotificationCreator.OnNotificationCreatedListener() {
                @Override
                public void onSuccess(NotificationDTO notification) {
                    Log.d(TAG, "✅ Payment notification sent");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error: " + error);
                }
            });
        
        // Show success dialog
        showPaymentSuccessDialog(orderId);
    }
}
```

---

### **5. Payment Failed (Khi thanh toán thất bại)**

**File:** `PaymentActivity.java`

```java
private void handlePaymentCallback(String orderId, String status, String errorMessage) {
    if ("FAILED".equals(status)) {
        Long userId = authManager.getUserId();
        
        NotificationCreator creator = new NotificationCreator(this);
        creator.notifyPaymentFailed(userId, orderId, errorMessage, 
            new NotificationCreator.OnNotificationCreatedListener() {
                @Override
                public void onSuccess(NotificationDTO notification) {
                    Log.d(TAG, "✅ Payment failed notification sent");
                }
                
                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error: " + error);
                }
            });
        
        // Show retry dialog
        showPaymentFailedDialog(orderId, errorMessage);
    }
}
```

---

### **6. Refund Processed (Khi hoàn tiền)**

**File:** `RefundActivity.java` hoặc admin refund processing

```java
private void processRefund(String orderId, double amount) {
    refundService.processRefund(orderId, amount).enqueue(new Callback<ApiResponse>() {
        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            if (response.isSuccessful()) {
                Long userId = getCurrentUserId(orderId);
                
                NotificationCreator creator = new NotificationCreator(getApplicationContext());
                creator.notifyRefundProcessed(userId, orderId, amount, 
                    new NotificationCreator.OnNotificationCreatedListener() {
                        @Override
                        public void onSuccess(NotificationDTO notification) {
                            Log.d(TAG, "✅ Refund notification sent");
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error: " + error);
                        }
                    });
            }
        }
        
        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            // Handle failure
        }
    });
}
```

---

### **7. Back In Stock (Khi sản phẩm có hàng trở lại)**

**File:** `ProductStockUpdateService.java` hoặc `AdminProductActivity.java`

**Location:** Sau khi admin update stock quantity > 0 cho sản phẩm đã hết hàng

```java
private void updateProductStock(String productId, int newQuantity) {
    productService.updateStock(productId, newQuantity).enqueue(new Callback<ApiResponse>() {
        @Override
        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
            if (response.isSuccessful() && newQuantity > 0) {
                // Get danh sách users đã wishlist sản phẩm này
                List<Long> interestedUsers = getWishlistUsers(productId);
                String productName = getProductName(productId);
                
                NotificationCreator creator = new NotificationCreator(getApplicationContext());
                
                // Gửi notification cho tất cả users quan tâm
                for (Long userId : interestedUsers) {
                    creator.notifyBackInStock(userId, productId, productName, 
                        new NotificationCreator.OnNotificationCreatedListener() {
                            @Override
                            public void onSuccess(NotificationDTO notification) {
                                Log.d(TAG, "✅ Back-in-stock notification sent to: " + userId);
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error: " + error);
                            }
                        });
                }
            }
        }
        
        @Override
        public void onFailure(Call<ApiResponse> call, Throwable t) {
            // Handle failure
        }
    });
}
```

---

### **8. Review Reminder (Nhắc nhở đánh giá)**

**File:** `ScheduledJobService.java` hoặc `WorkManager` background task

**Location:** Background job chạy sau 2-3 ngày khi đơn hàng giao thành công

```java
public class ReviewReminderWorker extends Worker {
    
    public ReviewReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        // Get orders delivered 2-3 days ago without reviews
        List<Order> ordersNeedReview = getOrdersNeedingReview();
        
        NotificationCreator creator = new NotificationCreator(getApplicationContext());
        
        for (Order order : ordersNeedReview) {
            // Gửi reminder cho từng sản phẩm trong đơn
            for (OrderItem item : order.getItems()) {
                creator.notifyReviewReminder(
                    order.getUserId(), 
                    order.getOrderId(), 
                    item.getProductId(), 
                    item.getProductName(),
                    new NotificationCreator.OnNotificationCreatedListener() {
                        @Override
                        public void onSuccess(NotificationDTO notification) {
                            Log.d(TAG, "✅ Review reminder sent");
                        }
                        
                        @Override
                        public void onError(String error) {
                            Log.e(TAG, "Error: " + error);
                        }
                    });
            }
        }
        
        return Result.success();
    }
}

// Schedule worker (trong Application.onCreate() hoặc OrderDeliveredActivity)
private void scheduleReviewReminder() {
    OneTimeWorkRequest reviewReminderWork = new OneTimeWorkRequest.Builder(ReviewReminderWorker.class)
        .setInitialDelay(2, TimeUnit.DAYS)  // Chạy sau 2 ngày
        .build();
    
    WorkManager.getInstance(this).enqueue(reviewReminderWork);
}
```

---

## 🎯 Best Practices

### **1. Error Handling**
```java
// ✅ GOOD: Silent fail - không ảnh hưởng user experience
creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
    @Override
    public void onSuccess(NotificationDTO notification) {
        Log.d(TAG, "Notification sent");
    }
    
    @Override
    public void onError(String error) {
        // Chỉ log error, không show dialog cho user
        Log.e(TAG, "Failed to send notification: " + error);
    }
});

// ❌ BAD: Show error dialog - làm gián đoạn UX
creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
    @Override
    public void onError(String error) {
        showErrorDialog("Không thể gửi thông báo: " + error);  // Don't do this!
    }
});
```

### **2. Timing**
```java
// ✅ GOOD: Gửi notification AFTER main action thành công
orderService.createOrder(order).enqueue(new Callback<OrderResponse>() {
    @Override
    public void onResponse(...) {
        if (response.isSuccessful()) {
            // 1. Main action thành công
            showSuccessDialog();
            
            // 2. Gửi notification (không blocking)
            notificationCreator.notifyOrderConfirmed(...);
        }
    }
});

// ❌ BAD: Gửi notification BEFORE main action
notificationCreator.notifyOrderConfirmed(...);  // Too early!
orderService.createOrder(order).enqueue(...);   // Main action
```

### **3. User Context**
```java
// ✅ GOOD: Kiểm tra user đã login
Long userId = authManager.getUserId();
if (userId != null) {
    notificationCreator.notifyOrderConfirmed(userId, orderId, ...);
} else {
    Log.w(TAG, "User not logged in - skipping notification");
}

// ❌ BAD: Không kiểm tra userId
notificationCreator.notifyOrderConfirmed(null, orderId, ...);  // Will error!
```

### **4. Batch Operations**
```java
// ✅ GOOD: Gửi nhiều notifications song song
for (Long userId : userIds) {
    notificationCreator.notifyBackInStock(userId, productId, productName, ...);
}

// OK: Các API calls chạy async, không blocking nhau
```

---

## 📊 DataPayload Structure

DataPayload là JSON string chứa metadata cho notification. Frontend sẽ dùng để navigate hoặc display detail.

### **ORDER Notifications:**
```json
{
  "orderId": "12345",
  "status": "CONFIRMED",
  "action": "VIEW_ORDER"
}
```

### **PAYMENT Notifications:**
```json
{
  "orderId": "12345",
  "amount": 500000,
  "status": "SUCCESS",
  "action": "VIEW_RECEIPT"
}
```

### **SYSTEM Notifications (Back In Stock):**
```json
{
  "productId": "PROD-001",
  "productName": "Son môi MAC",
  "action": "VIEW_PRODUCT"
}
```

### **SYSTEM Notifications (Review Reminder):**
```json
{
  "orderId": "12345",
  "productId": "PROD-001",
  "productName": "Son môi MAC",
  "action": "WRITE_REVIEW"
}
```

---

## 🧪 Testing

### **Test 1: Order Confirmed**
```java
@Test
public void testNotifyOrderConfirmed() {
    NotificationCreator creator = new NotificationCreator(context);
    
    creator.notifyOrderConfirmed(7L, "ORDER-123", new OnNotificationCreatedListener() {
        @Override
        public void onSuccess(NotificationDTO notification) {
            assertNotNull(notification);
            assertEquals("ORDER", notification.getNotificationType());
            assertTrue(notification.getMessage().contains("ORDER-123"));
        }
        
        @Override
        public void onError(String error) {
            fail("Should not fail: " + error);
        }
    });
}
```

### **Test 2: Manual Test trong App**
1. Login vào app với userId = 7
2. Tạo 1 đơn hàng mới
3. Xác nhận đơn hàng thành công
4. Mở tab "Của bạn" trong Notifications
5. Kiểm tra notification "Đơn hàng #XXX của bạn đã được xác nhận" hiển thị
6. Click vào notification → Navigate đến order detail

---

## 📝 TODO - Implementation Checklist

- [ ] **Step 1:** Test API endpoint `POST /notifications/create` với Postman/Swagger
- [ ] **Step 2:** Integrate vào `CheckoutActivity` (Order Confirmed)
- [ ] **Step 3:** Integrate vào `PaymentActivity` (Payment Success/Failed)
- [ ] **Step 4:** Integrate vào admin order update (Shipping/Delivered)
- [ ] **Step 5:** Integrate vào admin refund process (Refund Processed)
- [ ] **Step 6:** Implement wishlist tracking cho Back In Stock
- [ ] **Step 7:** Setup WorkManager cho Review Reminder
- [ ] **Step 8:** Test tất cả notifications trong tab "Của bạn"
- [ ] **Step 9:** Implement navigation logic trong `NotificationsFragment` cho các actions
- [ ] **Step 10:** Monitor logs và fix issues

---

## 🚀 Next Steps

1. **Build app** để compile các files mới
2. **Test API** với backend team
3. **Integrate** vào các activities theo guide
4. **Test end-to-end** flow
5. **Monitor** production logs

---

## 📞 Support

Nếu có vấn đề, check logs:
```
adb logcat | grep "NotificationCreator"
adb logcat | grep "NotificationsFragment"
```

Hoặc tham khảo `NotificationUsageExamples.java` để xem các ví dụ chi tiết.

