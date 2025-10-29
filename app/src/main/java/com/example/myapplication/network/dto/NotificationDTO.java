package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO (Data Transfer Object) cho Notification từ API
 * Dùng để parse JSON response từ backend
 * Tách riêng với Room Entity để tránh conflict
 */
public class NotificationDTO {
    
    @SerializedName("notificationId")
    private Long notificationId;
    
    @SerializedName("userId")
    private Long userId;  // Backend có thể trả về Long thay vì String
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("notificationType")
    private String notificationType;
    
    @SerializedName("isRead")
    private Boolean isRead;
    
    @SerializedName("createdAt")
    private String createdAt;  // Backend có thể trả về String datetime
    
    @SerializedName("dataPayload")
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
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", notificationType='" + notificationType + '\'' +
                ", isRead=" + isRead +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}

