package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "[CartItems]", schema = "dbo")
public class CartItems {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartItemID")
    int CartItemID;
    @ManyToOne
    @JoinColumn(name = "CartID", referencedColumnName = "CartID", nullable = false)
    Cart cart;
    @ManyToOne
    @JoinColumn(name = "ProductID", referencedColumnName = "ProductID", nullable = false)
    Products products;
    @Column(name = "Quantity")
    int quantity;
    @Column(name = "Price")
    BigDecimal price;

    public CartItems(Cart cart, Products products, int quantity, BigDecimal price) {
        this.cart = cart;
        this.products = products;
        this.quantity = quantity;
        this.price = price;
    }
}
