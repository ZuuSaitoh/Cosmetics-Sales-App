package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CartItem implements Serializable {
    @SerializedName("cartItemID")
    private Long cartItemID;
    @SerializedName("products")
    private Product product;
    @SerializedName("quantity")
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = Math.max(1, quantity);
    }

    public Product getProduct() {
        return product;
    }

    public Long getCartItemID() {
        return cartItemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public double getItemTotal() {
        if (product != null) {
            return product.getPrice() * quantity;
        }
        return 0;
    }
}
