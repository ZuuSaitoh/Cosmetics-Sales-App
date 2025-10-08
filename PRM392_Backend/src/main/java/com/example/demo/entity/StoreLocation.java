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
@Table(name = "StoreLocations")
public class StoreLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LocationID")
    Integer locationID;

    @Column(name = "Latitude", nullable = false)
    BigDecimal latitude;

    @Column(name = "Longitude", nullable = false)
    BigDecimal longitude;

    @Column(name = "Address", nullable = false, length = 255)
    String address;

}
