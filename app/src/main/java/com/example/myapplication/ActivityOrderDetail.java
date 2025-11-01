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
    private TextView tvOrderDate, tvOrderStatus, tvAddress, tvPaymentMethod, tvTotal;
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
        
        // Hiển thị trạng thái đơn hàng
        tvOrderStatus.setText("Trạng thái: " + order.getOrderStatus());
        
        // Hiển thị địa chỉ giao hàng
        tvAddress.setText(order.getBillingAddress());
        
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
                    
                    // Cập nhật UI
                    tvOrderStatus.setText("Trạng thái: Cancelled");
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