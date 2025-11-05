package com.example.myapplication.network;

import com.example.myapplication.model.Conversation;
import com.example.myapplication.network.dto.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ConversationService {

    @GET("conversations/user/{userID}")
    Call<ApiResponse<Conversation>> getConversationByUser(@Path("userID") long userId);

    @POST("conversations/create/{userID}")
    Call<ApiResponse<Conversation>> createConversation(@Path("userID") long userId);
}


