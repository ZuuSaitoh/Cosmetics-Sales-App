package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.CartAdapter;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Cart;
import com.example.myapplication.model.CartItem;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.CartItemsResponse;
import com.example.myapplication.network.dto.CreateCartRequest;

import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnItemQuantityChangedListener, CartAdapter.OnItemRemovedListener {
    private RecyclerView recyclerView;
    private TextView textGrandTotal;
    private ImageButton btnBack;
    private Button btnContinueShopping;
    private Button btnOrder;
    private LinearLayout emptyState;
    private LinearLayout totalSection;

    private AuthService authService;
    private AuthManager authManager;
    private CartAdapter cartAdapter;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        authManager = new AuthManager(this);

        initViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartData();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_cart);
        textGrandTotal = findViewById(R.id.text_grand_total);
        btnBack = findViewById(R.id.btn_back);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        btnOrder = findViewById(R.id.btn_order);
        emptyState = findViewById(R.id.empty_state);
        totalSection = findViewById(R.id.total_section);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(Collections.emptyList());
        cartAdapter.setOnItemQuantityChangedListener(this);
        cartAdapter.setOnItemRemovedListener(this);
        recyclerView.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        btnOrder.setOnClickListener(v -> proceedOrder());
    }

    private void loadCartData() {
        Long userId = authManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng.", Toast.LENGTH_LONG).show();
            updateCartUI(Collections.emptyList()); // Show empty cart
            return;
        }

        authService.getCartByUserId(userId).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchCartItems(response.body().getCartID());
                } else {
                    // If cart not found, create one
                    createCart(userId);
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi khi lấy giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createCart(Long userId) {
        authService.createCart(new CreateCartRequest(userId)).enqueue(new Callback<Cart>() {
            @Override
            public void onResponse(Call<Cart> call, Response<Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Cart created, but it's empty, so just show the empty state
                    updateCartUI(Collections.emptyList());
                } else {
                    Toast.makeText(CartActivity.this, "Không thể tạo giỏ hàng.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Cart> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi khi tạo giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCartItems(Long cartId) {
        authService.getCartItems(cartId).enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateCartUI(response.body().getCartItems());
                } else {
                    Toast.makeText(CartActivity.this, "Không thể tải các mặt hàng trong giỏ.", Toast.LENGTH_SHORT).show();
                    updateCartUI(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi khi tải giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateCartUI(List<CartItem> items) {
        if (items == null || items.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            totalSection.setVisibility(View.GONE);
            cartAdapter.updateItems(Collections.emptyList());
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            totalSection.setVisibility(View.VISIBLE);
            cartAdapter.updateItems(items);
        }
        updateGrandTotal(items);
    }

    private void updateGrandTotal(List<CartItem> items) {
        double total = 0;
        if (items != null) {
            for (CartItem item : items) {
                total += item.getItemTotal();
            }
        }
        textGrandTotal.setText(numberFormat.format((long) total) + " VND");
        btnOrder.setEnabled(total > 0);
    }

    private void proceedOrder() {
        // This would navigate to a checkout activity.
        // For now, it will just show a toast.
        Toast.makeText(this, "Tiến hành thanh toán...", Toast.LENGTH_SHORT).show();
        // Example: startActivity(new Intent(this, CheckoutActivity.class));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        // Here you would make an API call to update the quantity in the backend
        // For now, we'll just show a toast and reload the cart data
        Toast.makeText(this, "Đã cập nhật số lượng cho " + item.getProduct().getName(), Toast.LENGTH_SHORT).show();
        loadCartData();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        // Here you would make an API call to remove the item from the cart
        // For now, we'll just show a toast and reload the cart data
        Toast.makeText(this, "Đã xóa " + item.getProduct().getName() + " khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
        loadCartData();
    }
}
