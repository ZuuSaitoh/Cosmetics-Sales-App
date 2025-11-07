package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.myapplication.auth.AuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ActivityAdminMain extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    private MaterialButton logoutButton;
    private FloatingActionButton fabAddMenu;
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

        // Setup FloatingActionButton for Product/Category menu
        fabAddMenu = findViewById(R.id.fab_add_menu);
        if (fabAddMenu != null) {
            fabAddMenu.setOnClickListener(v -> showAddMenu());
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
                selected = new OrderAdminListFragment();
            } else if (id == R.id.nav_customers) {
                selected = new CustomerListFragment();
            } else if (id == R.id.nav_reports) {
                selected = new AdminNotificationFragment();
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
    
    private void showAddMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_menu, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        
        MaterialButton btnEditProduct = dialogView.findViewById(R.id.btn_edit_product);
        MaterialButton btnCategory = dialogView.findViewById(R.id.btn_category);
        MaterialButton btnChat = dialogView.findViewById(R.id.btn_chat);
        
        btnEditProduct.setOnClickListener(v -> {
            dialog.dismiss();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new ProductListFragment())
                    .commit();
        });
        
        btnCategory.setOnClickListener(v -> {
            dialog.dismiss();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new AdminCategoryFragment())
                    .commit();
        });
        
        btnChat.setOnClickListener(v -> {
            dialog.dismiss();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new ChatConversationListFragment())
                    .commit();
        });
        
        dialog.show();
    }
    
    private void handleLogout() {
        // Clear cart data trước khi logout
        authManager.clearCartOnLogout(this);
        
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
