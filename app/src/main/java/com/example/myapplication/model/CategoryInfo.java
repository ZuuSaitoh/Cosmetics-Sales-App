package com.example.myapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CategoryInfo implements Serializable {
    @SerializedName("categoryID")
    private Long categoryID;
    
    @SerializedName("categoryName")
    private String categoryName;

    public CategoryInfo() {}

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
}
