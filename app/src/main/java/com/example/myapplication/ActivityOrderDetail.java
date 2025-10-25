package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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

public class ActivityOrderDetail extends AppCompatActivity {

    private TextView tvOrderDate, tvOrderStatus, tvAddress, tvPaymentMethod, tvTotal;
    private RecyclerView rvOrderItems;
    private MaterialButton btnCancelOrder;


    private AuthService authService;
    private OrderService orderService;
    private OrderItemAdapter adapter;
    private List<OrderItem> orderItems = new ArrayList<>();

    private int orderId;
    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        initViews();

        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        orderService = ApiClient.getRetrofit(this).create(OrderService.class);

        orderId = getIntent().getIntExtra("orderId", -1);

        if (orderId == -1) {
            Toast.makeText(this, "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchOrderDetail(orderId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        btnCancelOrder.setOnClickListener(v -> confirmCancelOrder());
    }

    private void initViews() {
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotal = findViewById(R.id.tvTotal);
        rvOrderItems = findViewById(R.id.rvOrderItems);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);

        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderItemAdapter(this, orderItems);
        rvOrderItems.setAdapter(adapter);
    }

    private void fetchOrderDetail(int orderId) {

        orderService.getOrderDetail(orderId).enqueue(new Callback<ApiResponse<OrderDetail>>() {
            @Override
            public void onResponse(Call<ApiResponse<OrderDetail>> call, Response<ApiResponse<OrderDetail>> response) {

                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    OrderDetail order = response.body().getResult();

                    //  Hiển thị ngày đặt hàng
                    try {
                        String isoDate = order.getOrderDate();
                        Date date = isoFormat.parse(isoDate);
                        tvOrderDate.setText("Ngày đặt: " + outputFormat.format(date));
                    } catch (Exception e) {
                        tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
                    }
                    tvOrderStatus.setText("Trạng thái: " + order.getOrderStatus());
                    tvAddress.setText(order.getBillingAddress());
                    tvPaymentMethod.setText(order.getPaymentMethod());
                    //  Hiển thị tổng tiền
                    if (order.getCart() != null) {
                        double totalPrice = order.getCart().getTotalPrice();
                        tvTotal.setText("Tổng tiền: " + currencyFormat.format(totalPrice));
                    } else {
                        tvTotal.setText("Tổng tiền: N/A");
                    }

                    if (order.getOrderStatus().equalsIgnoreCase("Processing")) {
                        btnCancelOrder.setVisibility(View.VISIBLE);
                    }

                    // Gọi tiếp API để lấy danh sách sản phẩm
                    fetchCartItems(order.getCart().getCartID());
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


    private void fetchCartItems(Long cartId) {
        authService.getCartItems(cartId).enqueue(new Callback<CartItemsResponse>() {
            @Override
            public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderItems.clear();
                    for (CartItem item : response.body().getResult()) {
                        orderItems.add(new OrderItem(
                                item.getProduct().getProductName(),
                                item.getProduct().getImageURL(),
                                item.getQuantity(),
                                item.getItemTotal()
                        ));
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                Toast.makeText(ActivityOrderDetail.this, "Lỗi khi tải sản phẩm: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmCancelOrder() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận hủy")
                .setMessage("Bạn có chắc muốn hủy đơn hàng này không?")
                .setPositiveButton("Hủy đơn", (d, w) -> cancelOrder())
                .setNegativeButton("Không", null)
                .show();
    }

    private void cancelOrder() {
        orderService.cancelOrder(orderId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getCode() == 9999) {
                    Toast.makeText(ActivityOrderDetail.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    tvOrderStatus.setText("Trạng thái: Đã hủy");
                    btnCancelOrder.setVisibility(View.GONE);

                    // Trả kết quả về ActivityOrderHistory để xóa item
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("deletedOrderId", orderId);
                    setResult(RESULT_OK, resultIntent);
                    finish();

                } else {
                    Toast.makeText(ActivityOrderDetail.this, "Không thể hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ActivityOrderDetail.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
