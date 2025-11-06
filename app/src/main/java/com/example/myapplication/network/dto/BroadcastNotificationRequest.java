package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO để gửi notification cho tất cả users
 * POST /notifications/send-notification-to-all
 */
public class BroadcastNotificationRequest {
    
    @SerializedName("message")
    private String message;
    
    public BroadcastNotificationRequest(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}

