package com.example.myapplication.network;

import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.ChatMessageDto;
import com.example.myapplication.network.dto.ChatMessageCreateRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;
import retrofit2.http.Path;

public interface ChatMessageService {
    @GET("chat-messages/conversation/{conversationID}")
    Call<ApiResponse<List<ChatMessageDto>>> getMessagesByConversation(@Path("conversationID") long conversationId);

    @POST("chat-messages/create")
    Call<ApiResponse<ChatMessageDto>> createMessage(@Body ChatMessageCreateRequest request);
}


