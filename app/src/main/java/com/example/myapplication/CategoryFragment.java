package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.CategoryAdapter;
import com.example.myapplication.animation.CartAnimation;
import com.example.myapplication.model.Category;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.CategoryService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private RecyclerView categoriesRecyclerView;
    private CategoryAdapter categoryAdapter;
    private List<Category> categories;
    private List<Category> filteredCategories;
    
    // Search and Cart
    private EditText searchEditText;
    private ImageButton cartIcon;
    private TextView cartBadge;

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_category, container, false);
            
            initViews(view);
            setupSearch();
            setupCart();
            setupCategories();
            setupRecyclerView();
            
            return view;
        } catch (Exception e) {
            e.printStackTrace();
            return inflater.inflate(R.layout.fragment_category, container, false);
        }
    }

    private void initViews(View view) {
        try {
            categoriesRecyclerView = view.findViewById(R.id.categoriesRecyclerView);
            searchEditText = view.findViewById(R.id.searchEditText);
            cartIcon = view.findViewById(R.id.cartIcon);
            cartBadge = view.findViewById(R.id.cartBadge);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearch() {
        try {
            if (searchEditText == null) return;
            
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        filterCategories(s.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupCart() {
        try {
            updateCartBadge();
            if (cartIcon != null) {
                cartIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            android.content.Context ctx = getContext();
                            if (ctx != null) {
                                android.content.Intent i = new android.content.Intent(ctx, com.example.myapplication.CartActivity.class);
                                startActivity(i);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterCategories(String query) {
        filteredCategories.clear();
        
        if (query.isEmpty()) {
            filteredCategories.addAll(categories);
        } else {
            for (Category category : categories) {
                if (category.getName().toLowerCase().contains(query.toLowerCase()) ||
                    category.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredCategories.add(category);
                }
            }
        }
        
        categoryAdapter.notifyDataSetChanged();
    }

    private void updateCartBadge() {
        int total = com.example.myapplication.CartManager.getInstance().getTotalQuantity();
        if (total > 0) {
            cartBadge.setText(String.valueOf(total));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    private void setupCategories() {
        categories = new ArrayList<>();
        filteredCategories = new ArrayList<>();
        
        // Load categories from API
        loadCategoriesFromAPI();
    }
    
    private void loadCategoriesFromAPI() {
        try {
            if (getContext() == null) {
                loadFallbackCategories();
                return;
            }
            
            CategoryService categoryService = ApiClient.getRetrofit(getContext()).create(CategoryService.class);
            
            categoryService.fetchAllCategories().enqueue(new Callback<com.example.myapplication.network.dto.ApiResponse<List<Category>>>() {
                @Override
                public void onResponse(Call<com.example.myapplication.network.dto.ApiResponse<List<Category>>> call, Response<com.example.myapplication.network.dto.ApiResponse<List<Category>>> response) {
                    try {
                        if (getContext() == null) return;
                        
                        if (response.isSuccessful() && response.body() != null) {
                            com.example.myapplication.network.dto.ApiResponse<List<Category>> apiResponse = response.body();
                            if (apiResponse.getCode() == 9999 && apiResponse.getResult() != null) {
                                List<Category> apiCategories = apiResponse.getResult();
                                categories.clear();
                                categories.addAll(apiCategories);
                                
                                // Set default icons and descriptions for categories
                                setDefaultIconsAndDescriptions(categories);
                                
                                filteredCategories.clear();
                                filteredCategories.addAll(categories);
                                if (categoryAdapter != null) {
                                    categoryAdapter.notifyDataSetChanged();
                                }
                                
                                // Toast.makeText(getContext(), "Đã tải " + categories.size() + " danh mục", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Lỗi API: " + (apiResponse.getMessage() != null ? apiResponse.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                                loadFallbackCategories();
                            }
                        } else {
                            Toast.makeText(getContext(), "Lỗi tải danh mục: " + response.code(), Toast.LENGTH_SHORT).show();
                            loadFallbackCategories();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        loadFallbackCategories();
                    }
                }

                @Override
                public void onFailure(Call<com.example.myapplication.network.dto.ApiResponse<List<Category>>> call, Throwable t) {
                    try {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi kết nối: " + (t.getMessage() != null ? t.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                        }
                        loadFallbackCategories();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            loadFallbackCategories();
        }
    }
    
    private void setDefaultIconsAndDescriptions(List<Category> categories) {
        int[] defaultIcons = {
            R.drawable.ic_face, R.drawable.ic_eye, R.drawable.ic_lips, R.drawable.ic_nail,
            R.drawable.ic_hair, R.drawable.ic_body, R.drawable.ic_perfume, R.drawable.ic_tools
        };
        
        String[] defaultDescriptions = {
            "Chăm sóc da mặt", "Trang điểm mắt", "Son môi và chăm sóc môi", "Sơn móng tay",
            "Chăm sóc tóc", "Chăm sóc cơ thể", "Nước hoa và hương thơm", "Dụng cụ trang điểm"
        };
        
        for (int i = 0; i < categories.size() && i < defaultIcons.length; i++) {
            Category category = categories.get(i);
            if (category.getIconUrl() == null || category.getIconUrl().isEmpty()) {
                category.setIconResId(defaultIcons[i]);
            }
            if (category.getDescription() == null || category.getDescription().isEmpty()) {
                category.setDescription(defaultDescriptions[i]);
            }
            // Set default product count if not provided
            if (category.getProductCount() == 0) {
                category.setProductCount(10 + i * 5); // Random product count
            }
        }
    }
    
    private void loadFallbackCategories() {
        categories.clear();
        categories.add(new Category("Mặt", R.drawable.ic_face, "Chăm sóc da mặt", 45));
        categories.add(new Category("Mắt", R.drawable.ic_eye, "Trang điểm mắt", 32));
        categories.add(new Category("Môi", R.drawable.ic_lips, "Son môi và chăm sóc môi", 28));
        categories.add(new Category("Móng", R.drawable.ic_nail, "Sơn móng tay", 15));
        categories.add(new Category("Tóc", R.drawable.ic_hair, "Chăm sóc tóc", 22));
        categories.add(new Category("Cơ thể", R.drawable.ic_body, "Chăm sóc cơ thể", 38));
        categories.add(new Category("Nước hoa", R.drawable.ic_perfume, "Nước hoa và hương thơm", 18));
        categories.add(new Category("Dụng cụ", R.drawable.ic_tools, "Dụng cụ trang điểm", 25));
        
        filteredCategories.clear();
        filteredCategories.addAll(categories);
        categoryAdapter.notifyDataSetChanged();
    }

    private void setupRecyclerView() {
        try {
            if (getContext() == null || categoriesRecyclerView == null) return;
            
            categoryAdapter = new CategoryAdapter(filteredCategories);
            categoryAdapter.setOnCategoryClickListener(new CategoryAdapter.OnCategoryClickListener() {
                @Override
                public void onCategoryClick(Category category) {
                    try {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đã chọn: " + category.getName(), Toast.LENGTH_SHORT).show();
                        }
                        // TODO: Navigate to products in this category
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            categoriesRecyclerView.setAdapter(categoryAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateCartBadge();
    }
    
    /**
     * Chạy animation bay vào giỏ hàng
     */
    private void runCartAnimation() {
        if (getView() != null && cartIcon != null && cartBadge != null) {
            // Tìm search box để làm source
            View sourceView = getView().findViewById(R.id.searchEditText);
            
            if (sourceView != null) {
                CartAnimation.simpleFlyToCart(sourceView, cartIcon, new CartAnimation.AnimationListener() {
                    @Override
                    public void onAnimationStart() {
                        // Animation bắt đầu
                    }
                    
                    @Override
                    public void onAnimationEnd() {
                        // Animation kết thúc - làm rung cart icon
                        if (cartIcon != null) {
                            cartIcon.animate()
                                .scaleX(1.2f)
                                .scaleY(1.2f)
                                .setDuration(150)
                                .withEndAction(() -> {
                                    cartIcon.animate()
                                        .scaleX(1.0f)
                                        .scaleY(1.0f)
                                        .setDuration(150);
                                });
                        }
                    }
                });
            }
        }
    }
}