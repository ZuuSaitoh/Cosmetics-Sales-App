package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import androidx.fragment.app.Fragment;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Order;
import com.example.myapplication.model.User;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.OrderService;
import com.example.myapplication.network.UserService;
import com.example.myapplication.network.dto.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {
    
    private AuthManager authManager;
    private UserService userService;
    private OrderService orderService;
    
    // Views
    private ImageView avatarImageView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private MaterialButton logoutButton;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private LinearLayout authButtonsContainer;
    private ProgressBar loadingProgressBar;
    private TextView wach_list_orders;
    private TextView badgeProcessing;
    private TextView badgeShipped;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        
        // Khởi tạo services
        authManager = new AuthManager(getContext());
        userService = ApiClient.getRetrofit(getContext()).create(UserService.class);
        orderService = ApiClient.getRetrofit(getContext()).create(OrderService.class);
        

        initializeViews(view);
        setupWatchListOrdersClick();
        setupLogout();
        setupLoginRegister();
        setupAvatarClick();
        updateUIForAuthState();
        String token = authManager.getToken();
        if (token != null && !token.isEmpty()) {
            loadUserInfo();
            loadOrderCounts();
        } else {
            // Khi chưa đăng nhập, chỉ hiển thị thông tin fallback và ẩn loading
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            showFallbackInfo();
        }
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Reload order counts khi fragment được resume
        String token = authManager.getToken();
        if (token != null && !token.isEmpty()) {
            loadOrderCounts();
        }
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
        wach_list_orders = view.findViewById(R.id.wach_list_orders);
        badgeProcessing = view.findViewById(R.id.badge_processing);
        badgeShipped = view.findViewById(R.id.badge_shipped);
        
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
                // Clear cart data trước khi logout
                if (getContext() != null) {
                    authManager.clearCartOnLogout(getContext());
                }
                
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
                // Kiểm tra xem user đã đăng nhập chưa
                String token = authManager.getToken();
                if (token == null || token.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng đăng nhập để cập nhật thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }
                
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
            loadOrderCounts();
        } else if ((requestCode == 1002 || requestCode == 1003) && resultCode == getActivity().RESULT_OK) {
            // Đăng nhập hoặc đăng ký thành công, reload thông tin
            Log.d("AccountFragment", "Login/Register successful, reloading info");
            loadUserInfo();
            loadOrderCounts();
        }
    }
    
    /** Load số lượng đơn hàng theo status và cập nhật badge */
    private void loadOrderCounts() {
        Long userId = authManager.getUserId();
        if (userId == null || orderService == null) {
            return;
        }
        
        // Load đơn hàng của user
        orderService.getOrdersByUserId(userId).enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body().getResult();
                    if (orders != null) {
                        int processingCount = 0;
                        int shippedCount = 0;
                        
                        for (Order order : orders) {
                            if (order != null && order.getOrderStatus() != null) {
                                String status = order.getOrderStatus().toLowerCase();
                                if (status.contains("processing")) {
                                    processingCount++;
                                } else if (status.contains("shipped")) {
                                    shippedCount++;
                                }
                            }
                        }
                        
                        updateBadge(badgeProcessing, processingCount);
                        updateBadge(badgeShipped, shippedCount);
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Log.e("AccountFragment", "Failed to load order counts", t);
            }
        });
    }
    
    /** Cập nhật badge với số lượng */
    private void updateBadge(TextView badge, int count) {
        if (badge != null) {
            if (count > 0) {
                badge.setText(String.valueOf(count));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
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
        setupOrderStatusClickListener(view, R.id.order_status_processing, "Đang xử lý");
        setupOrderStatusClickListener(view, R.id.order_status_shipped, "Đang giao");
        setupOrderStatusClickListener(view, R.id.order_status_delivered, "Đã giao");
        setupOrderStatusClickListener(view, R.id.order_status_cancelled, "Đã hủy");
    }

    private void setupWatchListOrdersClick() {
        if (wach_list_orders != null) {
            wach_list_orders.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), ActivityOrderHistory.class);
                startActivity(intent);
            });
        }
    }





    private void setupOrderStatusClickListener(View root, int viewId, String statusName) {
        View v = root.findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(click -> {
                String statusParam = null;
                if ("Đang xử lý".equalsIgnoreCase(statusName)) {
                    statusParam = "Processing";
                } else if ("Đang giao".equalsIgnoreCase(statusName)) {
                    statusParam = "Shipped";
                } else if ("Đã giao".equalsIgnoreCase(statusName)) {
                    statusParam = "Delivered";
                } else if ("Đã hủy".equalsIgnoreCase(statusName)) {
                    statusParam = "Cancelled";
                }

                Intent intent = new Intent(getContext(), ActivityOrderHistory.class);
                if (statusParam != null) {
                    intent.putExtra("status", statusParam);
                    intent.putExtra("status_name", statusName);
                }
                startActivity(intent);
            });
        }
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
        setupProductInteractionClickListener(view, R.id.purchased_viewed, "Đã xem");
        setupProductInteractionClickListener(view, R.id.purchased_liked, "Yêu thích");
        setupProductInteractionClickListener(view, R.id.purchased_rated, "Đánh giá");
        setupProductInteractionClickListener(view, R.id.purchased_redeem, "Đổi quà");
    }
    
    private void setupProductInteractionClickListener(View root, int viewId, String interactionName) {
        View v = root.findViewById(viewId);
        if (v != null) {
            v.setOnClickListener(click -> {
                Toast.makeText(getContext(), "Sản phẩm đã mua: " + interactionName, Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void setupAccountOptionsClickListeners(View view) {
        setupAccountOptionClickListener(view, R.id.option_personal, "Cá nhân");
        setupAccountOptionClickListener(view, R.id.option_address_book, "Sổ địa chỉ");
        setupAccountOptionClickListener(view, R.id.option_qa, "Hỏi đáp");
        setupAccountOptionClickListener(view, R.id.option_brands, "Thương hiệu");
        setupAccountOptionClickListener(view, R.id.option_new_arrivals, "Sản phẩm mới về");
        setupAccountOptionClickListener(view, R.id.option_best_sellers, "Sản phẩm bán chạy");
        setupAccountOptionClickListener(view, R.id.option_chat, "CSKH");
        setupAccountOptionClickListener(view, R.id.option_loyalty, "Tri ân");
        setupAccountOptionClickListener(view, R.id.option_regulations, "Quy định");
        setupAccountOptionClickListener(view, R.id.option_support, "Hỗ trợ");
        setupAccountOptionClickListener(view, R.id.option_recruitment, "Tuyển dụng");
        setupAccountOptionClickListener(view, R.id.option_voucher, "Voucher trải nghiệm");
    }
    
    private void setupAccountOptionClickListener(View root, int viewId, String optionName) {
        View optionView = root.findViewById(viewId);
        if (optionView != null) {
            optionView.setOnClickListener(v -> {
                if (viewId == R.id.option_address_book) {
                    Intent intent = new Intent(getContext(), AddressBookActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), optionName + " đang được phát triển", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
