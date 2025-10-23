package com.example.myapplication;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * CheckoutActivity
 * Màn hình “Thanh toán” - Bước cuối trong quy trình mua sắm.
 * Gọi API POST /orders/place-new-orders để tạo đơn hàng mới.
 */
public class ActivityCheckout extends AppCompatActivity {

    private ImageButton btnBack;
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCashOnDelivery, rbVNPay;
    private TextView tvReceiverName, tvReceiverPhone, tvReceiverAddress;
    private TextView tvTotalPrice;
    private MaterialButton btnCheckout;
    private ProgressBar progressBar;

    private String selectedPaymentMethod = "COD";
    private double totalPrice = 1000000.0; // ví dụ (đồng)
    private boolean isPlacingOrder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        setupListeners();
        loadDefaultData();
    }

    /**
     * Ánh xạ View
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCashOnDelivery = findViewById(R.id.rbCashOnDelivery);
        rbVNPay = findViewById(R.id.rbVNPay);
        tvReceiverName = findViewById(R.id.tvReceiverName);
        tvReceiverPhone = findViewById(R.id.tvReceiverPhone);
        tvReceiverAddress = findViewById(R.id.tvReceiverAddress);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCheckout = findViewById(R.id.btnCheckout);

        // Bạn có thể thêm progressBar trong layout nếu cần loading overlay
        progressBar = new ProgressBar(this);
    }

    /**
     * Thiết lập sự kiện
     */
    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCashOnDelivery) {
                selectedPaymentMethod = "COD";
            } else if (checkedId == R.id.rbVNPay) {
                selectedPaymentMethod = "VNPAY";
            }
        });

        btnCheckout.setOnClickListener(v -> showConfirmDialog());
    }

    /**
     * Hiển thị dữ liệu mặc định (demo)
     */
    private void loadDefaultData() {
        // Thông tin demo - trong thực tế lấy từ SharedPreferences hoặc API
        tvReceiverName.setText("Nguyễn Văn A");
        tvReceiverPhone.setText("0123 456 789");
        tvReceiverAddress.setText("123 Nguyễn Huệ, P. Bến Nghé, Q.1, TP. HCM");

        tvTotalPrice.setText(formatMoney(totalPrice));
    }

    /**
     * Định dạng tiền VND
     */
    private String formatMoney(double amount) {
        return String.format("%,.0fđ", amount);
    }

    /**
     * Hộp thoại xác nhận đặt hàng
     */
    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận đặt hàng")
                .setMessage("Bạn có chắc chắn muốn đặt đơn hàng này không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> placeOrder())
                .setNegativeButton("Hủy", null)
                .show();
    }

    /**
     * Gửi yêu cầu đặt hàng (POST /orders/place-new-orders)
     */
    private void placeOrder() {
        if (isPlacingOrder) return; // tránh spam click
        isPlacingOrder = true;
        btnCheckout.setEnabled(false);
        btnCheckout.setText("Đang xử lý...");
        progressBar.setVisibility(View.VISIBLE);

        // Giả lập dữ liệu JSON gửi đi
        JSONObject orderRequest = new JSONObject();
        try {
            orderRequest.put("userId", "USER123");
            orderRequest.put("paymentMethod", selectedPaymentMethod);
            orderRequest.put("address", tvReceiverAddress.getText().toString());
            orderRequest.put("totalAmount", totalPrice);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // TODO: Gọi API thực tế bằng Retrofit hoặc OkHttp
        // Hiện tại mô phỏng call API (2 giây)
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnCheckout.setEnabled(true);
            btnCheckout.setText("Đặt hàng");
            isPlacingOrder = false;

            boolean isSuccess = true; // mô phỏng kết quả API
            if (isSuccess) {
                Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                // Chuyển sang trang thành công
//                Intent intent = new Intent(this, ActivityOrderSuccess.class);
//                intent.putExtra("ORDER_TOTAL", totalPrice);
//                startActivity(intent);
//                finish();
            } else {
                Toast.makeText(this, "Đặt hàng thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }
}
