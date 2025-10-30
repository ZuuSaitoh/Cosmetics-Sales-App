package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO để tạo notification mới
 * POST /notifications/create
 */
public class NotificationRequest {
    
    @SerializedName("userId")
    private Long userId;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("notificationType")
    private String notificationType;
    
    @SerializedName("dataPayload")
    private String dataPayload;
    
    // Constructor
    public NotificationRequest() {
    }
    
    public NotificationRequest(Long userId, String message, String notificationType) {
        this.userId = userId;
        this.message = message;
        this.notificationType = notificationType;
    }
    
    public NotificationRequest(Long userId, String message, String notificationType, String dataPayload) {
        this.userId = userId;
        this.message = message;
        this.notificationType = notificationType;
        this.dataPayload = dataPayload;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public String getDataPayload() {
        return dataPayload;
    }
    
    public void setDataPayload(String dataPayload) {
        this.dataPayload = dataPayload;
    }
}

