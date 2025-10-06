 package com.example.myapplication.model;

import java.io.Serializable;

public class CartItem implements Serializable {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = Math.max(1, quantity);
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    public double getItemTotal() {
        return product.getPrice() * quantity;
    }
}


