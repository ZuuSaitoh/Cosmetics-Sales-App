package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO (Data Transfer Object) cho Notification từ API
 * Dùng để parse JSON response từ backend
 * Tách riêng với Room Entity để tránh conflict
 * 
 * Hỗ trợ nhiều field name variations từ backend
 */
public class NotificationDTO {
    
    // Hỗ trợ nhiều variations: id, notificationId, notification_id, notificationID
    @SerializedName(value = "notificationId", alternate = {"id", "notification_id", "notificationID"})
    private Long notificationId;
    
    // Backend trả về nested User object, không phải userId đơn thuần
    @SerializedName("user")
    private UserDTO user;
    
    // Title - có thể null vì backend không trả về
    @SerializedName("title")
    private String title;
    
    @SerializedName("message")
    private String message;
    
    // Hỗ trợ nhiều variations: notificationType, notification_type, type
    // Có thể null vì backend không trả về
    @SerializedName(value = "notificationType", alternate = {"notification_type", "type"})
    private String notificationType;
    
    // Hỗ trợ nhiều variations: isRead, is_read, read
    @SerializedName(value = "isRead", alternate = {"is_read", "read"})
    private Boolean isRead;
    
    // Hỗ trợ nhiều variations: createdAt, created_at, timestamp, createAt
    @SerializedName(value = "createdAt", alternate = {"created_at", "timestamp", "createAt"})
    private String createdAt;  // Backend có thể trả về String datetime
    
    // Hỗ trợ nhiều variations: dataPayload, data_payload, payload
    @SerializedName(value = "dataPayload", alternate = {"data_payload", "payload"})
    private String dataPayload;
    
    // Constructors
    public NotificationDTO() {
    }
    
    // Getters and Setters
    public Long getNotificationId() {
        return notificationId;
    }
    
    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
    
    public UserDTO getUser() {
        return user;
    }
    
    public void setUser(UserDTO user) {
        this.user = user;
    }
    
    /**
     * Helper method để lấy userId từ nested user object
     */
    public Long getUserId() {
        return (user != null) ? user.getUserID() : null;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getNotificationType() {
        return notificationType;
    }
    
    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }
    
    public Boolean getIsRead() {
        return isRead;
    }
    
    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getDataPayload() {
        return dataPayload;
    }
    
    public void setDataPayload(String dataPayload) {
        this.dataPayload = dataPayload;
    }
    
    @Override
    public String toString() {
        return "NotificationDTO{" +
                "notificationId=" + notificationId +
                ", user=" + (user != null ? user.toString() : "null") +
                ", userId=" + getUserId() +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", isRead=" + isRead +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}

