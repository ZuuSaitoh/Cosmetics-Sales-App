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
        // Set the content view to your new login XML file
        setContentView(R.layout.activity_login00000);

        // Initialize services
        authService = ApiClient.getRetrofit(this).create(AuthService.class);
        authManager = new AuthManager(this);

        // Initialize Views using the new IDs from your XML
        initializeViews();

        // Handle window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up click listeners for the interactive elements
        setupClickListeners();
    }

    private void initializeViews() {
        // IMPORTANT: IDs are taken from your 'activity_login00000.xml'
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        // Note: The ID 'registerFullNameEditText' is a bit confusing in a login screen,
        // you might consider renaming it to 'loginFullNameEditText' for clarity.
        fullNameEditText = findViewById(R.id.registerFullNameEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpTextView = findViewById(R.id.signUpText);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        backButton = findViewById(R.id.backButton);
    }

    private void setupClickListeners() {
        // Login button action
        loginButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!validateInput(fullName, password)) {
                return; // Stop if validation fails
            }
            performLogin(fullName, password);
        });

        // "Sign up" text action: Navigate to the new SignUp screen
        signUpTextView.setOnClickListener(v -> {
            Intent intent = new Intent(ActivityLogin.this, ActivitySignUp.class); // Assumes you have a SignUpScreen.java
            startActivity(intent);
        });

        // Back button action
        backButton.setOnClickListener(v -> onBackPressed());

        // Forgot password action
        forgotPasswordTextView.setOnClickListener(v -> {
            Toast.makeText(ActivityLogin.this, "Forgot Password Clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to your Forgot Password activity
        });
    }

    private boolean validateInput(String fullName, String password) {
        // Reset errors
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
        loginButton.setEnabled(false); // Disable button to prevent multiple clicks
        Call<LoginResponse> call = authService.login(new LoginRequest(username, password));

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loginButton.setEnabled(true); // Re-enable button
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse body = response.body();
                    authManager.saveAuth(body.getToken(), body.getRole());

                    // Navigate to the main part of the app
                    Intent intent = new Intent(ActivityLogin.this, Main.class); // Change MainActivity.class to your main activity
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish(); // Finish LoginScreen so user can't go back to it
                } else {
                    Toast.makeText(ActivityLogin.this, "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loginButton.setEnabled(true); // Re-enable button
                Toast.makeText(ActivityLogin.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}