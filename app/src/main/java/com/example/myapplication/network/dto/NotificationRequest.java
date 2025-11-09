package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO để tạo notification mới
 * POST /notifications/create
 */
public class NotificationRequest {
    
    // Backend expect "userID" với I và D viết hoa!
    @SerializedName(value = "userID", alternate = {"userId", "user_id"})
    private Long userId;
    
    @SerializedName("message")
    private String message;
    
    // Optional - không bắt buộc cho admin notification
    @SerializedName("notificationType")
    private String notificationType;
    
    // Optional - không bắt buộc
    @SerializedName("dataPayload")
    private String dataPayload;
    @SerializedName("title")
    private String title;
    
    // Constructor cho Admin (chỉ userID và message)
    public NotificationRequest(Long userId, String message) {
        this.userId = userId;
        this.message = message;
        // Set các field không cần thiết thành null để Gson không serialize
        this.notificationType = null;
        this.dataPayload = null;
        this.title = null;
    }
    
    // Constructor đầy đủ cho system notifications
//    public NotificationRequest(Long userId, String message, String notificationType) {
//        this.userId = userId;
//        this.message = message;
//        this.notificationType = notificationType;
//    }
    
    public NotificationRequest(Long userId, String message, String notificationType, String dataPayload) {
        this.userId = userId;
        this.message = message;
        this.notificationType = notificationType;
        this.dataPayload = dataPayload;
    }


    public NotificationRequest(Long userId, String message, String title) {
        this.userId = userId;
        this.message = message;
        this.title = title;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

