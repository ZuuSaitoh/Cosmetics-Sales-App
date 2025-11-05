package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.NotificationService;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.BroadcastNotificationRequest;
import com.example.myapplication.network.dto.NotificationDTO;
import com.example.myapplication.network.dto.NotificationRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Fragment cho Admin gửi notification đến users
 * Notification sẽ hiển thị ở tab "Khuyến mãi" của user
 */
public class AdminNotificationFragment extends Fragment {
    
    private static final String TAG = "AdminNotification";
    
    private EditText etUserID;
    private EditText etMessage;
    private RadioGroup rgNotificationType;
    private RadioButton rbPromotion, rbSystem;
    private Button btnSendNotification;
    private Button btnFlashSale, btnNewProduct, btnBirthday, btnAbandonedCart;
    private Button btnLogout;
    
    private NotificationService notificationService;
    
    public AdminNotificationFragment() {
        // Required empty public constructor
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_notification, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize service
        notificationService = ApiClient.getRetrofit(requireContext()).create(NotificationService.class);
        
        // Initialize views
        initViews(view);
        
        // Setup listeners
        setupListeners();
    }
    
    private void initViews(View view) {
        etUserID = view.findViewById(R.id.et_user_id);
        etMessage = view.findViewById(R.id.et_message);
        rgNotificationType = view.findViewById(R.id.rg_notification_type);
        rbPromotion = view.findViewById(R.id.rb_promotion);
        rbSystem = view.findViewById(R.id.rb_system);
        btnSendNotification = view.findViewById(R.id.btn_send_notification);
        
        // Template buttons
        btnFlashSale = view.findViewById(R.id.btn_flash_sale);
        btnNewProduct = view.findViewById(R.id.btn_new_product);
        btnBirthday = view.findViewById(R.id.btn_birthday);
        btnAbandonedCart = view.findViewById(R.id.btn_abandoned_cart);
        
        // Logout button
        btnLogout = view.findViewById(R.id.btn_logout);
        
        // Default: PROMOTION
        rbPromotion.setChecked(true);
    }
    
    private void setupListeners() {
        // Template buttons
        btnFlashSale.setOnClickListener(v -> {
            etMessage.setText("🔥 FLASH SALE SỐC! Giảm 50% tất cả Serum dưỡng ẩm chỉ trong 2 giờ!");
            rbPromotion.setChecked(true);
        });
        
        btnNewProduct.setOnClickListener(v -> {
            etMessage.setText("NEW! Khám phá ngay bộ sưu tập son môi mới nhất đang gây sốt!");
            rbPromotion.setChecked(true);
        });
        
        btnBirthday.setOnClickListener(v -> {
            etMessage.setText("🎉 Chúc mừng sinh nhật! Bạn có một mã giảm 20% đang chờ.");
            rbPromotion.setChecked(true);
        });
        
        btnAbandonedCart.setOnClickListener(v -> {
            etMessage.setText("Bạn quên mất sản phẩm trong giỏ hàng rồi! Hoàn tất đơn hàng ngay với ưu đãi đặc biệt.");
            rbSystem.setChecked(true);
        });
        
        // Send button
        btnSendNotification.setOnClickListener(v -> sendNotification());
        
        // Logout button
        btnLogout.setOnClickListener(v -> logout());
    }
    
