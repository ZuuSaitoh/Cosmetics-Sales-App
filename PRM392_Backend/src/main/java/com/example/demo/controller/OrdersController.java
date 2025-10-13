package com.example.demo.controller;


import com.example.demo.dto.request.OrdersPlaceNewOrdersRequest;
import com.example.demo.dto.request.OrdersUpdateOrderStatusRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.entity.Orders;
import com.example.demo.service.OrdersService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersController {
    @Autowired
    OrdersService ordersService;

    @PostMapping("/place-new-orders")
    ApiResponse<Orders> placeNewOrders(@RequestBody @Valid OrdersPlaceNewOrdersRequest request){
        return new ApiResponse<Orders>(9999, "Success",
                ordersService.placeOrders(request.getUserID(), request.getPaymentMethod()));
    }
    @GetMapping("/fetch-all")
    ApiResponse<List<Orders>> fetchAllOrders(){
        return new ApiResponse(9999, "Success", ordersService.viewAllOrders());
    }
    @GetMapping("/fetch-by-user-id/{userID}")
    ApiResponse<List<Orders>> fetchOrdersByUserID(@PathVariable int userID){
        return new ApiResponse(9999, "Success", ordersService.viewAllOrdersByUserID(userID));
    }
    @GetMapping("/fetch-by-order-id/{orderID}")
    ApiResponse<Orders> fetchOrdersByOrderID(@PathVariable int orderID){
        return new ApiResponse(9999, "Success", ordersService.viewOrderByOrderID(orderID));
    }

    @DeleteMapping("/delete-by-order-id/{orderID}")
    ApiResponse<String> deleteOrdersByOrderID(@PathVariable int orderID){
        ordersService.deleteOrder(orderID);
        return new ApiResponse(9999, "Success", "Deleted order with ID: " + orderID);
    }

    @PutMapping("/update-order-status/{orderID}")
    ApiResponse<Orders> updateOrderStatus(@PathVariable int orderID, @RequestBody @Valid OrdersUpdateOrderStatusRequest request){
        return new ApiResponse(9999, "Success", ordersService.updateOrderStatus(orderID, request.getOrderStatus()));
    }
}
