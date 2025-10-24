package com.example.myapplication.network;

import com.example.myapplication.model.Product;
import com.example.myapplication.network.dto.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ProductService {
    @GET("products/fetchAll")
    Call<ApiResponse<List<Product>>> fetchAllProducts();
    
    @GET("products/{id}")
    Call<Product> getProductById(@Path("id") Long productId);
    
    @GET("products/by-category/{categoryId}")
    Call<ApiResponse<List<Product>>> getProductsByCategory(@Path("categoryId") Long categoryId);
}
