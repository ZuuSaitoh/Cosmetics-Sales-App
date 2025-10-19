package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {

    @SerializedName("code")
    private int code;

    // SỬA: Dùng lớp ResultData vừa tạo để hứng đối tượng "result"
    @SerializedName("result")
    private ResultData result;
    private String token;
    private String userId;
    private String role;


    public int getCode() {
        return code;
    }

    public ResultData getResult() {
        return result;
    }
    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }
}




