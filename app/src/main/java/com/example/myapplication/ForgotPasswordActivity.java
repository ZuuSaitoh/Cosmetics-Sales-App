package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private MaterialButton sendResetLinkButton;
    private TextView backToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailInputLayout = findViewById(R.id.forgotPasswordEmailInputLayout);
        emailEditText = findViewById(R.id.forgotPasswordEmailEditText);
        sendResetLinkButton = findViewById(R.id.sendResetLinkButton);
        backToLogin = findViewById(R.id.backToLoginFromForgot);
    }

    private void setupClickListeners() {
        sendResetLinkButton.setOnClickListener(v -> sendResetLink());
        backToLogin.setOnClickListener(v -> finish()); // Go back to the previous screen (Login)
    }

    private void sendResetLink() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailInputLayout.setError("Vui lòng nhập email của bạn");
            return;
        } else {
            emailInputLayout.setError(null);
        }

        // --- Simulate API call ---
        sendResetLinkButton.setEnabled(false);
        Toast.makeText(this, "Đang gửi liên kết...", Toast.LENGTH_SHORT).show();

        // Here you would make the actual API call to your backend
        // For now, we'll just simulate a success response after a delay.
        new android.os.Handler().postDelayed(
                () -> {
                    sendResetLinkButton.setEnabled(true);
                    Toast.makeText(ForgotPasswordActivity.this, "Nếu email tồn tại, bạn sẽ nhận được một liên kết đặt lại mật khẩu.", Toast.LENGTH_LONG).show();
                    finish(); // Go back to login after showing the message
                },
                2000 // 2-second delay
        );
    }
}
