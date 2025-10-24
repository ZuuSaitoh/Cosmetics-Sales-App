package com.example.myapplication.model;

import com.example.myapplication.R;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Category implements Serializable {
    @SerializedName("categoryID")
    private Long categoryID;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    // For local use
    private String description;
    private Integer productCount;
    private String iconUrl;
    private int iconResId;

    public Category() {
    }

    public Category(String name, int iconResId, String description, int productCount) {
        this.categoryName = name;
        this.iconResId = iconResId;
        this.description = description;
        this.productCount = productCount;
    }

    // Getters and Setters
    public Long getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(Long categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getProductCount() {
        return productCount != null ? productCount : 0;
    }

    public void setProductCount(Integer productCount) {
        this.productCount = productCount;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getIconResId() {
        return iconResId != 0 ? iconResId : R.drawable.ic_face; // Default icon
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    // For backward compatibility
    public String getName() {
        return categoryName;
    }
}
