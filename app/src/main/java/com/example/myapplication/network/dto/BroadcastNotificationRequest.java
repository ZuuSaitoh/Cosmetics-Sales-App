package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO để gửi notification cho tất cả users
 * POST /notifications/send-notification-to-all
 */
public class BroadcastNotificationRequest {
    
    @SerializedName("message")
    private String message;
    @SerializedName("title")
    private String title;

    public BroadcastNotificationRequest(String message, String title) {
        this.message = message;
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

