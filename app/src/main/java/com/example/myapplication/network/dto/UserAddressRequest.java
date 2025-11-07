package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class UserAddressRequest {
    @SerializedName("userAddressID")
    public Long userAddressId;

    @SerializedName("userID")
    public Long userId;

    @SerializedName("recipientName")
    public String recipientName;

    @SerializedName("phone")
    public String phone;

    @SerializedName("address")
    public String address;

    @SerializedName("CreatedAt")
    public String createdAt;
}


