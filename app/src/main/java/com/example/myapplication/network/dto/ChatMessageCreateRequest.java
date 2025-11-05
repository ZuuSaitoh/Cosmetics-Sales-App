package com.example.myapplication.network.dto;

public class ChatMessageCreateRequest {
    private Long userID;
    private String message;
    private Long conversationID;

    public ChatMessageCreateRequest(Long userID, String message, Long conversationID) {
        this.userID = userID;
        this.message = message;
        this.conversationID = conversationID;
    }

    public Long getUserID() { return userID; }
    public String getMessage() { return message; }
    public Long getConversationID() { return conversationID; }
}


