package com.example.myapplication; // Đảm bảo đúng package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class ActivityWelcome extends AppCompatActivity {

    private MaterialButton getStartedButton;
    private ImageButton backButton;
    private ImageButton closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome);

        // Ánh xạ các view
        getStartedButton = findViewById(R.id.getStartedButton);
        backButton = findViewById(R.id.backButton);
        closeButton = findViewById(R.id.closeButton);

        // Xử lý hiển thị tràn viền
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập sự kiện click
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Chuyển đến màn hình Đăng nhập khi nhấn nút
        getStartedButton.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityWelcome.this, Main.class);
            startActivity(intent);
            finish(); // Đóng màn hình này để người dùng không quay lại được
        });

        backButton.setOnClickListener(v -> {
            // Tạm thời hiển thị Toast, hoặc có thể finish() activity
            Toast.makeText(this, "Back clicked", Toast.LENGTH_SHORT).show();
            onBackPressed();
        });

        closeButton.setOnClickListener(v -> {
            // Đóng activity hoặc chuyển đến màn hình chính
            Toast.makeText(this, "Close clicked", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}