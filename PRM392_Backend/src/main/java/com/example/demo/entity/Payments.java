package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "[Payments]", schema = "dbo")
public class Payments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PaymentID")
    Integer paymentID;
    @ManyToOne
    @JoinColumn(name = "OrderID", referencedColumnName = "OrderID", nullable = false)
    Orders order;
    @Column(name = "Amount")
    BigDecimal amount;
    @Column(name = "PaymentDate")
    LocalDateTime paymentDate;
    @Column(name = "PaymentStatus")
    String paymentStatus;

}
