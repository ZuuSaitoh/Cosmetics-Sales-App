package com.example.myapplication.network;

import com.example.myapplication.model.Category;
import com.example.myapplication.network.dto.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryService {
    @GET("categories/fetchAll")
    Call<ApiResponse<List<Category>>> fetchAllCategories();
}
