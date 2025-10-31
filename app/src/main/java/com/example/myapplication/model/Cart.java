package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Cart {

    @SerializedName("cartID")
    private Long cartID;

    @SerializedName("users")
    private User users;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("status")
    private String status;

    private CartItem cartItem;

    public CartItem getCartItem() {
        return cartItem;
    }

    public Long getCartID() {
        return cartID;
    }

    public void setCartID(Long cartID) {
        this.cartID = cartID;
    }

    public User getUsers() {
        return users;
    }

    public void setUsers(User users) {
        this.users = users;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
