# 📋 DANH SÁCH CÁC FILE CHO CHỨC NĂNG NOTIFICATION

## 📁 1. MODEL & ENTITY

### 1.1. Model (Room Entity)
- **`app/src/main/java/com/example/myapplication/model/Notification.java`**
  - Entity class cho Notification (Room database)
  - Chứa: notificationId, userId, message, isRead, createdAt, notificationType, dataPayload, title
  - Có các constants cho notification types: CART, ORDER, PROMOTION, SYSTEM, PAYMENT

---

## 📁 2. NETWORK LAYER

### 2.1. API Service
- **`app/src/main/java/com/example/myapplication/network/NotificationService.java`**
  - Retrofit interface định nghĩa các API endpoints:
    - `getAllNotificationsByUserId()` - Lấy tất cả notifications
    - `getUnreadNotificationsByUserId()` - Lấy notifications chưa đọc
    - `createNotification()` - Tạo notification mới
    - `markNotificationAsRead()` - Đánh dấu đã đọc
    - `markAllNotificationsAsRead()` - Đánh dấu tất cả đã đọc
    - `deleteNotification()` - Xóa notification
    - `sendNotificationToAll()` - Gửi notification cho tất cả users

### 2.2. DTOs (Data Transfer Objects)
- **`app/src/main/java/com/example/myapplication/network/dto/NotificationRequest.java`**
  - DTO cho request tạo notification
  - Fields: userId, message, notificationType, dataPayload, title

- **`app/src/main/java/com/example/myapplication/network/dto/NotificationDTO.java`**
  - DTO cho response từ API
  - Hỗ trợ nhiều field name variations (notificationId, notification_id, etc.)
  - Chứa nested UserDTO object

- **`app/src/main/java/com/example/myapplication/network/dto/BroadcastNotificationRequest.java`**
  - DTO cho request gửi notification cho tất cả users
  - Fields: message, title
  - Dùng cho API: `POST /notifications/send-notification-to-all`

- **`app/src/main/java/com/example/myapplication/network/dto/NotificationMapper.java`**
  - Mapper class để convert giữa NotificationDTO (API) và Notification (Entity)
  - Methods:
    - `toEntity()` - Convert DTO → Entity (có xử lý ID conversion: Long → int)
    - `toEntityList()` - Convert list DTOs → Entities
    - `parseCreatedAt()` - Parse datetime string → timestamp (hỗ trợ nhiều formats)

---

## 📁 3. NOTIFICATION HELPER CLASSES

### 3.1. Notification Creator
- **`app/src/main/java/com/example/myapplication/notification/NotificationCreator.java`**
  - Helper class chính để tạo notifications
  - Các methods:
    - `notifyProductAddedToCart()` - Thông báo thêm vào giỏ hàng
    - `notifyOrderConfirmed()` - Đơn hàng đã xác nhận
    - `notifyOrderShipping()` - Đơn hàng đang giao
    - `notifyOrderDelivered()` - Đơn hàng đã giao
    - `notifyOrderCancelled()` - Đơn hàng bị hủy
    - `notifyPaymentSuccess()` - Thanh toán thành công
    - `notifyPaymentFailed()` - Thanh toán thất bại
    - `notifyRefundProcessed()` - Hoàn tiền đã xử lý
    - `notifyBackInStock()` - Sản phẩm có hàng trở lại
    - `notifyReviewReminder()` - Nhắc nhở đánh giá

### 3.2. Notification Helper
- **`app/src/main/java/com/example/myapplication/notification/NotificationHelper.java`**
  - Utility class để quản lý và hiển thị system notifications (Android NotificationManager)
  - Tạo notification channels (Cart, Order, Promotion, System)
  - Hiển thị notifications với badge, sound, vibration
  - Update app badge count
  - Methods:
    - `createNotificationChannels()` - Setup channels cho Android 8.0+
    - `showCartBadgeNotification()` - Hiển thị cart notification
    - `showOrderNotification()` - Hiển thị order notification
    - `showPromotionNotification()` - Hiển thị promotion notification
    - `updateAppBadge()` - Update badge trên app icon

### 3.3. Notification Receiver
- **`app/src/main/java/com/example/myapplication/notification/NotificationReceiver.java`**
  - BroadcastReceiver để nhận push notifications (nếu có)

### 3.4. Notification Usage Examples
- **`app/src/main/java/com/example/myapplication/notification/NotificationUsageExamples.java`**
  - File ví dụ cách sử dụng NotificationCreator

---

## 📁 4. UI COMPONENTS

### 4.1. Fragments
- **`app/src/main/java/com/example/myapplication/NotificationsFragment.java`**
  - Fragment hiển thị danh sách notifications cho user
  - Sử dụng API: `getAllNotificationsByUserId()`
  - Features: pull-to-refresh, mark as read, delete, unread count

- **`app/src/main/java/com/example/myapplication/AdminNotificationFragment.java`**
  - Fragment cho admin gửi notifications
  - Features: gửi cho user cụ thể, broadcast cho tất cả, template messages

### 4.2. Activities
- **`app/src/main/java/com/example/myapplication/notification/NotificationListActivity.java`**
  - Activity hiển thị danh sách notifications (nếu có)

### 4.3. Adapter
- **`app/src/main/java/com/example/myapplication/notification/NotificationAdapter.java`**
  - RecyclerView adapter cho danh sách notifications
  - Hiển thị: icon, title, message, time, unread indicator, delete button

