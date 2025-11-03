package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.model.Order;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.OrderService;
import com.example.myapplication.network.dto.ApiResponse;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment hiển thị dashboard quản lý cho admin
 * - Thống kê doanh thu
 * - Số đơn đang xử lý và đơn mới
 * - Biểu đồ doanh thu
 * - Đánh giá trung bình
 */
public class DashboardFragment extends Fragment {

    // ========== VIEW COMPONENTS ==========
    private TextView tvRevenue;            // Revenue
    private TextView tvProcessedOrders;    // Processed orders
    private TextView tvNewOrders;          // New orders
    private TextView tvRating;             // Rating
    private TextView tvTotalReviews;        // Total reviews
    private LineChart chartRevenue;        // Revenue chart

    // ========== NETWORK SERVICES ==========
    private OrderService orderService;
    
    // ========== CONSTANTS ==========
    private static final String TAG = "DashboardFragment";
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Initialize services
        orderService = ApiClient.getRetrofit(requireContext()).create(OrderService.class);

        // Initialize views
        initViews(root);

        // Load dashboard statistics from API
        loadDashboardStatistics();

        return root;
    }

    /**
     * Initialize view components
     */
    private void initViews(View root) {
        // Revenue
        tvRevenue = root.findViewById(R.id.tv_revenue_value);
        
        // Order statistics
        tvProcessedOrders = root.findViewById(R.id.tv_running_orders_value);
        tvNewOrders = root.findViewById(R.id.tv_order_request_value);
        
        // Reviews
        tvRating = root.findViewById(R.id.tvRating);
        tvTotalReviews = root.findViewById(R.id.tvTotalReviews);
        
        // Chart
        chartRevenue = root.findViewById(R.id.chartRevenue);
        setupChart();
    }

    /**
     * Load dashboard statistics from API
     * - Call getAllOrders() to get all orders
     * - Calculate: revenue (from Delivered orders), processed orders, new orders
     */
    private void loadDashboardStatistics() {
        Log.d(TAG, "Loading dashboard statistics from API...");
        
        orderService.getAllOrders().enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Order>> apiResponse = response.body();
                    if (apiResponse.getCode() == 9999) {
                        List<Order> allOrders = apiResponse.getResult();
                        if (allOrders != null) {
                            Log.d(TAG, "Loaded " + allOrders.size() + " orders");
                            calculateAndDisplayStatistics(allOrders);
                            updateChart(allOrders);
                        } else {
                            Log.e(TAG, "Orders list is null");
                            showDefaultValues();
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Không thể tải dữ liệu";
                        Log.e(TAG, "API Error: " + errorMsg);
                        showError(errorMsg);
                        showDefaultValues();
                    }
                } else {
                    Log.e(TAG, "Response not successful. Code: " + response.code());
                    showError("Không thể tải dữ liệu (HTTP " + response.code() + ")");
                    showDefaultValues();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Lỗi mạng: " + t.getMessage());
                showDefaultValues();
            }
        });
    }

    /**
     * Calculate and display statistics from orders list
     */
    private void calculateAndDisplayStatistics(List<Order> allOrders) {
        double totalRevenue = 0.0;
        int processedOrders = 0;      // Delivered orders
        int newOrders = 0;            // Processing orders

        for (Order order : allOrders) {
            String status = order.getOrderStatus();
            if (status == null) continue;

            String statusLower = status.trim().toLowerCase();

            // Calculate revenue: only count Delivered orders
            if (statusLower.contains("delivered") || statusLower.contains("đã giao")) {
                if (order.getCart() != null) {
                    totalRevenue += order.getCart().getTotalPrice();
                }
                processedOrders++;
            }

            // Count new orders: Processing orders
            if (statusLower.contains("processing") || statusLower.contains("đang xử")) {
                newOrders++;
            }
        }

        // Display revenue
        if (tvRevenue != null) {
            tvRevenue.setText(currencyFormat.format(totalRevenue));
        }

        // Display processed orders
        if (tvProcessedOrders != null) {
            tvProcessedOrders.setText(String.valueOf(processedOrders));
        }

        // Display new orders
        if (tvNewOrders != null) {
            tvNewOrders.setText(String.format("%02d", newOrders));
        }

        // Reviews: temporarily keep mock data (need reviews API)
        if (tvRating != null) {
            tvRating.setText("4.9");
        }
        
        if (tvTotalReviews != null) {
            tvTotalReviews.setText("Tổng 20 đánh giá");
        }

        Log.d(TAG, String.format("Statistics: Revenue=%.0f, Processed=%d, New=%d", 
            totalRevenue, processedOrders, newOrders));
    }

    /**
     * Display default values when error occurs
     */
    private void showDefaultValues() {
        if (tvRevenue != null) {
            tvRevenue.setText("₫0");
        }
        if (tvProcessedOrders != null) {
            tvProcessedOrders.setText("0");
        }
        if (tvNewOrders != null) {
            tvNewOrders.setText("00");
        }
        if (tvRating != null) {
            tvRating.setText("0.0");
        }
        if (tvTotalReviews != null) {
            tvTotalReviews.setText("Tổng 0 đánh giá");
        }
    }

    /**
     * Display error message to user
     */
    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Setup chart appearance and configuration
     */
    private void setupChart() {
        if (chartRevenue == null) return;

        // Disable description
        chartRevenue.getDescription().setEnabled(false);

        // Enable touch gestures
        chartRevenue.setTouchEnabled(true);
        chartRevenue.setDragEnabled(true);
        chartRevenue.setScaleEnabled(true);
        chartRevenue.setPinchZoom(false);

        // Configure X axis
        XAxis xAxis = chartRevenue.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setTextColor(0xFF6B7280);
        xAxis.setTextSize(10f);
        xAxis.setAxisLineColor(0xFFE5E7EB);
        xAxis.setGridColor(0xFFE5E7EB);
        xAxis.setDrawGridLines(true);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, (int) value - 6);
                return new SimpleDateFormat("dd/MM", Locale.getDefault()).format(cal.getTime());
            }
        });

        // Configure Y axis (left)
        YAxis leftAxis = chartRevenue.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(0xFF6B7280);
        leftAxis.setTextSize(10f);
        leftAxis.setAxisLineColor(0xFFE5E7EB);
        leftAxis.setGridColor(0xFFE5E7EB);
        leftAxis.setDrawGridLines(true);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value >= 1000000) {
                    return String.format(Locale.getDefault(), "%.1fM", value / 1000000);
                } else if (value >= 1000) {
                    return String.format(Locale.getDefault(), "%.0fK", value / 1000);
                }
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });

        // Disable right Y axis
        YAxis rightAxis = chartRevenue.getAxisRight();
        rightAxis.setEnabled(false);

        // Disable legend
        chartRevenue.getLegend().setEnabled(false);

        // Set background
        chartRevenue.setBackgroundColor(0xFFF9FAFB);
        chartRevenue.setNoDataText("Đang tải dữ liệu...");
        chartRevenue.setNoDataTextColor(0xFF6B7280);
    }

    /**
     * Update chart with revenue data from orders
     * Shows revenue for the last 7 days
     */
    private void updateChart(List<Order> allOrders) {
        if (chartRevenue == null) return;

        try {
            // Calculate revenue by date for last 7 days
            Calendar cal = Calendar.getInstance();
            Map<String, Double> dailyRevenue = new HashMap<>();
            
            // Initialize map with last 7 days (0 revenue)
            SimpleDateFormat dateKeyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (int i = 6; i >= 0; i--) {
                Calendar dayCal = Calendar.getInstance();
                dayCal.add(Calendar.DAY_OF_MONTH, -i);
                String dateKey = dateKeyFormat.format(dayCal.getTime());
                dailyRevenue.put(dateKey, 0.0);
            }

            // Parse order dates and calculate revenue
            SimpleDateFormat orderDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            
            // Try multiple date formats
            SimpleDateFormat[] dateFormats = {
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
                new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            };

            for (Order order : allOrders) {
                String status = order.getOrderStatus();
                if (status == null) continue;

                String statusLower = status.trim().toLowerCase();
                
                // Only count delivered orders for revenue
                if (statusLower.contains("delivered") || statusLower.contains("đã giao")) {
                    if (order.getCart() == null) continue;
                    
                    double revenue = order.getCart().getTotalPrice();
                    String orderDateStr = order.getOrderDate();
                    
                    if (orderDateStr != null && !orderDateStr.isEmpty()) {
                        Date orderDate = null;
                        for (SimpleDateFormat format : dateFormats) {
                            try {
                                orderDate = format.parse(orderDateStr);
                                break;
                            } catch (ParseException e) {
                                // Try next format
                            }
                        }
                        
                        if (orderDate != null) {
                            String dateKey = dateKeyFormat.format(orderDate);
                            // Only count if within last 7 days
                            if (dailyRevenue.containsKey(dateKey)) {
                                dailyRevenue.put(dateKey, dailyRevenue.get(dateKey) + revenue);
                            }
                        }
                    }
                }
            }

            // Create chart entries
            List<Entry> entries = new ArrayList<>();
            cal = Calendar.getInstance();
            for (int i = 6; i >= 0; i--) {
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_MONTH, -i);
                String dateKey = dateKeyFormat.format(cal.getTime());
                double revenue = dailyRevenue.getOrDefault(dateKey, 0.0);
                entries.add(new Entry(6 - i, (float) revenue));
            }

            // Create dataset
            LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
            dataSet.setColor(0xFF10B981); // cosmetic_primary green
            dataSet.setLineWidth(2.5f);
            dataSet.setCircleColor(0xFF10B981);
            dataSet.setCircleRadius(4f);
            dataSet.setCircleHoleRadius(2f);
            dataSet.setDrawCircleHole(true);
            dataSet.setDrawValues(false);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            dataSet.setCubicIntensity(0.2f);
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(0xFF10B981);
            dataSet.setFillAlpha(60);

            // Create line data and set to chart
            LineData lineData = new LineData(dataSet);
            chartRevenue.setData(lineData);
            chartRevenue.invalidate();

            Log.d(TAG, "Chart updated with " + entries.size() + " data points");
        } catch (Exception e) {
            Log.e(TAG, "Error updating chart: " + e.getMessage(), e);
        }
    }
}
