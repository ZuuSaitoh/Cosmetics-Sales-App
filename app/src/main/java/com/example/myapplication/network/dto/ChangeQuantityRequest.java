package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class ChangeQuantityRequest {
    @SerializedName("cartItemID")
    private Long cartItemID;
    
    @SerializedName("quantity")
    private int quantity;

    public ChangeQuantityRequest(Long cartItemID, int quantity) {
        this.cartItemID = cartItemID;
        this.quantity = quantity;
    }

    public Long getCartItemID() {
        return cartItemID;
    }

    public void setCartItemID(Long cartItemID) {
        this.cartItemID = cartItemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
