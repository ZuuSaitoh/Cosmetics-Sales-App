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

import com.example.myapplication.auth.AuthManager;
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
        
        // Debug SharedPreferences
        debugSharedPreferences();
        
        // Khởi tạo API service
        api = ApiClient.getRetrofit(this).create(UserService.class);

        // Load thông tin user - luôn gọi để lấy từ token
        loadUserInfo();

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
        
        // Debug log
        Log.d("CustomerProfile", "userId from prefs: " + userId);
        Log.d("CustomerProfile", "All prefs keys: " + prefs.getAll().toString());
        
        // Hiển thị tên người dùng ngay từ SharedPreferences
        String username = prefs.getString("username", "Người dùng");
        tvUsername.setText(username);
        
        if (userId != null && !userId.isEmpty() && !userId.trim().isEmpty()) {
            try {
                Integer.parseInt(userId.trim()); // Validate that it's a number
                Log.d("CustomerProfile", "userId is valid: " + userId);
            } catch (NumberFormatException e) {
                Log.e("CustomerProfile", "userID không phải là số: " + userId);
                userId = null;
            }
        } else {
            Log.e("CustomerProfile", "userId is null or empty: '" + userId + "'");
            userId = null;
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
        // Test với userId cố định trước
        Log.d("CustomerProfile", "Loading user info with test userId");
        
        // Sử dụng userId = 1 để test
        int testUserId = 1;
        Log.d("CustomerProfile", "Using test userId: " + testUserId);
        
        api.getUserById(testUserId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> res) {
                if (res.isSuccessful() && res.body() != null) {
                    currentUser = res.body();
                    displayUserInfo(currentUser);
                    
                    // Cập nhật userId từ response để sử dụng cho update
                    if (currentUser.getUserID() != 0) {
                        userId = String.valueOf(currentUser.getUserID());
                        Log.d("CustomerProfile", "Updated userId from API: " + userId);
                    }
                } else {
                    Log.e("CustomerProfile", "API error: " + res.code());
                    Toast.makeText(CustomerProfileActivity.this,
                            "Không tải được thông tin (" + res.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("CustomerProfile", "Network error: " + t.getMessage());
                Toast.makeText(CustomerProfileActivity.this, "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        // Chỉ cập nhật tên nếu API trả về tên khác và không rỗng
        String apiUsername = nz(user.getUsername());
        if (!apiUsername.isEmpty() && !apiUsername.equals("Người dùng")) {
            tvUsername.setText(apiUsername);
        }
        
        tvCurrentEmail.setText(nz(user.getEmail()));
        edtEmail.setText(nz(user.getEmail()));
        edtPhone.setText(nz(user.getPhoneNumber()));
        edtAddress.setText(nz(user.getAddress()));
    }

    private void saveProfileChanges() {
        Log.d("CustomerProfile", "=== SAVE PROFILE CHANGES ===");
        Log.d("CustomerProfile", "Current userId: " + userId);
        
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();

        Log.d("CustomerProfile", "Email: " + email + ", Phone: " + phone + ", Address: " + address);

        // Validation
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            return;
        }
        if (phone.isEmpty()) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            return;
        }
        
        // Validation phone number format
        if (phone.length() < 10 || phone.length() > 15) {
            edtPhone.setError("Số điện thoại phải có từ 10-15 số");
            return;
        }
        
        // Validation email format
        if (!email.contains("@") || !email.contains(".")) {
            edtEmail.setError("Email không đúng định dạng");
            return;
        }

        // Kiểm tra userId
        if (userId == null || userId.isEmpty()) {
            Log.e("CustomerProfile", "userId is null or empty, cannot save");
            Toast.makeText(this, "Không tìm thấy ID người dùng. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
            return;
        }

        // Tạo request body đơn giản để test
        String username = tvUsername.getText().toString();
        UserUpdateRequest body = new UserUpdateRequest(phone, address, "User", email, username);
        Log.d("CustomerProfile", "Request data: phone=" + phone + ", address=" + address + ", role=User, email=" + email + ", username=" + username);

        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        try {
            int userIdInt = Integer.parseInt(userId.trim());
            Log.d("CustomerProfile", "Calling API updateUser with userId: " + userIdInt);
            
            api.updateUser(userIdInt, body).enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> res) {
                    Log.d("CustomerProfile", "Update API response: " + res.code());
                    Log.d("CustomerProfile", "Response body: " + res.body());
                    Log.d("CustomerProfile", "Response error body: " + res.errorBody());
                    
                    btnSave.setEnabled(true);
                    btnSave.setText("LƯU THÔNG TIN");
                    
                    if (res.isSuccessful()) {
                        Log.d("CustomerProfile", "Update successful");
                        Toast.makeText(CustomerProfileActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                        // Cập nhật lại thông tin hiển thị
                        if (res.body() != null) {
                            displayUserInfo(res.body());
                        }
                    } else {
                        Log.e("CustomerProfile", "Update failed with code: " + res.code());
                        Log.e("CustomerProfile", "Error details: " + res.message());
                        
                        // Đọc error body chi tiết
                        try {
                            String errorBody = res.errorBody() != null ? res.errorBody().string() : "No error body";
                            Log.e("CustomerProfile", "Error body content: " + errorBody);
                            Log.e("CustomerProfile", "Response headers: " + res.headers());
                        } catch (Exception e) {
                            Log.e("CustomerProfile", "Error reading error body: " + e.getMessage());
                        }
                        
                        Toast.makeText(CustomerProfileActivity.this, "Lỗi: " + res.code() + " - " + res.message(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Lỗi định dạng ID người dùng: " + userId, Toast.LENGTH_LONG).show();
        }
    }

    private void changePassword() {
        Log.d("CustomerProfile", "=== CHANGE PASSWORD ===");
        Log.d("CustomerProfile", "Current userId: " + userId);
        
        String currentPassword = edtCurrentPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        Log.d("CustomerProfile", "Password fields filled: current=" + !currentPassword.isEmpty() + 
                ", new=" + !newPassword.isEmpty() + ", confirm=" + !confirmPassword.isEmpty());

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

        // Kiểm tra userId
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID người dùng. Vui lòng đăng nhập lại!", Toast.LENGTH_LONG).show();
            return;
        }

        // Tạo request body theo API spec
        ChangePasswordRequest body = new ChangePasswordRequest(newPassword);

        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Đang đổi...");

        try {
            int userIdInt = Integer.parseInt(userId.trim());
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

    private void debugSharedPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        Log.d("CustomerProfile", "=== DEBUG SHARED PREFERENCES ===");
        Log.d("CustomerProfile", "All keys: " + prefs.getAll().keySet());
        for (String key : prefs.getAll().keySet()) {
            Object value = prefs.getAll().get(key);
            Log.d("CustomerProfile", key + " = " + value + " (type: " + value.getClass().getSimpleName() + ")");
        }
        Log.d("CustomerProfile", "=== END DEBUG ===");
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}