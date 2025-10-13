package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "[Orders]", schema = "dbo")
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrderID")
    Integer orderID;
    @ManyToOne
    @JoinColumn(name = "CartID", referencedColumnName = "CartID", nullable = false)
    Cart cart;
    @ManyToOne
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", nullable = false)
    Users user;
    @Column(name = "PaymentMethod")
    String paymentMethod;
    @Column(name = "BillingAddress" )
    String billingAddress;
    @Column(name = "OrderStatus" )
    String orderStatus;
    @Column(name ="OrderDate" )
    LocalDateTime orderDate;

    public Orders(Cart cart, Users user, String paymentMethod, String billingAddress, String orderStatus, LocalDateTime orderDate) {
        this.cart = cart;
        this.user = user;
        this.paymentMethod = paymentMethod;
        this.billingAddress = billingAddress;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }
}
