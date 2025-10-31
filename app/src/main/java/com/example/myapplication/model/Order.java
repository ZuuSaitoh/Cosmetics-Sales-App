package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

public class Order {

    @SerializedName("orderID")
    private int orderID;

    @SerializedName("cart")
    private Cart cart;

    @SerializedName("user")
    private User user;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("billingAddress")
    private String billingAddress;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("orderDate")
    private String orderDate;


    public Order(int orderID, User user, Cart cart, String paymentMethod, String billingAddress, String orderStatus, String orderDate) {
        this.orderID = orderID;
        this.user = user;
        this.cart = cart;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }
}
