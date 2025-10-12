package com.example.myapplication;

import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.myapplication.R;
import com.example.myapplication.model.Product;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ProductService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView imageProduct;
    private TextView textName, textPrice, textDescription, textFullDescription, textBrand, textCategory, textStock;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageButton btnCart;

    private Product product;
    private Long productId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        getProductDataFromIntent();
        
        if (productId != null) {
            loadProductDetailFromAPI();
        } else {
            displayProductData();
        }
        
        // Always display basic data first, then enhance with API data
        displayProductData();

        btnAddToCart.setOnClickListener(v -> showQuantityPopup());
        btnBack.setOnClickListener(v -> finish());
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, com.example.myapplication.cart.CartActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        imageProduct = findViewById(R.id.image_product);
        textName = findViewById(R.id.text_name);
        textPrice = findViewById(R.id.text_price);
        textDescription = findViewById(R.id.text_description);
        textFullDescription = findViewById(R.id.text_full_description);
        textBrand = findViewById(R.id.text_brand);
        textCategory = findViewById(R.id.text_category);
        textStock = findViewById(R.id.text_stock);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBack = findViewById(R.id.btn_back);
        btnCart = findViewById(R.id.btn_cart);
    }

    private void getProductDataFromIntent() {
        product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) {
            productId = product.getProductID();
            android.util.Log.d("ProductDetail", "Product ID from intent: " + productId);
            android.util.Log.d("ProductDetail", "Product name: " + product.getProductName());
        } else {
            Toast.makeText(this, "Không nhận được dữ liệu sản phẩm!", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void loadProductDetailFromAPI() {
        try {
            if (productId == null) {
                Toast.makeText(this, "Product ID is null", Toast.LENGTH_SHORT).show();
                displayProductData();
                return;
            }
            
            ProductService productService = ApiClient.getRetrofit(this).create(ProductService.class);
            
            productService.getProductById(productId).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    try {
                        android.util.Log.d("ProductDetail", "Response code: " + response.code());
                        android.util.Log.d("ProductDetail", "Response body: " + (response.body() != null ? response.body().toString() : "null"));
                        
                        if (response.isSuccessful() && response.body() != null) {
                            Product apiProduct = response.body();
                            android.util.Log.d("ProductDetail", "API Product: " + apiProduct.toString());
                            
                            product = apiProduct;
                            displayProductData();
                        } else {
                            Toast.makeText(ProductDetailActivity.this, "Lỗi tải chi tiết sản phẩm: " + response.code(), Toast.LENGTH_SHORT).show();
                            displayProductData(); // Fallback to basic data
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ProductDetailActivity.this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        displayProductData(); // Fallback to basic data
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    displayProductData(); // Fallback to basic data
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Exception in loadProductDetailFromAPI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            displayProductData(); // Fallback to basic data
        }
    }

    private void displayProductData() {
        if (product == null) {
            Toast.makeText(this, "Product data is null", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            textName.setText(product.getProductName() != null ? product.getProductName() : "Tên sản phẩm");
            
            // Format price as VND
            String formattedPrice = String.format("%,.0f VND", product.getPrice());
            textPrice.setText(formattedPrice);
            
            textDescription.setText(product.getBriefDescription() != null ? product.getBriefDescription() : "Mô tả sản phẩm");
            
            // Display additional details if available
            if (textFullDescription != null) {
                textFullDescription.setText(product.getFullDescription() != null ? product.getFullDescription() : "Mô tả chi tiết");
            }
            
            if (textBrand != null) {
                textBrand.setText("Thương hiệu: " + (product.getBrand() != null ? product.getBrand() : "Không có"));
            }
            
            if (textCategory != null && product.getCategoryID() != null) {
                textCategory.setText("Danh mục: " + product.getCategoryID().getCategoryName());
            } else if (textCategory != null) {
                textCategory.setText("Danh mục: Không có");
            }
            
            if (textStock != null) {
                textStock.setText("Còn lại: " + (product.getInstockQuantity() != null ? product.getInstockQuantity() : 0) + " sản phẩm");
            }
            
            loadProductImage();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying product data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductImage() {
        try {
            if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
                // TODO: Load image from URL using Glide or Picasso
                // For now, use default image
                imageProduct.setImageResource(R.drawable.img_no_product);
            } else {
                imageProduct.setImageResource(product.getImageResId());
            }
        } catch (Exception e) {
            imageProduct.setImageResource(R.drawable.img_no_product);
        }
    }

    private void showQuantityPopup() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProductDetailActivity.this);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_quantity);

        TextView textTotalPrice = bottomSheetDialog.findViewById(R.id.text_total_price);
        TextView textQuantity = bottomSheetDialog.findViewById(R.id.text_quantity);
        ImageButton btnMinus = bottomSheetDialog.findViewById(R.id.btn_minus);
        ImageButton btnPlus = bottomSheetDialog.findViewById(R.id.btn_plus);
        Button btnConfirm = bottomSheetDialog.findViewById(R.id.btn_confirm_add);

        if (textQuantity == null || btnMinus == null || btnPlus == null || btnConfirm == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy layout popup!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set bottom sheet behavior after showing
        bottomSheetDialog.setOnShowListener(dialog -> {
            View bottomSheetView = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheetView != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetView);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                //behavior.setPeekHeight(800); // Increased height to show all content
                behavior.setSkipCollapsed(true); // Allow full expansion
            }
        });

        final int[] quantity = {1};
        textQuantity.setText(String.valueOf(quantity[0]));
        
        // Calculate total price correctly
        double totalPrice = product.getPrice() * quantity[0];
        textTotalPrice.setText(String.format("%,.0f VND", totalPrice));


        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                textQuantity.setText(String.valueOf(quantity[0]));
                // Update total price
                double totalPriceMinus = product.getPrice() * quantity[0];
                textTotalPrice.setText(String.format("%,.0f VND", totalPriceMinus));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            textQuantity.setText(String.valueOf(quantity[0]));
            // Update total price
            double totalPricePlus = product.getPrice() * quantity[0];
            textTotalPrice.setText(String.format("%,.0f VND", totalPricePlus));
        });

        btnConfirm.setOnClickListener(v -> {
            // Add to cart
            com.example.myapplication.data.CartManager.getInstance().add(product, quantity[0]);
            Toast.makeText(ProductDetailActivity.this,
                    "Đã thêm " + quantity[0] + " sản phẩm vào giỏ hàng!",
                    Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }
}
