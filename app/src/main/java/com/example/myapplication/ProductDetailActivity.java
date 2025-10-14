package com.example.myapplication;

import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;

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

public class ProductDetailActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_LOGIN = 1001; // Hằng số cho startActivityForResult

    private ImageView imageProduct;
    private TextView textName, textPrice, textDescription;
    private Button btnAddToCart;
    private ImageButton btnBack;
    private ImageButton btnCart;

    private String productName;
    private String productPrice;
    private String productDescription;
    private String productImageUrl;
    private Product product;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();
        getProductDataFromIntent();
        displayProductData();

        btnAddToCart.setOnClickListener(v -> showQuantityPopup());
        btnBack.setOnClickListener(v -> finish());
        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }

    private void initViews() {
        imageProduct = findViewById(R.id.image_product);
        textName = findViewById(R.id.text_name);
        textPrice = findViewById(R.id.text_price);
        textDescription = findViewById(R.id.text_description);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBack = findViewById(R.id.btn_back);
        btnCart = findViewById(R.id.btn_cart);
    }

    private void getProductDataFromIntent() {
        product = (Product) getIntent().getSerializableExtra("product");
        if (product != null) {
            productName = product.getName();
            productPrice = String.format("$%.2f", product.getPrice());
            productDescription = product.getDescription();
            productImageUrl = String.valueOf(product.getImageResId());
        } else {
            Toast.makeText(this, "Không nhận được dữ liệu sản phẩm!", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayProductData() {
        textName.setText(productName);
        textPrice.setText(productPrice);
        textDescription.setText(productDescription);
        loadProductImage(productImageUrl);
    }

    private void loadProductImage(String imageResIdString) {
        try {
            int imageResId = parseInt(imageResIdString);
            imageProduct.setImageResource(imageResId);
        } catch (NumberFormatException e) {
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
        
        // Calculate total price correctly
        double totalPrice = product.getPrice() * quantity[0];
        textTotalPrice.setText(String.format("$%.2f", totalPrice));


        btnMinus.setOnClickListener(v -> {
            if (quantity[0] > 1) {
                quantity[0]--;
                textQuantity.setText(String.valueOf(quantity[0]));
                // Update total price
                double totalPriceMinus = product.getPrice() * quantity[0];
                textTotalPrice.setText(String.format("$%.2f", totalPriceMinus));
            }
        });

        btnPlus.setOnClickListener(v -> {
            quantity[0]++;
            textQuantity.setText(String.valueOf(quantity[0]));
            // Update total price
            double totalPricePlus = product.getPrice() * quantity[0];
            textTotalPrice.setText(String.format("$%.2f", totalPricePlus));
        });

        btnConfirm.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();

            // 1. Kiểm tra trạng thái đăng nhập
            if (new AuthManager(this).isLoggedIn()) {
                // ĐÃ đăng nhập: Tiến hành thêm vào giỏ hàng
                CartManager.getInstance(). addToCart(product, quantity[0]);

            } else {
                // CHƯA đăng nhập: Chuyển hướng đến màn hình LoginScreen
                Toast.makeText(ProductDetailActivity.this, "Vui lòng đăng nhập để thêm sản phẩm.", Toast.LENGTH_SHORT).show();

                Intent loginIntent = new Intent(ProductDetailActivity.this, ActivityLogin.class);

                // Truyền thông tin sản phẩm và số lượng để xử lý sau khi đăng nhập
                loginIntent.putExtra("pending_product", product);
                loginIntent.putExtra("pending_quantity", quantity[0]);

                // Dùng startActivityForResult để đợi kết quả đăng nhập
                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
            }
        });

        bottomSheetDialog.show();
    }


}
