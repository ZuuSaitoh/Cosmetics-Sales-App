package com.example.myapplication.network.dto;



import com.example.myapplication.model.OrderDetail;
import com.google.gson.annotations.SerializedName;

public class OrderResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private OrderDetail result;

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public OrderDetail getResult() { return result; }
}
