package com.example.myapplication.model;

import java.io.Serializable;

public class Product implements Serializable {
    private final String name;
    private final String description;
    private final double price;
    private final int imageResId;

    public Product(String name, String description, double price, int imageResId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }

}


