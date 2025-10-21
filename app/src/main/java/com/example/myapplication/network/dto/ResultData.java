
package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class ResultData {

    @SerializedName("token")
    private String token;

    @SerializedName("authenticated")
    private boolean authenticated;

    // Yêu cầu backend trả "role" trong này
    @SerializedName("role")
    private String role;

    // Thêm userID từ login response
    @SerializedName("userId")
    private Long userID;

    // --- Getters ---
    public String getToken() {
        return token;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getRole() {
        return role;
    }

    public Long getUserID() {
        return userID;
    }

    @Override
    public String toString() {
        return "ResultData{" +
                "token='" + token + '\'' +
                ", authenticated=" + authenticated +
                ", role='" + role + '\'' +
                ", userID=" + userID +
                '}';
    }
}