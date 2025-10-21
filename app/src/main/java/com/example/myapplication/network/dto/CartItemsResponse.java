package com.example.myapplication.network.dto;

import com.example.myapplication.model.CartItem;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CartItemsResponse {
    // Fix: Changed from @SerializedName("cartItems") to @SerializedName("result")
    @SerializedName("result")
    private List<CartItem> result;

    public List<CartItem> getCartItems() {
        return result;
    }

    public void setCartItems(List<CartItem> result) {
        this.result = result;
    }
}
