# 📊 Báo Cáo Luồng Notification - Giỏ Hàng & Đơn Hàng

## 1️⃣ NOTIFICATION KHI THÊM SẢN PHẨM VÀO GIỎ HÀNG

### 📍 Vị trí code:
**File:** `ProductDetailActivity.java` (line 659-688)

### 🔄 Luồng hoạt động:

```java
// Bước 1: User thêm sản phẩm vào giỏ hàng
callAddProduct(cartId, productId, quantity);

// Bước 2: Sau khi thêm thành công, tạo notification
createCartNotification(quantity);

// Bước 3: Gọi NotificationCreator
NotificationCreator creator = new NotificationCreator(this);
creator.notifyProductAddedToCart(userId, productName, addedQuantity, totalItems, 
    new NotificationCreator.OnNotificationCreatedListener() {
        @Override
        public void onSuccess(NotificationDTO notification) {
            Log.d("ProductDetailActivity", "✅ Cart notification created: " + notification.getNotificationId());
        }
        
        @Override
        public void onError(String error) {
            Log.e("ProductDetailActivity", "Failed to create cart notification: " + error);
        }
    });
```

### 📤 Request gửi lên backend:

```json
POST /notifications/create
{
  "userID": 9,
  "message": "Bạn vừa thêm \"Kem chống nắng\" vào giỏ hàng. Giỏ hàng có 3 sản phẩm.",
  "notificationType": "CART",  ← ✅ ĐÚNG TYPE
  "dataPayload": "{\"productName\":\"Kem chống nắng\",\"quantity\":1,\"totalItems\":3,\"action\":\"VIEW_CART\"}"
}
```

---

## 2️⃣ NOTIFICATION KHI ĐƠN HÀNG ĐƯỢC APPROVE

### 📍 Vị trí code:
**File:** `ActivityCheckout.java` (line 397-427)

### 🔄 Luồng hoạt động:

```java
// Bước 1: User đặt hàng thành công (COD hoặc VNPay)
// COD:
handleCODPaymentSuccess(order);

// VNPay:
handleVNPayPaymentSuccess(order);

// Bước 2: Sau khi đặt hàng thành công, tạo notification
createOrderNotification(order);

// Bước 3: Gọi NotificationCreator
NotificationCreator creator = new NotificationCreator(this);
creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
    @Override
    public void onSuccess(NotificationDTO notification) {
        Log.d("Checkout", "✅ Order notification created successfully - ID: " + notification.getNotificationId());
    }
    
    @Override
    public void onError(String error) {
        Log.e("Checkout", "Failed to create order notification: " + error);
    }
});
```

### 📤 Request gửi lên backend:

```json
POST /notifications/create
{
  "userID": 9,
  "message": "Đơn hàng #12345 của bạn đã được xác nhận.",
  "notificationType": "ORDER",  ← ✅ ĐÚNG TYPE
  "dataPayload": "{\"orderId\":\"12345\",\"status\":\"CONFIRMED\",\"action\":\"VIEW_ORDER\"}"
}
```

---

## 🐛 VẤN ĐỀ HIỆN TẠI

### ❌ Backend KHÔNG lưu `notificationType`

1. **Frontend GỬI:**
   - `notificationType: "CART"` hoặc `"ORDER"`
   
2. **Backend NHẬN nhưng BỎ QUA:**
   - Backend không lưu field `notificationType` từ request
   - Backend tự động set: `notificationType = "SYSTEM"` (mặc định)
   
3. **Backend TRẢ VỀ:**
   - `notificationType: "SYSTEM"` (SAI!)
   
4. **Frontend NHẬN và FILTER:**
   - Type = `SYSTEM` → Filter vào tab **"Khuyến mãi"** (dành cho admin)
   - ❌ Notification KHÔNG hiển thị ở tab **"Của bạn"**

---

## 📊 KẾT QUẢ HIỆN TẠI

### Tab "Khuyến mãi":
- ❌ **SAI:** Có notification giỏ hàng và đơn hàng (vì backend lưu type = "SYSTEM")
- ✅ **ĐÚNG:** Có notification Flash Sale từ admin

### Tab "Của bạn":
- ❌ **THIẾU:** Không có notification giỏ hàng và đơn hàng
- ⚠️ **LƯU Ý:** Tab này đang dùng API `/notifications/get-unread-notifications/{userID}`
  - Hiển thị **TẤT CẢ** notification CHƯA ĐỌC
  - Nhưng nếu notification bị mark as read → biến mất

---

