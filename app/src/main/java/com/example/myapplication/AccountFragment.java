package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.auth.AuthManager;

public class AccountFragment extends Fragment {
    
    private AuthManager authManager;
    private TextView userInfoTextView;
    private Button logoutButton;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        
        // Khởi tạo AuthManager
        authManager = new AuthManager(getContext());
        
        // Ánh xạ views
        userInfoTextView = view.findViewById(R.id.userInfoTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        
        // Thiết lập thông tin người dùng
        setupUserInfo();
        
        // Thiết lập sự kiện đăng xuất
        setupLogout();
        
        return view;
    }
    
    private void setupUserInfo() {
        String token = authManager.getToken();
        String role = authManager.getRole();
        
        if (token != null && !token.isEmpty()) {
            // Hiển thị thông tin đăng nhập (có thể ẩn token vì lý do bảo mật)
            String userInfo = "Đã đăng nhập\n";
            if (role != null && !role.isEmpty()) {
                userInfo += "Vai trò: " + role;
            }
            userInfoTextView.setText(userInfo);
        } else {
            userInfoTextView.setText("Chưa đăng nhập");
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
}
