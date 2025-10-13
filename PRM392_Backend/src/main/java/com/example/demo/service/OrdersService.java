package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrdersRepository;
import com.example.demo.repository.ProductsRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrdersService {
    @Autowired
    OrdersRepository ordersRepository;
    @Autowired
    CartService cartService;
    @Autowired
    CartItemsService cartItemsService;
    @Autowired
    UsersService usersService;
    @Autowired
    CartRepository cartRepository;
    @Autowired
    ProductsRepository productsRepository;

    public Orders placeOrders(Integer userID, String paymentMethod) {
        Cart cart = cartService.getActiveCart(userID);
        Users users= usersService.getUserByID(userID);
        List<CartItems> cartItems = cartItemsService.viewAllItemInCart(cart.getCartID());

        if (cart.getStatus().equals("completed")) {
            throw new AppException(ErrorCode.CART_ALREADY_CHECKED_OUT);
        }

        // Check if cart is empty
        if (cartItems.isEmpty()){
            throw new AppException(ErrorCode.CART_HAVE_NOTHING);
        }

        // Check if any product is out of stock
        for (CartItems item : cartItems) {
            Products product = item.getProducts();
            if (item.getQuantity() > product.getInstockQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }
        }

        //check payment method
        List<String> supported = List.of("COD", "Momo", "ZaloPay");
        if (!supported.contains(paymentMethod)) {
            throw new AppException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
        }

        if (users.getAddress() == null || users.getAddress().isEmpty()) {
            throw new AppException(ErrorCode.BILLING_ADDRESS_NOT_VALID);
        }

        //tao order
        Orders orders = new Orders(cart, users, "COD", users.getAddress(),
                "Processing", java.time.LocalDateTime.now());
        cartService.changeStatus(userID);

        // Update stock quantities
        for (CartItems item : cartItems) {
            Products product = item.getProducts();
            product.setInstockQuantity(product.getInstockQuantity() - item.getQuantity());
            productsRepository.save(product); // Cập nhật lại số lượng tồn kho của sản phẩm
        }

        //save order and cart
        cartRepository.save(cart);
        return ordersRepository.save(orders);
    }

    public List<Orders> viewAllOrders(){
        return ordersRepository.findAll();
    }

    public Orders viewOrderByOrderID(Integer orderID){
        return ordersRepository.findById(orderID).orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_EXISTED));
    }

    public List<Orders> viewAllOrdersByUserID(Integer userID){
        return ordersRepository.findByUser_UserID(userID)
                .orElseThrow(()-> new AppException(ErrorCode.ORDER_NOT_EXISTED));
    }

    public void deleteOrder(Integer orderID){
        ordersRepository.deleteById(orderID);
    }

    public Orders updateOrderStatus(Integer orderID, String status) {
        Orders order = viewOrderByOrderID(orderID);

        List<String> validStatuses = List.of("Processing", "Shipped", "Delivered", "Cancelled");
        if (!validStatuses.contains(status)) {
            throw new AppException(ErrorCode.STATUS_NOT_VALID);
        }
        order.setOrderStatus(status);
        return ordersRepository.save(order);
    }
}













