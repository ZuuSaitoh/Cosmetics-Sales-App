package com.example.demo.controller;

import com.example.demo.dto.request.CartItemsAddProductToCartRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.CartItems;
import com.example.demo.service.CartItemsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart-items")
public class CartItemsController {
    @Autowired
    CartItemsService cartItemsService;

    @PostMapping("/add-products")
    ApiResponse<CartItems> addProductToCart(@RequestBody @Valid CartItemsAddProductToCartRequest request){
        return new ApiResponse<CartItems>(9999,
                "Add product to cart successfully!",
                cartItemsService.addProductToCart(request.getCartID(), request.getProductID(), request.getQuantity()));
    }
    @GetMapping("/fetchAll/{cartID}")
    ApiResponse<List<CartItems>> viewAllCartItem(@PathVariable Integer cartID){
        return new ApiResponse<List<CartItems>>(9999,
                "All products in your cart",
                cartItemsService.viewAllItemInCart(cartID));
    }

    @DeleteMapping("delete/{cartID}")
    ApiResponse<String> deleteAllItemsInCart(@PathVariable Integer cartID){
        cartItemsService.deleteAllItemsInCart(cartID);
        return new ApiResponse<String>(9999, "Delete all items in cart");
    }

}
