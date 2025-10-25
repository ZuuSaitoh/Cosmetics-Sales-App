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

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityResetPassword extends AppCompatActivity {

    // --- VIEWS ---
    private TextInputLayout newPasswordInputLayout;
    private TextInputEditText newPasswordEditText;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText confirmPasswordEditText;
    private MaterialButton resetPasswordButton;
    private TextView loginTextView;
    private TextView emailDisplay;
    private ImageButton backButton;

    // --- DATA ---
    private String userEmail;

    // --- SERVICES ---
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        // Lấy email từ Intent
        userEmail = getIntent().getStringExtra("email");

        // Khởi tạo service để gọi API
        authService = ApiClient.getRetrofit(this).create(AuthService.class);

        // Ánh xạ các view từ file XML
        initializeViews();

        // Hiển thị email
        if (userEmail != null) {
            emailDisplay.setText(userEmail);
        }

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
        newPasswordInputLayout = findViewById(R.id.newPasswordInputLayout);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        loginTextView = findViewById(R.id.loginText);
        emailDisplay = findViewById(R.id.email_display);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Thiết lập các sự kiện OnClickListener cho các view tương tác.
     */
    private void setupClickListeners() {
        // Sự kiện click cho nút "Đặt lại mật khẩu"
        resetPasswordButton.setOnClickListener(v -> handleResetPassword());

        // Sự kiện click cho nút "Back"
        backButton.setOnClickListener(v -> onBackPressed());

        // Sự kiện click cho chữ "Đăng nhập"
        loginTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityResetPassword.this, ActivityLogin.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Xử lý logic khi người dùng nhấn nút đặt lại mật khẩu.
     */
    private void handleResetPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        // Kiểm tra tính hợp lệ của dữ liệu đầu vào
        if (!validateInput(newPassword, confirmPassword)) {
            return; // Dừng lại nếu dữ liệu không hợp lệ
        }

        // Vô hiệu hóa nút để tránh click nhiều lần
        resetPasswordButton.setEnabled(false);
        resetPasswordButton.setText("Đang xử lý...");

        // Gọi API đặt lại mật khẩu
        callResetPasswordAPI(userEmail, newPassword);
    }

    /**
     * Gọi API đặt lại mật khẩu
     */
    private void callResetPasswordAPI(String email, String newPassword) {
        Log.d("ActivityResetPassword", "Calling reset password API for: " + email);

        // TODO: Implement API call to /users/forgot-password
        // Tạm thời hiển thị thông báo thành công
        Toast.makeText(this, "Mật khẩu đã được đặt lại thành công!", Toast.LENGTH_LONG).show();
        
        // Chuyển về trang đăng nhập
        Intent intent = new Intent(this, ActivityLogin.class);
        startActivity(intent);
        finish();
    }

    /**
     * Kiểm tra các trường nhập liệu và hiển thị lỗi nếu cần.
     * @return true nếu tất cả các trường hợp lệ, ngược lại là false.
     */
    private boolean validateInput(String newPassword, String confirmPassword) {
        // Xóa các lỗi cũ
        newPasswordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);

        if (newPassword.isEmpty()) {
            newPasswordInputLayout.setError("Vui lòng nhập mật khẩu mới");
            return false;
        }

        if (newPassword.length() < 6) {
            newPasswordInputLayout.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.setError("Vui lòng xác nhận mật khẩu");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInputLayout.setError("Mật khẩu xác nhận không khớp");
            return false;
        }

        return true;
    }
}
