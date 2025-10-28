package com.example.myapplication.network;

import com.example.myapplication.model.Order; // Đảm bảo import đúng model Order
import com.example.myapplication.model.OrderDetail;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.CreateOrderRequest;
import com.example.myapplication.network.dto.CreateOrderResponse;
import com.example.myapplication.network.dto.PlaceOrderRequest;
import com.example.myapplication.network.dto.PlaceOrderResponse;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface OrderService {
    @POST("/orders/place-new-orders")
    Call<PlaceOrderResponse> placeNewOrder(@Body PlaceOrderRequest request);
    @GET("orders/fetch-by-user-id/{userID}")
    Call<ApiResponse<List<Order>>> getOrdersByUserId(@Path("userID") String userId);
    @GET("orders/fetch-by-order-id/{orderID}")
    Call<ApiResponse<OrderDetail>> getOrderDetail(@Path("orderID") int orderID);

    @DELETE("orders/delete-by-order-id/{orderID}")
    Call<ApiResponse> cancelOrder(@Path("orderID") int orderID);

    @POST("/orders/create-order")
    Call<CreateOrderResponse> createOrder(@Body CreateOrderRequest request);



}