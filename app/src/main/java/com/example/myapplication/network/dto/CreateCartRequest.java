package com.example.myapplication.network.dto;

public class CreateCartRequest {
    private final Long userID;

    public CreateCartRequest(Long userID) {
        this.userID = userID;
    }

    public Long getUserID() {
        return userID;
    }
}
