package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

public class ActivityOtpVerification extends AppCompatActivity {

    // --- VIEWS ---
    private TextInputLayout otpInputLayout;
    private TextInputEditText otpEditText;
    private MaterialButton verifyOtpButton;
    private TextView resendOtpLink;
    private TextView forgotPasswordText;
    private TextView emailDisplay;
    private ImageButton backButton;

    // --- DATA ---
    private String userEmail;
    private String correctOtp;
    private CountDownTimer resendTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp_verification);

        // Lấy dữ liệu từ Intent
        userEmail = getIntent().getStringExtra("email");
        correctOtp = getIntent().getStringExtra("otp");

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

        // Bắt đầu đếm ngược để gửi lại OTP
        startResendTimer();
    }

    /**
     * Ánh xạ các biến view với các component trong file XML.
     */
    private void initializeViews() {
        otpInputLayout = findViewById(R.id.otpInputLayout);
        otpEditText = findViewById(R.id.otpEditText);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpLink = findViewById(R.id.resendOtpLink);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        emailDisplay = findViewById(R.id.email_display);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Thiết lập các sự kiện OnClickListener cho các view tương tác.
     */
    private void setupClickListeners() {
        // Sự kiện click cho nút "Xác thực OTP"
        verifyOtpButton.setOnClickListener(v -> handleVerifyOtp());

        // Sự kiện click cho nút "Back"
        backButton.setOnClickListener(v -> onBackPressed());

        // Sự kiện click cho "Gửi lại mã OTP"
        resendOtpLink.setOnClickListener(v -> handleResendOtp());

        // Sự kiện click cho "Quên mật khẩu"
        forgotPasswordText.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityOtpVerification.this, ActivityForgotPassword.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Xử lý logic khi người dùng nhấn nút xác thực OTP.
     */
    private void handleVerifyOtp() {
        String enteredOtp = otpEditText.getText().toString().trim();

        // Kiểm tra tính hợp lệ của OTP
        if (!validateOtp(enteredOtp)) {
            return; // Dừng lại nếu OTP không hợp lệ
        }

        // Vô hiệu hóa nút để tránh click nhiều lần
        verifyOtpButton.setEnabled(false);
        verifyOtpButton.setText("Đang xác thực...");

        // Kiểm tra OTP
        if (enteredOtp.equals(correctOtp)) {
            // OTP đúng - chuyển đến trang đặt lại mật khẩu
            Toast.makeText(this, "Xác thực thành công! Vui lòng đặt lại mật khẩu.", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(this, ActivityResetPassword.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            finish();
        } else {
            // OTP sai
            Toast.makeText(this, "Mã OTP không đúng. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            otpInputLayout.setError("Mã OTP không đúng");
            
            // Kích hoạt lại nút
            verifyOtpButton.setEnabled(true);
            verifyOtpButton.setText("Xác thực OTP");
        }
    }

    /**
     * Xử lý logic khi người dùng nhấn "Gửi lại mã OTP".
     */
    private void handleResendOtp() {
        if (!canResend) {
            Toast.makeText(this, "Vui lòng đợi để gửi lại mã OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo OTP mới
        correctOtp = generateNewOtp();
        Toast.makeText(this, "Mã OTP mới: " + correctOtp, Toast.LENGTH_LONG).show();
        
        // Bắt đầu đếm ngược lại
        startResendTimer();
    }

    /**
     * Tạo mã OTP mới.
     */
    private String generateNewOtp() {
        java.util.Random random = new java.util.Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Bắt đầu đếm ngược để gửi lại OTP.
     */
    private void startResendTimer() {
        canResend = false;
        resendOtpLink.setTextColor(getResources().getColor(android.R.color.darker_gray));
        
        resendTimer = new CountDownTimer(60000, 1000) { // 60 giây
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                resendOtpLink.setText("Gửi lại mã OTP (" + seconds + "s)");
            }

            @Override
            public void onFinish() {
                canResend = true;
                resendOtpLink.setText("Gửi lại mã OTP");
                resendOtpLink.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        }.start();
    }

    /**
     * Kiểm tra OTP có hợp lệ không.
     * @param otp OTP cần kiểm tra
     * @return true nếu OTP hợp lệ, ngược lại là false.
     */
    private boolean validateOtp(String otp) {
        // Xóa lỗi cũ
        otpInputLayout.setError(null);

        if (otp.isEmpty()) {
            otpInputLayout.setError("Vui lòng nhập mã OTP");
            return false;
        }

        if (otp.length() != 6) {
            otpInputLayout.setError("Mã OTP phải có 6 chữ số");
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resendTimer != null) {
            resendTimer.cancel();
        }
    }
}
