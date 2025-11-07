package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ChatActivity extends AppCompatActivity {
    
    private RecyclerView recyclerMessages;
    private EditText editMessage;
    private ImageButton btnSend, btnBack, btnMore, btnAttach;
    private TextView textContactName, textStatus;
    
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private AuthManager authManager;
    private Long conversationId;
    
    // HTTP Polling để nhận tin nhắn mới
    private Handler pollingHandler;
    private Runnable pollingRunnable;
    private static final long POLLING_INTERVAL = 2000; // 2 giây
    private boolean isPollingActive = false;
    private Date lastMessageTimestamp = null;
    private Set<Long> addedMessageIds = new HashSet<>(); // Track message IDs đã thêm để tránh duplicate
    
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
        setupPolling();
        
        // If admin opened with an existing conversationId, use it
        if (getIntent() != null && getIntent().hasExtra("conversationId")) {
            long cid = getIntent().getLongExtra("conversationId", -1L);
            if (cid != -1L) {
                conversationId = cid;
                android.util.Log.d("ChatActivity", "Started with conversationId=" + conversationId);
                fetchConversationMessages();
            } else {
                ensureConversationExists();
            }
        } else {
            ensureConversationExists();
        }
    }
    
    private void setupPolling() {
        pollingHandler = new Handler(Looper.getMainLooper());
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPollingActive && conversationId != null) {
                    pollForNewMessages();
                    pollingHandler.postDelayed(this, POLLING_INTERVAL);
                }
            }
        };
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Bắt đầu polling khi activity được hiển thị
        startPolling();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Dừng polling khi activity bị ẩn để tiết kiệm pin và băng thông
        stopPolling();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đảm bảo dừng polling khi activity bị hủy
        stopPolling();
    }
    
    private void startPolling() {
        if (!isPollingActive && conversationId != null) {
            isPollingActive = true;
            pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL);
            android.util.Log.d("ChatActivity", "Polling started");
        }
    }
    
    private void stopPolling() {
        if (isPollingActive) {
            isPollingActive = false;
            pollingHandler.removeCallbacks(pollingRunnable);
            android.util.Log.d("ChatActivity", "Polling stopped");
        }
    }
    
    private void pollForNewMessages() {
        if (conversationId == null) return;
        
        ChatMessageService messageService = ApiClient.getRetrofit(this).create(ChatMessageService.class);
        messageService.getMessagesByConversation(conversationId).enqueue(new retrofit2.Callback<ApiResponse<java.util.List<ChatMessageDto>>>() {
            @Override
            public void onResponse(retrofit2.Call<ApiResponse<java.util.List<ChatMessageDto>>> call, retrofit2.Response<ApiResponse<java.util.List<ChatMessageDto>>> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getResult() == null) {
                    return;
                }
                
                List<ChatMessageDto> dtoList = response.body().getResult();
                Long currentUserId = authManager.getUserId();
                
                // Chỉ thêm tin nhắn mới dựa trên message ID
                boolean hasNewMessages = false;
                for (ChatMessageDto dto : dtoList) {
                    Long messageId = dto.getChatMessageID();
                    
                    // Bỏ qua nếu message ID null hoặc đã được thêm rồi
                    if (messageId == null || addedMessageIds.contains(messageId)) {
                        continue;
                    }
                    
                    Date messageDate;
                    try {
                        messageDate = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.getDefault()).parse(dto.getSentAt());
                    } catch (Exception e) {
                        messageDate = new Date();
                    }
                    
                    boolean fromUser = currentUserId != null && dto.getUser() != null && currentUserId.equals(dto.getUser().getUserID());
                    ChatMessage newMessage = new ChatMessage(messageId, dto.getMessage(), fromUser, messageDate);
                    
                    // Thêm vào list và mark đã thêm
                    messages.add(newMessage);
                    addedMessageIds.add(messageId);
                    hasNewMessages = true;
                    
                    // Cập nhật lastMessageTimestamp
                    if (lastMessageTimestamp == null || messageDate.after(lastMessageTimestamp)) {
                        lastMessageTimestamp = messageDate;
                    }
                }
                
                if (hasNewMessages) {
                    // Sắp xếp lại messages theo thời gian
                    messages.sort((m1, m2) -> m1.getTimestamp().compareTo(m2.getTimestamp()));
                    chatAdapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        recyclerMessages.scrollToPosition(messages.size() - 1);
                    }
                    android.util.Log.d("ChatActivity", "New messages received via polling: " + hasNewMessages);
                }
            }
            
            @Override
            public void onFailure(retrofit2.Call<ApiResponse<java.util.List<ChatMessageDto>>> call, Throwable t) {
                // Lỗi polling - không cần log vì sẽ thử lại sau
            }
        });
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
            // Tạo tin nhắn từ người dùng (tạm thời không có ID, sẽ được cập nhật khi server trả về)
            ChatMessage userMessage = new ChatMessage(
                null, // messageId sẽ được cập nhật sau
                messageText,
                true, // isFromUser
                new Date()
            );
            
            messages.add(userMessage);
            chatAdapter.notifyItemInserted(messages.size() - 1);
            recyclerMessages.scrollToPosition(messages.size() - 1);
            
            // Cập nhật lastMessageTimestamp
            lastMessageTimestamp = userMessage.getTimestamp();
            
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
                        
                        // Cập nhật message ID từ server response
                        if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                            ChatMessageDto createdDto = response.body().getResult();
                            Long createdMessageId = createdDto.getChatMessageID();
                            
                            if (createdMessageId != null && !messages.isEmpty()) {
                                // Tìm tin nhắn vừa gửi (tin nhắn gần nhất không có ID và khớp nội dung)
                                // Tìm từ cuối lên để tìm tin nhắn mới nhất
                                for (int i = messages.size() - 1; i >= 0; i--) {
                                    ChatMessage msg = messages.get(i);
                                    if (msg.getMessageId() == null && 
                                        msg.getMessage().equals(messageText) && 
                                        msg.isFromUser() &&
                                        !addedMessageIds.contains(createdMessageId)) {
                                        msg.setMessageId(createdMessageId);
                                        addedMessageIds.add(createdMessageId);
                                        android.util.Log.d("ChatActivity", "Updated message ID: " + createdMessageId);
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<ApiResponse<ChatMessageDto>> call, Throwable t) {
                        android.util.Log.e("ChatActivity", "createMessage failed", t);
                    }
                });
            } else {
                android.util.Log.w("ChatActivity", "createMessage skipped: conversationId or userId is null");
            }
        }
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
                
                // Clear messages và reset tracking
                messages.clear();
                addedMessageIds.clear();
                lastMessageTimestamp = null;
                
                for (ChatMessageDto dto : dtoList) {
                    Long messageId = dto.getChatMessageID();
                    if (messageId == null) continue; // Bỏ qua nếu không có ID
                    
                    boolean fromUser = currentUserId != null && dto.getUser() != null && currentUserId.equals(dto.getUser().getUserID());
                    Date ts;
                    try {
                        ts = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", java.util.Locale.getDefault()).parse(dto.getSentAt());
                    } catch (Exception e) {
                        ts = new Date();
                    }
                    
                    ChatMessage msg = new ChatMessage(messageId, dto.getMessage(), fromUser, ts);
                    messages.add(msg);
                    addedMessageIds.add(messageId); // Track message ID đã thêm
                    
                    // Cập nhật lastMessageTimestamp
                    if (lastMessageTimestamp == null || ts.after(lastMessageTimestamp)) {
                        lastMessageTimestamp = ts;
                    }
                }
                chatAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    recyclerMessages.scrollToPosition(messages.size() - 1);
                }
                
                // Bắt đầu polling sau khi đã load xong messages
                startPolling();
            }

            @Override
            public void onFailure(retrofit2.Call<ApiResponse<java.util.List<ChatMessageDto>>> call, Throwable t) {
                android.util.Log.e("ChatActivity", "fetchConversationMessages failed", t);
            }
        });
    }
}
