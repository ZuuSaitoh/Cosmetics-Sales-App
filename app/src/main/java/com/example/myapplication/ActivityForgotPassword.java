package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.myapplication.network.EmailJSService;

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

        // Sử dụng Android's built-in email functionality
        Log.d("ActivityForgotPassword", "Using Android's built-in email");
        sendOtpViaAndroidEmail(email, generatedOtp);
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
     * Gửi mã OTP qua EmailJS với fallback.
     */
    private void sendOtpViaEmailJS(String email, String otp) {
        // Thêm timeout để tránh bị đứng mãi
        android.os.Handler timeoutHandler = new android.os.Handler();
        timeoutHandler.postDelayed(() -> {
            if (!sendOtpButton.isEnabled()) {
                // Nếu sau 10 giây vẫn chưa có response, dùng fallback
                runOnUiThread(() -> {
                    Toast.makeText(ActivityForgotPassword.this, "EmailJS timeout, sử dụng fallback...", Toast.LENGTH_SHORT).show();
                    useFallbackMethod(email, otp);
                });
            }
        }, 5000); // 5 giây timeout
        
        EmailJSService.sendOtpEmail(email, otp, new EmailJSService.EmailCallback() {
            @Override
            public void onSuccess() {
                timeoutHandler.removeCallbacksAndMessages(null); // Cancel timeout
                runOnUiThread(() -> {
                    Toast.makeText(ActivityForgotPassword.this, "Mã OTP đã được gửi đến email của bạn!", Toast.LENGTH_LONG).show();
                    
                    // Chuyển đến màn hình xác thực OTP
                    Intent intent = new Intent(ActivityForgotPassword.this, ActivityOtpVerification.class);
                    intent.putExtra("email", email);
                    intent.putExtra("otp", otp);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                timeoutHandler.removeCallbacksAndMessages(null); // Cancel timeout
                runOnUiThread(() -> {
                    Toast.makeText(ActivityForgotPassword.this, "EmailJS lỗi: " + error + ". Sử dụng fallback...", Toast.LENGTH_LONG).show();
                    useFallbackMethod(email, otp);
                });
            }
        });
    }
    
    /**
     * Sử dụng phương thức fallback khi EmailJS không hoạt động.
     */
    private void useFallbackMethod(String email, String otp) {
        Log.d("ActivityForgotPassword", "Using fallback method for email: " + email + ", OTP: " + otp);
        
        EmailJSService.sendOtpFallback(email, otp, new EmailJSService.EmailCallback() {
            @Override
            public void onSuccess() {
                Log.d("ActivityForgotPassword", "Fallback success");
                runOnUiThread(() -> {
                    // Hiển thị OTP trong Toast để test
                    Toast.makeText(ActivityForgotPassword.this, "Mã OTP: " + otp + " (Fallback mode)", Toast.LENGTH_LONG).show();
                    
                    // Chuyển đến màn hình xác thực OTP
                    Intent intent = new Intent(ActivityForgotPassword.this, ActivityOtpVerification.class);
                    intent.putExtra("email", email);
                    intent.putExtra("otp", otp);
                    startActivity(intent);
                    finish();
                });
            }
            
            @Override
            public void onError(String error) {
                Log.e("ActivityForgotPassword", "Fallback error: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(ActivityForgotPassword.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    sendOtpButton.setEnabled(true);
                    sendOtpButton.setText("Gửi mã OTP");
                });
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
    
    /**
     * Test EmailJS API trực tiếp
     */
    private void testEmailJS() {
        Toast.makeText(this, "Testing EmailJS API...", Toast.LENGTH_SHORT).show();
        
        // Test với một email đơn giản
        String testEmail = "test@example.com";
        String testOtp = "123456";
        
        EmailJSService.sendOtpEmail(testEmail, testOtp, new EmailJSService.EmailCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(ActivityForgotPassword.this, "EmailJS API hoạt động!", Toast.LENGTH_SHORT).show();
                });
            }
            
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ActivityForgotPassword.this, "EmailJS Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    
    /**
     * Gửi OTP qua Android's built-in email functionality
     */
    private void sendOtpViaAndroidEmail(String email, String otp) {
        Log.d("ActivityForgotPassword", "Sending OTP via Android email: " + email + ", OTP: " + otp);
        
        try {
            // Tạo Intent để mở email app
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Mã OTP đặt lại mật khẩu - Cosmetics Sales App");
            emailIntent.putExtra(Intent.EXTRA_TEXT, 
                "Xin chào,\n\n" +
                "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản " + email + ".\n\n" +
                "Mã OTP của bạn là: " + otp + "\n\n" +
                "Mã này có hiệu lực trong 10 phút.\n\n" +
                "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                "Trân trọng,\n" +
                "Đội ngũ Cosmetics Sales App"
            );
            
            // Kiểm tra xem có email app nào không
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, "Chọn ứng dụng email"));
                
                // Hiển thị OTP trong Toast để user có thể copy
                Toast.makeText(this, "Mã OTP: " + otp + " (Copy để sử dụng)", Toast.LENGTH_LONG).show();
                
                // Chuyển đến màn hình xác thực OTP
                Intent intent = new Intent(this, ActivityOtpVerification.class);
                intent.putExtra("email", email);
                intent.putExtra("otp", otp);
                startActivity(intent);
                finish();
            } else {
                // Không có email app, sử dụng fallback
                Toast.makeText(this, "Không tìm thấy ứng dụng email. Mã OTP: " + otp, Toast.LENGTH_LONG).show();
                simpleOtpFlow(email, otp);
            }
            
        } catch (Exception e) {
            Log.e("ActivityForgotPassword", "Error sending email", e);
            Toast.makeText(this, "Lỗi gửi email. Mã OTP: " + otp, Toast.LENGTH_LONG).show();
            simpleOtpFlow(email, otp);
        }
    }
    
    /**
     * Method đơn giản để bypass EmailJS và chuyển trang ngay
     */
    private void simpleOtpFlow(String email, String otp) {
        Log.d("ActivityForgotPassword", "Simple OTP flow - Email: " + email + ", OTP: " + otp);
        
        // Hiển thị OTP trong Toast
        Toast.makeText(this, "Mã OTP: " + otp + " (Test mode)", Toast.LENGTH_LONG).show();
        
        // Chuyển đến màn hình xác thực OTP ngay lập tức
        Intent intent = new Intent(this, ActivityOtpVerification.class);
        intent.putExtra("email", email);
        intent.putExtra("otp", otp);
        startActivity(intent);
        finish();
    }
    
    /**
     * Alternative method: Sử dụng notification để hiển thị OTP
     */
    private void sendOtpViaNotification(String email, String otp) {
        Log.d("ActivityForgotPassword", "Sending OTP via notification: " + email + ", OTP: " + otp);
        
        // Tạo notification với OTP
        android.app.NotificationManager notificationManager = 
            (android.app.NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                "otp_channel", "OTP Notifications", android.app.NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        
        android.app.Notification notification = new android.app.NotificationCompat.Builder(this, "otp_channel")
            .setContentTitle("Mã OTP đặt lại mật khẩu")
            .setContentText("Mã OTP của bạn là: " + otp)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setPriority(android.app.NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build();
        
        notificationManager.notify(1, notification);
        
        // Hiển thị OTP trong Toast
        Toast.makeText(this, "Mã OTP: " + otp + " (Đã gửi notification)", Toast.LENGTH_LONG).show();
        
        // Chuyển đến màn hình xác thực OTP
        Intent intent = new Intent(this, ActivityOtpVerification.class);
        intent.putExtra("email", email);
        intent.putExtra("otp", otp);
        startActivity(intent);
        finish();
    }
}
