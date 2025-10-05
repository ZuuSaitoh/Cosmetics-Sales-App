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
@Table(name = "[Carts]", schema = "dbo")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CartID")
    int cartID;
    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    Users users;
    @Column(name = "TotalPrice")
    BigDecimal totalPrice;
    @Column(name = "Status")
    String status;

    public Cart(Users users, BigDecimal totalPrice, String status) {
        this.users = users;
        this.totalPrice = totalPrice;
        this.status = status;
    }
}
