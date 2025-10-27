package com.example.myapplication.network;

import com.example.myapplication.network.dto.SendOtpResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EmailService {
    @GET("emails/send-otp")
    Call<SendOtpResponse> sendOtp(@Query("email") String email);
}

