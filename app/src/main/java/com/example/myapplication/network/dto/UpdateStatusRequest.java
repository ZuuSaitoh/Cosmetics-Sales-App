package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateStatusRequest {
    @SerializedName("orderID")
    private Integer orderID;
    
    @SerializedName("orderStatus")
    private String orderStatus;

    // Constructor mặc định (cho Gson)
    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    
    public UpdateStatusRequest(String orderStatus, Integer orderID) {
        this.orderStatus = orderStatus;
        this.orderID = orderID;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    
    public Integer getOrderID() {
        return orderID;
    }
    
    public void setOrderID(Integer orderID) {
        this.orderID = orderID;
    }
}
