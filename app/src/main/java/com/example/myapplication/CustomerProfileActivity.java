package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.User;
import com.example.myapplication.model.UserUpdateRequest;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.UserService;
import com.example.myapplication.network.dto.ChangePasswordRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvCurrentEmail;
    private ImageView ivAvatar;
    private EditText edtEmail, edtPhone, edtAddress;
    private EditText edtCurrentPassword, edtNewPassword, edtConfirmPassword;
    private Button btnSave, btnChangePassword, btnEditAvatar;

    private UserService api;
    private String userId = null;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        // Khởi tạo views
        initViews();
        
        // Lấy userId từ SharedPreferences
        getUserIdFromPrefs();
        
        // Khởi tạo API service
        api = ApiClient.getRetrofit(this).create(UserService.class);

        // Load thông tin user
        if (userId != null) {
            loadUserInfo();
        } else {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set up click listeners
        setupClickListeners();
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvCurrentEmail = findViewById(R.id.tvCurrentEmail);
        ivAvatar = findViewById(R.id.ivAvatar);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtCurrentPassword = findViewById(R.id.edtCurrentPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSave = findViewById(R.id.btnSave);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnEditAvatar = findViewById(R.id.btnEditAvatar);
    }

    private void getUserIdFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = prefs.getString("userID", null);
        
        if (userId != null && !userId.isEmpty()) {
            try {
                Integer.parseInt(userId); // Validate that it's a number
            } catch (NumberFormatException e) {
                Log.e("CustomerProfile", "userID không phải là số: " + userId);
                userId = null;
            }
        }
    }

    private void setupClickListeners() {
        btnSave.setOnClickListener(v -> saveProfileChanges());
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnEditAvatar.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thay đổi ảnh đại diện đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadUserInfo() {
        try {
            int userIdInt = Integer.parseInt(userId);
            api.getUserById(userIdInt).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> res) {
                    if (res.isSuccessful() && res.body() != null) {
                        currentUser = res.body();
                        displayUserInfo(currentUser);
                    } else {
                        Toast.makeText(CustomerProfileActivity.this,
                                "Không tải được thông tin (" + res.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Toast.makeText(CustomerProfileActivity.this, "Lỗi kết nối: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi định dạng ID người dùng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayUserInfo(User user) {
        tvUsername.setText(nz(user.getUsername()));
        tvCurrentEmail.setText(nz(user.getEmail()));
        edtEmail.setText(nz(user.getEmail()));
        edtPhone.setText(nz(user.getPhoneNumber()));
        edtAddress.setText(nz(user.getAddress()));
    }

    private void saveProfileChanges() {
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        // Validation
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }

        // Tạo request body theo API spec
        UserUpdateRequest body = new UserUpdateRequest(phone, address, "Customer", email);

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        try {
            int userIdInt = Integer.parseInt(userId);
            api.updateUser(userIdInt, body).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> res) {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU THÔNG TIN");
                    
                    if (res.isSuccessful()) {
                        Toast.makeText(CustomerProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        // Cập nhật lại thông tin hiển thị
                        if (res.body() != null) {
                            displayUserInfo(res.body());
                        }
                    } else {
                        Toast.makeText(CustomerProfileActivity.this, "Lỗi: " + res.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU THÔNG TIN");
                    Toast.makeText(CustomerProfileActivity.this, "Không kết nối được server",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            btnSave.setEnabled(true);
            btnSave.setText("LƯU THÔNG TIN");
            Toast.makeText(this, "Lỗi định dạng ID người dùng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Validation
        if (currentPassword.isEmpty()) {
            edtCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            return;
        }
        if (newPassword.isEmpty()) {
            edtNewPassword.setError("Vui lòng nhập mật khẩu mới");
            return;
        }
        if (confirmPassword.isEmpty()) {
            edtConfirmPassword.setError("Vui lòng xác nhận mật khẩu mới");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            edtConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            return;
        }
        if (newPassword.length() < 6) {
            edtNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        // Tạo request body theo API spec
        ChangePasswordRequest body = new ChangePasswordRequest(newPassword);

        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Đang đổi...");

        try {
            int userIdInt = Integer.parseInt(userId);
            // Giả sử có API changePassword, nếu không có thì sẽ cần thêm vào UserService
            api.changePassword(userIdInt, body).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> res) {
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("ĐỔI MẬT KHẨU");
                    
                    if (res.isSuccessful()) {
                        Toast.makeText(CustomerProfileActivity.this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        // Clear password fields
                        edtCurrentPassword.setText("");
                        edtNewPassword.setText("");
                        edtConfirmPassword.setText("");
                    } else {
                        Toast.makeText(CustomerProfileActivity.this, "Lỗi đổi mật khẩu: " + res.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("ĐỔI MẬT KHẨU");
                    Toast.makeText(CustomerProfileActivity.this, "Không kết nối được server",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } catch (NumberFormatException e) {
            btnChangePassword.setEnabled(true);
            btnChangePassword.setText("ĐỔI MẬT KHẨU");
            Toast.makeText(this, "Lỗi định dạng ID người dùng!", Toast.LENGTH_SHORT).show();
        }
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}