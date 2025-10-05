package com.example.demo.controller;

import com.example.demo.dto.request.CartCreationRequest;
import com.example.demo.dto.request.CartFindByUserIDRequest;
import com.example.demo.dto.request.CartUpdateStatusRequest;
import com.example.demo.dto.request.ProductsAddRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Products;
import com.example.demo.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
public class CartController {
    @Autowired
    CartService cartService;

    @GetMapping("/{id}")
    Cart getCartByID(@PathVariable Integer id){
        return cartService.findCartByID(id);
    }

    @GetMapping("/get-by-userID/{userID}")
    Cart getActiveCartByUserID(@PathVariable Integer userID){
        return cartService.getActiveCart(userID);
    }

    @PostMapping("/create")
    ApiResponse<Cart> addNewCart(@RequestBody @Valid CartCreationRequest request){
        ApiResponse<Cart> apiResponse= new ApiResponse<>();
        apiResponse.setResult(cartService.createNewCart(request.getUserID()));
        return apiResponse;
    }

    @DeleteMapping("/delete/{id}")
    ApiResponse<String> deleteCartById(@PathVariable Integer id){
        cartService.deleteCartByID(id);
        return new ApiResponse<String>(9998, "Delete cart successfully!");
    }

    @PutMapping("/update-status")
    ApiResponse<Cart> updateCartStatus(@RequestBody @Valid CartUpdateStatusRequest request){
        return new ApiResponse<Cart>(9999,"Cart status have been update to completed",
                cartService.changeStatus(request.getUserID()));
    }
    @GetMapping("/fetchAll")
    ApiResponse<List<Cart>> getAllCart(){
        return new ApiResponse<List<Cart>>(9999,"List of cart", cartService.viewAllCart());
    }
}
