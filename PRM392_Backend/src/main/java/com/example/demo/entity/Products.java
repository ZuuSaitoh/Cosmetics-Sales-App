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
@Table(name = "[Products]", schema = "dbo")
public class Products {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ProductID")
    int productID;
    @Column(name = "ProductName")
    String productName;
    @Column(name ="BriefDescription" )
    String briefDescription;
    @Column(name = "FullDescription")
    String fullDescription;
    @Column(name = "Price", precision = 18, scale = 2)
    BigDecimal price;
    @Column(name = "InstockQuantity")
    int instockQuantity;
    @Column(name = "ImageURL")
    String imageURL;
    @ManyToOne
    @JoinColumn(name = "CategoryID", referencedColumnName = "CategoryID", nullable = false)
    Category categoryID;
    @Column(name = "Brand")
    String brand;

    public Products(String productName, String briefDescription, String fullDescription, BigDecimal price, int instockQuantity, String imageURL, Category categoryID, String brand) {
        this.productName = productName;
        this.briefDescription = briefDescription;
        this.fullDescription = fullDescription;
        this.price = price;
        this.instockQuantity = instockQuantity;
        this.imageURL = imageURL;
        this.categoryID = categoryID;
        this.brand = brand;
    }
}
