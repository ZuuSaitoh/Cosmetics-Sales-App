package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.LoginRequest;
import com.example.myapplication.network.dto.LoginResponse;
import com.example.myapplication.network.dto.RegisterRequest;
import com.example.myapplication.network.dto.RegisterResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginScreen extends AppCompatActivity {

    private TextInputLayout emailOrPhoneInputLayout;
    private TextInputEditText emailOrPhoneEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton, signUpButton, createAccountButton;
    
    // Register form fields
    private TextInputEditText registerFullNameEditText;
    private TextInputEditText registerEmailOrPhoneEditText;
    private TextInputEditText registerPasswordEditText;
    private TextInputEditText registerConfirmPasswordEditText;
    
    // Layouts
    private LinearLayout loginForm;
    private LinearLayout registerForm;
    private TextView backToLoginText;
    private TextView forgotPasswordTextView;
    
    // Social login buttons
    private ImageButton facebookLoginButton;
    private ImageButton googleLoginButton;
    private ImageButton appleLoginButton;

    private AuthService authService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        authManager = new AuthManager(this);

        // Ánh xạ view
        emailOrPhoneInputLayout = findViewById(R.id.emailOrPhoneInputLayout);
        emailOrPhoneEditText = findViewById(R.id.emailOrPhoneEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);
        createAccountButton = findViewById(R.id.createAccountButton);
        
        // Register form fields
        registerFullNameEditText = findViewById(R.id.registerFullNameEditText);
        registerEmailOrPhoneEditText = findViewById(R.id.registerEmailOrPhoneEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        registerConfirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText);
        
        // Layouts
        loginForm = findViewById(R.id.loginForm);
        registerForm = findViewById(R.id.registerForm);
        backToLoginText = findViewById(R.id.backToLoginText);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        
        // Social login buttons
        facebookLoginButton = findViewById(R.id.facebookLoginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        appleLoginButton = findViewById(R.id.appleLoginButton);

        // Xử lý insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Sự kiện đăng nhập
        loginButton.setOnClickListener(v -> {
            String username = emailOrPhoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty()) {
                emailOrPhoneInputLayout.setError("Vui lòng nhập email hoặc số điện thoại");
                return;
            } else {
                emailOrPhoneInputLayout.setError(null);
            }

            if (password.isEmpty()) {
                passwordInputLayout.setError("Vui lòng nhập mật khẩu");
                return;
            } else {
                passwordInputLayout.setError(null);
            }

            performLogin(username, password);
        });
        
        // Sự kiện đăng ký
        signUpButton.setOnClickListener(v -> {
            loginForm.setVisibility(View.GONE);
            registerForm.setVisibility(View.VISIBLE);
        });
        
        // Sự kiện quay lại đăng nhập
        backToLoginText.setOnClickListener(v -> {
            registerForm.setVisibility(View.GONE);
            loginForm.setVisibility(View.VISIBLE);
        });
        
        // Sự kiện tạo tài khoản
        createAccountButton.setOnClickListener(v -> {
            String fullName = registerFullNameEditText.getText().toString().trim();
            String emailOrPhone = registerEmailOrPhoneEditText.getText().toString().trim();
            String password = registerPasswordEditText.getText().toString().trim();
            String confirmPassword = registerConfirmPasswordEditText.getText().toString().trim();
            
            if (fullName.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (emailOrPhone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email hoặc số điện thoại", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }
            
            performRegister(fullName, emailOrPhone, password, confirmPassword);
        });
        
        // Sự kiện quên mật khẩu
        forgotPasswordTextView.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng quên mật khẩu đang được phát triển", Toast.LENGTH_SHORT).show();
        });
        
        // Sự kiện đăng nhập social
        facebookLoginButton.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng nhập Facebook đang được phát triển", Toast.LENGTH_SHORT).show();
        });
        
        googleLoginButton.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng nhập Google đang được phát triển", Toast.LENGTH_SHORT).show();
        });
        
        appleLoginButton.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng nhập Apple đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void performLogin(String username, String password) {
        loginButton.setEnabled(false);
        Call<LoginResponse> call = authService.login(new LoginRequest(username, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loginButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    
                    // Debug log để xem response
                    Log.d("LoginScreen", "Login response: token=" + body.getToken() + ", userId=" + body.getUserId() + ", role=" + body.getRole());
                    Log.d("LoginScreen", "userID (Integer) from response: " + body.getUserID());
                    Log.d("LoginScreen", "userIdInt from response: " + body.getUserIdInt());
                    
                    authManager.saveAuth(body.getToken(), body.getRole());
                    if (body.getUserId() != null) {
                        authManager.saveUserId(body.getUserId());
                    }

                    // ✅ Lưu thông tin user vào SharedPreferences
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    String userId = body.getUserId();
                    
                    // Debug log
                    Log.d("LoginScreen", "API returned userId: " + userId);
                    Log.d("LoginScreen", "Input username: " + username);
                    
                    // Nếu userId từ API null hoặc rỗng, thử lấy từ token hoặc sử dụng username
                    if (userId == null || userId.trim().isEmpty()) {
                        Log.w("LoginScreen", "userId is null/empty from API, using fallback");
                        // Fallback: sử dụng username hoặc tạo ID tạm thời
                        userId = username; // Hoặc có thể là "1" cho demo
                        Log.d("LoginScreen", "Using fallback userId: " + userId);
                    }
                    
                    prefs.edit()
                            .putString("username", username) // Lưu username từ input
                            .putString("userID", userId.trim())
                            .apply();
                    Log.d("LoginScreen", "Saved username: " + username);
                    Log.d("LoginScreen", "Saved userId: " + userId.trim());

                    // Chuyển sang màn hình chính
                    Intent intent = new Intent(LoginScreen.this, Main.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginScreen.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginButton.setEnabled(true);
                Toast.makeText(LoginScreen.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void performRegister(String fullName, String emailOrPhone, String password, String confirmPassword) {
        createAccountButton.setEnabled(false);
        Call<RegisterResponse> call = authService.register(new RegisterRequest(fullName, emailOrPhone, password, confirmPassword));
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                createAccountButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(LoginScreen.this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
                    // Chuyển về form đăng nhập
                    registerForm.setVisibility(View.GONE);
                    loginForm.setVisibility(View.VISIBLE);
                    // Điền thông tin email/phone vào form đăng nhập
                    emailOrPhoneEditText.setText(emailOrPhone);
                } else {
                    Toast.makeText(LoginScreen.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                createAccountButton.setEnabled(true);
                Toast.makeText(LoginScreen.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
