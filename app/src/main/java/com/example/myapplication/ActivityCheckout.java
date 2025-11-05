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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.CheckoutItemAdapter;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.CartItem;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.OrderService;
import com.example.myapplication.network.dto.PlaceOrderRequest;
import com.example.myapplication.network.dto.PlaceOrderResponse;
import com.example.myapplication.network.dto.NotificationDTO;
import com.example.myapplication.notification.NotificationCreator;
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
    private double totalPrice = 0.0;
    private Long currentCartId;
    private Long userId;
    private List<Long> selectedItems = new ArrayList<>();
    private boolean isPlacingOrder = false;

    private AuthService authService;
    private OrderService orderService;
    private AuthManager authManager;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "cart_selection";
    private static final String SELECTED_ITEMS_KEY = "selected_items";
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    private RecyclerView rvCheckoutItems;
    private CheckoutItemAdapter checkoutItemAdapter;
    private ArrayList<CartItem> selectedProductList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        orderService = ApiClient.getRetrofit(this).create(OrderService.class);
        authManager = new AuthManager(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        userId = authManager.getUserId();

        initViews();
        loadDataFromIntent();
        loadSelectedItems();
        setupListeners();
        loadDisplayData();
    }

    /** Ánh xạ View */
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
        rvCheckoutItems = findViewById(R.id.rvCheckoutItems);
    }

    /** Lấy dữ liệu được gửi từ CartActivity */
    private void loadDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            totalPrice = intent.getDoubleExtra("totalAmount", 0.0);
            currentCartId = intent.getLongExtra("cartId", -1L);
            if (currentCartId == -1L) {
                currentCartId = null;
            }
            if (intent.hasExtra("selectedProducts")) {
                selectedProductList = (ArrayList<CartItem>) intent.getSerializableExtra("selectedProducts");
            }
        }
    }

    /** Lấy danh sách sản phẩm đã chọn */
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

    /** Thiết lập sự kiện */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCashOnDelivery) {
                selectedPaymentMethod = "COD";
            } else if (checkedId == R.id.rbVNPay) {
                selectedPaymentMethod = "VNPay";
            }
        });

        btnCheckout.setOnClickListener(v -> showConfirmDialog());
    }

    /** Dùng data demo khi gọi API thất bại */
    private void setDemoUserData() {
        tvReceiverName.setText("Nguyễn Văn A (Demo)");
        tvReceiverPhone.setText("0123 456 789");
        tvReceiverAddress.setText("123 Nguyễn Huệ, P. Bến Nghé, Q.1, TP. HCM");
    }

    /** Lấy thông tin User từ API */
    private void loadUserProfile() {
        if (userId == null) {
            Toast.makeText(this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
            return;
        }
        authService.getUserProfile(userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    tvReceiverName.setText(user.getUsername());
                    tvReceiverPhone.setText(user.getPhoneNumber());
                    tvReceiverAddress.setText(user.getAddress());
                } else {
                    Toast.makeText(ActivityCheckout.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    setDemoUserData();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ActivityCheckout.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                setDemoUserData();
            }
        });
    }

    /** Hiển thị danh sách sản phẩm */
    private void setupProductRecyclerView() {
        if (selectedProductList != null && !selectedProductList.isEmpty()) {
            rvCheckoutItems.setVisibility(View.VISIBLE);
            checkoutItemAdapter = new CheckoutItemAdapter(selectedProductList, this);
            rvCheckoutItems.setLayoutManager(new LinearLayoutManager(this));
            rvCheckoutItems.setAdapter(checkoutItemAdapter);
        } else {
            TextView tvProductHeader = findViewById(R.id.tvProductHeader);
            if (tvProductHeader != null) tvProductHeader.setVisibility(View.GONE);
            rvCheckoutItems.setVisibility(View.GONE);
        }
    }

    /** Hiển thị dữ liệu */
    private void loadDisplayData() {
        loadUserProfile();
        setupProductRecyclerView();
        tvTotalPrice.setText(formatMoney(totalPrice));
        if (selectedProductList == null || selectedProductList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào được chọn. Vui lòng quay lại giỏ hàng.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /** Định dạng tiền VND */
    private String formatMoney(double amount) {
        return String.format("%,.0fđ", amount);
    }

    /** Hiển thị hộp thoại xác nhận */
    private void showConfirmDialog() {
        if (userId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }
        if (currentCartId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy giỏ hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedProductList == null || selectedProductList.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có sản phẩm nào được chọn.", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("Checkout", "Validation - UserID: " + userId);
        android.util.Log.d("Checkout", "Validation - CartID: " + currentCartId);
        android.util.Log.d("Checkout", "Validation - SelectedProductList size: " + selectedProductList.size());
        android.util.Log.d("Checkout", "Validation - TotalPrice: " + totalPrice);

        checkCartExists();
    }

    /** Kiểm tra cart tồn tại */
    private void checkCartExists() {
        android.util.Log.d("Checkout", "checkCartExists() called - UserID: " + userId);
        authService.getCartByUserId(userId).enqueue(new Callback<com.example.myapplication.model.Cart>() {
            @Override
            public void onResponse(Call<com.example.myapplication.model.Cart> call, Response<com.example.myapplication.model.Cart> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Long serverCartId = response.body().getCartID();
                    android.util.Log.d("Checkout", "Server Cart ID: " + serverCartId + ", Local Cart ID: " + currentCartId);
                    if (!currentCartId.equals(serverCartId)) {
                        currentCartId = serverCartId;
                        android.util.Log.d("Checkout", "Updated Cart ID to: " + currentCartId);
                    }
                    showConfirmDialogInternal();
                } else {
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

    /** Tạo mới cart */
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

    /** Hiển thị dialog xác nhận thực sự */
    private void showConfirmDialogInternal() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage("Bạn có chắc chắn muốn đặt đơn hàng này không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> placeOrderInternal())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /** Gửi yêu cầu đặt hàng */
    private void placeOrderInternal() {
        if (isPlacingOrder) return;
        isPlacingOrder = true;
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");

        String billingAddress = tvReceiverAddress.getText().toString();

        // Extract IDs from selectedProductList
        List<Long> itemIds = new ArrayList<>();
        if (selectedProductList != null && !selectedProductList.isEmpty()) {
            for (CartItem item : selectedProductList) {
                if (item.getCartItemID() != null) {
                    itemIds.add(item.getCartItemID());
                }
            }
        }

        PlaceOrderRequest orderRequest = new PlaceOrderRequest(
                userId,
                currentCartId,
                selectedPaymentMethod,
                billingAddress,
                itemIds,
                totalPrice
        );

        android.util.Log.d("Checkout", "Unified Order Request - UserID: " + userId);
        android.util.Log.d("Checkout", "Unified Order Request - CartID: " + currentCartId);
        android.util.Log.d("Checkout", "Unified Order Request - PaymentMethod: " + selectedPaymentMethod);
        android.util.Log.d("Checkout", "Unified Order Request - BillingAddress: " + billingAddress);
        android.util.Log.d("Checkout", "Unified Order Request - SelectedItemIds: " + itemIds.toString());
        android.util.Log.d("Checkout", "Unified Order Request - TotalAmount: " + totalPrice);

        orderService.placeNewOrder(orderRequest).enqueue(new Callback<PlaceOrderResponse>() {
            @Override
            public void onResponse(Call<PlaceOrderResponse> call, Response<PlaceOrderResponse> response) {
                isPlacingOrder = false;
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Đặt hàng");

                if (response.isSuccessful() && response.body() != null) {
                    PlaceOrderResponse orderResponse = response.body();

                    if (orderResponse.getCode() == 9999 && orderResponse.getResult() != null) {
                        Order order = orderResponse.getResult();
                        clearSelectedItems(); // Xóa các mục đã chọn cho cả hai phương thức
                        
                        // Tạo notification đơn hàng đã xác nhận
                        createOrderNotification(order);

                        if ("VNPay".equals(selectedPaymentMethod)) {
                            // Xử lý VNPay
                            Intent intent = new Intent(ActivityCheckout.this, VNPayActivity.class);
                            intent.putExtra("orderId", order.getOrderID());
                            intent.putExtra("amount", totalPrice); // Sử dụng tổng giá tiền đã có
                            startActivity(intent);
                            finish(); // Kết thúc activity checkout
                        } else {
                            // Xử lý COD
                            Toast.makeText(ActivityCheckout.this,
                                    "Đặt hàng thành công! Mã đơn hàng: " + order.getOrderID(),
                                    Toast.LENGTH_SHORT).show();

                            Intent successIntent = new Intent(ActivityCheckout.this, ActivityOrderSuccess.class);
                            successIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(successIntent);
                            finish();
                        }
                    } else {
                        // Xử lý lỗi từ API (ví dụ: code != 9999)
                        Toast.makeText(ActivityCheckout.this,
                                "Đặt hàng thất bại: " + orderResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Xử lý lỗi HTTP
                    Toast.makeText(ActivityCheckout.this,
                            "Đặt hàng thất bại. (HTTP " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PlaceOrderResponse> call, Throwable t) {
                isPlacingOrder = false;
                btnCheckout.setEnabled(true);
                btnCheckout.setText("Đặt hàng");
                Toast.makeText(ActivityCheckout.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Xóa các mục đã chọn sau khi đặt hàng thành công */
    private void clearSelectedItems() {
        sharedPreferences.edit()
                .remove(SELECTED_ITEMS_KEY)
                .apply();
    }
    
    /**
     * Tạo notification khi đơn hàng được xác nhận thành công
     * Notification sẽ hiển thị ở tab "Của bạn" trong trang Notifications
     */
    private void createOrderNotification(Order order) {
        if (order == null || order.getOrderID() <= 0) {
            android.util.Log.w("Checkout", "Cannot create notification - order is null or orderID is invalid");
            return;
        }
        
        if (userId == null) {
            android.util.Log.w("Checkout", "Cannot create notification - userId is null");
            return;
        }
        
        String orderId = String.valueOf(order.getOrderID());
        
        android.util.Log.d("Checkout", "Creating order notification - OrderID: " + orderId + ", UserID: " + userId);
        
        // Tạo notification qua API
        NotificationCreator creator = new NotificationCreator(this);
        creator.notifyOrderConfirmed(userId, orderId, new NotificationCreator.OnNotificationCreatedListener() {
            @Override
            public void onSuccess(NotificationDTO notification) {
                android.util.Log.d("Checkout", "✅ Order notification created successfully - ID: " + notification.getNotificationId());
                // Không cần show gì cho user - notification sẽ hiển thị trong NotificationsFragment
            }
            
            @Override
            public void onError(String error) {
                // Silent fail - không ảnh hưởng UX chính
                android.util.Log.e("Checkout", "Failed to create order notification: " + error);
            }
        });
    }
}
