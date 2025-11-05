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
import com.example.myapplication.model.Conversation;
import com.example.myapplication.network.ConversationService;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.ChatMessageService;
import com.example.myapplication.network.dto.ChatMessageDto;
import com.example.myapplication.network.dto.ChatMessageCreateRequest;

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
    private Long conversationId;
    
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
        ensureConversationExists();
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
            Long currentUserId = authManager.getUserId();
            android.util.Log.d("ChatActivity", "sendMessage: text='" + messageText + "', conversationId=" + conversationId + ", userId=" + currentUserId);
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
            
            android.util.Log.d("ChatActivity", "sendMessage: appended to UI. totalMessages=" + messages.size());

            // Gọi API tạo tin nhắn
            if (conversationId != null && currentUserId != null) {
                ChatMessageService messageService = ApiClient.getRetrofit(this).create(ChatMessageService.class);
                ChatMessageCreateRequest req = new ChatMessageCreateRequest(currentUserId, messageText, conversationId);
                messageService.createMessage(req).enqueue(new retrofit2.Callback<ApiResponse<ChatMessageDto>>() {
                    @Override
                    public void onResponse(retrofit2.Call<ApiResponse<ChatMessageDto>> call, retrofit2.Response<ApiResponse<ChatMessageDto>> response) {
                        android.util.Log.d("ChatActivity", "createMessage response code=" + response.code());
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ApiResponse<ChatMessageDto>> call, Throwable t) {
                        android.util.Log.e("ChatActivity", "createMessage failed", t);
                    }
                });
            } else {
                android.util.Log.w("ChatActivity", "createMessage skipped: conversationId or userId is null");
            }

            // Simulate phản hồi từ hỗ trợ (demo)
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

    private void ensureConversationExists() {
        try {
            Long userId = authManager.getUserId();
            if (userId == null) {
                android.util.Log.w("ChatActivity", "UserId is null, cannot ensure conversation");
                return;
            }

            android.util.Log.d("ChatActivity", "ensureConversationExists: checking for userId=" + userId);
            ConversationService service = ApiClient.getRetrofit(this).create(ConversationService.class);
            service.getConversationByUser(userId).enqueue(new retrofit2.Callback<ApiResponse<Conversation>>() {
                @Override
                public void onResponse(retrofit2.Call<ApiResponse<Conversation>> call, retrofit2.Response<ApiResponse<Conversation>> response) {
                    android.util.Log.d("ChatActivity", "check conversation response code=" + response.code());
                    if (!response.isSuccessful()) {
                        createConversationFallback(service, userId);
                        return;
                    }
                    ApiResponse<Conversation> body = response.body();
                    if (body != null && body.getResult() != null) {
                        conversationId = body.getResult().getConversationID();
                        android.util.Log.d("ChatActivity", "Existing conversation: " + conversationId);
                        // load messages for this conversation
                        fetchConversationMessages();
                    } else {
                        createConversationFallback(service, userId);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<ApiResponse<Conversation>> call, Throwable t) {
                    android.util.Log.e("ChatActivity", "check conversation failed", t);
                    createConversationFallback(service, userId);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ChatActivity", "ensureConversationExists error", e);
        }
    }

    private void createConversationFallback(ConversationService service, long userId) {
        android.util.Log.d("ChatActivity", "createConversationFallback: creating for userId=" + userId);
        service.createConversation(userId).enqueue(new retrofit2.Callback<ApiResponse<Conversation>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<Conversation>> call, retrofit2.Response<ApiResponse<Conversation>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                    conversationId = response.body().getResult().getConversationID();
                    android.util.Log.d("ChatActivity", "Created conversation: " + conversationId);
                    fetchConversationMessages();
                } else {
                    android.widget.Toast.makeText(ChatActivity.this, "Không thể khởi tạo cuộc trò chuyện", android.widget.Toast.LENGTH_SHORT).show();
                    android.util.Log.w("ChatActivity", "createConversationFallback: failed body or result is null");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<Conversation>> call, Throwable t) {
                android.widget.Toast.makeText(ChatActivity.this, "Lỗi mạng khi tạo chat", android.widget.Toast.LENGTH_SHORT).show();
                android.util.Log.e("ChatActivity", "createConversationFallback: network error", t);
            }
        });
    }

    private void fetchConversationMessages() {
        if (conversationId == null) return;
        android.util.Log.d("ChatActivity", "fetchConversationMessages: conversationId=" + conversationId);
        ChatMessageService messageService = ApiClient.getRetrofit(this).create(ChatMessageService.class);
        messageService.getMessagesByConversation(conversationId).enqueue(new retrofit2.Callback<ApiResponse<java.util.List<ChatMessageDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<java.util.List<ChatMessageDto>>> call, retrofit2.Response<ApiResponse<java.util.List<ChatMessageDto>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getResult() == null) {
                    android.util.Log.w("ChatActivity", "fetchConversationMessages: bad response code=" + response.code());
                    return;
                }
                List<ChatMessageDto> dtoList = response.body().getResult();
                android.util.Log.d("ChatActivity", "fetchConversationMessages: received=" + dtoList.size());
                Long currentUserId = authManager.getUserId();
                messages.clear();
                for (ChatMessageDto dto : dtoList) {
                    boolean fromUser = currentUserId != null && dto.getUser() != null && currentUserId.equals(dto.getUser().getUserID());
                    Date ts;
                    try {
                        ts = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.getDefault()).parse(dto.getSentAt());
                    } catch (Exception e) {
                        ts = new Date();
                    }
                    messages.add(new ChatMessage(dto.getMessage(), fromUser, ts));
                }
                chatAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    recyclerMessages.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<java.util.List<ChatMessageDto>>> call, Throwable t) {
                android.util.Log.e("ChatActivity", "fetchConversationMessages failed", t);
            }
        });
    }
}
