package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityUpdateProfile extends AppCompatActivity {

    private AuthManager authManager;
    private UserService userService;
    
    // Views
    private EditText emailEditText;
    private EditText phoneEditText;
    private EditText addressEditText;
    private Button saveButton;
    private Button cancelButton;
    private ImageButton backButton;
    private ProgressBar loadingProgressBar;
    
    private User currentUser;
    private Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_profile);

        // Khởi tạo services
        authManager = new AuthManager(this);
        userService = ApiClient.getRetrofit(this).create(UserService.class);

        // Ánh xạ views
        initializeViews();

        // Thiết lập click listeners
        setupClickListeners();

        // Load thông tin user hiện tại
        loadCurrentUserInfo();

        // Xử lý window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
        backButton = findViewById(R.id.backButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> saveUserInfo());
        cancelButton.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
    }

    private void loadCurrentUserInfo() {
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        userId = authManager.getUserId();
        if (userId == null) {
            Log.e("ActivityUpdateProfile", "No userId found");
            Toast.makeText(this, "Không tìm thấy ID người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("ActivityUpdateProfile", "Loading user info for userId: " + userId);

        Call<User> call = userService.getUserById(userId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                loadingProgressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    populateFields();
                } else {
                    Log.e("ActivityUpdateProfile", "Failed to load user info: " + response.code());
                    Toast.makeText(ActivityUpdateProfile.this, "Không thể tải thông tin người dùng", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                Log.e("ActivityUpdateProfile", "Error loading user info", t);
                Toast.makeText(ActivityUpdateProfile.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        if (currentUser != null) {
            emailEditText.setText(currentUser.getEmail());
            phoneEditText.setText(currentUser.getPhoneNumber());
            addressEditText.setText(currentUser.getAddress());
        }
    }

    private void saveUserInfo() {
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (!validateInput(email, phone, address)) {
            return;
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Tạo User object với thông tin mới
        User updatedUser = new User();
        updatedUser.setUserID(userId);
        updatedUser.setUsername(currentUser.getUsername()); // Giữ nguyên username
        updatedUser.setEmail(email);
        updatedUser.setPhoneNumber(phone);
        updatedUser.setAddress(address);
        updatedUser.setRole(currentUser.getRole()); // Giữ nguyên role

        Log.d("ActivityUpdateProfile", "Updating user: " + userId);

        Call<User> call = userService.updateUser(userId, updatedUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                loadingProgressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("ActivityUpdateProfile", "User updated successfully");
                    Toast.makeText(ActivityUpdateProfile.this, "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show();
                    
                    // Trả về kết quả để AccountFragment có thể refresh
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("user_updated", true);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Log.e("ActivityUpdateProfile", "Update failed: " + response.code());
                    Toast.makeText(ActivityUpdateProfile.this, "Cập nhật thất bại (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Log.e("ActivityUpdateProfile", "Error updating user", t);
                Toast.makeText(ActivityUpdateProfile.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String email, String phone, String address) {
        boolean isValid = true;

        if (email.isEmpty()) {
            emailEditText.setError("Vui lòng nhập email");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Email không hợp lệ");
            isValid = false;
        }

        if (phone.isEmpty()) {
            phoneEditText.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        }

        if (address.isEmpty()) {
            addressEditText.setError("Vui lòng nhập địa chỉ");
            isValid = false;
        }

        return isValid;
    }
}
