package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class ActivityOrderSuccess extends AppCompatActivity {

    private MaterialButton btnViewOrders, btnBackHome;
    // Bỏ numberFormat nếu không dùng
    // private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        btnViewOrders = findViewById(R.id.btnViewOrders);
        btnBackHome = findViewById(R.id.btnBackHome);

        // 3. Thiết lập sự kiện cho nút "Về trang chủ"
        btnBackHome.setOnClickListener(v -> {
            // TODO: Thay 'Main.class' bằng Activity trang chủ của bạn nếu tên khác
            Intent mainIntent = new Intent(ActivityOrderSuccess.this, ActivityMain.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });

        // 4. Thiết lập sự kiện cho nút "Xem đơn hàng"
        btnViewOrders.setOnClickListener(v -> {
            // Chuyển đến màn hình "Đơn hàng đang xử lý" với filter Processing
            Intent orderHistoryIntent = new Intent(ActivityOrderSuccess.this, ActivityOrderHistory.class);
            orderHistoryIntent.putExtra("status", "Processing");
            orderHistoryIntent.putExtra("status_name", "Đang xử lý");
            startActivity(orderHistoryIntent);
            finish(); // Đóng màn hình success sau khi chuyển
        });
    }

    /**
     * Ghi đè nút Back của Android
     * để đảm bảo người dùng không quay lại được màn hình Checkout
     */
    @Override
    public void onBackPressed() {
        // Thay vì super.onBackPressed(), chúng ta tự động bấm nút "Về trang chủ"
        btnBackHome.performClick();
    }
}