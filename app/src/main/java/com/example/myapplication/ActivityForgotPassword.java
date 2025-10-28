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

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.EmailService;
import com.example.myapplication.network.dto.CheckMailResponse;
import com.example.myapplication.network.dto.SendOtpResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityForgotPassword extends AppCompatActivity {

    // --- VIEWS ---
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private MaterialButton sendOtpButton;
    private TextView loginTextView;
    private ImageButton backButton;

    // --- SERVICES ---
    private AuthService authService;
    private EmailService emailService;

    // --- OTP GENERATION ---
    private String generatedOtp;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Initialize services
        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        emailService = ApiClient.getRetrofit(this).create(EmailService.class);

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

        // Lưu email
        userEmail = email;

        // Vô hiệu hóa nút để tránh click nhiều lần
        sendOtpButton.setEnabled(false);
        sendOtpButton.setText("Đang kiểm tra...");

        // Kiểm tra email có tồn tại trong hệ thống không
        checkEmailExists(email);
    }

    /**
     * Kiểm tra email có tồn tại trong hệ thống không và gửi OTP
     */
    private void checkEmailExists(String email) {
        Call<CheckMailResponse> call = authService.checkMail(email);
        
        call.enqueue(new Callback<CheckMailResponse>() {
            @Override
            public void onResponse(Call<CheckMailResponse> call, Response<CheckMailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CheckMailResponse checkMailResponse = response.body();
                    
                    // Kiểm tra email có tồn tại (result = true)
                    if (checkMailResponse.getResult() != null && checkMailResponse.getResult()) {
                        // Email tồn tại, tiến hành gửi OTP qua API
                        Log.d("ActivityForgotPassword", "Email exists, sending OTP via API");
                        sendOtpViaAPI(email);
                    } else {
                        // Email không tồn tại
                        sendOtpButton.setEnabled(true);
                        sendOtpButton.setText("Gửi mã OTP");
                        emailInputLayout.setError("Email không tồn tại trong hệ thống");
                        Toast.makeText(ActivityForgotPassword.this, 
                                "Email không tồn tại. Vui lòng kiểm tra lại.", 
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    sendOtpButton.setEnabled(true);
                    sendOtpButton.setText("Gửi mã OTP");
                    Log.e("ActivityForgotPassword", "Failed to check email. Response: " + response.code());
                    Toast.makeText(ActivityForgotPassword.this, 
                            "Lỗi khi kiểm tra email. Vui lòng thử lại.", 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<CheckMailResponse> call, Throwable t) {
                sendOtpButton.setEnabled(true);
                sendOtpButton.setText("Gửi mã OTP");
                Log.e("ActivityForgotPassword", "Network error checking email", t);
                Toast.makeText(ActivityForgotPassword.this, 
                        "Lỗi mạng. Vui lòng kiểm tra kết nối và thử lại.", 
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Gửi OTP qua API backend
     */
    private void sendOtpViaAPI(String email) {
        sendOtpButton.setText("Đang gửi OTP...");
        
        Call<SendOtpResponse> call = emailService.sendOtp(email);
        
        call.enqueue(new Callback<SendOtpResponse>() {
            @Override
            public void onResponse(Call<SendOtpResponse> call, Response<SendOtpResponse> response) {
                sendOtpButton.setEnabled(true);
                sendOtpButton.setText("Gửi mã OTP");
                
                if (response.isSuccessful() && response.body() != null) {
                    SendOtpResponse otpResponse = response.body();
                    
                    // Kiểm tra code 1111 là thành công
                    if (otpResponse.getCode() == 1111) {
                        // Lấy OTP từ result
                        String otp = otpResponse.getResult();
                        
                        Log.d("ActivityForgotPassword", "OTP sent successfully. Message: " + otpResponse.getMessage());
                        Log.d("ActivityForgotPassword", "OTP from API: " + otp);
                        
                        // Hiển thị thông báo
                        Toast.makeText(ActivityForgotPassword.this, 
                                "OTP đã được gửi đến email của bạn.", 
                                Toast.LENGTH_SHORT).show();
                        
                        // Chuyển đến màn hình xác thực OTP
                        Intent intent = new Intent(ActivityForgotPassword.this, ActivityOtpVerification.class);
                        intent.putExtra("email", email);
                        // Lưu OTP để verify (cho development/testing)
                        if (otp != null && !otp.isEmpty()) {
                            intent.putExtra("otp", otp);
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        // Code không phải 1111 - có lỗi
                        Log.e("ActivityForgotPassword", "Failed to send OTP. Code: " + otpResponse.getCode() + ", Message: " + otpResponse.getMessage());
                        Toast.makeText(ActivityForgotPassword.this, 
                                "Lỗi: " + otpResponse.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("ActivityForgotPassword", "Failed to send OTP. Response: " + response.code());
                    Toast.makeText(ActivityForgotPassword.this, 
                            "Lỗi khi gửi OTP. Vui lòng thử lại.", 
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<SendOtpResponse> call, Throwable t) {
                sendOtpButton.setEnabled(true);
                sendOtpButton.setText("Gửi mã OTP");
                Log.e("ActivityForgotPassword", "Network error sending OTP", t);
                Toast.makeText(ActivityForgotPassword.this, 
                        "Lỗi mạng. Vui lòng kiểm tra kết nối và thử lại.", 
                        Toast.LENGTH_SHORT).show();
            }
        });
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