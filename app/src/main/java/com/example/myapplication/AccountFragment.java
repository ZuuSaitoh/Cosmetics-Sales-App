package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    
    private AuthManager authManager;
    private UserService userService;
    
    // Views
    private ImageView avatarImageView;
    private TextView usernameTextView;
    private TextView roleTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView addressTextView;
    private Button logoutButton;
    private ProgressBar loadingProgressBar;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        
        // Khởi tạo services
        authManager = new AuthManager(getContext());
        userService = ApiClient.getRetrofit(getContext()).create(UserService.class);
        
        // Ánh xạ views
        initializeViews(view);
        
        // Thiết lập sự kiện đăng xuất và avatar click
        setupLogout();
        setupAvatarClick();
        
        // Load thông tin người dùng từ API
        loadUserInfo();
        
        return view;
    }
    
    private void initializeViews(View view) {
        avatarImageView = view.findViewById(R.id.avatarImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        roleTextView = view.findViewById(R.id.roleTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
    }
    
    private void loadUserInfo() {
        // Hiển thị loading
        loadingProgressBar.setVisibility(View.VISIBLE);
        
        // Force refresh userId từ token để đảm bảo có userId mới nhất
        Log.d("AccountFragment", "Force refreshing userId from token");
        Long userId = authManager.refreshUserId();
        
        Log.d("AccountFragment", "=== LOADING USER INFO ===");
        Log.d("AccountFragment", "Loading user info for userId: " + userId);
        Log.d("AccountFragment", "Token exists: " + (authManager.getToken() != null));
        Log.d("AccountFragment", "Role: " + authManager.getRole());
        
        // Log token để debug
        String token = authManager.getToken();
        if (token != null) {
            Log.d("AccountFragment", "Token preview: " + token.substring(0, Math.min(50, token.length())) + "...");
            Log.d("AccountFragment", "Is JWT token: " + com.example.myapplication.auth.JwtDecoder.isJwtToken(token));
            
            // Debug JWT token
            com.example.myapplication.auth.JwtDecoder.JwtInfo jwtInfo = com.example.myapplication.auth.JwtDecoder.getJwtInfo(token);
            if (jwtInfo != null) {
                Log.d("AccountFragment", "JWT Info - UserID: " + jwtInfo.userId);
                Log.d("AccountFragment", "JWT Payload: " + jwtInfo.payload);
            }
        }
        
        if (userId == null) {
            Log.w("AccountFragment", "No userId found in AuthManager, trying fallback methods");
            
            // Thử các phương pháp fallback
            userId = tryGetUserIdFromToken();
            
            if (userId == null) {
                Log.w("AccountFragment", "All fallback methods failed, showing fallback info");
                loadingProgressBar.setVisibility(View.GONE);
                showFallbackInfo();
                Toast.makeText(getContext(), "Không thể xác định ID người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // Tạo final variable cho inner class
        final Long finalUserId = userId;
        
        Call<User> call = userService.getUserById(finalUserId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                loadingProgressBar.setVisibility(View.GONE);
                
                Log.d("AccountFragment", "=== API RESPONSE ===");
                Log.d("AccountFragment", "API Response - Code: " + response.code() + ", Success: " + response.isSuccessful());
                Log.d("AccountFragment", "Requested userId: " + finalUserId);
                
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d("AccountFragment", "User data loaded:");
                    Log.d("AccountFragment", "  - UserID: " + user.getUserID());
                    Log.d("AccountFragment", "  - Username: " + user.getUsername());
                    Log.d("AccountFragment", "  - Role: " + user.getRole());
                    Log.d("AccountFragment", "  - Email: " + user.getEmail());
                    
                    // Kiểm tra userId có khớp không
                    if (!finalUserId.equals(user.getUserID())) {
                        Log.w("AccountFragment", "WARNING: Requested userId (" + finalUserId + ") != Response userId (" + user.getUserID() + ")");
                    }
                    
                    updateUserInfo(user);
                } else {
                    Log.e("AccountFragment", "API call failed - Code: " + response.code() + ", Message: " + response.message());
                    // Fallback: hiển thị thông tin từ AuthManager
                    showFallbackInfo();
                    Toast.makeText(getContext(), "Không thể tải thông tin người dùng (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                loadingProgressBar.setVisibility(View.GONE);
                Log.e("AccountFragment", "API call failed", t);
                // Fallback: hiển thị thông tin từ AuthManager
                showFallbackInfo();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Thử lấy userID từ token hoặc các phương pháp khác
     */
    private Long tryGetUserIdFromToken() {
        // Phương pháp 1: Thử decode token để lấy userID
        // (Trong thực tế, bạn có thể decode JWT token)
        
        // Phương pháp 2: Kiểm tra role để suy đoán userID
        String role = authManager.getRole();
        Log.d("AccountFragment", "Trying to get userId from role: " + role);
        
        // KHÔNG hardcode userID = 1 nữa
        // Thay vào đó, hiển thị thông báo lỗi rõ ràng
        Log.w("AccountFragment", "Cannot determine userID from available data");
        return null; // Trả về null để hiển thị fallback info
    }
    
    private void updateUserInfo(User user) {
        usernameTextView.setText(user.getUsername());
        roleTextView.setText(user.getRole());
        emailTextView.setText(user.getEmail());
        phoneTextView.setText(user.getPhoneNumber());
        addressTextView.setText(user.getAddress());
    }
    
    private void showFallbackInfo() {
        String token = authManager.getToken();
        String role = authManager.getRole();
        
        if (token != null && !token.isEmpty()) {
            usernameTextView.setText("Người dùng");
            roleTextView.setText(role != null ? role : "User");
            emailTextView.setText("Không có thông tin");
            phoneTextView.setText("Không có thông tin");
            addressTextView.setText("Không có thông tin");
        } else {
            usernameTextView.setText("Chưa đăng nhập");
            roleTextView.setText("Guest");
            emailTextView.setText("Chưa đăng nhập");
            phoneTextView.setText("Chưa đăng nhập");
            addressTextView.setText("Chưa đăng nhập");
        }
    }
    
    private void setupLogout() {
        logoutButton.setOnClickListener(v -> {
            // Xóa token và thông tin đăng nhập
            authManager.clear();
            
            // Hiển thị thông báo
            Toast.makeText(getContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
            
            // Chuyển về ActivityWelcome
            Intent intent = new Intent(getContext(), ActivityWelcome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            
            // Đóng MainActivity
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }
    
    private void setupAvatarClick() {
        avatarImageView.setOnClickListener(v -> {
            Log.d("AccountFragment", "Avatar clicked, opening update profile");
            Intent intent = new Intent(getContext(), ActivityUpdateProfile.class);
            startActivityForResult(intent, 1001); // Request code để nhận kết quả
        });
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK) {
            // User đã update thành công, reload thông tin
            Log.d("AccountFragment", "User updated, reloading info");
            loadUserInfo();
        }
    }
}
