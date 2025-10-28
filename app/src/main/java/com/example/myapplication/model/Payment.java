package com.example.myapplication.model;

public class Payment {
    private int orderID;
    private double amount;
    private String paymentStatus;

    public Payment(int orderID, double amount, String paymentStatus) {
        this.orderID = orderID;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}
