package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapter.OrderItemAdapter;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.OrderService;
import com.example.myapplication.model.*;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.CartItemsResponse;
import com.example.myapplication.network.dto.UpdateStatusRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity hiển thị chi tiết đơn hàng
 * - Hiển thị thông tin đơn hàng: ngày đặt, trạng thái, địa chỉ, phương thức thanh toán, tổng tiền
 * - Hiển thị danh sách sản phẩm trong đơn hàng
 * - Cho phép hủy đơn hàng nếu trạng thái là "Processing"
 */
public class ActivityOrderDetail extends AppCompatActivity {

    // ========== UI COMPONENTS ==========
    private TextView tvOrderDate, tvOrderStatus, tvRecipientName, tvRecipientPhone, tvAddress, tvPaymentMethod, tvTotal;
    private RecyclerView rvOrderItems;
    private MaterialButton btnCancelOrder;

    // ========== NETWORK SERVICES ==========
    private AuthService authService;
    private OrderService orderService;
    
    // ========== DATA ==========
    private OrderItemAdapter adapter;
    private List<OrderItem> orderItems = new ArrayList<>();
    private int orderId;

    // ========== FORMATTERS ==========
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // ========== LIFECYCLE METHODS ==========

    /**
     * Khởi tạo Activity
     * - Lấy orderId từ Intent
     * - Khởi tạo views và services
     * - Tải dữ liệu đơn hàng
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Khởi tạo UI components
        initViews();

        // Khởi tạo network services
        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        orderService = ApiClient.getRetrofit(this).create(OrderService.class);

        // Lấy orderId từ Intent
        orderId = getIntent().getIntExtra("orderId", -1);
        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar navigation
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Setup nút hủy đơn hàng
        btnCancelOrder.setOnClickListener(v -> confirmCancelOrder());

        // Tải thông tin đơn hàng
        fetchOrderDetail(orderId);
    }

    // ========== INITIALIZATION METHODS ==========

    /**
     * Khởi tạo và ánh xạ tất cả UI components
     * - Ánh xạ các TextView, RecyclerView, Button
     * - Setup RecyclerView với adapter
     * - Ẩn nút hủy mặc định (chỉ hiển thị khi status = Processing)
     */
    private void initViews() {
        // Ánh xạ các view components
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvRecipientName = findViewById(R.id.tvRecipientName);
        tvRecipientPhone = findViewById(R.id.tvRecipientPhone);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotal = findViewById(R.id.tvTotal);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        // Setup RecyclerView cho danh sách sản phẩm
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(this, orderItems);
        rvOrderItems.setAdapter(adapter);
        
        // Ẩn nút hủy mặc định, chỉ hiển thị khi status là Processing
        btnCancelOrder.setVisibility(View.GONE);
    }

    // ========== NETWORK METHODS ==========

