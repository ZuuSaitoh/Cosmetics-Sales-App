package com.example.myapplication.network;

import com.example.myapplication.model.Cart;
import com.example.myapplication.network.dto.CartItemsResponse;
import com.example.myapplication.network.dto.AddCartItemRequest;
import com.example.myapplication.network.dto.ChangeQuantityRequest;
import okhttp3.ResponseBody;
import com.example.myapplication.network.dto.CreateCartRequest;
import com.example.myapplication.network.dto.LoginRequest;
import com.example.myapplication.network.dto.LoginResponse;
import com.example.myapplication.network.dto.RegisterRequest;
import com.example.myapplication.network.dto.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface AuthService {
    @POST("users/auth/token")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("users/create")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("carts/get-by-userID/{userID}")
    Call<Cart> getCartByUserId(@Path("userID") Long userID);

    @POST("carts/create")
    Call<Cart> createCart(@Body CreateCartRequest request);

    @GET("cart-items/fetchAll/{cartID}")
    Call<CartItemsResponse> getCartItems(@Path("cartID") Long cartID);

    @POST("cart-items/add-products")
    Call<ResponseBody> addProductToCart(@Body AddCartItemRequest request);

    @PUT("cart-items/change-quantity")
    Call<ResponseBody> changeQuantity(@Body ChangeQuantityRequest request);

    @DELETE("cart-items/delete/{cartID}")
    Call<Void> deleteAllCartItems(@Path("cartID") Long cartID);

    @DELETE("cart-items/delete/item/{cartItemID}")
    Call<Void> deleteCartItem(@Path("cartItemID") Long cartItemID);
}
