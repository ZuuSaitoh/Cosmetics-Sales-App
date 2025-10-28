package com.example.myapplication;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.auth.AuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ActivityMain extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    private AuthManager authManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo AuthManager
        authManager = new AuthManager(this);

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