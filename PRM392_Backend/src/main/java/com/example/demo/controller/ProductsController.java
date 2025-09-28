package com.example.demo.controller;

import com.example.demo.dto.request.ProductsAddRequest;
import com.example.demo.dto.request.UpdateProductInfoRequest;
import com.example.demo.dto.request.UpdateQuantityProductRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Products;
import com.example.demo.service.ProductsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {
    @Autowired
    ProductsService productsService;

    @PostMapping("/add-new-product")
    ApiResponse<Products> addNewProduct(@RequestBody @Valid ProductsAddRequest request){
        ApiResponse<Products> apiResponse= new ApiResponse<>();
        apiResponse.setResult(productsService.addNewProducts(request));
        return apiResponse;
    }

    @GetMapping("/{id}")
    Products getProductByID(@PathVariable int id){
        return productsService.getProductByID(id);
    }

    @GetMapping("/fetchAll")
    ApiResponse<List<Products>> getAllProduct(){
        return new ApiResponse<List<Products>>(9999, "List of Products", productsService.getAllProducts());
    }
    @DeleteMapping("/delete/{id}")
    ApiResponse<String> deleteProductById(@PathVariable int id){
        productsService.deleteProductByID(id);
        return new ApiResponse<String>(9998, "Delete product successfully!");
    }
    @PutMapping("/update-info/{id}")
    ApiResponse<Products> updateProductInfo(@PathVariable int id, @RequestBody @Valid UpdateProductInfoRequest updateProductInfo){
        return new ApiResponse<Products>(9997, "Product's information have been updated!",
                productsService.updateProductInfo(id, updateProductInfo));
    }
    @PutMapping("/update-quantity/{id}")
    ApiResponse<Products> updateQuantity(@PathVariable int id, @RequestBody @Valid UpdateQuantityProductRequest request){
        return new ApiResponse<Products>(9996, "Product's quantity has been upadated!",
                productsService.updateQuantity(id, request));
    }
}
