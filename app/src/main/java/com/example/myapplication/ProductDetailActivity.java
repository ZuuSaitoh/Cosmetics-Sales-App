package com.example.myapplication;

import static java.lang.Integer.parseInt;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.ProductService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.model.Product;
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.CartManager;
import com.example.myapplication.auth.AuthManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOGIN = 1001; // Hằng số cho startActivityForResult

    private ImageView imageProduct;
    private TextView textName, textPrice, textDescription, textFullDescription, textBrand, textCategory, textStock;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageButton btnCart;

    private Product product;
    private Long productId;
    private AuthManager authManager;

    // --- SỬA: XÓA 2 BIẾN PENDING ---
    // private Product pendingProductFromLogin = null;
    // private int pendingQuantityFromLogin = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        authManager = new AuthManager(getApplicationContext());

        initViews();
        getProductDataFromIntent();

        if (productId != null) {
            loadProductDetailFromAPI();
        } else {
            displayProductData();
        }
        displayProductData();

        // --- SỬA: ĐƠN GIẢN HÓA LOGIC CLICK ---
        btnAddToCart.setOnClickListener(v -> {
            authManager = new AuthManager(getApplicationContext()); // Luôn refresh authManager

            if ( authManager.isLoggedIn()) {
                // Đã login -> Mở popup
                showQuantityPopup();
            } else {
                // Chưa login -> Đi login
                Intent loginIntent = new Intent(ProductDetailActivity.this, ActivityLogin.class);
                loginIntent.putExtra("pending_product", product);
                loginIntent.putExtra("pending_quantity", 1); // Gửi số lượng 1
                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    // ... (Hàm initViews(), getProductDataFromIntent(), loadProductDetailFromAPI(),
    //      displayProductData(), loadProductImage() GIỮ NGUYÊN NHƯ FILE CỦA BẠN) ...
    // ... (Copy/paste 5 hàm đó vào đây) ...

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
        } else {
            Toast.makeText(this, "Không nhận được dữ liệu sản phẩm!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProductDetailFromAPI() {
        // (Giữ nguyên code hàm này của bạn)
        if (productId == null) {
            displayProductData();
            return;
        }
        ProductService productService = ApiClient.getRetrofit(this).create(ProductService.class);
        productService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = response.body();
                    displayProductData();
                } else {
                    displayProductData(); // Fallback
                }
            }
            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                displayProductData(); // Fallback
            }
        });
    }

    private void displayProductData() {
        if (product == null) {
            return;
        }
        textName.setText(product.getProductName() != null ? product.getProductName() : "Tên sản phẩm");
        String formattedPrice = String.format("%,.0f VND", product.getPrice());
        textPrice.setText(formattedPrice);
        textDescription.setText(product.getBriefDescription() != null ? product.getBriefDescription() : "Mô tả sản phẩm");
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
    }

    private void loadProductImage() {
        try {
            if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
                // TODO: Load image from URL using Glide or Picasso
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

        final int[] quantity = {1};
        textQuantity.setText(String.valueOf(quantity[0]));

        double totalPrice = product.getPrice() * quantity[0];
        textTotalPrice.setText(String.format("%,.0f VND", totalPrice));

        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                textQuantity.setText(String.valueOf(quantity[0]));
                double totalPriceMinus = product.getPrice() * quantity[0];
                textTotalPrice.setText(String.format("%,.0f VND", totalPriceMinus));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            textQuantity.setText(String.valueOf(quantity[0]));
            double totalPricePlus = product.getPrice() * quantity[0];
            textTotalPrice.setText(String.format("%,.0f VND", totalPricePlus));
        });

        // --- SỬA: XÓA LOGIC KIỂM TRA LOGIN BÊN TRONG POPUP ---
        btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();

            // Chỉ cần thêm vào giỏ hàng, vì hàm này giờ chỉ được gọi khi đã login
            CartManager.getInstance().addToCart(product, quantity[0]);
            Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();

            // XÓA KHỐI "ELSE { ... ĐI LOGIN ... }" Ở ĐÂY
        });

        bottomSheetDialog.show();
    }

    // --- SỬA: THAY ĐỔI HOÀN TOÀN LOGIC onActivityResult ---
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOGIN) {
            // 1. Refresh lại AuthManager để đọc SharedPreferences mới nhất
            authManager = new AuthManager(getApplicationContext());

            // 2. Kiểm tra xem login CÓ THỰC SỰ thành công không
            if (resultCode == RESULT_OK && authManager.isLoggedIn()) {

                Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                // 3. TỰ ĐỘNG MỞ POPUP (đây là mấu chốt của flow)
                showQuantityPopup();

            } else {
                // Login thất bại (vì token null) hoặc user bấm back (hủy)
                Toast.makeText(this, "Chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}