    private void logout() {
        // Clear auth data
        com.example.myapplication.auth.AuthManager authManager = new com.example.myapplication.auth.AuthManager(requireContext());
        authManager.clear();
        
        Log.d(TAG, "Admin logged out");
        showToast("Đã đăng xuất");
        
        // Navigate to login screen
        android.content.Intent intent = new android.content.Intent(requireContext(), ActivityLogin.class);
        intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
    
    private void sendNotification() {
        // Validate inputs
        String userIDStr = etUserID.getText().toString().trim();
        String message = etMessage.getText().toString().trim();
        
        if (userIDStr.isEmpty()) {
            showToast("Vui lòng nhập User ID");
            return;
        }
        
        if (message.isEmpty()) {
            showToast("Vui lòng nhập nội dung thông báo");
            return;
        }
        
        // Parse userID
        Long userId;
        try {
            userId = Long.parseLong(userIDStr);
        } catch (NumberFormatException e) {
            showToast("User ID không hợp lệ");
            return;
        }
        
        // Get notification type (chỉ để hiển thị trong log, không gửi lên server)
        String notificationType = rbPromotion.isChecked() ? "PROMOTION" : "SYSTEM";
        
        // Disable button
        btnSendNotification.setEnabled(false);
        btnSendNotification.setText("Đang gửi...");
        
        // Check if broadcasting to all users
        if (userId == 0) {
            // Sử dụng API mới: /notifications/send-notification-to-all
            sendNotificationToAll(message, notificationType);
        } else {
            // Gửi cho user cụ thể
            sendNotificationToUser(userId, message, notificationType);
        }
    }
    
    /**
     * Gửi notification cho TẤT CẢ users
     */
    private void sendNotificationToAll(String message, String notificationType) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "📢 BROADCASTING notification to ALL USERS");
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Type (UI only): " + notificationType);
        
        // Create request - CHỈ CẦN message
        BroadcastNotificationRequest request = new BroadcastNotificationRequest(message);
        
        // Debug: Log request với Gson để thấy exact JSON
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String requestJson = gson.toJson(request);
        Log.d(TAG, "📤 REQUEST");
        Log.d(TAG, "Endpoint: POST /notifications/send-notification-to-all");
        Log.d(TAG, "Body: " + requestJson);
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Call API
        Call<ApiResponse<String>> call = notificationService.sendNotificationToAll(request);
        
