package com.example.myapplication.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Main;
import com.example.myapplication.R;
import com.example.myapplication.data.CartManager;
import com.example.myapplication.model.CartItem;

import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView textGrandTotal;
    private ImageButton btnBack;
    private Button btnContinueShopping;
    private LinearLayout emptyState;
    private LinearLayout totalSection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupClickListeners();
        updateCartDisplay();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_cart);
        textGrandTotal = findViewById(R.id.text_grand_total);
        btnBack = findViewById(R.id.btn_back);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        emptyState = findViewById(R.id.empty_state);
        totalSection = findViewById(R.id.total_section);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnContinueShopping.setOnClickListener(v -> {
            Intent intent = new Intent(CartActivity.this, Main.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void updateCartDisplay() {
        List<CartItem> items = CartManager.getInstance().getItems();
        
        if (items.isEmpty()) {
            // Show empty state
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            totalSection.setVisibility(View.GONE);
        } else {
            // Show cart items
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            totalSection.setVisibility(View.VISIBLE);
            
            recyclerView.setAdapter(new CartAdapter(items));
            updateGrandTotal();
        }
    }

    private void updateGrandTotal() {
        double total = CartManager.getInstance().getGrandTotal();
        textGrandTotal.setText(String.format("$%.2f", total));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartDisplay();
    }
}


