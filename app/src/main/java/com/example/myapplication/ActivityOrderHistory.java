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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        orderService = ApiClient.getRetrofit(this).create(OrderService.class);
        initViews();
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

        orderService.getOrdersByUserId(String.valueOf(userId))
                .enqueue(new Callback<ApiResponse<List<Order>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                        swipeRefreshLayout.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            List<Order> orders = response.body().getResult();

                            if (orders != null && !orders.isEmpty()) {
                                emptyLayout.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                orderAdapter.setOrderList(orders);
                            } else {
                                emptyLayout.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        } else {
                            showError("Không thể tải đơn hàng. Mã lỗi: " + response.code());
                            emptyLayout.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                        swipeRefreshLayout.setRefreshing(false);
                        showError("Lỗi kết nối: " + t.getMessage());
                        emptyLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }
                });
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
