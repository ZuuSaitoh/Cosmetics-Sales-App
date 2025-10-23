package com.example.myapplication;


import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Order;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.PlaceOrderRequest;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ActivityCheckout extends AppCompatActivity {

    private ImageButton btnBack;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCashOnDelivery, rbVNPay;
    private TextView tvReceiverName, tvReceiverPhone, tvReceiverAddress;
    private TextView tvTotalPrice;
    private MaterialButton btnCheckout;

    private String selectedPaymentMethod = "COD";
    private double totalPrice = 0.0; // Giá trị đơn hàng (đồng)
    private Long currentCartId;
    private Long userId;
    private List<Long> selectedItems = new ArrayList<>();
    private boolean isPlacingOrder = false;


    private AuthService authService;
    private AuthManager authManager;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "cart_selection";
    private static final String SELECTED_ITEMS_KEY = "selected_items";
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        authManager = new AuthManager(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = authManager.getUserId();

        initViews();
        loadDataFromIntent();
        loadSelectedItems();
        setupListeners();
        loadDisplayData();
    }

    /**
     * Ánh xạ View
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCashOnDelivery = findViewById(R.id.rbCashOnDelivery);
        rbVNPay = findViewById(R.id.rbVNPay);
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvReceiverAddress = findViewById(R.id.tvReceiverAddress);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);
    }

    /**
     *  Lấy dữ liệu được gửi từ CartActivity
     */
    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            totalPrice = intent.getDoubleExtra("totalAmount", 0.0);
            // Lấy cartId, -1L là giá trị mặc định nếu không tìm thấy
            currentCartId = intent.getLongExtra("cartId", -1L);
            if (currentCartId == -1L) {
                currentCartId = null;
            }
        }
    }


    /**
     * Lấy danh sách sản phẩm đã chọn
     */
    private void loadSelectedItems() {
        String selectedItemsString = sharedPreferences.getString(SELECTED_ITEMS_KEY, "");
        selectedItems.clear();
        android.util.Log.d("Checkout", "Loading selected items from: " + selectedItemsString);
        
        if (!selectedItemsString.isEmpty()) {
            String[] itemIds = selectedItemsString.split(",");
            for (String itemId : itemIds) {
                try {
                    Long id = Long.parseLong(itemId.trim());
                    selectedItems.add(id);
                    android.util.Log.d("Checkout", "Added selected item ID: " + id);
                } catch (NumberFormatException e) {
                    android.util.Log.e("Checkout", "Invalid item ID: " + itemId);
                }
            }
        }
        
        android.util.Log.d("Checkout", "Final selected items count: " + selectedItems.size());
    }
    /**
     * Thiết lập sự kiện
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCashOnDelivery) {
                selectedPaymentMethod = "COD";
            } else if (checkedId == R.id.rbVNPay) {
                selectedPaymentMethod = "VNPAY";
            }
        });

        btnCheckout.setOnClickListener(v -> showConfirmDialog());
    }

    /**
     * Hiển thị dữ liệu
     */
    private void loadDisplayData() {
        // TODO: Lấy thông tin user (tên, sđt, địa chỉ) từ API hoặc SharedPreferences
        // Dữ liệu demo:
        tvReceiverName.setText("Nguyễn Văn A");
        tvReceiverPhone.setText("0123 456 789");
        tvReceiverAddress.setText("123 Nguyễn Huệ, P. Bến Nghé, Q.1, TP. HCM");

        // Sử dụng tổng tiền thật từ Intent
        tvTotalPrice.setText(formatMoney(totalPrice));
        
        // Kiểm tra nếu không có sản phẩm được chọn
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn. Vui lòng quay lại giỏ hàng.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Định dạng tiền VND
     */
    private String formatMoney(double amount) {
        return String.format("%,.0fđ", amount);
    }


    /**
     * Hộp thoại xác nhận đặt hàng (Kiểm tra dữ liệu)
     */
    private void showConfirmDialog() {
        if (userId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentCartId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy giỏ hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedItems == null || selectedItems.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có sản phẩm nào được chọn.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Debug: Log validation data
        android.util.Log.d("Checkout", "Validation - UserID: " + userId);
        android.util.Log.d("Checkout", "Validation - CartID: " + currentCartId);
        android.util.Log.d("Checkout", "Validation - SelectedItems size: " + (selectedItems != null ? selectedItems.size() : "null"));
        android.util.Log.d("Checkout", "Validation - TotalPrice: " + totalPrice);
        
        // Kiểm tra cart có tồn tại không trước khi đặt hàng
        checkCartExists();
    }


    /**
     * Gửi yêu cầu đặt hàng (POST /orders/place-new-orders)
     */
    private void placeOrder() {
        if (isPlacingOrder) return;
        
        android.util.Log.d("Checkout", "placeOrder() called - checking cart exists");
        // Kiểm tra cart trước khi đặt hàng
        checkCartExists();
    }
    
    private void placeOrderInternal() {
        if (isPlacingOrder) return;
        isPlacingOrder = true;
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");

        // Lấy địa chỉ từ TextView (đây là dữ liệu demo)
        // Trong thực tế, bạn nên cho người dùng nhập hoặc chọn địa chỉ
        String billingAddress = tvReceiverAddress.getText().toString();

        // Tạo đối tượng Request DTO
        PlaceOrderRequest orderRequest = new PlaceOrderRequest(
                userId,
                currentCartId,
                selectedPaymentMethod,
                billingAddress,
                selectedItems, // Danh sách ID sản phẩm đã chọn
                totalPrice
        );

        // Debug: Log dữ liệu gửi lên server
        android.util.Log.d("Checkout", "Order Request - UserID: " + userId);
        android.util.Log.d("Checkout", "Order Request - CartID: " + currentCartId);
        android.util.Log.d("Checkout", "Order Request - PaymentMethod: " + selectedPaymentMethod);
        android.util.Log.d("Checkout", "Order Request - BillingAddress: " + billingAddress);
        android.util.Log.d("Checkout", "Order Request - SelectedItems: " + selectedItems.toString());
        android.util.Log.d("Checkout", "Order Request - TotalAmount: " + totalPrice);

        // Gọi API thật bằng Retrofit
        authService.placeNewOrder(orderRequest).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                // Xử lý khi API trả về
                isPlacingOrder = false;
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Đặt hàng");

                // Debug: Log response
                android.util.Log.d("Checkout", "Response Code: " + response.code());
                android.util.Log.d("Checkout", "Response Message: " + response.message());
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        android.util.Log.e("Checkout", "Error Response Body: " + errorBody);
                    } catch (Exception e) {
                        android.util.Log.e("Checkout", "Error reading response body: " + e.getMessage());
                    }
                }

                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    Toast.makeText(ActivityCheckout.this, "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderID(), Toast.LENGTH_SHORT).show();
                    // Xóa các item đã chọn khỏi SharedPreferences
                    clearSelectedItems();
                    finish(); // Đóng ActivityCheckout

                } else {
                    // Xử lý lỗi từ server (ví dụ: 400, 500)
                    Toast.makeText(ActivityCheckout.this, "Đặt hàng thất bại. Vui lòng thử lại! (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                // Xử lý khi lỗi mạng
                isPlacingOrder = false;
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Đặt hàng");
                Toast.makeText(ActivityCheckout.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Kiểm tra cart có tồn tại không
     */
    private void checkCartExists() {
        android.util.Log.d("Checkout", "checkCartExists() called - UserID: " + userId);
        authService.getCartByUserId(userId).enqueue(new Callback<com.example.myapplication.model.Cart>() {
            @Override
            public void onResponse(Call<com.example.myapplication.model.Cart> call, Response<com.example.myapplication.model.Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long serverCartId = response.body().getCartID();
                    android.util.Log.d("Checkout", "Server Cart ID: " + serverCartId + ", Local Cart ID: " + currentCartId);
                    
                    if (!currentCartId.equals(serverCartId)) {
                        // Cart ID không khớp, cập nhật lại
                        currentCartId = serverCartId;
                        android.util.Log.d("Checkout", "Updated Cart ID to: " + currentCartId);
                    }
                    
                    // Tiếp tục với dialog xác nhận
                    showConfirmDialogInternal();
                } else {
                    // Cart không tồn tại, tạo mới
                    android.util.Log.d("Checkout", "Cart not found, creating new cart");
                    createNewCart();
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.model.Cart> call, Throwable t) {
                android.util.Log.e("Checkout", "Error checking cart: " + t.getMessage());
                Toast.makeText(ActivityCheckout.this, "Lỗi kiểm tra giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void createNewCart() {
        authService.createCart(new com.example.myapplication.network.dto.CreateCartRequest(userId)).enqueue(new Callback<com.example.myapplication.model.Cart>() {
            @Override
            public void onResponse(Call<com.example.myapplication.model.Cart> call, Response<com.example.myapplication.model.Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentCartId = response.body().getCartID();
                    android.util.Log.d("Checkout", "Created new cart with ID: " + currentCartId);
                    showConfirmDialogInternal();
                } else {
                    Toast.makeText(ActivityCheckout.this, "Không thể tạo giỏ hàng mới", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.model.Cart> call, Throwable t) {
                android.util.Log.e("Checkout", "Error creating cart: " + t.getMessage());
                Toast.makeText(ActivityCheckout.this, "Lỗi tạo giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showConfirmDialogInternal() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage("Bạn có chắc chắn muốn đặt đơn hàng này không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> placeOrderInternal())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * THÊM HÀM: Xóa các mục đã chọn sau khi đặt hàng thành công
     */
    private void clearSelectedItems() {
        sharedPreferences.edit()
                .remove(SELECTED_ITEMS_KEY)
                .apply();
    }


}
