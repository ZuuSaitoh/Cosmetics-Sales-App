package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.adapter.OrderHistoryAdapter;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Order;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.OrderService;
import com.example.myapplication.network.dto.ApiResponse;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityOrderHistory extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private LinearLayout emptyLayout;

    private OrderHistoryAdapter orderAdapter;
    private List<Order> orderList;
    private OrderService orderService;
    private String filterStatus; // e.g., Processing, Shipped, Delivered, Cancelled
    private String filterStatusName; // localized name for toolbar

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        orderService = ApiClient.getRetrofit(this).create(OrderService.class);
        initViews();
        // Read optional status filter from intent
        Intent intent = getIntent();
        if (intent != null) {
            filterStatus = intent.getStringExtra("status");
            filterStatusName = intent.getStringExtra("status_name");
        }

        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        fetchOrders();
    }

    private void initViews(){
        toolbar = findViewById(R.id.toolbarOrderHistory);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshOrders);
        recyclerView = findViewById(R.id.recyclerOrders);
        emptyLayout = findViewById(R.id.emptyLayout);
    }
    private void setupToolbar() {
        if (filterStatusName != null && !filterStatusName.isEmpty()) {
            toolbar.setTitle(filterStatusName);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        orderAdapter = new OrderHistoryAdapter(this, orderList, order -> {
            Intent intent = new Intent(ActivityOrderHistory.this, ActivityOrderDetail.class);
            intent.putExtra("orderId", order.getOrderID());
            startActivityForResult(intent, 100);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(orderAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchOrders);
    }

    private void fetchOrders() {
        swipeRefreshLayout.setRefreshing(true);
        AuthManager authManager = new AuthManager(this);
        Long userId = authManager.getUserId();

        if (userId == null) {
            swipeRefreshLayout.setRefreshing(false);
            showError("Không thể xác định người dùng. Vui lòng đăng nhập lại.");
            return;
        }
        // Luôn tải theo user để đảm bảo đồng nhất, sau đó lọc client-side theo status nếu có
        orderService.getOrdersByUserId(userId)
                .enqueue(new Callback<ApiResponse<List<Order>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Order> orders = response.body().getResult();
                            if (filterStatus != null && !filterStatus.isEmpty()) {
                                List<Order> filtered = new ArrayList<>();
                                if (orders != null) {
                                    for (Order o : orders) {
                                        if (o != null && matchesStatus(o.getOrderStatus(), filterStatus)) {
                                            filtered.add(o);
                                        }
                                    }
                                }
                                updateList(filtered);
                            } else {
                                updateList(orders);
                            }
                        } else {
                            showError("Không thể tải đơn hàng. Mã lỗi: " + response.code());
                            showEmpty();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        showError("Lỗi kết nối: " + t.getMessage());
                        showEmpty();
                    }
                });
    }

    private boolean matchesStatus(String orderStatus, String filter) {
        if (orderStatus == null || filter == null) return false;
        String s = orderStatus.trim().toLowerCase();
        String f = filter.trim().toLowerCase();
        // Chấp nhận nhiều biến thể tiếng Anh/Việt, có/không dấu, viết sai thường gặp
        if (f.contains("process")) {
            return s.contains("process") || s.contains("đang xử");
        }
        if (f.contains("ship") || f.contains("giao")) {
            return s.contains("ship") || s.contains("giao");
        }
        if (f.contains("deliver") || f.contains("giao")) {
            return s.contains("deliver") || s.contains("đã giao");
        }
        if (f.contains("cancel")) {
            return s.contains("cancel") || s.contains("hủy") || s.contains("huy");
        }
        // Fallback: so sánh bằng nhau sau khi bỏ khoảng trắng thừa
        return s.equals(f);
    }

    private void updateList(List<Order> orders) {
        if (orders != null && !orders.isEmpty()) {
            emptyLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            orderAdapter.setOrderList(orders);
        } else {
            showEmpty();
        }
    }

    private void showEmpty() {
        emptyLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            int deletedOrderId = data.getIntExtra("deletedOrderId", -1);
            if (deletedOrderId != -1) {
                removeOrderById(deletedOrderId);
            }
        }
    }

    // Hàm xóa order khỏi danh sách
    private void removeOrderById(int orderId) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderID() == orderId) {
                orderList.remove(i);
                orderAdapter.notifyItemRemoved(i);
                if (orderList.isEmpty()) {
                    emptyLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                break;
            }
        }
    }

}
