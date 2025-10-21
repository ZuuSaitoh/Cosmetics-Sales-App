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

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();

                    String token = null;
                    String role = null;
                    Long userId = null;

                    if (body.getResult() != null) {
                        token = body.getResult().getToken();
                        role = body.getResult().getRole();
                        userId = body.getResult().getUserID();
                    }

                    if (userId == null && token != null && !token.isEmpty()) {
                        userId = com.example.myapplication.auth.JwtDecoder.getUserIdFromToken(token);
                        // If we decode the token for userId, we should also decode it for the role.
                        role = com.example.myapplication.auth.JwtDecoder.getRoleFromToken(token);
                    }

                    if (token != null && !token.isEmpty()) {
                        authManager.saveAuth(token, role, userId);

                        // Case 1: Return to ProductDetail if there's a pending product
                        if (getIntent() != null && getIntent().hasExtra("pending_product")) {
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("pending_product", getIntent().getSerializableExtra("pending_product"));
                            resultIntent.putExtra("pending_quantity", getIntent().getIntExtra("pending_quantity", 1));
                            setResult(RESULT_OK, resultIntent);
                            finish(); // Finish LoginActivity and return to ProductDetailActivity
                            return; // Important: Stop further execution
                        }

                        // Case 2: Normal login, redirect based on role
                        setResult(RESULT_OK);
                        Intent intent;
                        if ("Admin".equals(role)) {
                            intent = new Intent(ActivityLogin.this, AdminActivity.class);
                        } else {
                            intent = new Intent(ActivityLogin.this, Main.class);
                        }
                        startActivity(intent);
                        finish(); // Finish LoginActivity after redirection

                    } else {
                        Toast.makeText(ActivityLogin.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                    }
                } else {
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
