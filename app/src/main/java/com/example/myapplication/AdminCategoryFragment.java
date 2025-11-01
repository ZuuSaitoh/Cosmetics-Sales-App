package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.CategoryAdapter;
import com.example.myapplication.model.Category;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.CategoryService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCategoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyText;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories;
    private com.google.android.material.textfield.TextInputEditText etSearch;

    public AdminCategoryFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_admin_category, container, false);

        recyclerView = root.findViewById(R.id.rv_categories_admin);
        progressBar = root.findViewById(R.id.progress_bar);
        emptyText = root.findViewById(R.id.tv_empty);
        etSearch = root.findViewById(R.id.et_search);

        categories = new ArrayList<>();
        
        // Setup RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        categoryAdapter = new CategoryAdapter(categories);
        recyclerView.setAdapter(categoryAdapter);

        // Setup search
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterCategories(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // Load categories from API
        loadCategoriesFromAPI();

        return root;
    }

    private void loadCategoriesFromAPI() {
        if (getContext() == null) return;

        progressBar.setVisibility(View.VISIBLE);
        emptyText.setVisibility(View.GONE);

        CategoryService categoryService = ApiClient.getRetrofit(getContext()).create(CategoryService.class);

        categoryService.fetchAllCategories().enqueue(new Callback<com.example.myapplication.network.dto.ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<com.example.myapplication.network.dto.ApiResponse<List<Category>>> call,
                                   Response<com.example.myapplication.network.dto.ApiResponse<List<Category>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    com.example.myapplication.network.dto.ApiResponse<List<Category>> apiResponse = response.body();
                    
                    if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                        List<Category> apiCategories = apiResponse.getResult();
                        categories.clear();
                        categories.addAll(apiCategories);

                        if (categories.isEmpty()) {
                            emptyText.setVisibility(View.VISIBLE);
                            emptyText.setText("Không có danh mục nào");
                        } else {
                            emptyText.setVisibility(View.GONE);
                        }

                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "Lỗi API: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                        emptyText.setVisibility(View.VISIBLE);
                        emptyText.setText("Không thể tải danh mục");
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải danh mục: " + response.code(), Toast.LENGTH_SHORT).show();
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Không thể tải danh mục");
                }
            }

            @Override
            public void onFailure(Call<com.example.myapplication.network.dto.ApiResponse<List<Category>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"), 
                            Toast.LENGTH_SHORT).show();
                }
                emptyText.setVisibility(View.VISIBLE);
                emptyText.setText("Không thể tải danh mục");
            }
        });
    }

    private void filterCategories(String query) {
        if (categoryAdapter == null || categories == null) return;

        List<Category> filtered = new ArrayList<>();
        
        if (query.isEmpty()) {
            filtered.addAll(categories);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Category category : categories) {
                if (category.getName() != null && category.getName().toLowerCase().contains(lowerQuery)) {
                    filtered.add(category);
                }
            }
        }

        // Update adapter with filtered list
        categoryAdapter.updateCategories(filtered);
        
        if (filtered.isEmpty() && !query.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Không tìm thấy danh mục");
        } else if (categories.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText("Không có danh mục nào");
        } else {
            emptyText.setVisibility(View.GONE);
        }
    }
}

