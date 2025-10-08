package com.example.demo.repository;

import com.example.demo.entity.StoreLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreLocationRepository extends JpaRepository<StoreLocation, Integer> {
}

