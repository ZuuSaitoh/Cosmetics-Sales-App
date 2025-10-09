package com.example.myapplication.network;

import com.example.myapplication.network.dto.LoginRequest;
import com.example.myapplication.network.dto.LoginResponse;
import com.example.myapplication.network.dto.RegisterRequest;
import com.example.myapplication.network.dto.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("users/auth/token")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("users/create")
    Call<RegisterResponse> register(@Body RegisterRequest request);
}


