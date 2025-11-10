package com.example.myapplication;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.auth.AuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ActivityMain extends AppCompatActivity {

    private static final String PREFS_NOTIFICATION = "notification_permission_prefs";
    private static final String PREF_KEY_PREFIX = "notification_prompt_shown_user_";

    BottomNavigationView bottomNavigationView;
    private AuthManager authManager;
    private ActivityResultLauncher<String> notificationPermissionLauncher;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo AuthManager
        authManager = new AuthManager(this);

        registerPermissionLauncher();
        maybeShowNotificationPermissionDialog();

        // Kiểm tra trạng thái đăng nhập
        //checkAuthentication();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, new HomeFragment()).commit();

        replaceFragment(new HomeFragment());

        bottomNavigationView.setBackground(null);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    replaceFragment(new HomeFragment());
                } else if (itemId == R.id.chat) {
                    replaceFragment(new ChatFragment());
                } else if (itemId == R.id.category) {
                    replaceFragment(new CategoryFragment());
                } else if (itemId == R.id.notification) {
                    replaceFragment(new NotificationsFragment());
                } else if (itemId == R.id.account) {
                    replaceFragment(new AccountFragment());
                }
                return true;
            }
        });
    }
    //outslide oncreate
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void registerPermissionLauncher() {
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(this, "Bạn đã bật thông báo thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bạn có thể bật lại thông báo trong cài đặt hệ thống.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void maybeShowNotificationPermissionDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Long userId = authManager.getUserId();
        String key = PREF_KEY_PREFIX + (userId != null ? userId : "guest");
        SharedPreferences prefs = getSharedPreferences(PREFS_NOTIFICATION, MODE_PRIVATE);

        if (prefs.getBoolean(key, false)) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Cho phép thông báo")
                .setMessage("Ứng dụng cần quyền thông báo để cập nhật giỏ hàng, đơn hàng và các khuyến mãi mới. Bạn có muốn bật thông báo ngay bây giờ không?")
                .setCancelable(false)
                .setPositiveButton("Cho phép", (dialog, which) -> {
                    prefs.edit().putBoolean(key, true).apply();
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                })
                .setNegativeButton("Không", (dialog, which) -> {
                    prefs.edit().putBoolean(key, true).apply();
                    dialog.dismiss();
                })
                .show();
    }

    /**
     * Kiểm tra trạng thái đăng nhập và chuyển hướng đến login nếu cần
     */
//    private void checkAuthentication() {
//        if (!authManager.isLoggedIn()) {
//            // Người dùng chưa đăng nhập, chuyển đến ActivityLogin
//            Intent intent = new Intent(Main.this, ActivityLogin.class);
//            startActivity(intent);
//            finish(); // Đóng MainActivity để không thể quay lại
//        }
//    }

}