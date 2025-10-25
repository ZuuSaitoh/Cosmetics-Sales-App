package com.example.myapplication.network.dto;

public class CreateOrderRequest {
    private Long userId;
    private String paymentMethod;

    public CreateOrderRequest(Long userId, String paymentMethod) {
        this.userId = userId;
        this.paymentMethod = paymentMethod;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
