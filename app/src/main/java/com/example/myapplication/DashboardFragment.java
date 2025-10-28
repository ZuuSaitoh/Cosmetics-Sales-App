package com.example.myapplication;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapter.TopProductAdapter;
import com.example.myapplication.model.ProductAdmin;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private Toolbar toolbar;
    private TextView tvRevenueValue, tvTotalOrdersValue, tvShippingValue;
    private RecyclerView rvTopProducts;
    private TopProductAdapter adapter;

    public DashboardFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        toolbar = root.findViewById(R.id.toolbar);
        tvRevenueValue = root.findViewById(R.id.tv_revenue_value);
        tvTotalOrdersValue = root.findViewById(R.id.tv_total_orders_value);
        tvShippingValue = root.findViewById(R.id.tv_shipping_value);
        rvTopProducts = root.findViewById(R.id.rv_top_products);

        toolbar.inflateMenu(R.menu.menu_dashboard);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                // TODO: handle profile click
                return true;
            }
            return false;
        });

        // Dữ liệu mô phỏng
        loadMockMetrics();
        List<ProductAdmin> topProducts = createMockProducts();

        rvTopProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TopProductAdapter(topProducts);
        rvTopProducts.setAdapter(adapter);

        return root;
    }

    private void loadMockMetrics() {
        tvRevenueValue.setText("₫125,400,000");
        tvTotalOrdersValue.setText("1,254");
        tvShippingValue.setText("78");
    }

    private List<ProductAdmin> createMockProducts() {
        List<ProductAdmin> list = new ArrayList<>();
        list.add(new ProductAdmin("Serum Dưỡng Da", "Skincare", 350, ""));
        list.add(new ProductAdmin("Kem Dưỡng Ẩm", "Skincare", 300, ""));
        list.add(new ProductAdmin("Son Hồng Pastel", "Makeup", 210, ""));
        list.add(new ProductAdmin("Nước Tẩy Trang", "Skincare", 180, ""));
        list.add(new ProductAdmin("Mặt Nạ Dưỡng Trắng", "Mask", 150, ""));
        return list;
    }
}
