package com.example.myapplication.model;


public class ProductAdmin {
    private String name;
    private String category;
    private int salesCount;
    private String imageUrl;

    public ProductAdmin(String name, String category, int salesCount, String imageUrl) {
        this.name = name;
        this.category = category;
        this.salesCount = salesCount;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getSalesCount() { return salesCount; }
    public void setSalesCount(int salesCount) { this.salesCount = salesCount; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
