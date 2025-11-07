package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.model.Conversation;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ConversationService;
import com.example.myapplication.network.dto.ApiResponse;

public class ChatFragment extends Fragment {
    
    private Button btnStartChat;
    private TextView textWelcome;
    private TextView textDescription;
    private ImageView imageChatIcon;
    private AuthManager authManager;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        
        // Khởi tạo AuthManager
        authManager = new AuthManager(getContext());
        
        initViews(view);
        setupClickListeners();
        checkLoginStatus();
        
        return view;
    }
    
    private void initViews(View view) {
        btnStartChat = view.findViewById(R.id.btn_start_chat);
        textWelcome = view.findViewById(R.id.text_welcome);
        textDescription = view.findViewById(R.id.text_description);
        imageChatIcon = view.findViewById(R.id.image_chat_icon);
    }
    
    private void setupClickListeners() {
        btnStartChat.setOnClickListener(v -> {
            // Kiểm tra đăng nhập trước khi mở chat
            if (authManager.isLoggedIn()) {
                // Đã đăng nhập -> Mở ChatActivity
                Intent intent = new Intent(getContext(), ChatActivity.class);
                startActivity(intent);
            } else {
                // Chưa đăng nhập -> Chuyển đến login
                Intent intent = new Intent(getContext(), ActivityLogin.class);
                intent.putExtra("redirect_to_chat", true); // Flag để biết cần redirect về chat sau khi login
                startActivity(intent);
            }
        });
    }
    
    private void checkLoginStatus() {
        if (authManager.isLoggedIn()) {
            // Đã đăng nhập
            textWelcome.setText("Chào mừng trở lại!");
            textDescription.setText("Nhấn vào nút bên dưới để bắt đầu trò chuyện với đội ngũ hỗ trợ khách hàng của chúng tôi.");
            btnStartChat.setText("Bắt đầu chat");
            // Kiểm tra nếu đã có hội thoại thì vào thẳng khung chat
            Long userId = authManager.getUserId();
            if (userId != null) {
                ConversationService service = ApiClient.getRetrofit(requireContext()).create(ConversationService.class);
                service.getConversationByUser(userId).enqueue(new retrofit2.Callback<ApiResponse<Conversation>>() {
                    @Override
                    public void onResponse(retrofit2.Call<ApiResponse<Conversation>> call, retrofit2.Response<ApiResponse<Conversation>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                            // Đã có cuộc trò chuyện -> mở thẳng ChatActivity
                            startActivity(new android.content.Intent(getContext(), ChatActivity.class));
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ApiResponse<Conversation>> call, Throwable t) {
                        // ignore, giữ UI bắt đầu chat
                    }
                });
            }
        } else {
            // Chưa đăng nhập
            textWelcome.setText("Vui lòng đăng nhập để chat với CSKH");
            textDescription.setText("Bạn cần đăng nhập để có thể sử dụng tính năng chat với đội ngũ hỗ trợ khách hàng.");
            btnStartChat.setText("Đăng nhập để chat");
        }
    }
}