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

    //view all cart items in 1 cart by cartID
    public List<CartItems> viewAllItemInCart(Integer cartID) {
        List<CartItems> cartItems = cartItemsRepository.findByCart_CartID(cartID)
                .orElseThrow(() -> new AppException(ErrorCode.CART_IS_EMPTY));
        Cart cart = cartItems.get(0).getCart();
        boolean updated = false;

        for (CartItems item : cartItems) {
            Products product = item.getProducts();

            if (item.getQuantity() > product.getInstockQuantity()) {
                // Nếu kho còn ít hơn số lượng trong giỏ
                changeQuantity(item.getCartItemID(), product.getInstockQuantity());
                updated = true;
            }

            if (product.getInstockQuantity() == 0) {
                // Nếu hết hàng, có thể chọn xoá item khỏi giỏ
                deleteOneItemInCart(item.getCartItemID());
            }
        }
        return cartItems;
    }

    @Transactional
    public void deleteAllItemsInCart(Integer cartID){
        Cart cart = cartService.findCartByID(cartID);
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
        cartItemsRepository.deleteByCart_CartID(cartID);
    }

    //view 1 cart item
    public CartItems findByCartItemsID(Integer cartItemsID){
        return cartItemsRepository.findById(cartItemsID)
                .orElseThrow(()-> new AppException(ErrorCode.PRODUCT_NOT_IN_CART));
    }

    public void deleteOneItemInCart(Integer cartItemsID){
        CartItems cartItems = findByCartItemsID(cartItemsID);// vao CartItemsID
        Cart cart = cartItems.getCart();//vao Cart
        cart.setTotalPrice(cart.getTotalPrice().subtract(cartItems.getPrice()));//lay total price ben cart - price ben cart items
        cartRepository.save(cart);
        cartItemsRepository.deleteById(cartItemsID);
    }

    public void changeQuantity(Integer cartItemsID, Integer newQuantity){
        CartItems cartItems = findByCartItemsID(cartItemsID);
        Cart cart = cartItems.getCart();
        if (newQuantity == 0){
            deleteOneItemInCart(cartItemsID);
            return;
        }
        if (newQuantity > cartItems.getProducts().getInstockQuantity()){
            throw new AppException(ErrorCode.PRODUCT_QUANTITY_NOT_FULLFILL);
        } else if (newQuantity > cartItems.getQuantity()){ //them vao
            int increaseQuantity = newQuantity - cartItems.getQuantity();
            BigDecimal addPrice = cartItems.getProducts().getPrice().multiply(BigDecimal.valueOf(increaseQuantity));
            cartItems.setPrice(cartItems.getPrice().add(addPrice));//chinh sua price ben cartItem
            cart.setTotalPrice(cart.getTotalPrice().add(addPrice));//chinh sua TotalPrice ben Cart
            cartItems.setQuantity(newQuantity);// sua quantity moi
            cartRepository.save(cart);
            cartItemsRepository.save(cartItems);
        } else { //giam di
            int decreaseQuantity = cartItems.getQuantity() - newQuantity;
            BigDecimal decreasePrice = cartItems.getProducts().getPrice().multiply(BigDecimal.valueOf(decreaseQuantity));
            cartItems.setPrice(cartItems.getPrice().subtract(decreasePrice));
            cart.setTotalPrice(cart.getTotalPrice().subtract(decreasePrice));
            cartItems.setQuantity(newQuantity);
            cartRepository.save(cart);
            cartItemsRepository.save(cartItems);
        }
    }
    //thieu lenh check lai current quantity khi load gio hang


}
