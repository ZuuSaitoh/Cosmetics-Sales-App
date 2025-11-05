package com.example.myapplication.network.dto;

import com.example.myapplication.model.Conversation;
import com.example.myapplication.model.User;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ChatMessageDto implements Serializable {
    @SerializedName("chatMessageID")
    private Long chatMessageID;

    @SerializedName("user")
    private User user;

    @SerializedName("message")
    private String message;

    @SerializedName("sentAt")
    private String sentAt;

    @SerializedName("conversation")
    private Conversation conversation;

    public Long getChatMessageID() { return chatMessageID; }
    public User getUser() { return user; }
    public String getMessage() { return message; }
    public String getSentAt() { return sentAt; }
    public Conversation getConversation() { return conversation; }
}


