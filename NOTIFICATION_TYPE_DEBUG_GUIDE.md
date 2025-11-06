# 🔍 Hướng Dẫn Debug Vấn Đề Notification Type

## ❌ Vấn đề hiện tại:
- Thông báo **Giỏ hàng** (CART) đang hiển thị ở tab **"Khuyến mãi"** thay vì tab **"Của bạn"**
- Nguyên nhân: Backend có thể **KHÔNG lưu `notificationType`** từ request

## 📊 Phân loại notifications:

| Type | Tab hiển thị | Ví dụ |
|------|--------------|-------|
| `CART` | **Của bạn** | "Giỏ hàng của bạn có 3 sản phẩm" |
| `ORDER` | **Của bạn** | "Đơn hàng #123 đã được xác nhận" |
| `PAYMENT` | **Của bạn** | "Thanh toán thành công" |
| `PROMOTION` | **Khuyến mãi** | "FLASH SALE 50%" |
| `SYSTEM` | **Khuyến mãi** | "Sản phẩm về hàng" |

## 🧪 Cách kiểm tra:

### Bước 1: Thêm sản phẩm vào giỏ hàng
1. Mở app và thêm 1 sản phẩm vào giỏ hàng
2. Mở **Logcat** và filter: `NotificationCreator`

### Bước 2: Xem log REQUEST
Tìm log như sau:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📤 CREATING NOTIFICATION
UserId: 9
Type: CART                    ← Frontend GỬI type = CART
Message: Bạn vừa thêm "..." vào giỏ hàng
DataPayload: {"productName":"...","quantity":1,...}
Request JSON: {"userID":9,"message":"...","notificationType":"CART",...}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### Bước 3: Xem log RESPONSE
Tìm log như sau:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 NOTIFICATION CREATED SUCCESSFULLY
Notification ID: 15
User ID: 9
Type FROM BACKEND: SYSTEM     ← Backend TRẢ VỀ type = SYSTEM (SAI!)
Message: Bạn vừa thêm "..." vào giỏ hàng
⚠️ EXPECTED Type: CART
❌ TYPE MISMATCH!
   Sent to backend: CART
   Received from backend: SYSTEM
   => Backend is NOT saving notificationType from request!
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

## 🐛 Nguyên nhân xác định:

### ❌ Backend KHÔNG lưu `notificationType` từ request
- Frontend gửi: `notificationType: "CART"`
- Backend nhận nhưng **BỎ QUA** field này
- Backend tự động set: `notificationType = "SYSTEM"` (mặc định)
- Frontend nhận lại: `notificationType: "SYSTEM"`
- Kết quả: Notification bị filter vào tab **"Khuyến mãi"** thay vì **"Của bạn"**

## 🔧 Giải pháp:

### Backend cần sửa:
Trong API `POST /notifications/create`, backend cần:
1. **Nhận** field `notificationType` từ request body
2. **LƯU** field này vào database
3. **TRẢ VỀ** field này trong response

**Backend Controller cần sửa:**
```java
@PostMapping("/create")
public ResponseEntity<?> createNotification(@RequestBody NotificationRequest request) {
    Notification notification = new Notification();
    notification.setUserId(request.getUserID());
    notification.setMessage(request.getMessage());
    
    // ✅ THÊM DÒNG NÀY để lưu notificationType
    notification.setNotificationType(request.getNotificationType()); 
    
    notification.setDataPayload(request.getDataPayload());
    notification.setIsRead(false);
    notification.setCreatedAt(LocalDateTime.now());
    
    notificationRepository.save(notification);
    return ResponseEntity.ok(notification);
}
```

## 📝 Request Body mẫu:

```json
{
  "userID": 9,
  "message": "Bạn vừa thêm \"Kem chống nắng\" vào giỏ hàng",
  "notificationType": "CART",     ← QUAN TRỌNG: Backend cần LƯU field này
  "dataPayload": "{\"productName\":\"Kem chống nắng\",\"quantity\":1}"
}
```

## 🎯 Kết quả mong đợi sau khi sửa:

### Log sẽ hiển thị:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📥 NOTIFICATION CREATED SUCCESSFULLY
Type FROM BACKEND: CART       ← ✅ ĐÚNG!
⚠️ EXPECTED Type: CART
✅ Type matches - backend saved correctly
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

### UI sẽ hiển thị:
- Tab **"Khuyến mãi"**: Chỉ có notifications PROMOTION, SYSTEM (từ admin)
- Tab **"Của bạn"**: Có notifications CART, ORDER, PAYMENT ✅

---

## 📌 Lưu ý quan trọng:

1. **Frontend đã ĐÚNG** - đang gửi đầy đủ `notificationType` trong request
2. **Backend CẦN SỬA** - hiện đang bỏ qua field `notificationType` từ request
3. Sau khi backend sửa, **KHÔNG cần thay đổi gì ở frontend**
4. Test lại bằng cách:
   - Xóa tất cả notifications cũ
   - Thêm sản phẩm mới vào giỏ hàng
   - Kiểm tra notification xuất hiện ở tab "Của bạn"

---

## 🚀 Testing Checklist:

- [ ] Thêm sản phẩm vào giỏ hàng → Notification xuất hiện ở tab **"Của bạn"** (type = CART)
- [ ] Đặt hàng → Notification xuất hiện ở tab **"Của bạn"** (type = ORDER)
- [ ] Thanh toán thành công → Notification xuất hiện ở tab **"Của bạn"** (type = PAYMENT)
- [ ] Admin gửi Flash Sale → Notification xuất hiện ở tab **"Khuyến mãi"** (type = PROMOTION/SYSTEM)

---

## 📧 Thông tin backend cần:

**API Endpoint:** `POST /notifications/create`

**Request Body cần nhận:**
- `userID` (Long) ✅
- `message` (String) ✅
- `notificationType` (String) ❌ Đang bị bỏ qua
- `dataPayload` (String, optional) ✅

**Các giá trị hợp lệ cho `notificationType`:**
- `"CART"` - Thông báo giỏ hàng
- `"ORDER"` - Thông báo đơn hàng
- `"PAYMENT"` - Thông báo thanh toán
- `"PROMOTION"` - Thông báo khuyến mãi (từ admin)
- `"SYSTEM"` - Thông báo hệ thống (từ admin)

