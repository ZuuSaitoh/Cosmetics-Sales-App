package com.example.demo.service;

import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItems;
import com.example.demo.entity.Products;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.CartItemsRepository;
import com.example.demo.repository.CartRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartItemsService {
    @Autowired
    CartItemsRepository cartItemsRepository;

    @Autowired
    CartService cartService;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductsService productsService;


    public CartItems addProductToCart(Integer cartID, Integer productID, Integer quantity) {
        Cart cart = cartService.findCartByID(cartID);
        Products products = productsService.getProductByID(productID);
        if (cartItemsRepository.existsByCart_CartIDAndProducts_ProductID(cartID, productID)){
            throw new AppException(ErrorCode.PRODUCT_ALREADY_IN_CART);
        }

        if (quantity > products.getInstockQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_QUANTITY_NOT_FULLFILL);
        }
        // price trong day la price cua san pham * quantity
        BigDecimal price = products.getPrice().multiply(BigDecimal.valueOf(quantity));
        cart.setTotalPrice(cart.getTotalPrice().add(price));
        cartRepository.save(cart);
        return cartItemsRepository.save(new CartItems(cart, products, quantity, price));
    }

    public List<CartItems> viewAllItemInCart(Integer cartID) {
        return cartItemsRepository.findByCart_CartID(cartID)
                .orElseThrow(() -> new AppException(ErrorCode.CART_HAVE_NOTHING));
    }

    @Transactional
    public void deleteAllItemsInCart(Integer cartID){
        Cart cart = cartService.findCartByID(cartID);
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
        cartItemsRepository.deleteByCart_CartID(cartID);
    }

    //thieu lenh update quantity(dieu chinh gia totalPrice)
    //thieu lenh xoa tung san pham khoi gio hang
    //thieu lenh check lai current quantity khi load gio hang











}
