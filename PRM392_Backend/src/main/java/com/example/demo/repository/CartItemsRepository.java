package com.example.demo.repository;

import com.example.demo.entity.CartItems;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemsRepository extends JpaRepository<CartItems, Integer> {
    Optional<List<CartItems>> findByCart_CartID(Integer cartID);
    Boolean existsByCart_CartIDAndProducts_ProductID(Integer cartID, Integer productID);
    void deleteByCart_CartID(Integer cartID);
}
