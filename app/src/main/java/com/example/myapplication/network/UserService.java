package com.example.myapplication.network;

import com.example.myapplication.model.User;
import com.example.myapplication.model.UserUpdateRequest;
import com.example.myapplication.network.dto.ChangePasswordRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserService {
    @GET("users/{id}")
    Call<User> getUserById(@Path("id") int id);

    @GET("users/profile")
    Call<User> getCurrentUser(@Header("Authorization") String token);

    @PUT("users/update/{id}")
    Call<User> updateUser(@Path("id") int id, @Body UserUpdateRequest body);

    @PUT("users/update/password/{id}")
    Call<Void> changePassword(@Path("id") int id, @Body ChangePasswordRequest body);
}
