package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.model.Category;
import com.example.myapplication.model.Product;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ProductService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    
    // Sort views
    private TextView btnSortFeatured;
    private TextView btnSortBestSelling;
    private TextView btnSortNewest;
    private TextView textPriceLabel;
    private LinearLayout btnSortPrice;
    private ImageView imgPriceSort;
    private ImageButton btnLayoutToggle;
    
    // Sort state
    private String currentSort = "Nổi bật";
    private boolean isPriceAscending = true;
    private boolean isGridView = true;
    
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
        
        // Sort views
        btnSortFeatured = findViewById(R.id.btnSortFeatured);
        btnSortBestSelling = findViewById(R.id.btnSortBestSelling);
        btnSortNewest = findViewById(R.id.btnSortNewest);
        btnSortPrice = findViewById(R.id.btnSortPrice);
        textPriceLabel = findViewById(R.id.textPriceLabel);
        imgPriceSort = findViewById(R.id.imgPriceSort);
        btnLayoutToggle = findViewById(R.id.btnLayoutToggle);
        
        textCategoryName.setText(category.getCategoryName());
        
        btnBack.setOnClickListener(v -> finish());
        
        setupSortListeners();
        setupLayoutToggle();
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
                        products = new ArrayList<>(categoryProducts);
                        
                        // Apply initial sort (Nổi bật)
                        sortProducts("Nổi bật");
                        updateSortButtons();
                        
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
    
    private void setupSortListeners() {
        btnSortFeatured.setOnClickListener(v -> {
            currentSort = "Nổi bật";
            sortProducts("Nổi bật");
            updateSortButtons();
        });
        
        btnSortBestSelling.setOnClickListener(v -> {
            currentSort = "Bán chạy";
            sortProducts("Bán chạy");
            updateSortButtons();
        });
        
        btnSortNewest.setOnClickListener(v -> {
            currentSort = "Mới nhất";
            sortProducts("Mới nhất");
            updateSortButtons();
        });
        
        btnSortPrice.setOnClickListener(v -> {
            currentSort = "Giá";
            isPriceAscending = !isPriceAscending;
            sortProducts("Giá");
            updateSortButtons();
        });
    }
    
    private void setupLayoutToggle() {
        btnLayoutToggle.setOnClickListener(v -> {
            isGridView = !isGridView;
            toggleLayout();
        });
    }
    
    private void sortProducts(String sortType) {
        if (products == null || products.isEmpty()) return;
        
        List<Product> sortedList = new ArrayList<>(products);
        
        switch (sortType) {
            case "Nổi bật":
                // Hardcoded: Simulate featured items by mixing order
                Collections.shuffle(sortedList);
                break;
                
            case "Bán chạy":
                // Hardcoded: Sort by stock quantity (higher stock = more sales)
                Collections.sort(sortedList, (p1, p2) -> {
                    int qty1 = p1.getInstockQuantity() != null ? p1.getInstockQuantity() : 0;
                    int qty2 = p2.getInstockQuantity() != null ? p2.getInstockQuantity() : 0;
                    return Integer.compare(qty2, qty1);
                });
                break;
                
            case "Mới nhất":
                // Hardcoded: Sort by product ID (higher ID = newer)
                Collections.sort(sortedList, (p1, p2) -> {
                    Long id1 = p1.getProductID() != null ? p1.getProductID() : 0L;
                    Long id2 = p2.getProductID() != null ? p2.getProductID() : 0L;
                    return Long.compare(id2, id1);
                });
                break;
                
            case "Giá":
                if (isPriceAscending) {
                    Collections.sort(sortedList, (p1, p2) -> 
                        Double.compare(p1.getPrice(), p2.getPrice()));
                } else {
                    Collections.sort(sortedList, (p1, p2) -> 
                        Double.compare(p2.getPrice(), p1.getPrice()));
                }
                break;
        }
        
        productAdapter.updateProducts(sortedList);
    }
    
    private void updateSortButtons() {
        // Reset all buttons
        btnSortFeatured.setTextColor(0xFF666666);
        btnSortBestSelling.setTextColor(0xFF666666);
        btnSortNewest.setTextColor(0xFF666666);
        textPriceLabel.setTextColor(0xFF666666);
        
        // Reset price icon to default
        imgPriceSort.setImageResource(R.drawable.ic_price_sort_default);
        
        switch (currentSort) {
            case "Nổi bật":
                btnSortFeatured.setTextColor(0xFF4CAF50);
                break;
            case "Bán chạy":
                btnSortBestSelling.setTextColor(0xFF4CAF50);
                break;
            case "Mới nhất":
                btnSortNewest.setTextColor(0xFF4CAF50);
                break;
            case "Giá":
                textPriceLabel.setTextColor(0xFF4CAF50);
                if (isPriceAscending) {
                    imgPriceSort.setImageResource(R.drawable.ic_price_sort_asc);
                } else {
                    imgPriceSort.setImageResource(R.drawable.ic_price_sort_desc);
                }
                break;
        }
    }
    
    private void toggleLayout() {
        if (isGridView) {
            GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
            recyclerView.setLayoutManager(layoutManager);
            btnLayoutToggle.setImageResource(R.drawable.ic_list_view);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            btnLayoutToggle.setImageResource(R.drawable.ic_grid_view);
        }
    }
}
