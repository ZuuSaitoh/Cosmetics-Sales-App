package com.example.myapplication.model;

public class OrderItem {
    private String productName;
    private String imageUrl;
    private int quantity;
    private double price;

    public OrderItem(String productName, String imageUrl, int quantity, double price) {
        this.productName = productName;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() { return productName; }
    public String getImageUrl() { return imageUrl; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
}
