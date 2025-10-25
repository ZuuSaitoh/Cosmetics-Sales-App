package com.example.myapplication.network.dto;

import com.example.myapplication.model.CartItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CartItemsResponse {

    @SerializedName("code")
    private int code;

    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private List<CartItem> result;

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public List<CartItem> getResult() { return result; }


}
