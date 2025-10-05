package com.example.demo.service;


import com.example.demo.entity.Cart;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CartRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartService {
    @Autowired
    CartRepository cartRepository;

    @Autowired
    UsersService usersService;

    public Cart getActiveCart(Integer userID) {
        return cartRepository.findByUsers_UserIDAndStatus(userID, "active")
                .orElseThrow(() -> new AppException(ErrorCode.ACTIVE_CART_NOT_EXISTED));
    }

    public boolean hasActiveCart(Integer userID) {
        return cartRepository.existsByUsers_UserIDAndStatus(userID, "active");
    }

    public Cart createNewCart(Integer userID){
        if (hasActiveCart(userID)){
            throw new AppException(ErrorCode.ACTIVE_CART_EXISTED);
        } else {
            return cartRepository.save(new Cart(usersService.getUserByID(userID), BigDecimal.ZERO, "active"));
        }
    }
    public Cart changeStatus(Integer userID){
        Cart cart = getActiveCart(userID);
        cart.setStatus("complete");
        return cartRepository.save(cart);
    }

    public void deleteCartByID(Integer cartID){
        cartRepository.deleteById(cartID);
    }

    public List<Cart> viewAllCart(){
        return cartRepository.findAll();
    }

    public Cart findCartByID(Integer cartID){
        return cartRepository.findById(cartID).orElseThrow(()-> new AppException(ErrorCode.CART_NOT_EXISTED));
    }
}
