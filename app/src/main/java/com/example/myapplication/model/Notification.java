package com.example.myapplication.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Date;

/**
 * Entity class cho Notification
 * Lưu trữ thông tin về các thông báo trong ứng dụng
 */
@Entity(tableName = "notifications")
public class Notification {
    
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    private int notificationId;
    
    @ColumnInfo(name = "user_id")
    @NonNull
    private String userId;
    
    @ColumnInfo(name = "message")
    @NonNull
    private String message;
    
    @ColumnInfo(name = "is_read")
    private boolean isRead;
    
    @ColumnInfo(name = "created_at")
    private long createdAt; // Unix timestamp (milliseconds)
    
    // Type của notification (cart, order, promotion, etc.)
    @ColumnInfo(name = "notification_type")
    private String notificationType;
    
    // Data payload (JSON string) - để lưu thông tin thêm
    @ColumnInfo(name = "data_payload")
    private String dataPayload;
    
    // Title của notification
    @ColumnInfo(name = "title")
    private String title;
    
    // ============ CONSTRUCTORS ============
    
    public Notification() {
        // Constructor mặc định
        this.createdAt = System.currentTimeMillis();
        this.isRead = false;
    }
    
    public Notification(@NonNull String userId, @NonNull String message) {
        this.userId = userId;
        this.message = message;
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }
    
    public Notification(@NonNull String userId, String title, @NonNull String message, 
                       String notificationType) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.notificationType = notificationType;
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }
    
    // ============ GETTERS ============
    
    public int getNotificationId() {
        return notificationId;
    }
    
    @NonNull
    public String getUserId() {
        return userId;
    }
    
    @NonNull
    public String getMessage() {
        return message;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public String getDataPayload() {
        return dataPayload;
    }
    
    public String getTitle() {
        return title;
    }
    
    /**
     * Lấy created time dưới dạng Date object
     */
    public Date getCreatedAtDate() {
        return new Date(createdAt);
    }
    
    // ============ SETTERS ============
    
    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }
    
    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }
    
    public void setMessage(@NonNull String message) {
        this.message = message;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public void setDataPayload(String dataPayload) {
        this.dataPayload = dataPayload;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    // ============ UTILITY METHODS ============
    
    /**
     * Đánh dấu notification đã đọc
     */
    public void markAsRead() {
        this.isRead = true;
    }
    
    /**
     * Đánh dấu notification chưa đọc
     */
    public void markAsUnread() {
        this.isRead = false;
    }
    
    /**
     * Kiểm tra notification có phải là cart notification không
     */
    public boolean isCartNotification() {
        return "CART".equals(notificationType);
    }
    
    /**
     * Kiểm tra notification có phải là order notification không
     */
    public boolean isOrderNotification() {
        return "ORDER".equals(notificationType);
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "notificationId=" + notificationId +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", isRead=" + isRead +
                ", notificationType='" + notificationType + '\'' +
                ", createdAt=" + new Date(createdAt) +
                '}';
    }
    
    // ============ NOTIFICATION TYPE CONSTANTS ============
    
    public static class NotificationType {
        public static final String CART = "CART";
        public static final String ORDER = "ORDER";
        public static final String PROMOTION = "PROMOTION";
        public static final String SYSTEM = "SYSTEM";
        public static final String PAYMENT = "PAYMENT";
    }
}

