package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.LoginRequest;
import com.example.myapplication.network.dto.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityLogin extends AppCompatActivity {

    // --- VIEWS ---
    private TextInputLayout fullNameInputLayout;
    private TextInputEditText fullNameEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private MaterialButton loginButton;
    private TextView signUpTextView;
    private TextView forgotPasswordTextView;
    private ImageButton backButton;

    // --- SERVICES & MANAGERS ---
    private AuthService authService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login00000);

        // Initialize services
        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        authManager = new AuthManager(this);

        // Initialize Views
        initializeViews();

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setupClickListeners();
    }

    private void initializeViews() {
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        fullNameEditText = findViewById(R.id.registerFullNameEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpText);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> {
            String userName = fullNameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!validateInput(userName, password)) {
                return;
            }
            performLogin(userName, password);
        });

        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLogin.this, ActivitySignUp.class);
            startActivity(intent);
        });

        backButton.setOnClickListener(v -> onBackPressed());

        forgotPasswordTextView.setOnClickListener(v -> {
            Toast.makeText(ActivityLogin.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateInput(String fullName, String password) {
        fullNameInputLayout.setError(null);
        passwordInputLayout.setError(null);

        if (fullName.isEmpty()) {
            fullNameInputLayout.setError("Please enter your full name");
            return false;
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Please enter your password");
            return false;
        }

        return true;
    }

    private void performLogin(String username, String password) {
        loginButton.setEnabled(false);
        Call<LoginResponse> call = authService.login(new LoginRequest(username, password));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loginButton.setEnabled(true);

                // --- SỬA LOGIC KIỂM TRA TOKEN ---
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();

                    // Debug: Log toàn bộ response
                    android.util.Log.d("ActivityLogin", "=== FULL LOGIN RESPONSE ===");
                    android.util.Log.d("ActivityLogin", "Response Code: " + response.code());
                    android.util.Log.d("ActivityLogin", "Response Body: " + body.toString());
                    android.util.Log.d("ActivityLogin", "Result Object: " + (body.getResult() != null ? body.getResult().toString() : "null"));

                    String token = null;
                    String role = null;
                    Long userId = null;


                    // 1. Lấy token/role/userID từ đối tượng "result"
                    if (body.getResult() != null) {
                        token = body.getResult().getToken();
                        role = body.getResult().getRole();
                        userId = body.getResult().getUserID();

                        // Debug logging
                        android.util.Log.d("ActivityLogin", "Login Response - Token: " + (token != null ? "exists" : "null"));
                        android.util.Log.d("ActivityLogin", "Login Response - Role: " + role);
                        android.util.Log.d("ActivityLogin", "Login Response - UserID: " + userId);
                    } else {
                        android.util.Log.w("ActivityLogin", "Result object is null!");
                    }

                    // Thử lấy userID từ các nơi khác nếu không có trong result
                    if (userId == null) {
                        android.util.Log.d("ActivityLogin", "Trying to get userId from other fields...");
                        // Thử lấy từ LoginResponse trực tiếp
                        String userIdStr = body.getUserId();
                        if (userIdStr != null && !userIdStr.isEmpty()) {
                            try {
                                userId = Long.parseLong(userIdStr);
                                android.util.Log.d("ActivityLogin", "Found userId in LoginResponse: " + userId);
                            } catch (NumberFormatException e) {
                                android.util.Log.w("ActivityLogin", "Cannot parse userId: " + userIdStr);
                            }
                        }

                        // Nếu vẫn không có userId, thử decode từ JWT token
                        if (userId == null && token != null && !token.isEmpty()) {
                            android.util.Log.d("ActivityLogin", "Trying to decode userId from JWT token");
                            userId = com.example.myapplication.auth.JwtDecoder.getUserIdFromToken(token);
                            android.util.Log.d("ActivityLogin", "Decoded userId from JWT: " + userId);
                        }
                    }

                    // 2. Chỉ tiếp tục khi token hợp lệ (không null, không rỗng)
                    if (token != null && !token.isEmpty()) {
                        // Token hợp lệ, lưu lại
                        authManager.saveAuth(token, role, userId);

                        // Trường hợp 1: Quay về ProductDetail
                        Intent resultIntent = new Intent();
                        if (getIntent() != null && getIntent().hasExtra("pending_product")) {
                            resultIntent.putExtra("pending_product", getIntent().getSerializableExtra("pending_product"));
                            resultIntent.putExtra("pending_quantity", getIntent().getIntExtra("pending_quantity", 1));
                            setResult(RESULT_OK, resultIntent);
                            finish(); // Quay về ProductDetailActivity
                            return;
                        }

                        // Trường hợp 2: Redirect về chat sau khi login
                        if (getIntent() != null && getIntent().getBooleanExtra("redirect_to_chat", false)) {
                            Intent intent = new Intent(ActivityLogin.this, ChatActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }

                        // Trường hợp 3: Login thông thường, vào Main
                        setResult(RESULT_OK);
                        Intent intent = new Intent(ActivityLogin.this, Main.class);
                        startActivity(intent);
                        finish();

                    } else {
                        // Token rỗng hoặc null -> Login thất bại
                        Toast.makeText(ActivityLogin.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                        // KHÔNG GỌI setResult(RESULT_OK)
                    }
                } else {
                    // Response không thành công (401, 404, 500...)
                    Toast.makeText(ActivityLogin.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginButton.setEnabled(true);
                Toast.makeText(ActivityLogin.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}