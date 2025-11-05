package com.example.myapplication;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.LinearLayout;

import com.example.myapplication.adapter.OrderAdminListAdapter;
import com.example.myapplication.model.Order;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.OrderService;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.UpdateStatusRequest;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderAdminListFragment extends Fragment {

    private RecyclerView recyclerOrders;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutEmpty;
    private TextInputEditText etSearch;
    private OrderAdminListAdapter adapter;
    private List<Order> orderList; // Danh sách đầy đủ từ API
    private List<Order> filteredOrderList; // Danh sách sau khi filter (status + search)
    private ChipGroup chipGroupStatus;
    private OrderService orderService;
    private static final String TAG = "OrderListFragment";
    private static final String FILTER_ALL = "ALL"; // Constant để đánh dấu "Tất cả"
    private String currentFilterStatus = FILTER_ALL; // FILTER_ALL = fetch tất cả
    private String searchQuery = ""; // Query tìm kiếm

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate layout cho Fragment (layout bạn cung cấp ở trên)
        return inflater.inflate(R.layout.fragment_order_admin_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerOrders = view.findViewById(R.id.recyclerOrders);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
        etSearch = view.findViewById(R.id.etSearch);
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus);

        orderService = ApiClient.getRetrofit(requireContext()).create(OrderService.class);
        orderList = new ArrayList<>();
        filteredOrderList = new ArrayList<>();

        adapter = new OrderAdminListAdapter(requireContext(), filteredOrderList, new OrderAdminListAdapter.OnOrderActionListener() {
            @Override
            public void onConfirmClick(Order order) {
                // Xác nhận đơn hàng - chuyển từ Processing sang Shipped
                updateOrderStatusToShipped(order);
            }

            @Override
            public void onCancelClick(Order order) {
                // Hủy đơn hàng - chuyển sang Cancelled
                updateOrderStatusToCancelled(order);
            }
        });

        recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerOrders.setAdapter(adapter);

        // Refresh để tải lại dữ liệu
        swipeRefreshLayout.setOnRefreshListener(() -> {
            fetchOrders();
        });

        // 🔍 Setup Search Bar - Tìm kiếm theo mã đơn hàng
        setupSearchBar();

        // Bộ lọc Chip - Set listener TRƯỚC khi set checked
        chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            // Reset tất cả chips về trạng thái unselected trước
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                if (child instanceof Chip) {
                    Chip c = (Chip) child;
                    // Reset background về white và text về primary color khi uncheck
                    c.setChipBackgroundColorResource(android.R.color.white);
                    c.setTextColor(requireContext().getColor(R.color.text_primary));
                    c.setChecked(c.getId() == checkedId);
                }
            }
            
            Chip chip = view.findViewById(checkedId);
            if (chip != null) {
                // Set màu background theo màu stroke của chip khi được chọn
                applyChipSelectedColor(chip);
                
                String chipText = chip.getText().toString();
                String apiStatus = mapChipToStatus(chipText);
                currentFilterStatus = apiStatus;
                Log.d(TAG, "Chip selected: " + chipText + " -> API Status: " + apiStatus);
                // Chỉ fetch từ API nếu có data mới, còn lại filter local
                if (orderList.isEmpty()) {
                    fetchOrders();
                } else {
                    filterOrders(); // Filter local data theo status + search
                }
            }
        });

        // Set default: "Tất cả" - Set sau khi listener đã được set
        Chip chipAll = view.findViewById(R.id.chipAll);
        if (chipAll != null) {
            chipAll.setChecked(true);
            // Đảm bảo currentFilterStatus = FILTER_ALL khi chip "Tất cả" được checked
            currentFilterStatus = FILTER_ALL;
            // Áp dụng màu cho chip được chọn mặc định
            applyChipSelectedColor(chipAll);
        }

        // Load orders ban đầu
        fetchOrders();
    }

    /**
     * Áp dụng màu background cho chip khi được chọn
     * Màu background sẽ giống màu stroke của chip đó
     */
    private void applyChipSelectedColor(Chip chip) {
        if (chip == null) return;
        
        int chipId = chip.getId();
        
        if (chipId == R.id.chipAll) {
            // Màu brand_primary (blue) - #2563EB
            chip.setChipBackgroundColorResource(R.color.brand_primary);
            chip.setTextColor(0xFFFFFFFF); // Text màu trắng
        } else if (chipId == R.id.chipProcessing) {
            // Màu #FFA726 (orange) - giống stroke color
            chip.setChipBackgroundColor(ColorStateList.valueOf(0xFFFFA726));
            chip.setTextColor(0xFFFFFFFF); // Text màu trắng
        } else if (chipId == R.id.chipShipped) {
            // Màu #42A5F5 (blue) - giống stroke color
            chip.setChipBackgroundColor(ColorStateList.valueOf(0xFF42A5F5));
            chip.setTextColor(0xFFFFFFFF); // Text màu trắng
        } else if (chipId == R.id.chipDelivered) {
            // Màu #66BB6A (green) - giống stroke color
            chip.setChipBackgroundColor(ColorStateList.valueOf(0xFF66BB6A));
            chip.setTextColor(0xFFFFFFFF); // Text màu trắng
        } else if (chipId == R.id.chipCancelled) {
            // Màu #EF5350 (red) - giống stroke color
            chip.setChipBackgroundColor(ColorStateList.valueOf(0xFFEF5350));
            chip.setTextColor(0xFFFFFFFF); // Text màu trắng
        }
    }

    /**
     * Map chip text sang status API
     * Trả về FILTER_ALL nếu là "Tất cả"
     */
    private String mapChipToStatus(String chipText) {
        if (chipText == null) return FILTER_ALL;
        chipText = chipText.trim();
        
        if (chipText.equals("Tất cả")) {
            return FILTER_ALL; // FILTER_ALL = fetch tất cả
        } else if (chipText.equals("Đang xử lý")) {
            return "Processing";
        } else if (chipText.equals("Đang giao")) {
            return "Shipped";
        } else if (chipText.equals("Đã giao")) {
            return "Delivered";
        } else if (chipText.equals("Đã hủy")) {
            return "Cancelled";
        }
        return FILTER_ALL;
    }

    /**
     * Setup search bar để tìm kiếm theo mã đơn hàng
     */
    private void setupSearchBar() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().trim();
                filterOrders();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý
            }
        });
    }

    /**
     * Filter orders dựa trên status filter và search query
     * Sắp xếp đơn hàng mới nhất (orderID lớn nhất) lên đầu
     */
    private void filterOrders() {
        filteredOrderList.clear();

        // Nếu không có search query và đang chọn "Tất cả", hiển thị tất cả
        if (searchQuery.isEmpty() && currentFilterStatus.equals(FILTER_ALL)) {
            filteredOrderList.addAll(orderList);
        } else {
            for (Order order : orderList) {
                // Filter theo status
                boolean matchesStatus = false;
                if (currentFilterStatus.equals(FILTER_ALL)) {
                    matchesStatus = true;
                } else if (order.getOrderStatus() != null) {
                    matchesStatus = order.getOrderStatus().equalsIgnoreCase(currentFilterStatus);
                }

                if (!matchesStatus) {
                    continue;
                }

                // Filter theo search query (mã đơn hàng)
                if (searchQuery.isEmpty()) {
                    filteredOrderList.add(order);
                } else {
                    // Tìm kiếm theo OrderID
                    String orderIDStr = String.valueOf(order.getOrderID());
                    // Tìm kiếm theo CartID (nếu có)
                    String cartIDStr = "";
                    if (order.getCart() != null && order.getCart().getCartID() != null) {
                        cartIDStr = String.valueOf(order.getCart().getCartID());
                    }
                    // Tìm kiếm theo mã đơn hàng (format: #123456)
                    String orderCode = "#" + (!cartIDStr.isEmpty() ? cartIDStr : orderIDStr);

                    // Kiểm tra nếu query match với OrderID, CartID, hoặc OrderCode
                    if (orderIDStr.toLowerCase(Locale.getDefault()).contains(searchQuery.toLowerCase(Locale.getDefault())) ||
                        cartIDStr.toLowerCase(Locale.getDefault()).contains(searchQuery.toLowerCase(Locale.getDefault())) ||
                        orderCode.toLowerCase(Locale.getDefault()).contains(searchQuery.toLowerCase(Locale.getDefault()))) {
                        filteredOrderList.add(order);
                    }
                }
            }
        }

        // ⭐ Sắp xếp đơn hàng MỚI NHẤT lên đầu (orderID giảm dần)
        java.util.Collections.sort(filteredOrderList, new java.util.Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                // Sort theo orderID giảm dần (mới nhất lên đầu)
                return Integer.compare(o2.getOrderID(), o1.getOrderID());
            }
        });

        updateAdapter();
        updateEmptyState();
    }

    /**
     * Fetch orders từ API
     * - Nếu currentFilterStatus == FILTER_ALL → gọi getAllOrders() (tất cả)
     * - Nếu currentFilterStatus != FILTER_ALL → gọi getOrdersByStatus(status)
     */
    private void fetchOrders() {
        swipeRefreshLayout.setRefreshing(true);

        if (currentFilterStatus != null && !currentFilterStatus.equals(FILTER_ALL)) {
            // Fetch theo status cụ thể
            Log.d(TAG, "Fetching orders by status: " + currentFilterStatus);
            orderService.getOrdersByStatus(currentFilterStatus).enqueue(new Callback<ApiResponse<List<Order>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                    swipeRefreshLayout.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<Order>> apiResponse = response.body();
                        Log.d(TAG, "Response code: " + apiResponse.getCode());
                        if (apiResponse.getCode() == 9999) {
                            orderList = apiResponse.getResult();
                            if (orderList == null) orderList = new ArrayList<>();
                            Log.d(TAG, "Loaded " + orderList.size() + " orders by status");
                            filterOrders(); // Apply filters (status + search)
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể tải đơn hàng";
                            Log.e(TAG, "API Error: " + errorMsg);
                            showError(errorMsg);
                            showEmpty();
                        }
                    } else {
                        Log.e(TAG, "Response not successful. Code: " + response.code());
                        showError("Không thể tải đơn hàng (HTTP " + response.code() + ")");
                        showEmpty();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, "Network error: " + t.getMessage(), t);
                    showError("Lỗi mạng: " + t.getMessage());
                    showEmpty();
                }
            });
        } else {
            // Fetch TẤT CẢ đơn hàng (khi click "Tất cả" hoặc lần đầu vào)
            Log.d(TAG, "Fetching ALL orders");
            orderService.getAllOrders().enqueue(new Callback<ApiResponse<List<Order>>>() {
                @Override
                public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                    swipeRefreshLayout.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<Order>> apiResponse = response.body();
                        Log.d(TAG, "Response code: " + apiResponse.getCode());
                        if (apiResponse.getCode() == 9999) {
                            orderList = apiResponse.getResult();
                            if (orderList == null) orderList = new ArrayList<>();
                            Log.d(TAG, "Loaded " + orderList.size() + " orders (ALL)");
                            filterOrders(); // Apply filters (status + search)
                        } else {
                            String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể tải đơn hàng";
                            Log.e(TAG, "API Error: " + errorMsg);
                            showError(errorMsg);
                            showEmpty();
                        }
                    } else {
                        Log.e(TAG, "Response not successful. Code: " + response.code());
                        if (response.errorBody() != null) {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                            } catch (Exception e) {
                                Log.e(TAG, "Cannot read error body", e);
                            }
                        }
                        showError("Không thể tải đơn hàng (HTTP " + response.code() + ")");
                        showEmpty();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                    swipeRefreshLayout.setRefreshing(false);
                    Log.e(TAG, "Network error: " + t.getMessage(), t);
                    showError("Lỗi mạng: " + t.getMessage());
                    showEmpty();
                }
            });
        }
    }

    private void updateAdapter() {
        adapter.setOrderList(filteredOrderList);
    }

    private void updateEmptyState() {
        if (filteredOrderList.isEmpty()) {
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.VISIBLE);
            }
            if (recyclerOrders != null) {
                recyclerOrders.setVisibility(View.GONE);
            }
        } else {
            if (layoutEmpty != null) {
                layoutEmpty.setVisibility(View.GONE);
            }
            if (recyclerOrders != null) {
                recyclerOrders.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showEmpty() {
        orderList.clear();
        filteredOrderList.clear();
        adapter.setOrderList(filteredOrderList);
        updateEmptyState();
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Cập nhật trạng thái đơn hàng sang "Shipped" khi admin xác nhận
     */
    private void updateOrderStatusToShipped(Order order) {
        if (order == null || order.getOrderID() <= 0) {
            showError("Đơn hàng không hợp lệ");
            return;
        }

        // Tạo request để update status sang "Shipped"
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setOrderStatus("Shipped");
        request.setOrderID(order.getOrderID());

        Log.d(TAG, "Updating order #" + order.getOrderID() + " to Shipped");

        orderService.updateOrderStatus(order.getOrderID(), request).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Order> apiResponse = response.body();
                    if (apiResponse.getCode() == 9999) {
                        Log.d(TAG, "Order status updated successfully");
                        Toast.makeText(requireContext(), "Đã xác nhận đơn hàng #" + order.getOrderID(), Toast.LENGTH_SHORT).show();
                        // Refresh danh sách đơn hàng
                        fetchOrders();
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể cập nhật trạng thái";
                        Log.e(TAG, "API Error: " + errorMsg);
                        showError(errorMsg);
                    }
                } else {
                    Log.e(TAG, "Response not successful. Code: " + response.code());
                    showError("Không thể cập nhật trạng thái (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    /**
     * Cập nhật trạng thái đơn hàng sang "Cancelled" khi admin hủy
     */
    private void updateOrderStatusToCancelled(Order order) {
        if (order == null || order.getOrderID() <= 0) {
            showError("Đơn hàng không hợp lệ");
            return;
        }

        // Tạo request để update status sang "Cancelled"
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setOrderStatus("Cancelled");
        request.setOrderID(order.getOrderID());

        Log.d(TAG, "Updating order #" + order.getOrderID() + " to Cancelled");

        orderService.updateOrderStatus(order.getOrderID(), request).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Order> apiResponse = response.body();
                    if (apiResponse.getCode() == 9999) {
                        Log.d(TAG, "Order status updated successfully");
                        Toast.makeText(requireContext(), "Đã hủy đơn hàng #" + order.getOrderID(), Toast.LENGTH_SHORT).show();
                        // Refresh danh sách đơn hàng
                        fetchOrders();
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể cập nhật trạng thái";
                        Log.e(TAG, "API Error: " + errorMsg);
                        showError(errorMsg);
                    }
                } else {
                    Log.e(TAG, "Response not successful. Code: " + response.code());
                    showError("Không thể cập nhật trạng thái (HTTP " + response.code() + ")");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}

