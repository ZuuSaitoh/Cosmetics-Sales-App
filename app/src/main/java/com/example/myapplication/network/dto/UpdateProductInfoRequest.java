package com.example.myapplication.network.dto;

import com.google.gson.annotations.SerializedName;

public class UpdateProductInfoRequest {
    @SerializedName("productName")
    public String productName;

    @SerializedName("briefDescription")
    public String briefDescription;

    @SerializedName("fullDescription")
    public String fullDescription;

    @SerializedName("price")
    public double price;

    @SerializedName("imageURL")
    public String imageURL;

    @SerializedName("brand")
    public String brand;

    @SerializedName("categoryID")
    public long categoryID;
}