        call.enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<String>> call, 
                                 @NonNull Response<ApiResponse<String>> response) {
                
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Log.d(TAG, "📥 RESPONSE - BROADCAST TO ALL");
                Log.d(TAG, "HTTP Code: " + response.code());
                Log.d(TAG, "Is Successful: " + response.isSuccessful());
                
                // Re-enable button
                btnSendNotification.setEnabled(true);
                btnSendNotification.setText("📩 Gửi thông báo");
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<String> apiResponse = response.body();
                    
                    Log.d(TAG, "Response Code: " + apiResponse.getCode());
                    Log.d(TAG, "Response Message: " + apiResponse.getMessage());
                    Log.d(TAG, "Result: " + apiResponse.getResult());
                    
                    // Kiểm tra success (có thể là code 0 hoặc 9999)
                    if (apiResponse.getCode() == 0 || apiResponse.getCode() == 9999) {
                        Log.d(TAG, "✅ Broadcast successful!");
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        
                        showToast("✅ Đã gửi thông báo cho TẤT CẢ users!");
                        
                        // Clear form
                        etUserID.setText("");
                        etMessage.setText("");
                        rbPromotion.setChecked(true);
                        
                    } else {
                        String error = "Lỗi: " + apiResponse.getMessage();
                        Log.e(TAG, error);
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        showToast(error);
                    }
                } else {
                    handleErrorResponse(response);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<String>> call, @NonNull Throwable t) {
                handleFailure(t);
            }
        });
    }
    
    /**
     * Gửi notification cho user cụ thể
     */
    private void sendNotificationToUser(Long userId, String message, String notificationType) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.d(TAG, "📤 Sending notification to specific user: " + userId);
        Log.d(TAG, "Message: " + message);
        Log.d(TAG, "Type (UI only): " + notificationType);
        
        // Create request - CHỈ GỬI userID và message theo API spec
        NotificationRequest request = new NotificationRequest(userId, message);
        
        // Debug: Log request với Gson để thấy exact JSON
        com.google.gson.Gson gson = new com.google.gson.Gson();
        String requestJson = gson.toJson(request);
        Log.d(TAG, "📤 REQUEST");
        Log.d(TAG, "Endpoint: POST /notifications/create");
        Log.d(TAG, "Body: " + requestJson);
        Log.d(TAG, "UserID value: " + request.getUserId());
        Log.d(TAG, "Message value: " + request.getMessage());
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Call API
        Call<ApiResponse<NotificationDTO>> call = notificationService.createNotification(request);
        
        call.enqueue(new Callback<ApiResponse<NotificationDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<NotificationDTO>> call, 
                                 @NonNull Response<ApiResponse<NotificationDTO>> response) {
                
                Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                Log.d(TAG, "📥 RESPONSE - SPECIFIC USER");
                Log.d(TAG, "HTTP Code: " + response.code());
                Log.d(TAG, "Is Successful: " + response.isSuccessful());
                
                // Re-enable button
                btnSendNotification.setEnabled(true);
                btnSendNotification.setText("📩 Gửi thông báo");
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<NotificationDTO> apiResponse = response.body();
                    
                    Log.d(TAG, "Response Code: " + apiResponse.getCode());
                    Log.d(TAG, "Response Message: " + apiResponse.getMessage());
                    Log.d(TAG, "Has Result: " + (apiResponse.getResult() != null));
                    
                    if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                        Log.d(TAG, "✅ Notification sent successfully - ID: " + apiResponse.getResult().getNotificationId());
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        
                        showToast("✅ Đã gửi thông báo thành công!");
                        
                        // Clear form
                        etUserID.setText("");
                        etMessage.setText("");
                        rbPromotion.setChecked(true);
                        
                    } else {
                        String error = "Lỗi: " + apiResponse.getMessage();
                        Log.e(TAG, error);
                        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                        showToast(error);
                    }
                } else {
                    handleErrorResponse(response);
                }
            }
            
            @Override
            public void onFailure(@NonNull Call<ApiResponse<NotificationDTO>> call, @NonNull Throwable t) {
                handleFailure(t);
            }
        });
    }
    
    /**
     * Xử lý error response chung
     */
    private void handleErrorResponse(Response<?> response) {
        // Log error body để debug
        String errorBody = "";
        String errorMessage = "Lỗi gửi thông báo: " + response.code();
        
        Log.e(TAG, "❌ HTTP ERROR " + response.code());
        
        try {
            if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
                Log.e(TAG, "Error Body: " + errorBody);
                
                // Parse error body để hiển thị message rõ ràng
                if (errorBody.contains("\"code\":1005") || errorBody.contains("Users not existed")) {
                    String userIdStr = etUserID.getText().toString().trim();
                    errorMessage = "User ID " + userIdStr + " không tồn tại! Vui lòng kiểm tra lại User ID.";
                    Log.e(TAG, "User ID " + userIdStr + " not found in database");
                } else if (errorBody.contains("message")) {
                    // Try to extract message from JSON
                    try {
                        int messageStart = errorBody.indexOf("\"message\":\"") + 11;
                        int messageEnd = errorBody.indexOf("\"", messageStart);
                        if (messageStart > 10 && messageEnd > messageStart) {
                            String extractedMsg = errorBody.substring(messageStart, messageEnd);
                            errorMessage = "Lỗi: " + extractedMsg;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Cannot extract error message", e);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Cannot read error body", e);
        }
        
        Log.e(TAG, "Final error message: " + errorMessage);
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        showToast(errorMessage);
        
        // Re-enable button
        btnSendNotification.setEnabled(true);
        btnSendNotification.setText("📩 Gửi thông báo");
    }
    
    /**
     * Xử lý network failure chung
     */
    private void handleFailure(Throwable t) {
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        Log.e(TAG, "❌ NETWORK FAILURE");
        Log.e(TAG, "Error: " + t.getClass().getSimpleName());
        Log.e(TAG, "Message: " + t.getMessage());
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        
        // Re-enable button
        btnSendNotification.setEnabled(true);
        btnSendNotification.setText("📩 Gửi thông báo");
        
        String error = "Lỗi kết nối: " + t.getMessage();
        showToast(error);
    }
    
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}


