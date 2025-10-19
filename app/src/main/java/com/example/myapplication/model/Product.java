package com.example.myapplication.model;

import com.example.myapplication.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Product implements Serializable {
    @SerializedName("productID")
    private Long productID;
    
    @SerializedName("productName")
    private String productName;
    
    @SerializedName("briefDescription")
    private String briefDescription;
    
    @SerializedName("fullDescription")
    private String fullDescription;
    
    @SerializedName("price")
    private double price;
    
    @SerializedName("imageURL")
    private String imageURL;
    
    @SerializedName("categoryID")
    private CategoryInfo categoryID;
    
    @SerializedName("instockQuantity")
    private Integer instockQuantity;
    
    @SerializedName("brand")
    private String brand;
    
    // For backward compatibility
    private int imageResId;
    
    // Default constructor for Gson
    public Product() {}

    // Constructor for local use
    public Product(String name, String description, double price, int imageResId) {
        this.productName = name;
        this.briefDescription = description;
        this.price = price;
        this.imageResId = imageResId;
    }

    // Getters and Setters
    public Long getProductID() {
        return productID;
    }

    public void setProductID(Long productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBriefDescription() {
        return briefDescription;
    }

    public void setBriefDescription(String briefDescription) {
        this.briefDescription = briefDescription;
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public CategoryInfo getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(CategoryInfo categoryID) {
        this.categoryID = categoryID;
    }

    public Integer getInstockQuantity() {
        return instockQuantity;
    }

    public void setInstockQuantity(Integer instockQuantity) {
        this.instockQuantity = instockQuantity;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    // For backward compatibility with existing code
    public String getName() {
        return productName;
    }

    public String getDescription() {
        return briefDescription;
    }

    public int getImageResId() {
        return imageResId != 0 ? imageResId : R.drawable.banner; // Default image
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}


