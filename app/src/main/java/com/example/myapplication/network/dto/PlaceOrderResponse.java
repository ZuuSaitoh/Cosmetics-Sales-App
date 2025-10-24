package com.example.myapplication.network.dto;

import com.example.myapplication.model.Order;


public class PlaceOrderResponse {
    private int code;
    private String message;
    private Order result;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Order getResult() {
        return result;
    }
}
