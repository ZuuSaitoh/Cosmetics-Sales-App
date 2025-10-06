package com.example.myapplication.data;

import com.example.myapplication.model.CartItem;
import com.example.myapplication.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartManager {
    private static CartManager instance;

    private final Map<String, CartItem> productIdToItem;

    private CartManager() {
        this.productIdToItem = new HashMap<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    private String getKey(Product product) {
        // Use product name as a simple key for now; in real app use unique ID
        return product.getName();
    }

    public void add(Product product, int quantity) {
        String key = getKey(product);
        CartItem existing = productIdToItem.get(key);
        if (existing == null) {
            productIdToItem.put(key, new CartItem(product, quantity));
        } else {
            existing.setQuantity(existing.getQuantity() + Math.max(1, quantity));
        }
    }

    public void updateQuantity(Product product, int quantity) {
        String key = getKey(product);
        CartItem existing = productIdToItem.get(key);
        if (existing != null) {
            existing.setQuantity(quantity);
        }
    }

    public void remove(Product product) {
        productIdToItem.remove(getKey(product));
    }

    public void clear() {
        productIdToItem.clear();
    }

    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : productIdToItem.values()) {
            total += item.getQuantity();
        }
        return total;
    }

    public double getGrandTotal() {
        double total = 0.0;
        for (CartItem item : productIdToItem.values()) {
            total += item.getItemTotal();
        }
        return total;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(productIdToItem.values());
    }
}


