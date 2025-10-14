package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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

    private CardView loginCard, registerCard;
    private TextInputLayout emailOrPhoneInputLayout, passwordInputLayout;
    private TextInputEditText emailOrPhoneEditText, passwordEditText;
    private MaterialButton loginButton, createAccountButton;
    private TextView signUpPrompt, backToLoginText, forgotPasswordTextView;
    private ImageButton facebookLoginButton, googleLoginButton, appleLoginButton;

    private TextInputLayout registerFullNameInputLayout, registerEmailOrPhoneInputLayout, registerPasswordInputLayout, registerConfirmPasswordInputLayout;
    private TextInputEditText registerFullNameEditText, registerEmailOrPhoneEditText, registerPasswordEditText, registerConfirmPasswordEditText;

    private AuthService authService;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        authManager = new AuthManager(this);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        loginCard = findViewById(R.id.loginCard);
        registerCard = findViewById(R.id.registerCard);

        // Login fields
        emailOrPhoneInputLayout = findViewById(R.id.emailOrPhoneInputLayout);
        emailOrPhoneEditText = findViewById(R.id.emailOrPhoneEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpPrompt = findViewById(R.id.signUpPrompt);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        // Registration fields
        registerFullNameInputLayout = findViewById(R.id.registerFullNameInputLayout);
        registerFullNameEditText = findViewById(R.id.registerFullNameEditText);
        registerEmailOrPhoneInputLayout = findViewById(R.id.registerEmailOrPhoneInputLayout);
        registerEmailOrPhoneEditText = findViewById(R.id.registerEmailOrPhoneEditText);
        registerPasswordInputLayout = findViewById(R.id.registerPasswordInputLayout);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        registerConfirmPasswordInputLayout = findViewById(R.id.registerConfirmPasswordInputLayout);
        registerConfirmPasswordEditText = findViewById(R.id.registerConfirmPasswordEditText);
        createAccountButton = findViewById(R.id.createAccountButton);
        backToLoginText = findViewById(R.id.backToLoginText);

        // Social login buttons
        facebookLoginButton = findViewById(R.id.facebookLoginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
        appleLoginButton = findViewById(R.id.appleLoginButton);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        signUpPrompt.setOnClickListener(v -> showRegisterForm());
        backToLoginText.setOnClickListener(v -> showLoginForm());
        createAccountButton.setOnClickListener(v -> handleRegisterClick());

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginScreen.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Placeholder listeners for social login
        facebookLoginButton.setOnClickListener(v -> Toast.makeText(this, "Facebook Login clicked", Toast.LENGTH_SHORT).show());
        googleLoginButton.setOnClickListener(v -> Toast.makeText(this, "Google Login clicked", Toast.LENGTH_SHORT).show());
        appleLoginButton.setOnClickListener(v -> Toast.makeText(this, "Apple Login clicked", Toast.LENGTH_SHORT).show());
    }

    private void showLoginForm() {
        registerCard.setVisibility(View.GONE);
        loginCard.setVisibility(View.VISIBLE);
    }

    private void showRegisterForm() {
        loginCard.setVisibility(View.GONE);
        registerCard.setVisibility(View.VISIBLE);
    }

    private void performLogin() {
        String emailOrPhone = emailOrPhoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (validateLoginInput(emailOrPhone, password)) {
            loginButton.setEnabled(false);
            Call<LoginResponse> call = authService.login(new LoginRequest(emailOrPhone, password));
            call.enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                    loginButton.setEnabled(true);
                    if (response.isSuccessful() && response.body() != null) {
                        LoginResponse body = response.body();
                        authManager.saveAuth(body.getToken(), body.getRole());
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
    }

    private boolean validateLoginInput(String emailOrPhone, String password) {
        boolean isValid = true;
        if (emailOrPhone.isEmpty()) {
            emailOrPhoneInputLayout.setError("Không được để trống");
            isValid = false;
        } else {
            emailOrPhoneInputLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Không được để trống");
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }
        return isValid;
    }

    private void handleRegisterClick() {
        String fullName = registerFullNameEditText.getText().toString().trim();
        String email = registerEmailOrPhoneEditText.getText().toString().trim();
        String password = registerPasswordEditText.getText().toString().trim();
        String confirmPassword = registerConfirmPasswordEditText.getText().toString().trim();

        if (validateRegisterInput(fullName, email, password, confirmPassword)) {
            createAccountButton.setEnabled(false);
            RegisterRequest request = new RegisterRequest(fullName, password, confirmPassword, email);
            Call<RegisterResponse> call = authService.register(request);
            call.enqueue(new Callback<RegisterResponse>() {
                @Override
                public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                    createAccountButton.setEnabled(true);
                    if (response.isSuccessful()) {
                        Toast.makeText(LoginScreen.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        showLoginForm();
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

    private boolean validateRegisterInput(String fullName, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (fullName.isEmpty()) {
            registerFullNameInputLayout.setError("Không được để trống");
            isValid = false;
        } else {
            registerFullNameInputLayout.setError(null);
        }

        if (email.isEmpty()) {
            registerEmailOrPhoneInputLayout.setError("Không được để trống");
            isValid = false;
        } else {
            registerEmailOrPhoneInputLayout.setError(null);
        }

        if (password.isEmpty()) {
            registerPasswordInputLayout.setError("Không được để trống");
            isValid = false;
        } else {
            registerPasswordInputLayout.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            registerConfirmPasswordInputLayout.setError("Mật khẩu không khớp");
            isValid = false;
        } else {
            registerConfirmPasswordInputLayout.setError(null);
        }

        return isValid;
    }
}