    /**
     * Gọi API để lấy thông tin chi tiết đơn hàng
     * - Hiển thị thông tin: ngày đặt, trạng thái, địa chỉ, phương thức thanh toán, tổng tiền
     * - Kiểm tra status để hiển thị/ẩn nút hủy
     * - Sau đó gọi fetchCartItems() để lấy danh sách sản phẩm
     * 
     * @param orderId ID của đơn hàng cần lấy thông tin
     */
    private void fetchOrderDetail(int orderId) {
        orderService.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<OrderDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderDetail>> call, Response<ApiResponse<OrderDetail>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    OrderDetail order = response.body().getResult();
                    
                    // Hiển thị thông tin đơn hàng lên UI
                    bindOrderDataToViews(order);
                    
                    // Kiểm tra và hiển thị/ẩn nút hủy dựa trên status
                    updateCancelButtonVisibility(order.getOrderStatus());
                    
                    // Gọi API để lấy danh sách sản phẩm trong đơn hàng
                    if (order.getCart() != null && order.getCart().getCartID() != null) {
                        fetchCartItems(order.getCart().getCartID());
                    }
                } else {
                    Toast.makeText(ActivityOrderDetail.this, "Không tải được dữ liệu đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<OrderDetail>> call, Throwable t) {
                Toast.makeText(ActivityOrderDetail.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Gọi API để lấy danh sách sản phẩm trong giỏ hàng của đơn hàng
     * - Chuyển đổi CartItem thành OrderItem để hiển thị
     * - Cập nhật RecyclerView adapter
     * 
     * @param cartId ID của giỏ hàng
     */
    private void fetchCartItems(Long cartId) {
        authService.getCartItems(cartId).enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    orderItems.clear();
                    // Chuyển đổi CartItem thành OrderItem
                    for (CartItem item : response.body().getResult()) {
                        orderItems.add(new OrderItem(
                                item.getProduct().getProductName(),
                                item.getProduct().getImageURL(),
                                item.getQuantity(),
                                item.getItemTotal()
                        ));
                    }
                    // Cập nhật UI
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                Toast.makeText(ActivityOrderDetail.this, "Lỗi khi tải sản phẩm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========== UI UPDATE METHODS ==========

    /**
     * Hiển thị thông tin đơn hàng lên các TextView
     * - Ngày đặt hàng (format từ ISO sang dd/MM/yyyy HH:mm)
     * - Trạng thái đơn hàng
     * - Địa chỉ giao hàng
     * - Phương thức thanh toán
     * - Tổng tiền (format currency VN)
     * 
     * @param order Đối tượng OrderDetail chứa thông tin đơn hàng
     */
    private void bindOrderDataToViews(OrderDetail order) {
        // Hiển thị ngày đặt hàng (parse và format)
        try {
            String isoDate = order.getOrderDate();
            Date date = isoFormat.parse(isoDate);
            tvOrderDate.setText("Ngày đặt: " + outputFormat.format(date));
        } catch (Exception e) {
            tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
        }
        
        // Hiển thị trạng thái đơn hàng - Chuyển sang tiếng Việt
        String statusVietnamese = getStatusInVietnamese(order.getOrderStatus());
        tvOrderStatus.setText("Trạng thái: " + statusVietnamese);

        // Hiển thị địa chỉ giao hàng (format: Tên | SĐT | Địa chỉ)
        String billingAddr = order.getBillingAddress();
        if (billingAddr != null && !billingAddr.trim().isEmpty()) {
            // Nếu địa chỉ có format "Tên | SĐT | Địa chỉ", parse và hiển thị riêng biệt
            if (billingAddr.contains(" | ")) {
                String[] parts = billingAddr.split(" \\| ", 3);
                if (parts.length >= 3) {
                    // Có đầy đủ: Tên, SĐT, Địa chỉ
                    tvRecipientName.setText(parts[0].trim());
                    tvRecipientPhone.setText(parts[1].trim());
                    tvAddress.setText(parts[2].trim());
                } else if (parts.length == 2) {
                    // Có 2 phần: có thể là Tên | SĐT hoặc Tên | Địa chỉ
                    // Kiểm tra phần thứ 2 có phải là số điện thoại không
                    String part2 = parts[1].trim();
                    if (part2.matches(".*\\d{10,11}.*") || part2.matches(".*0\\d{9}.*")) {
                        // Phần 2 là SĐT
                        tvRecipientName.setText(parts[0].trim());
                        tvRecipientPhone.setText(part2);
                        tvAddress.setText("Chưa có địa chỉ");
                    } else {
                        // Phần 2 là địa chỉ
                        tvRecipientName.setText(parts[0].trim());
                        tvRecipientPhone.setText("Chưa có SĐT");
                        tvAddress.setText(part2);
                    }
                } else {
                    // Chỉ có 1 phần: có thể là tên hoặc địa chỉ
                    tvRecipientName.setText(parts[0].trim());
                    tvRecipientPhone.setText("Chưa có SĐT");
                    tvAddress.setText("Chưa có địa chỉ");
                }
            } else {
                // Địa chỉ không có format đặc biệt, hiển thị toàn bộ ở địa chỉ
                tvRecipientName.setText("Người nhận");
                tvRecipientPhone.setText("Chưa có SĐT");
                tvAddress.setText(billingAddr);
            }
        } else if (order.getCart() != null && order.getCart().getUsers() != null) {
            // Fallback: lấy từ User profile nếu billingAddress rỗng
            tvRecipientName.setText(order.getCart().getUsers().getUsername() != null ? 
                    order.getCart().getUsers().getUsername() : "Người nhận");
            tvRecipientPhone.setText(order.getCart().getUsers().getPhoneNumber() != null ? 
                    order.getCart().getUsers().getPhoneNumber() : "Chưa có SĐT");
            tvAddress.setText(order.getCart().getUsers().getAddress() != null ? 
                    order.getCart().getUsers().getAddress() : "Chưa có địa chỉ");
        } else {
            // Không có thông tin
            tvRecipientName.setText("Người nhận");
            tvRecipientPhone.setText("Chưa có SĐT");
            tvAddress.setText("Không có địa chỉ giao hàng");
        }


        // Hiển thị phương thức thanh toán
        tvPaymentMethod.setText(order.getPaymentMethod());
        
        // Hiển thị tổng tiền (format currency VN)
        if (order.getCart() != null) {
            double totalPrice = order.getCart().getTotalPrice();
            tvTotal.setText("Tổng tiền: " + currencyFormat.format(totalPrice));
        } else {
            tvTotal.setText("Tổng tiền: N/A");
        }
    }

    /**
     * Cập nhật hiển thị/ẩn nút hủy đơn hàng dựa trên trạng thái
     * - Chỉ hiển thị nút khi status = "Processing" hoặc "Đang xử lý"
     * - Ẩn nút với các status khác (Shipped, Delivered, Cancelled...)
     * 
     * @param orderStatus Trạng thái hiện tại của đơn hàng
     */
    private void updateCancelButtonVisibility(String orderStatus) {
        if (orderStatus != null && isProcessingStatus(orderStatus)) {
            btnCancelOrder.setVisibility(View.VISIBLE);
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    // ========== USER INTERACTION HANDLERS ==========

    /**
     * Hiển thị dialog xác nhận trước khi hủy đơn hàng
     * - Nếu user chọn "Hủy đơn" → gọi cancelOrder()
     * - Nếu user chọn "Không" → đóng dialog
     */
    private void confirmCancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy")
                .setMessage("Bạn có chắc muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (d, w) -> cancelOrder())
                .setNegativeButton("Không", null)
                .show();
    }

    /**
     * Gọi API để cập nhật trạng thái đơn hàng thành "Cancelled"
     * - Không xóa đơn hàng, chỉ cập nhật status
     * - Sau khi hủy thành công:
     *   + Cập nhật UI: thay đổi text status, ẩn nút hủy
     *   + Gửi kết quả về ActivityOrderHistory để refresh danh sách
     *   + KHÔNG finish() để user vẫn có thể xem đơn đã hủy
     */
    private void cancelOrder() {
        UpdateStatusRequest request = new UpdateStatusRequest("Cancelled", orderId);
        // Đảm bảo cả hai field đều được set
        request.setOrderID(orderId);
        request.setOrderStatus("Cancelled");
        orderService.updateOrderStatus(orderId, request).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 9999) {
                    // Thông báo thành công
                    Toast.makeText(ActivityOrderDetail.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    
                    // Cập nhật UI - Chuyển sang tiếng Việt
                    tvOrderStatus.setText("Trạng thái: " + getStatusInVietnamese("Cancelled"));
                    btnCancelOrder.setVisibility(View.GONE);

                    // Gửi kết quả về ActivityOrderHistory để refresh danh sách
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("statusUpdated", true);
                    resultIntent.putExtra("updatedOrderId", orderId);
                    setResult(RESULT_OK, resultIntent);
                    // KHÔNG finish() để user vẫn có thể xem đơn đã hủy

                } else {
                    String errorMessage = "Không thể hủy đơn hàng";
                    Toast.makeText(ActivityOrderDetail.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Toast.makeText(ActivityOrderDetail.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========== HELPER METHODS ==========

    /**
     * Chuyển đổi status từ tiếng Anh sang tiếng Việt
     */
    private String getStatusInVietnamese(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Không xác định";
        }
        
        String statusLower = status.trim().toLowerCase();
        
        if (statusLower.contains("processing")) {
            return "Đang xử lý";
        } else if (statusLower.contains("shipped") || statusLower.contains("shipping")) {
            return "Đang giao";
        } else if (statusLower.contains("delivered")) {
            return "Đã giao";
        } else if (statusLower.contains("cancelled") || statusLower.contains("canceled")) {
            return "Đã hủy";
        } else if (statusLower.contains("đang xử")) {
            return "Đang xử lý";
        } else if (statusLower.contains("đang giao")) {
            return "Đang giao";
        } else if (statusLower.contains("đã giao")) {
            return "Đã giao";
        } else if (statusLower.contains("đã hủy") || statusLower.contains("da huy")) {
            return "Đã hủy";
        }
        
        // Nếu không match, trả về status gốc (có thể đã là tiếng Việt)
        return status;
    }

    /**
     * Kiểm tra xem status có phải là "Processing" hay không
     * - Hỗ trợ cả tiếng Anh ("processing") và tiếng Việt ("đang xử")
     * - Không phân biệt hoa thường
     * 
     * @param status Trạng thái cần kiểm tra
     * @return true nếu status là Processing, false nếu không
     */
    private boolean isProcessingStatus(String status) {
        if (status == null) return false;
        String s = status.trim().toLowerCase();
        return s.equals("processing") || s.contains("đang xử");
    }

}