package com.example.myapplication.network;

import com.example.myapplication.network.dto.ApiResponse;
import com.example.myapplication.network.dto.UserAddressRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface UserAddressService {
    @GET("user-addresses/{userId}")
    Call<ApiResponse<List<UserAddressRequest>>> getAddresses(@Path("userId") Long userId);

    @POST("user-addresses/create")
    Call<ApiResponse<UserAddressRequest>> create(@Body UserAddressRequest request);

    @PUT("user-addresses/update")
    Call<ApiResponse<UserAddressRequest>> update(@Body UserAddressRequest request);

    @DELETE("user-addresses/delete/{userAddressId}")
    Call<Void> delete(@Path("userAddressId") Long id);
}


