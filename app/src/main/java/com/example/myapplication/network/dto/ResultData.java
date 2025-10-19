
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
}