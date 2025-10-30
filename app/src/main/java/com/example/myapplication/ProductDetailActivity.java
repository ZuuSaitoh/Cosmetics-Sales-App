package com.example.myapplication;

import static java.lang.Integer.parseInt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import com.example.myapplication.animation.CartAnimation;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ProductService;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.Product;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.CreateCartRequest;
import com.example.myapplication.network.dto.AddCartItemRequest;
import com.example.myapplication.network.dto.NotificationDTO;
import com.example.myapplication.model.Cart;
import com.example.myapplication.notification.NotificationCreator;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOGIN = 1001; // Hằng số cho startActivityForResult

    private ImageView imageProduct;
    private TextView textName, textPrice, textDescription, textFullDescription, textBrand, textCategory, textStock;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageButton btnCart;

    private Product product;
    private Long productId;
    private AuthManager authManager;
    private AuthService authService;
    private TextView cartBadge;
    private ImageView cartIcon;


    // --- SỬA: XÓA 2 BIẾN PENDING ---
    // private Product pendingProductFromLogin = null;
    // private int pendingQuantityFromLogin = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        authManager = new AuthManager(getApplicationContext());
        // Initialize cart-related API service
        authService = ApiClient.getRetrofit(this).create(AuthService.class);

        initViews();
        getProductDataFromIntent();
        setupCart();

        if (productId != null) {
            loadProductDetailFromAPI();
        } else {
            displayProductData();
        }
        displayProductData();

        // --- SỬA: ĐƠN GIẢN HÓA LOGIC CLICK ---
        btnAddToCart.setOnClickListener(v -> {
            authManager = new AuthManager(getApplicationContext()); // Luôn refresh authManager

            if (authManager.isLoggedIn()) {
                // Đã login -> Mở popup
                showQuantityPopup();
            } else {
                // Chưa login -> Đi login
                Intent loginIntent = new Intent(ProductDetailActivity.this, ActivityLogin.class);
                loginIntent.putExtra("pending_product", product);
                loginIntent.putExtra("pending_quantity", 1); // Gửi số lượng 1
                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
            }
        });

        btnBack.setOnClickListener(v -> finish());

    }

    private void initViews() {
        imageProduct = findViewById(R.id.image_product);
        textName = findViewById(R.id.text_name);
        textPrice = findViewById(R.id.text_price);
        textDescription = findViewById(R.id.text_description);
        textFullDescription = findViewById(R.id.text_full_description);
        textBrand = findViewById(R.id.text_brand);
        textCategory = findViewById(R.id.text_category);
        textStock = findViewById(R.id.text_stock);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBack = findViewById(R.id.btn_back);
        btnCart = findViewById(R.id.btn_cart);
        cartBadge = findViewById(R.id.cart_badge);

    }

    public void setupCart() {
        updateCartBadge(); // Cập nhật badge lần đầu
        if (btnCart != null) {
            btnCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(ProductDetailActivity.this, ActivityCart.class);
                    startActivity(i);
                }
            });
        }
    }
    public void updateCartBadge() {
        // Thêm kiểm tra null an toàn
        if (cartBadge == null) {
            return;
        }

        int total = CartManager.getInstance().getTotalQuantity();
        if (total > 0) {
            cartBadge.setText(String.valueOf(total));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    private void getProductDataFromIntent() {
        product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) {
            productId = product.getProductID();
        } else {
            Toast.makeText(this, "Không nhận được dữ liệu sản phẩm!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductDetailFromAPI() {
        // (Giữ nguyên code hàm này của bạn)
        if (productId == null) {
            displayProductData();
            return;
        }
        ProductService productService = ApiClient.getRetrofit(this).create(ProductService.class);
        productService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = response.body();
                    displayProductData();
                } else {
                    displayProductData(); // Fallback
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                displayProductData(); // Fallback
            }
        });
    }

    private void displayProductData() {
        if (product == null) {
            return;
        }
        textName.setText(product.getProductName() != null ? product.getProductName() : "Tên sản phẩm");
        String formattedPrice = String.format("%,.0f VND", product.getPrice());
        textPrice.setText(formattedPrice);
        textDescription.setText(product.getBriefDescription() != null ? product.getBriefDescription() : "Mô tả sản phẩm");
        if (textFullDescription != null) {
            textFullDescription.setText(product.getFullDescription() != null ? product.getFullDescription() : "Mô tả chi tiết");
        }
        if (textBrand != null) {
            textBrand.setText("Thương hiệu: " + (product.getBrand() != null ? product.getBrand() : "Không có"));
        }
        if (textCategory != null && product.getCategoryID() != null) {
            textCategory.setText("Danh mục: " + product.getCategoryID().getCategoryName());
        } else if (textCategory != null) {
            textCategory.setText("Danh mục: Không có");
        }
        if (textStock != null) {
            textStock.setText("Còn lại: " + (product.getInstockQuantity() != null ? product.getInstockQuantity() : 0) + " sản phẩm");
        }
        loadProductImage();
    }

    private void loadProductImage() {
        try {
            String imageUrl = product.getImageURL();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.img_no_product) // ảnh tạm khi đang load
                        .error(R.drawable.img_no_product)       // ảnh khi lỗi
                        .into(imageProduct);
            } else {
                imageProduct.setImageResource(R.drawable.img_no_product);
            }

        } catch (Exception e) {
            imageProduct.setImageResource(R.drawable.img_no_product);
            e.printStackTrace();
        }
    }



    private void showQuantityPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProductDetailActivity.this);
        bottomSheetDialog.setContentView(R.layout.dialog_quantity_product);

        TextView textTotalPrice = bottomSheetDialog.findViewById(R.id.text_total_price);
        TextView textQuantity = bottomSheetDialog.findViewById(R.id.text_quantity);
        ImageButton btnMinus = bottomSheetDialog.findViewById(R.id.btn_minus);
        ImageButton btnPlus = bottomSheetDialog.findViewById(R.id.btn_plus);
        Button btnConfirm = bottomSheetDialog.findViewById(R.id.btn_confirm_add);
        
        // Thêm các view để hiển thị thông tin sản phẩm
        ImageView imageProductPopup = bottomSheetDialog.findViewById(R.id.image_product);
        TextView textProductName = bottomSheetDialog.findViewById(R.id.text_product_name);

        if (textQuantity == null || btnMinus == null || btnPlus == null || btnConfirm == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy layout popup!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Cập nhật thông tin sản phẩm trong popup
        if (product != null) {
            // Cập nhật tên sản phẩm
            if (textProductName != null) {
                textProductName.setText(product.getName());
            }
            
            // Cập nhật hình ảnh sản phẩm
            if (imageProductPopup != null) {
                if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
                    // Sử dụng Glide để load hình ảnh từ URL
                    com.bumptech.glide.Glide.with(this)
                        .load(product.getImageURL().trim())
                        .placeholder(R.drawable.img_no_product)
                        .error(R.drawable.img_no_product)
                        .into(imageProductPopup);
                } else {
                    // Fallback: sử dụng hình ảnh mặc định
                    imageProductPopup.setImageResource(R.drawable.img_no_product);
                }
            }
        }

        final int[] quantity = {1};
        textQuantity.setText(String.valueOf(quantity[0]));

        double totalPrice = product.getPrice() * quantity[0];
        textTotalPrice.setText(String.format("%,.0f VND", totalPrice));

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                textQuantity.setText(String.valueOf(quantity[0]));
                double totalPriceMinus = product.getPrice() * quantity[0];
                textTotalPrice.setText(String.format("%,.0f VND", totalPriceMinus));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            textQuantity.setText(String.valueOf(quantity[0]));
            double totalPricePlus = product.getPrice() * quantity[0];
            textTotalPrice.setText(String.format("%,.0f VND", totalPricePlus));
        });

        // Confirm add -> call backend add-to-cart API
        btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            ensureCartThenAdd(quantity[0]);
        });

        bottomSheetDialog.show();
    }

    private void ensureCartThenAdd(int quantity) {
        if (product == null) {
            Toast.makeText(this, "Không có sản phẩm để thêm.", Toast.LENGTH_SHORT).show();
            return;
        }

        Long userId = authManager != null ? authManager.getUserId() : null;
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập trước khi thêm giỏ hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1) Get cart by user, if not exists -> create, then add
        authService.getCartByUserId(userId).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long cartId = response.body().getCartID();
                    // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
                    checkAndAddProduct(cartId, product.getProductID(), quantity);
                } else {
                    // Create cart then add
                    authService.createCart(new CreateCartRequest(userId)).enqueue(new Callback<Cart>() {
                        @Override
                        public void onResponse(Call<Cart> call2, Response<Cart> resp2) {
                            if (resp2.isSuccessful() && resp2.body() != null) {
                                Long newCartId = resp2.body().getCartID();
                                // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
                                checkAndAddProduct(newCartId, product.getProductID(), quantity);
                            } else {
                                Toast.makeText(ProductDetailActivity.this, "Không thể tạo giỏ hàng.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Cart> call2, Throwable t) {
                            Toast.makeText(ProductDetailActivity.this, "Lỗi tạo giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi khi lấy giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndAddProduct(Long cartId, Long productId, int quantity) {
        // Kiểm tra sản phẩm đã tồn tại trong giỏ hàng chưa
        authService.getCartItems(cartId).enqueue(new Callback<com.example.myapplication.network.dto.CartItemsResponse>() {
            @Override
            public void onResponse(Call<com.example.myapplication.network.dto.CartItemsResponse> call, Response<com.example.myapplication.network.dto.CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.myapplication.network.dto.CartItemsResponse cartItemsResponse = response.body();
                    List<com.example.myapplication.model.CartItem> existingItems = cartItemsResponse.getResult();
                    
                    // Tìm sản phẩm đã tồn tại
                    com.example.myapplication.model.CartItem existingItem = null;
                    if (existingItems != null) {
                        for (com.example.myapplication.model.CartItem item : existingItems) {
                            if (item.getProduct().getProductID().equals(productId)) {
                                existingItem = item;
                                break;
                            }
                        }
                    }
                    
                    if (existingItem != null) {
                        // Sản phẩm đã tồn tại -> cập nhật số lượng
                        int newQuantity = existingItem.getQuantity() + quantity;
                        updateExistingProductQuantity(existingItem.getCartItemID(), newQuantity, quantity);
                    } else {
                        // Sản phẩm chưa tồn tại -> thêm mới
                        callAddProduct(cartId, productId, quantity);
                    }
                } else {
                    // Nếu không lấy được cart items, thử thêm mới
                    callAddProduct(cartId, productId, quantity);
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.network.dto.CartItemsResponse> call, Throwable t) {
                // Nếu có lỗi khi lấy cart items, thử thêm mới
                callAddProduct(cartId, productId, quantity);
            }
        });
    }

    private void updateExistingProductQuantity(Long cartItemId, int newQuantity, int quantityToAdd) {
        com.example.myapplication.network.dto.ChangeQuantityRequest request = 
            new com.example.myapplication.network.dto.ChangeQuantityRequest(cartItemId, newQuantity);
        
        authService.changeQuantity(request).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Cập nhật CartManager để badge hiển thị đúng
                    CartManager.getInstance().addToCart(product, quantityToAdd);
                    
                    // Chạy animation bay vào giỏ hàng
                    runCartAnimation();
                    
                    Toast.makeText(ProductDetailActivity.this, "Đã cập nhật số lượng sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show();
                    updateCartBadge();
                } else {
                    String msg = "Cập nhật số lượng thất bại (" + response.code() + ")";
                    Toast.makeText(ProductDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi cập nhật số lượng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callAddProduct(Long cartId, Long productId, int quantity) {
        if (cartId == null || productId == null) {
            Toast.makeText(this, "Thiếu thông tin giỏ hàng hoặc sản phẩm.", Toast.LENGTH_SHORT).show();
            return;
        }
        authService.addProductToCart(new AddCartItemRequest(cartId, productId, quantity)).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Cập nhật CartManager để badge hiển thị đúng
                    CartManager.getInstance().addToCart(product, quantity);
                    
                    // Chạy animation bay vào giỏ hàng
                    runCartAnimation();
                    
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    updateCartBadge();
                    
                    // Tạo notification "Giỏ hàng của bạn có X sản phẩm"
                    createCartNotification(quantity);
                } else {
                    String msg = "Thêm vào giỏ hàng thất bại (" + response.code() + ")";
                    Toast.makeText(ProductDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- SỬA: THAY ĐỔI HOÀN TOÀN LOGIC onActivityResult ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOGIN) {
            // 1. Refresh lại AuthManager để đọc SharedPreferences mới nhất
            authManager = new AuthManager(getApplicationContext());

            // 2. Kiểm tra xem login CÓ THỰC SỰ thành công không
            if (resultCode == RESULT_OK && authManager.isLoggedIn()) {

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // 3. TỰ ĐỘNG MỞ POPUP (đây là mấu chốt của flow)
                showQuantityPopup();

            } else {
                // Login thất bại (vì token null) hoặc user bấm back (hủy)
                Toast.makeText(this, "Chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Chạy animation bay vào giỏ hàng
     */
    private void runCartAnimation() {
        if (btnAddToCart != null && btnCart != null) {
            // Sử dụng hình ảnh sản phẩm từ ImageView nếu có
            if (imageProduct != null && imageProduct.getDrawable() != null) {
                CartAnimation.flyToCartWithProduct(btnAddToCart, btnCart, imageProduct, new CartAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart() {
                        // Animation bắt đầu
                    }
                    
                    @Override
                    public void onAnimationEnd() {
                        // Animation kết thúc - có thể thêm hiệu ứng khác ở đây
                        // Ví dụ: làm rung cart icon hoặc thay đổi màu
                        if (btnCart != null) {
                            btnCart.animate()
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    btnCart.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(100);
                                });
                        }
                    }
                });
            } else if (product != null) {
                // Sử dụng Product object để tạo animation
                CartAnimation.flyToCartWithProduct(btnAddToCart, btnCart, this, product, new CartAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart() {
                        // Animation bắt đầu
                    }
                    
                    @Override
                    public void onAnimationEnd() {
                        // Animation kết thúc - có thể thêm hiệu ứng khác ở đây
                        // Ví dụ: làm rung cart icon hoặc thay đổi màu
                        if (btnCart != null) {
                            btnCart.animate()
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    btnCart.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(100);
                                });
                        }
                    }
                });
            } else {
                // Fallback: sử dụng hình ảnh mặc định
                CartAnimation.flyToCartWithDrawable(btnAddToCart, btnCart, this, R.drawable.img_no_product, new CartAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart() {
                        // Animation bắt đầu
                    }
                    
                    @Override
                    public void onAnimationEnd() {
                        // Animation kết thúc - có thể thêm hiệu ứng khác ở đây
                        // Ví dụ: làm rung cart icon hoặc thay đổi màu
                        if (btnCart != null) {
                            btnCart.animate()
                                .scaleX(1.1f)
                                .scaleY(1.1f)
                                .setDuration(100)
                                .withEndAction(() -> {
                                    btnCart.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(100);
                                });
                        }
                    }
                });
            }
        }
    }
    
    /**
     * Tạo notification khi thêm sản phẩm vào giỏ hàng
     * Notification sẽ hiển thị ở tab "Của bạn" trong trang Notifications
     */
    private void createCartNotification(int addedQuantity) {
        Long userId = authManager != null ? authManager.getUserId() : null;
        if (userId == null) {
            android.util.Log.w("ProductDetailActivity", "Cannot create notification - userId is null");
            return;
        }
        
        // Lấy tổng số items trong giỏ hàng
        int totalItems = CartManager.getInstance().getTotalQuantity();
        
        // Lấy tên sản phẩm
        String productName = product != null ? product.getName() : "Sản phẩm";
        
        // Tạo notification qua API
        NotificationCreator creator = new NotificationCreator(this);
        creator.notifyProductAddedToCart(userId, productName, addedQuantity, totalItems, 
            new NotificationCreator.OnNotificationCreatedListener() {
                @Override
                public void onSuccess(NotificationDTO notification) {
                    android.util.Log.d("ProductDetailActivity", "✅ Cart notification created: " + notification.getNotificationId());
                    // Không cần show gì cho user - notification sẽ hiển thị trong NotificationsFragment
                }
                
                @Override
                public void onError(String error) {
                    // Silent fail - không ảnh hưởng UX chính
                    android.util.Log.e("ProductDetailActivity", "Failed to create cart notification: " + error);
                }
            });
    }
}