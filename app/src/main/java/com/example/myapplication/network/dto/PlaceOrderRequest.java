// Trong: network/dto/PlaceOrderRequest.java
package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlaceOrderRequest {


    @SerializedName("userID")
    private Long userId;


    @SerializedName("cartID")
    private Long cartId;


    @SerializedName("paymentMethod")
    private String paymentMethod;


    @SerializedName("billingAddress")
    private String billingAddress;

    /* * Các trường logic, không có trong bảng Orders
     * nhưng cần thiết cho backend xử lý
     */

    // Danh sách ID của các CartItem được chọn
    @SerializedName("selectedItemIds")
    private List<Long> selectedItemIds;

    // Tổng số tiền client tính toán (để backend xác thực)
    @SerializedName("totalAmount")
    private double totalAmount;

    // Constructor
    public PlaceOrderRequest(Long userId, Long cartId, String paymentMethod, String billingAddress, List<Long> selectedItemIds, double totalAmount) {
        this.userId = userId;
        this.cartId = cartId;
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
        this.selectedItemIds = selectedItemIds;
        this.totalAmount = totalAmount;
    }

    // Getters và Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCartId() {
        return cartId;
    }

    public void setCartId(Long cartId) {
        this.cartId = cartId;
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

    public List<Long> getSelectedItemIds() {
        return selectedItemIds;
    }

    public void setSelectedItemIds(List<Long> selectedItemIds) {
        this.selectedItemIds = selectedItemIds;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}