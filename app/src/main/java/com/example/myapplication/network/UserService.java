package com.example.myapplication.network;

import com.example.myapplication.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {
    @GET("users/{id}")
    Call<User> getUserById(@Path("id") Long userId);
    
    @PUT("users/update/{id}")
    Call<User> updateUser(@Path("id") Long userId, @Body User user);
}
