package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapter.BannerAdapter;
import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.animation.CartAnimation;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.map.MapsActivity;
import com.example.myapplication.model.Banner;
import com.example.myapplication.model.Cart;
import com.example.myapplication.model.CartItem;
import com.example.myapplication.model.Product;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.ProductService;
import com.example.myapplication.network.dto.CartItemsResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {
    private RecyclerView bannerRecyclerView;
    private RecyclerView productRecyclerView;
    private BannerAdapter bannerAdapter;
    private ProductAdapter productAdapter;
    
    // Search and Cart
    private EditText searchEditText;
    private ImageView cartIcon;
    private TextView cartBadge;
    private ImageView locationIcon;
    private int cartItemCount = 0;
    private AuthManager authManager;
    private AuthService authService;

    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private int currentBannerPosition = 0;
    private int scrollDelay = 5000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView);
        productRecyclerView = view.findViewById(R.id.productRecyclerView);
        
        // Initialize search and cart
        searchEditText = view.findViewById(R.id.searchEditText);
        cartIcon = view.findViewById(R.id.cartIcon);
        cartBadge = view.findViewById(R.id.cartBadge);
        locationIcon = view.findViewById(R.id.locationIcon);

        // Initialize auth manager and service
        authManager = new AuthManager(getContext());
        if (getContext() != null) {
            authService = ApiClient.getRetrofit(getContext()).create(AuthService.class);
        }

        setupSearch();
        setupCart();
        setupBannerList();
        setupProductList();
        startAutoScroll();
        setupLocation();
        
        return view;
    }

    private void setupLocation() {
        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Context ctx = getContext();
                if (ctx != null) {
                    android.content.Intent i = new android.content.Intent(ctx, MapsActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void setupSearch() {
        try {
            if (searchEditText == null) return;

            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        if (productAdapter != null) {
                            productAdapter.getFilter().filter(s);
                        }
                        boolean hasQuery = s != null && s.length() > 0;
                        if (bannerRecyclerView != null) {
                            bannerRecyclerView.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setupCart() {
        try {
            updateCartBadge();
            if (cartIcon != null) {
                cartIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            android.content.Context ctx = getContext();
                            if (ctx != null) {
                                android.content.Intent i = new android.content.Intent(ctx, ActivityCart.class);
                                startActivity(i);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // filter handled by adapter's Filterable implementation

    public void addToCart() {
        updateCartBadge();
        
        // Chạy animation bay vào giỏ hàng với hình ảnh sản phẩm mặc định
        runCartAnimationWithProduct();
        
        Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    public void updateCartBadge() {
        int total = CartManager.getInstance().getTotalQuantity();
        if (total > 0) {
            cartBadge.setText(String.valueOf(total));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (autoScrollRunnable != null) {
            startAutoScroll();
        }
        // Load cart from server to sync with CartManager
        loadCartFromServer();
    }
    
    /** Load cart from server and sync with CartManager */
    private void loadCartFromServer() {
        Long userId = authManager != null ? authManager.getUserId() : null;
        if (userId == null || authService == null) {
            // If not logged in, just update badge from CartManager
            updateCartBadge();
            return;
        }
        
        // Load cart from server
        authService.getCartByUserId(userId).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long cartId = response.body().getCartID();
                    // Load cart items
                    fetchCartItems(cartId);
                } else {
                    // Cart not found, clear CartManager
                    CartManager.getInstance().clear();
                    updateCartBadge();
                }
            }
            
            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                // On error, just update badge from current CartManager state
                updateCartBadge();
            }
        });
    }
    
    /** Fetch cart items and sync with CartManager */
    private void fetchCartItems(Long cartId) {
        if (cartId == null || authService == null) {
            updateCartBadge();
            return;
        }
        
        authService.getCartItems(cartId).enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CartItem> items = response.body().getResult();
                    // Sync CartManager with server data
                    syncCartManagerWithServer(items);
                } else {
                    // No items, clear CartManager
                    CartManager.getInstance().clear();
                }
                updateCartBadge();
            }
            
            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                // On error, just update badge from current CartManager state
                updateCartBadge();
            }
        });
    }
    
    /** Sync CartManager with server cart data */
    private void syncCartManagerWithServer(List<CartItem> items) {
        // Clear CartManager first
        CartManager.getInstance().clear();
        
        // Add all items from server to CartManager
        if (items != null) {
            for (CartItem item : items) {
                if (item.getProduct() != null) {
                    CartManager.getInstance().addToCart(item.getProduct(), item.getQuantity());
                }
            }
        }
    }

    private void setupBannerList() {
        bannerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner(R.drawable.banner_1));
        banners.add(new Banner(R.drawable.banner_2));
        banners.add(new Banner(R.drawable.banner_3));
        banners.add(new Banner(R.drawable.banner_4));
        
        bannerAdapter = new BannerAdapter(banners);
        bannerRecyclerView.setAdapter(bannerAdapter);
    }

    private void setupProductList() {
        try {
            if (getContext() == null || productRecyclerView == null) return;

            productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

            // Initialize with empty list
            List<Product> products = new ArrayList<>();
            productAdapter = new ProductAdapter(getContext(), products);
            productAdapter.setOnProductClickListener(this);
            productRecyclerView.setAdapter(productAdapter);

            // Load products from API
            loadProductsFromAPI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadProductsFromAPI() {
        try {
            if (getContext() == null) {
                loadFallbackProducts();
                return;
            }

            ProductService productService = ApiClient.getRetrofit(getContext()).create(ProductService.class);

            productService.fetchAllProducts().enqueue(new Callback<com.example.myapplication.network.dto.ApiResponse<List<Product>>>() {
                @Override
                public void onResponse(Call<com.example.myapplication.network.dto.ApiResponse<List<Product>>> call, Response<com.example.myapplication.network.dto.ApiResponse<List<Product>>> response) {
                    try {
                        if (getContext() == null) return;

                        if (response.isSuccessful() && response.body() != null) {
                            com.example.myapplication.network.dto.ApiResponse<List<Product>> apiResponse = response.body();
                            if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                                List<Product> products = apiResponse.getResult();
                                if (productAdapter != null) {
                                    productAdapter.updateProducts(products);
                                }
                                // Toast.makeText(getContext(), "Đã tải " + products.size() + " sản phẩm", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Lỗi API: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                                loadFallbackProducts();
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải sản phẩm: " + response.code(), Toast.LENGTH_SHORT).show();
                            loadFallbackProducts();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        loadFallbackProducts();
                    }
                }

                @Override
                public void onFailure(Call<com.example.myapplication.network.dto.ApiResponse<List<Product>>> call, Throwable t) {
                    try {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                        loadFallbackProducts();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            loadFallbackProducts();
        }
    }

    private void loadFallbackProducts() {
        List<Product> fallbackProducts = new ArrayList<>();
        fallbackProducts.add(new Product("Lipstick", "High-quality lipstick", 25.99, R.drawable.banner_1));
        fallbackProducts.add(new Product("Foundation", "Perfect coverage foundation", 35.99, R.drawable.banner_1));
        fallbackProducts.add(new Product("Eyeshadow", "Beautiful eyeshadow palette", 29.99, R.drawable.banner_1));
        fallbackProducts.add(new Product("Mascara", "Long-lasting mascara", 19.99, R.drawable.banner_1));
        fallbackProducts.add(new Product("Blush", "Natural blush color", 22.99, R.drawable.banner_1));
        fallbackProducts.add(new Product("Concealer", "Full coverage concealer", 24.99, R.drawable.banner_1));

        productAdapter.updateProducts(fallbackProducts);
    }

    private void startAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
                    currentBannerPosition = (currentBannerPosition + 1) % bannerAdapter.getItemCount();
                    bannerRecyclerView.smoothScrollToPosition(currentBannerPosition);
                }
                autoScrollHandler.postDelayed(this, scrollDelay);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, scrollDelay);
    }

    private void stopAutoScroll() {
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }
    
    /**
     * Chạy animation bay vào giỏ hàng với hình ảnh sản phẩm
     */
    private void runCartAnimationWithProduct() {
        if (getView() != null && cartIcon != null && cartBadge != null) {
            // Tìm một view để làm source (có thể là search box hoặc banner)
            View sourceView = getView().findViewById(R.id.searchEditText);
            if (sourceView == null) {
                sourceView = getView().findViewById(R.id.bannerRecyclerView);
            }
            
            if (sourceView != null) {
                // Sử dụng hình ảnh sản phẩm mặc định
                CartAnimation.flyToCartWithDrawable(sourceView, cartIcon, getContext(), R.drawable.img_no_product, new CartAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart() {
                        // Animation bắt đầu
                    }
                    
                    @Override
                    public void onAnimationEnd() {
                        // Animation kết thúc - làm rung cart icon
                        if (cartIcon != null) {
                            cartIcon.animate()
                                .scaleX(1.2f)
                                .scaleY(1.2f)
                                .setDuration(150)
                                .withEndAction(() -> {
                                    cartIcon.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(150);
                                });
                        }
                    }
                });
            }
        }
    }
    
    /**
     * Chạy animation bay vào giỏ hàng (backward compatibility)
     */
    private void runCartAnimation() {
        runCartAnimationWithProduct();
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }
}
