package com.example.myapplication.model;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    private String message;
    private boolean isFromUser;
    private Date timestamp;
    
    public ChatMessage() {}
    
    public ChatMessage(String message, boolean isFromUser, Date timestamp) {
        this.message = message;
        this.isFromUser = isFromUser;
        this.timestamp = timestamp;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isFromUser() {
        return isFromUser;
    }
    
    public void setFromUser(boolean fromUser) {
        isFromUser = fromUser;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
