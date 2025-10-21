package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class AddCartItemRequest {
    @SerializedName("cartID")
    private Long cartId;

    @SerializedName("productID")
    private Long productId;

    @SerializedName("quantity")
    private int quantity;

    public AddCartItemRequest(Long cartId, Long productId, int quantity) {
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getCartId() {
        return cartId;
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}


