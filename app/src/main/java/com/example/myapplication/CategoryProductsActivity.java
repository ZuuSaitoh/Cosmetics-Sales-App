package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Product;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ProductService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryProductsActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {
    
    private RecyclerView recyclerView;
    private TextView textCategoryName;
    private ImageButton btnBack;
    private ProductAdapter productAdapter;
    private List<Product> products;
    private Category category;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_products);
        
        // Get category from intent
        category = (Category) getIntent().getSerializableExtra("category");
        if (category == null) {
            Toast.makeText(this, "Không tìm thấy thông tin danh mục", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initViews();
        setupRecyclerView();
        loadProductsByCategory();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recycler_products);
        textCategoryName = findViewById(R.id.text_category_name);
        btnBack = findViewById(R.id.btn_back);
        
        textCategoryName.setText(category.getCategoryName());
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void setupRecyclerView() {
        products = new ArrayList<>();
        productAdapter = new ProductAdapter(this, products);
        productAdapter.setOnProductClickListener(this);
        
        // Use GridLayoutManager for 2 columns like in HomeFragment
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(productAdapter);
    }
    
    private void loadProductsByCategory() {
        ProductService productService = ApiClient.getRetrofit(this).create(ProductService.class);
        
        // Debug log
        android.util.Log.d("CategoryProducts", "Loading products for category ID: " + category.getCategoryID());
        
        productService.getProductsByCategory(category.getCategoryID()).enqueue(new Callback<com.example.myapplication.network.dto.ApiResponse<List<Product>>>() {
            @Override
            public void onResponse(Call<com.example.myapplication.network.dto.ApiResponse<List<Product>>> call, 
                                 Response<com.example.myapplication.network.dto.ApiResponse<List<Product>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.myapplication.network.dto.ApiResponse<List<Product>> apiResponse = response.body();
                    android.util.Log.d("CategoryProducts", "API Response Code: " + apiResponse.getCode());
                    android.util.Log.d("CategoryProducts", "API Response Message: " + apiResponse.getMessage());
                    
                    if (apiResponse.getCode() == 9995 && apiResponse.getResult() != null) {
                        List<Product> categoryProducts = apiResponse.getResult();
                        productAdapter.updateProducts(categoryProducts);
                        
                        // Update category name with product count
                        String categoryNameWithCount = category.getCategoryName() + " (" + categoryProducts.size() + " sản phẩm)";
                        textCategoryName.setText(categoryNameWithCount);
                    } else {
                        android.util.Log.e("CategoryProducts", "API Error - Code: " + apiResponse.getCode() + ", Message: " + apiResponse.getMessage());
                        Toast.makeText(CategoryProductsActivity.this, 
                            "Lỗi API: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"), 
                            Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CategoryProductsActivity.this, 
                        "Lỗi tải sản phẩm: " + response.code(), 
                        Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onFailure(Call<com.example.myapplication.network.dto.ApiResponse<List<Product>>> call, Throwable t) {
                Toast.makeText(CategoryProductsActivity.this, 
                    "Lỗi khi tải sản phẩm: " + t.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }
}
