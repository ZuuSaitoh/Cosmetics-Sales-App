package com.example.myapplication.model;


import com.google.gson.annotations.SerializedName;

public class Order {

    @SerializedName("orderID")
    private int orderID;

    @SerializedName("cartID")
    private int cartID;

    @SerializedName("userID")
    private int userID;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("billingAddress")
    private String billingAddress;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("orderDate")
    private String orderDate;


    public Order() {
    }


    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getCartID() {
        return cartID;
    }

    public void setCartID(int cartID) {
        this.cartID = cartID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
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