## ✅ GIẢI PHÁP

### Backend cần sửa:

**File backend:** `NotificationController.java` (hoặc tương tự)

```java
@PostMapping("/create")
public ResponseEntity<?> createNotification(@RequestBody NotificationRequest request) {
    Notification notification = new Notification();
    notification.setUserId(request.getUserID());
    notification.setMessage(request.getMessage());
    
    // ✅✅✅ THÊM DÒNG NÀY
    if (request.getNotificationType() != null && !request.getNotificationType().isEmpty()) {
        notification.setNotificationType(request.getNotificationType());
    } else {
        notification.setNotificationType("SYSTEM"); // Default
    }
    
    notification.setDataPayload(request.getDataPayload());
    notification.setIsRead(false);
    notification.setCreatedAt(LocalDateTime.now());
    
    notificationRepository.save(notification);
    return ResponseEntity.ok(notification);
}
```

### Sau khi backend sửa:

| Loại Notification | Type được lưu | Tab hiển thị | Khi chưa đọc |
|-------------------|---------------|--------------|--------------|
| Thêm vào giỏ hàng | `CART` ✅ | Của bạn ✅ | ✅ Hiển thị |
| Đơn hàng xác nhận | `ORDER` ✅ | Của bạn ✅ | ✅ Hiển thị |
| Flash Sale (admin) | `PROMOTION` ✅ | Khuyến mãi ✅ | ✅ Hiển thị |
| Thông báo hệ thống | `SYSTEM` ✅ | Khuyến mãi ✅ | ✅ Hiển thị |

---

## 🧪 CÁCH KIỂM TRA VẤN ĐỀ

### Bước 1: Xóa tất cả notification cũ (trong database)

### Bước 2: Thêm 1 sản phẩm vào giỏ hàng

### Bước 3: Mở Logcat và tìm log:

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📤 CREATING NOTIFICATION
UserId: 9
Type: CART                    ← Frontend GỬI
Message: Bạn vừa thêm "..." vào giỏ hàng
Request JSON: {"userID":9,"message":"...","notificationType":"CART",...}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 NOTIFICATION CREATED SUCCESSFULLY
Type FROM BACKEND: SYSTEM     ← Backend TRẢ VỀ (SAI!)
⚠️ EXPECTED Type: CART
❌ TYPE MISMATCH!
   Sent to backend: CART
   Received from backend: SYSTEM
   => Backend is NOT saving notificationType from request!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Bước 4: Vào trang Notification

- **Nếu backend CHƯA SỬA:**
  - Tab "Khuyến mãi": ❌ Thấy notification "Bạn vừa thêm... vào giỏ hàng"
  - Tab "Của bạn": ✅ Cũng thấy (vì API get-unread-notifications trả về tất cả)

- **Nếu backend ĐÃ SỬA:**
  - Tab "Khuyến mãi": ❌ KHÔNG thấy notification giỏ hàng
  - Tab "Của bạn": ✅ Thấy notification giỏ hàng

---

## 📝 FRONTEND ĐÃ ĐÚNG

✅ Frontend đã gửi đầy đủ `notificationType` trong request  
✅ Frontend đã log chi tiết để debug  
✅ Tab "Của bạn" đang dùng API unread (hiển thị tất cả notification chưa đọc)  

❌ Vấn đề là **BACKEND** không lưu field `notificationType`

---

## 🎯 CHECKLIST SAU KHI SỬA BACKEND

- [ ] Thêm sản phẩm vào giỏ → Notification xuất hiện ở tab "Của bạn" (chưa đọc)
- [ ] Đơn hàng được approve → Notification xuất hiện ở tab "Của bạn" (chưa đọc)
- [ ] Admin gửi Flash Sale → Notification xuất hiện ở tab "Khuyến mãi" (chưa đọc)
- [ ] Mark notification as read → Notification biến mất khỏi tab "Của bạn"
- [ ] Log không còn show "TYPE MISMATCH"

---

## 💡 TÓM TẮT

| Thành phần | Trạng thái | Ghi chú |
|------------|------------|---------|
| **Frontend (App)** | ✅ ĐÚNG | Đã gửi type CART, ORDER |
| **Backend API** | ❌ SAI | Không lưu type từ request |
| **Tab "Của bạn"** | ⚠️ OK | Hiển thị tất cả unread (đúng) |
| **Tab "Khuyến mãi"** | ⚠️ OK | Filter PROMOTION, SYSTEM |

**Kết luận:** Backend cần sửa để lưu `notificationType` từ request!

