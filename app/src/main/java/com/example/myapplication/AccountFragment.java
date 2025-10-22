package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

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
    private TextView emailTextView;
    private MaterialButton logoutButton;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private LinearLayout authButtonsContainer;
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
        setupLoginRegister();
        setupAvatarClick();
        
        // Cập nhật giao diện dựa trên trạng thái đăng nhập
        updateUIForAuthState();
        
        // Load thông tin người dùng từ API chỉ khi đã đăng nhập
        String token = authManager.getToken();
        if (token != null && !token.isEmpty()) {
            loadUserInfo();
        } else {
            // Khi chưa đăng nhập, chỉ hiển thị thông tin fallback và ẩn loading
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            showFallbackInfo();
        }
        
        return view;
    }
    
    private void initializeViews(View view) {
        avatarImageView = view.findViewById(R.id.avatarImageView);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        loginButton = view.findViewById(R.id.loginButton);
        registerButton = view.findViewById(R.id.registerButton);
        authButtonsContainer = view.findViewById(R.id.authButtonsContainer);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        
        // Setup click listeners for new sections
        setupOrderSectionClickListeners(view);
        setupPurchasedProductsClickListeners(view);
        setupAccountOptionsClickListeners(view);
    }
    
    private void loadUserInfo() {
        // Hiển thị loading
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        
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
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                showFallbackInfo();
                // Toast.makeText(getContext(), "Không thể xác định ID người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        // Tạo final variable cho inner class
        final Long finalUserId = userId;
        
        Call<User> call = userService.getUserById(finalUserId);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                
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
                    // Toast.makeText(getContext(), "Không thể tải thông tin người dùng (Code: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                Log.e("AccountFragment", "API call failed", t);
                // Fallback: hiển thị thông tin từ AuthManager
                showFallbackInfo();
                // Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        if (usernameTextView != null) usernameTextView.setText(user.getUsername());
        if (emailTextView != null) emailTextView.setText(user.getEmail());
    }
    
    private void showFallbackInfo() {
        String token = authManager.getToken();
        String role = authManager.getRole();
        
        if (token != null && !token.isEmpty()) {
            if (usernameTextView != null) usernameTextView.setText("Người dùng");
            if (emailTextView != null) emailTextView.setText("Không có thông tin");
        } else {
            if (usernameTextView != null) usernameTextView.setText("Chưa đăng nhập");
            if (emailTextView != null) emailTextView.setText("Chưa đăng nhập");
        }
    }
    
    private void setupLogout() {
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                // Xóa token và thông tin đăng nhập
                authManager.clear();
                
                // Hiển thị thông báo
                Toast.makeText(getContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();
                
                // Cập nhật giao diện
                updateUIForAuthState();
                
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
    }
    
    private void setupLoginRegister() {
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ActivityLogin.class);
                startActivityForResult(intent, 1002); // Request code để nhận kết quả
            });
        }
        
        if (registerButton != null) {
            registerButton.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ActivitySignUp.class);
                startActivityForResult(intent, 1003); // Request code để nhận kết quả
            });
        }
    }
    
    private void updateUIForAuthState() {
        String token = authManager.getToken();
        boolean isLoggedIn = token != null && !token.isEmpty();
        
        if (isLoggedIn) {
            // Đã đăng nhập: hiển thị thông tin người dùng và nút logout, ẩn nút đăng nhập/đăng ký
            if (logoutButton != null) {
                logoutButton.setVisibility(View.VISIBLE);
            }
            if (authButtonsContainer != null) {
                authButtonsContainer.setVisibility(View.GONE);
            }
        } else {
            // Chưa đăng nhập: ẩn nút logout, hiển thị nút đăng nhập/đăng ký
            if (logoutButton != null) {
                logoutButton.setVisibility(View.GONE);
            }
            if (authButtonsContainer != null) {
                authButtonsContainer.setVisibility(View.VISIBLE);
            }
            // Đảm bảo ẩn loading khi chưa đăng nhập
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
        }
    }
    
    private void setupAvatarClick() {
        if (avatarImageView != null) {
            avatarImageView.setOnClickListener(v -> {
                Log.d("AccountFragment", "Avatar clicked, opening update profile");
                Intent intent = new Intent(getContext(), ActivityUpdateProfile.class);
                startActivityForResult(intent, 1001); // Request code để nhận kết quả
            });
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK) {
            // User đã update thành công, reload thông tin
            Log.d("AccountFragment", "User updated, reloading info");
            loadUserInfo();
        } else if ((requestCode == 1002 || requestCode == 1003) && resultCode == getActivity().RESULT_OK) {
            // Đăng nhập hoặc đăng ký thành công, reload thông tin
            Log.d("AccountFragment", "Login/Register successful, reloading info");
            loadUserInfo();
        }
    }
    
    private void setupOrderSectionClickListeners(View view) {
        // "Xem tất cả đơn hàng" click listener
        View orderSection = view.findViewById(R.id.orders_section);
        if (orderSection != null) {
            orderSection.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Tính năng xem đơn hàng đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Individual order status click listeners
        setupOrderStatusClickListener(view, "new_order", "Mới đặt");
        setupOrderStatusClickListener(view, "processing", "Đang xử lý");
        setupOrderStatusClickListener(view, "successful", "Thành công");
        setupOrderStatusClickListener(view, "cancelled", "Đã hủy");
    }
    
    private void setupOrderStatusClickListener(View view, String status, String statusName) {
        // This would be implemented with actual order status filtering
        // For now, just show a toast when clicked
        // Note: Individual order status buttons would need specific IDs to implement this properly
    }
    
    private void setupPurchasedProductsClickListeners(View view) {
        // "Xem tất cả" click listener
        View purchasedSection = view.findViewById(R.id.purchased_products_section);
        if (purchasedSection != null) {
            purchasedSection.setOnClickListener(v -> {
                Toast.makeText(getContext(), "Tính năng xem sản phẩm đã mua đang được phát triển", Toast.LENGTH_SHORT).show();
            });
        }
        
        // Individual product interaction click listeners
        setupProductInteractionClickListener(view, "viewed", "Đã xem");
        setupProductInteractionClickListener(view, "liked", "Yêu thích");
        setupProductInteractionClickListener(view, "rated", "Đánh giá");
        setupProductInteractionClickListener(view, "redeem", "Đổi quà");
    }
    
    private void setupProductInteractionClickListener(View view, String interaction, String interactionName) {
        // This would be implemented with actual product interaction filtering
        // For now, just show a toast when clicked
        // Note: Individual product interaction buttons would need specific IDs to implement this properly
    }
    
    private void setupAccountOptionsClickListeners(View view) {
        // Account management options click listeners
        setupAccountOptionClickListener(view, "personal", "Cá nhân");
        setupAccountOptionClickListener(view, "address_book", "Sổ địa chỉ");
        setupAccountOptionClickListener(view, "qa", "Hỏi đáp");
        setupAccountOptionClickListener(view, "brands", "Thương hiệu");
        setupAccountOptionClickListener(view, "new_arrivals", "Sản phẩm mới về");
        setupAccountOptionClickListener(view, "best_sellers", "Sản phẩm bán chạy");
        setupAccountOptionClickListener(view, "chat", "CSKH");
        setupAccountOptionClickListener(view, "loyalty", "Tri ân");
        setupAccountOptionClickListener(view, "regulations", "Quy định");
        setupAccountOptionClickListener(view, "support", "Hỗ trợ");
        setupAccountOptionClickListener(view, "recruitment", "Tuyển dụng");
        setupAccountOptionClickListener(view, "voucher", "Voucher trải nghiệm");
    }
    
    private void setupAccountOptionClickListener(View view, String option, String optionName) {
        // This would be implemented with actual navigation to specific features
        // For now, just show a toast when clicked
        // Note: Individual account option buttons would need specific IDs to implement this properly
    }
}