### 4.4. DAO (Database Access Object)
- **`app/src/main/java/com/example/myapplication/notification/NotificationDao.java`**
  - Room DAO interface cho local database operations
  - CRUD operations: insert, update, delete, query
  - LiveData support cho reactive updates
  - Methods:
    - `getAllNotifications()` - Lấy tất cả notifications (LiveData)
    - `getUnreadNotifications()` - Lấy notifications chưa đọc
    - `getUnreadCount()` - Đếm số notifications chưa đọc
    - `markAsRead()` - Đánh dấu đã đọc
    - `markAllAsRead()` - Đánh dấu tất cả đã đọc
    - `getNotificationsByType()` - Lấy theo loại
    - `deleteOldNotifications()` - Xóa notifications cũ

---

## 📁 5. LAYOUT FILES (XML)

### 5.1. Fragment Layouts
- **`app/src/main/res/layout/fragment_notifications.xml`**
  - Layout cho NotificationsFragment
  - Chứa: RecyclerView, SwipeRefreshLayout, ProgressBar, EmptyState, UnreadCount, MarkAllRead button

- **`app/src/main/res/layout/fragment_admin_notification.xml`**
  - Layout cho AdminNotificationFragment
  - Chứa: EditText (UserID, Message), RadioGroup (Type), Send button, Template buttons

### 5.2. Item Layouts
- **`app/src/main/res/layout/item_notification.xml`**
  - Layout cho mỗi notification item trong RecyclerView
  - Chứa: Icon, Title, Message, Time, UnreadIndicator, Delete button

- **`app/src/main/res/layout/activity_notification_list.xml`**
  - Layout cho NotificationListActivity (nếu có)

### 5.3. Drawable Resources
- **`app/src/main/res/drawable/ic_notifications.xml`**
  - Icon vector cho notifications

- **`app/src/main/res/drawable/baseline_circle_notifications_24.xml`**
  - Icon vector cho notification indicator

---

## 📁 6. DOCUMENTATION FILES

### 6.1. Integration Guides
- **`NOTIFICATION_CREATION_INTEGRATION_GUIDE.md`**
  - Hướng dẫn tích hợp NotificationCreator vào các activities
  - Các integration points: Checkout, Payment, Order Tracking, etc.
  - Best practices và examples

- **`NOTIFICATION_FLOW_REPORT.md`**
  - Báo cáo luồng hoạt động của notifications
  - Vấn đề hiện tại và giải pháp
  - Flow cho Cart và Order notifications

- **`NOTIFICATION_TYPE_DEBUG_GUIDE.md`**
  - Hướng dẫn debug vấn đề notification type
  - Cách kiểm tra và fix lỗi type mismatch

---

## 📊 TỔNG KẾT

### Số lượng files:
- **Java/Kotlin files:** ~15 files
- **Layout XML files:** ~4 files
- **Drawable resources:** ~2 files
- **Documentation:** 3 files

### Các thành phần chính:

1. **Backend Integration:**
   - NotificationService (API endpoints)
   - NotificationRequest, NotificationDTO, BroadcastNotificationRequest (DTOs)
   - NotificationMapper (DTO ↔ Entity conversion)

2. **Business Logic:**
   - NotificationCreator (tạo notifications)
   - NotificationHelper (utilities)

3. **UI Layer:**
   - NotificationsFragment (user view)
   - AdminNotificationFragment (admin view)
   - NotificationAdapter (RecyclerView adapter)

4. **Data Layer:**
   - Notification (Room Entity)
   - NotificationDao (database access)

5. **Documentation:**
   - Integration guides
   - Flow reports
   - Debug guides

---

## 🔗 LIÊN KẾT CÁC FILE

### Flow tạo notification:
1. **NotificationCreator** → gọi **NotificationService.createNotification()**
2. **NotificationService** → gửi **NotificationRequest** lên backend
3. Backend trả về **NotificationDTO**
4. **NotificationMapper** → convert DTO sang **Notification** entity
5. **NotificationsFragment** → hiển thị qua **NotificationAdapter**

### Flow hiển thị notifications:
1. **NotificationsFragment** → gọi **NotificationService.getAllNotificationsByUserId()**
2. Nhận **List<NotificationDTO>** từ API
3. **NotificationMapper** → convert sang **List<Notification>**
4. **NotificationAdapter** → bind vào RecyclerView
5. User click → mark as read hoặc delete

---

## 📝 LƯU Ý QUAN TRỌNG

1. **Notification Type:** Backend cần lưu `notificationType` từ request (hiện đang bị bỏ qua)
2. **ID Conversion:** NotificationDTO dùng `Long` cho ID, Notification entity dùng `int`
3. **Offline Support:** Có thể cần NotificationDao nếu muốn cache notifications locally
4. **Push Notifications:** NotificationReceiver có thể cần setup Firebase Cloud Messaging

---

## 🚀 CÁCH SỬ DỤNG

### Tạo notification mới:
```java
NotificationCreator creator = new NotificationCreator(context);
creator.notifyOrderConfirmed(userId, orderId, new OnNotificationCreatedListener() {
    @Override
    public void onSuccess(NotificationDTO notification) {
        // Success
    }
    
    @Override
    public void onError(String error) {
        // Error
    }
});
```

### Hiển thị notifications:
- User: Mở `NotificationsFragment` trong app
- Admin: Mở `AdminNotificationFragment` để gửi notifications

---

*Last updated: 2025-01-XX*

