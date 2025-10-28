package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ActivityAdminMain extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    private MaterialButton logoutButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Initialize AuthManager
        authManager = new AuthManager(this);

        // Find logout button
        logoutButton = findViewById(R.id.logoutButton);
        
        // Setup logout button click
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> handleLogout());
        }

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
    
    private void handleLogout() {
        // Clear auth data
        authManager.clear();
        
        // Show logout message
        Toast.makeText(this, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
        
        // Navigate to welcome screen
        Intent intent = new Intent(this, ActivityWelcome.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
