package com.example.myapplication.model;

import com.example.myapplication.address.AddressEntry;
import com.google.gson.annotations.SerializedName;

public class OrderDetail {
    @SerializedName("orderID")
    private int orderID;

    @SerializedName("cart")
    private Cart cart;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("billingAddress")
    private String billingAddress;

    @SerializedName("orderStatus")
    private String orderStatus;

    @SerializedName("orderDate")
    private String orderDate;

    public int getOrderID() { return orderID; }
    public Cart getCart() { return cart; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getBillingAddress() { return billingAddress; }
    public String getOrderStatus() { return orderStatus; }
    public String getOrderDate() { return orderDate; }
}
