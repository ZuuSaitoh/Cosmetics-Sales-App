package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
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
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.graphics.Canvas;

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
        setupSwipeToDelete();
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

    /**
     * Setup swipe-to-delete gesture cho RecyclerView
     * - Khi swipe sang trái đủ xa: hiện popup xác nhận xóa ngay (chỉ cho đơn hàng Cancelled)
     * - Không cần click vào icon bin
     */
    private void setupSwipeToDelete() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false; // Không cho phép drag
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // Khi swipe đủ xa, hiện popup xác nhận xóa (chỉ cho đơn Cancelled)
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < orderList.size()) {
                    Order order = orderList.get(position);
                    OrderHistoryAdapter.OrderViewHolder vh = 
                        (OrderHistoryAdapter.OrderViewHolder) viewHolder;
                    
                    // Chỉ cho phép xóa đơn hàng ở trạng thái Cancelled
                    if (isCancelledStatus(order.getOrderStatus())) {
                        confirmDeleteOrder(order, position, vh);
                    } else {
                        // Reset về vị trí ban đầu nếu không phải đơn Cancelled
                        vh.foregroundView.setTranslationX(0);
                        vh.deleteBackground.setVisibility(View.GONE);
                        orderAdapter.notifyItemChanged(position);
                    }
                }
            }
            
            @Override
            public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
                // Threshold 0.5 = swipe 50% width thì trigger onSwiped
                return 0.5f;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                     RecyclerView.ViewHolder viewHolder,
                                     float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    OrderHistoryAdapter.OrderViewHolder vh = 
                        (OrderHistoryAdapter.OrderViewHolder) viewHolder;
                    
                    // Di chuyển foreground view
                    View foregroundView = vh.foregroundView;
                    foregroundView.setTranslationX(dX);
                    
                    // Hiển thị delete background khi swipe sang trái
                    if (dX < 0) {
                        vh.deleteBackground.setVisibility(View.VISIBLE);
                        float deleteWidth = vh.deleteBackground.getWidth();
                        if (deleteWidth == 0) {
                            vh.deleteBackground.measure(
                                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                                View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.AT_MOST)
                            );
                            deleteWidth = vh.deleteBackground.getMeasuredWidth();
                        }
                        
                        // Tính toán alpha dựa trên khoảng cách swipe
                        float alpha = Math.min(1.0f, Math.abs(dX) / Math.max(deleteWidth, 100));
                        vh.deleteBackground.setAlpha(alpha);
                    } else {
                        vh.deleteBackground.setVisibility(View.GONE);
                    }
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                OrderHistoryAdapter.OrderViewHolder vh = 
                    (OrderHistoryAdapter.OrderViewHolder) viewHolder;
                
                // Reset về vị trí ban đầu
                vh.foregroundView.setTranslationX(0);
                vh.deleteBackground.setVisibility(View.GONE);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    /**
     * Hiển thị dialog xác nhận trước khi xóa đơn hàng
     * - Khi user swipe đủ xa, popup này sẽ hiện ngay
     * - Nếu chọn "Hủy": item sẽ tự động reset về vị trí ban đầu (đã được xử lý trong clearView)
     */
    private void confirmDeleteOrder(Order order, int position, OrderHistoryAdapter.OrderViewHolder vh) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa đơn hàng #" + 
                    (order.getCart() != null && order.getCart().getCartID() != null ? 
                     order.getCart().getCartID() : order.getOrderID()) + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteOrder(order, position))
                .setNegativeButton("Hủy", (dialog, which) -> {
                    // Item sẽ tự động reset về vị trí ban đầu (đã được xử lý trong clearView)
                    orderAdapter.notifyItemChanged(position);
                })
                .show();
    }

    /**
     * Gọi API để xóa đơn hàng
     */
    private void deleteOrder(Order order, int position) {
        orderService.deleteOrder(order.getOrderID()).enqueue(new retrofit2.Callback<com.example.myapplication.network.dto.ApiResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.myapplication.network.dto.ApiResponse> call,
                                    retrofit2.Response<com.example.myapplication.network.dto.ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && 
                    response.body().getCode() == 9999) {
                    Toast.makeText(ActivityOrderHistory.this, "Đã xóa đơn hàng", Toast.LENGTH_SHORT).show();
                    // Xóa khỏi danh sách
                    orderList.remove(position);
                    orderAdapter.notifyItemRemoved(position);
                    // Kiểm tra danh sách rỗng
                    if (orderList.isEmpty()) {
                        showEmpty();
                    }
                } else {
                    Toast.makeText(ActivityOrderHistory.this, "Không thể xóa đơn hàng", Toast.LENGTH_SHORT).show();
                    // Refresh item để reset về trạng thái ban đầu
                    orderAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.myapplication.network.dto.ApiResponse> call, Throwable t) {
                Toast.makeText(ActivityOrderHistory.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Refresh item để reset về trạng thái ban đầu
                orderAdapter.notifyItemChanged(position);
            }
        });
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
                            if (orders != null) {
                                Collections.sort(orders, (o1, o2) -> Long.compare(o2.getOrderID(), o1.getOrderID()));
                            }
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

    /**
     * Kiểm tra xem order status có phải là Cancelled không
     * Hỗ trợ cả tiếng Anh và tiếng Việt
     */
    private boolean isCancelledStatus(String orderStatus) {
        if (orderStatus == null) return false;
        String s = orderStatus.trim().toLowerCase();
        return s.contains("cancelled") || s.contains("canceled") || s.contains("hủy") || s.contains("huy");
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
            // Nếu status được cập nhật, refresh lại danh sách
            boolean statusUpdated = data.getBooleanExtra("statusUpdated", false);
            if (statusUpdated) {
                fetchOrders();
                return;
            }
            
            // Tương thích ngược: nếu có deletedOrderId thì xóa item
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
