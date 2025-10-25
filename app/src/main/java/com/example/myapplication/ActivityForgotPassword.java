package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class ActivityForgotPassword extends AppCompatActivity {

    // --- VIEWS ---
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private MaterialButton sendOtpButton;
    private TextView loginTextView;
    private ImageButton backButton;

    // --- OTP GENERATION ---
    private String generatedOtp;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Ánh xạ các view từ file XML
        initializeViews();

        // Xử lý hiển thị tràn viền (edge-to-edge)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Thiết lập các sự kiện click
        setupClickListeners();
    }

    /**
     * Ánh xạ các biến view với các component trong file XML.
     */
    private void initializeViews() {
        emailInputLayout = findViewById(R.id.emailInputLayout);
        emailEditText = findViewById(R.id.emailEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        loginTextView = findViewById(R.id.loginText);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Thiết lập các sự kiện OnClickListener cho các view tương tác.
     */
    private void setupClickListeners() {
        // Sự kiện click cho nút "Gửi mã OTP"
        sendOtpButton.setOnClickListener(v -> handleSendOtp());

        // Sự kiện click cho nút "Back"
        backButton.setOnClickListener(v -> onBackPressed());

        // Sự kiện click cho chữ "Đăng nhập"
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityForgotPassword.this, ActivityLogin.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Xử lý logic khi người dùng nhấn nút gửi mã OTP.
     */
    private void handleSendOtp() {
        String email = emailEditText.getText().toString().trim();

        // Kiểm tra tính hợp lệ của email
        if (!validateEmail(email)) {
            return; // Dừng lại nếu email không hợp lệ
        }

        // Lưu email và tạo OTP
        userEmail = email;
        generatedOtp = generateOtp();

        // Vô hiệu hóa nút để tránh click nhiều lần
        sendOtpButton.setEnabled(false);
        sendOtpButton.setText("Đang gửi...");

        // Gửi OTP qua API backend
        Log.d("ActivityForgotPassword", "Sending OTP via backend API");
        sendOtpViaAPI(email, generatedOtp);
    }

    /**
     * Tạo mã OTP 6 chữ số ngẫu nhiên.
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // Tạo số từ 100000 đến 999999
        return String.valueOf(otp);
    }

    /**
     * Gửi OTP qua API backend
     */
    private void sendOtpViaAPI(String email, String otp) {
        Log.d("ActivityForgotPassword", "API OTP method - Email: " + email + ", OTP: " + otp);
        
        // Tạm thời sử dụng method đơn giản vì chưa có API implementation
        // TODO: Implement API call to /users/forgot-password
        Toast.makeText(this, "Mã OTP: " + otp + " (API mode)", Toast.LENGTH_LONG).show();
        
        // Chuyển đến màn hình xác thực OTP
        Intent intent = new Intent(this, ActivityOtpVerification.class);
        intent.putExtra("email", email);
        intent.putExtra("otp", otp);
        startActivity(intent);
        finish();
    }

    /**
     * Kiểm tra email có hợp lệ không.
     * @param email Email cần kiểm tra
     * @return true nếu email hợp lệ, ngược lại là false.
     */
    private boolean validateEmail(String email) {
        // Xóa lỗi cũ
        emailInputLayout.setError(null);

        if (email.isEmpty()) {
            emailInputLayout.setError("Vui lòng nhập email");
            return false;
        }

        // Kiểm tra định dạng email cơ bản
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Email không hợp lệ");
            return false;
        }

        return true;
    }
}