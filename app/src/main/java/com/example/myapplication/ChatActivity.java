package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ChatAdapter;
import com.example.myapplication.model.ChatMessage;
import com.example.myapplication.auth.AuthManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    
    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private ImageButton btnSend, btnBack, btnMore, btnAttach;
    private TextView textContactName, textStatus;
    
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        
        // Kiểm tra đăng nhập trước khi hiển thị chat
        authManager = new AuthManager(this);
        if (!authManager.isLoggedIn()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        loadSampleMessages();
    }
    
    private void initViews() {
        recyclerMessages = findViewById(R.id.recycler_messages);
        editMessage = findViewById(R.id.edit_message);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);
        btnMore = findViewById(R.id.btn_more);
        btnAttach = findViewById(R.id.btn_attach);
        textContactName = findViewById(R.id.text_contact_name);
        textStatus = findViewById(R.id.text_status);
        
        messages = new ArrayList<>();
    }
    
    private void setupRecyclerView() {
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messages);
        recyclerMessages.setAdapter(chatAdapter);
        
        // Đảm bảo RecyclerView có thể scroll
        recyclerMessages.setHasFixedSize(true);
        recyclerMessages.setNestedScrollingEnabled(false);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        btnSend.setOnClickListener(v -> sendMessage());
        
        btnMore.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        btnAttach.setOnClickListener(v -> {
            Toast.makeText(this, "Tính năng đang phát triển", Toast.LENGTH_SHORT).show();
        });
        
        // Gửi tin nhắn khi nhấn Enter
        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }
    
    private void sendMessage() {
        String messageText = editMessage.getText().toString().trim();
        
        if (!TextUtils.isEmpty(messageText)) {
            // Tạo tin nhắn từ người dùng
            ChatMessage userMessage = new ChatMessage(
                messageText,
                true, // isFromUser
                new Date()
            );
            
            messages.add(userMessage);
            chatAdapter.notifyItemInserted(messages.size() - 1);
            recyclerMessages.scrollToPosition(messages.size() - 1);
            
            // Xóa text trong input
            editMessage.setText("");
            
            // Simulate phản hồi từ hỗ trợ (sau 1 giây)
            simulateSupportResponse();
        }
    }
    
    private void simulateSupportResponse() {
        recyclerMessages.postDelayed(() -> {
            String[] responses = {
                "Cảm ơn bạn đã liên hệ! Chúng tôi sẽ hỗ trợ bạn ngay.",
                "Bạn có thể cho tôi biết thêm chi tiết về vấn đề này không?",
                "Tôi hiểu vấn đề của bạn. Để tôi kiểm tra và phản hồi sớm nhất.",
                "Có điều gì khác tôi có thể giúp bạn không?",
                "Chúng tôi sẽ xử lý yêu cầu của bạn trong thời gian sớm nhất."
            };
            
            String randomResponse = responses[(int) (Math.random() * responses.length)];
            
            ChatMessage supportMessage = new ChatMessage(
                randomResponse,
                false, // isFromUser
                new Date()
            );
            
            messages.add(supportMessage);
            chatAdapter.notifyItemInserted(messages.size() - 1);
            recyclerMessages.scrollToPosition(messages.size() - 1);
        }, 1000);
    }
    
    private void loadSampleMessages() {
        // Thêm một số tin nhắn mẫu
        messages.add(new ChatMessage(
            "Xin chào! Tôi có thể giúp gì cho bạn?",
            false,
            new Date(System.currentTimeMillis() - 300000) // 5 phút trước
        ));
        
        messages.add(new ChatMessage(
            "Tôi muốn hỏi về sản phẩm này",
            true,
            new Date(System.currentTimeMillis() - 240000) // 4 phút trước
        ));
        
        messages.add(new ChatMessage(
            "Tất nhiên! Bạn quan tâm đến sản phẩm nào?",
            false,
            new Date(System.currentTimeMillis() - 180000) // 3 phút trước
        ));
        
        // Đảm bảo adapter được notify và scroll xuống cuối
        chatAdapter.notifyDataSetChanged();
        
        // Debug: Log số lượng messages
        android.util.Log.d("ChatActivity", "Messages loaded: " + messages.size());
        
        recyclerMessages.post(() -> {
            if (messages.size() > 0) {
                recyclerMessages.scrollToPosition(messages.size() - 1);
            }
        });
    }
}
