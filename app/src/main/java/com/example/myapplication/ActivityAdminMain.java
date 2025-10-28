package com.example.myapplication;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ActivityAdminMain extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        // Mặc định load Dashboard
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new DashboardFragment())
                    .commit();
        }

        // Sử dụng if-else để tránh lỗi "constant expression required"
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                selected = new DashboardFragment();
            } else if (id == R.id.nav_orders) {
                selected = new OrderListFragment();
            } else if (id == R.id.nav_products) {
                selected = new ProductListFragment();
            } else if (id == R.id.nav_customers) {
                selected = new CustomerListFragment();
            } else if (id == R.id.nav_reports) {
                selected = new ReportFragment();
            }

            if (selected != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_host_fragment, selected)
                        .commit();
                return true;
            }
            return false;
        });
    }
}
