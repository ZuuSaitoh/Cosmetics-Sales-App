package com.example.myapplication.network;

import com.example.myapplication.network.dto.User;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface UserService {
    @GET("users/{id}")
    Call<User> getUserById(@Path("id") String id);
}


