package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class AddProductRequest {
    @SerializedName("productName")
    public String productName;

    @SerializedName("briefDescription")
    public String briefDescription;

    @SerializedName("fullDescription")
    public String fullDescription;

    @SerializedName("price")
    public double price;

    @SerializedName("instockQuantity")
    public int instockQuantity;

    @SerializedName("imageURL")
    public String imageURL;

    @SerializedName("categoryID")
    public long categoryID;

    @SerializedName("brand")
    public String brand;
}


