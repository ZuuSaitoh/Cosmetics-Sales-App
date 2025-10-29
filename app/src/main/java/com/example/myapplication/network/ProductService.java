package com.example.myapplication.network;

import com.example.myapplication.model.Product;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.AddProductRequest;

import java.util.List;

import retrofit2.Call;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.DELETE;
import okhttp3.ResponseBody;
import retrofit2.http.Path;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

public interface ProductService {
    @GET("products/fetchAll")
    Call<ApiResponse<List<Product>>> fetchAllProducts();
    
    @GET("products/{id}")
    Call<ApiResponse<Product>> getProductById(@Path("id") Long productId);
    
    @GET("products/by-category/{categoryId}")
    Call<ApiResponse<List<Product>>> getProductsByCategory(@Path("categoryId") Long categoryId);

    @POST("products/add-new-product")
    Call<ApiResponse<Product>> addProduct(@Body AddProductRequest request);

    // Multipart variant to send image file instead of imageURL
    @Multipart
    @POST("products/add-new-product")
    Call<ApiResponse<Product>> addProductMultipart(
            @Part MultipartBody.Part image,
            @Part("productName") RequestBody productName,
            @Part("briefDescription") RequestBody briefDescription,
            @Part("fullDescription") RequestBody fullDescription,
            @Part("price") RequestBody price,
            @Part("instockQuantity") RequestBody instockQuantity,
            @Part("categoryID") RequestBody categoryID,
            @Part("brand") RequestBody brand
    );

    @DELETE("products/delete/{id}")
    Call<ResponseBody> deleteProduct(@Path("id") Long id);

    @PUT("products/update-info/{id}")
    Call<ApiResponse<Product>> updateProductInfo(@Path("id") Long id, @Body com.example.myapplication.network.dto.UpdateProductInfoRequest request);

    @PUT("products/update-quantity/{id}")
    Call<ApiResponse<Product>> updateProductQuantity(@Path("id") Long id, @Body com.example.myapplication.network.dto.UpdateQuantityRequest request);
}
