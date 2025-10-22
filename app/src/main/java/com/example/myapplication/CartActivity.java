package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.CartAdapter;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Cart;
import com.example.myapplication.model.CartItem;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.CartItemsResponse;
import com.example.myapplication.network.dto.ChangeQuantityRequest;
import com.example.myapplication.network.dto.CreateCartRequest;
import com.example.myapplication.notification.BootReceiver;
import com.example.myapplication.notification.NotificationHelper;

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
    private ImageButton btnDeleteAll;
    private LinearLayout emptyState;
    private LinearLayout totalSection;
    private Long currentCartId;

    private AuthService authService;
    private AuthManager authManager;
    private CartAdapter cartAdapter;
    
    // Notification helper
    private NotificationHelper notificationHelper;
    private int currentCartItemCount = 0;
    
    // Permission launcher for notification (Android 13+)
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission granted - notification sẽ được show khi cần
                } else {
                    // Permission denied - có thể show explanation dialog
                    Toast.makeText(this, "Bạn cần cấp quyền thông báo để nhận cập nhật về giỏ hàng", Toast.LENGTH_LONG).show();
                }
            });

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        authManager = new AuthManager(this);
        notificationHelper = new NotificationHelper(this);

        initViews();
        setupClickListeners();
        
        // Request notification permission if needed (Android 13+)
        requestNotificationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear cart notification khi user vào cart activity
        notificationHelper.clearCartNotification();
        loadCartData();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Show notification nếu còn items trong cart khi rời khỏi activity
        showCartNotificationIfNeeded();
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Save cart state để restore sau khi device restart
        saveCartState();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_cart);
        textGrandTotal = findViewById(R.id.text_grand_total);
        btnBack = findViewById(R.id.btn_back);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        btnOrder = findViewById(R.id.btn_order);
        btnDeleteAll = findViewById(R.id.btn_delete_all);
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
        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());
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
                    currentCartId = response.body().getCartID();
                    fetchCartItems(currentCartId);
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
                    currentCartId = response.body().getCartID();
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
        boolean isLoggedIn = authManager.getUserId() != null;
        
        // Calculate total item count
        currentCartItemCount = 0;
        if (items != null) {
            for (CartItem item : items) {
                currentCartItemCount += item.getQuantity();
            }
        }
        
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
        
        // Only show buttons when user is logged in and cart has items
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        if (isLoggedIn && items != null && !items.isEmpty()) {
            buttonContainer.setVisibility(View.VISIBLE);
            // Update constraint to position total section above buttons
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) totalSection.getLayoutParams();
            params.bottomToTop = R.id.button_container;
            totalSection.setLayoutParams(params);
        } else {
            buttonContainer.setVisibility(View.GONE);
            // Update constraint to position total section at bottom
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) totalSection.getLayoutParams();
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            totalSection.setLayoutParams(params);
        }
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
        btnDeleteAll.setEnabled(total > 0);
    }

    private void proceedOrder() {
        // This would navigate to a checkout activity.
        // For now, it will just show a toast.
        Toast.makeText(this, "Tiến hành thanh toán...", Toast.LENGTH_SHORT).show();
        // Example: startActivity(new Intent(this, CheckoutActivity.class));
    }

    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        if (item.getCartItemID() == null) {
            Toast.makeText(this, "Không thể cập nhật số lượng sản phẩm này", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Log for debugging
        android.util.Log.d("CartActivity", "Changing quantity for cart item ID: " + item.getCartItemID() + " to: " + newQuantity);
        
        ChangeQuantityRequest request = new ChangeQuantityRequest(item.getCartItemID(), newQuantity);
        
        authService.changeQuantity(request).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                android.util.Log.d("CartActivity", "Change quantity response code: " + response.code());
                if (response.isSuccessful()) {
                    Toast.makeText(CartActivity.this, "Đã cập nhật số lượng cho " + item.getProduct().getName(), Toast.LENGTH_SHORT).show();
                    loadCartData(); // Reload to update the cart
                } else {
                    android.util.Log.e("CartActivity", "Change quantity failed with code: " + response.code());
                    Toast.makeText(CartActivity.this, "Không thể cập nhật số lượng (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                android.util.Log.e("CartActivity", "Change quantity request failed", t);
                Toast.makeText(CartActivity.this, "Lỗi khi cập nhật số lượng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemRemoved(CartItem item) {
        if (item.getCartItemID() == null) {
            Toast.makeText(this, "Không thể xóa sản phẩm này", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Log for debugging
        android.util.Log.d("CartActivity", "Removing cart item ID: " + item.getCartItemID());
        
        // Call API to delete the item from cart
        deleteCartItem(item.getCartItemID());
    }

    private void showDeleteAllConfirmation() {
        Dialog dialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_delete_confirmation_dialog, null);
        
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnConfirmDelete = dialogView.findViewById(R.id.btn_confirm_delete);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirmDelete.setOnClickListener(v -> {
            dialog.dismiss();
            deleteAllCartItems();
        });
        
        dialog.setContentView(dialogView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    private void deleteCartItem(Long cartItemId) {
        if (cartItemId == null) {
            Toast.makeText(this, "Không thể xóa sản phẩm này", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.deleteCartItem(cartItemId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CartActivity.this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    loadCartData(); // Reload to update the cart
                } else {
                    Toast.makeText(CartActivity.this, "Không thể xóa sản phẩm (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi khi xóa sản phẩm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAllCartItems() {
        if (currentCartId == null) {
            Toast.makeText(this, "Không tìm thấy giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        authService.deleteAllCartItems(currentCartId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CartActivity.this, "Đã xóa toàn bộ sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show();
                    loadCartData(); // Reload to show empty state
                } else {
                    Toast.makeText(CartActivity.this, "Không thể xóa giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi khi xóa giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    // ============ NOTIFICATION METHODS ============
    
    /**
     * Request notification permission (Android 13+)
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // Show explanation dialog
                    new AlertDialog.Builder(this)
                            .setTitle("Cho phép thông báo")
                            .setMessage("Ứng dụng cần quyền thông báo để cập nhật bạn về giỏ hàng và đơn hàng của bạn.")
                            .setPositiveButton("Cho phép", (dialog, which) -> 
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                            .setNegativeButton("Không", (dialog, which) -> dialog.dismiss())
                            .show();
                } else {
                    // No explanation needed, request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
    }
    
    /**
     * Show cart notification nếu còn items và app đang chuyển sang background
     */
    private void showCartNotificationIfNeeded() {
        Long userId = authManager.getUserId();
        
        // Chỉ show notification nếu:
        // 1. User đã login
        // 2. Có items trong cart
        // 3. Notification permission đã được granted (hoặc không cần trên Android < 13)
        if (userId != null && currentCartItemCount > 0) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || 
                notificationHelper.areNotificationsEnabled()) {
                notificationHelper.showCartBadgeNotification(String.valueOf(userId), currentCartItemCount);
            }
        }
    }
    
    /**
     * Save cart state để restore sau khi device restart
     */
    private void saveCartState() {
        Long userId = authManager.getUserId();
        if (userId != null) {
            BootReceiver.saveCartState(this, String.valueOf(userId), currentCartItemCount);
        }
    }
}
