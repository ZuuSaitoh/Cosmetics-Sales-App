package com.example.myapplication.cart;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.CartManager;
import com.example.myapplication.model.CartItem;

import java.util.List;

public class CartActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TextView textGrandTotal;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recycler_cart);
        textGrandTotal = findViewById(R.id.text_grand_total);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<CartItem> items = CartManager.getInstance().getItems();
        recyclerView.setAdapter(new CartAdapter(items));

        updateGrandTotal();
    }

    private void updateGrandTotal() {
        double total = CartManager.getInstance().getGrandTotal();
        textGrandTotal.setText(String.format("$%.2f", total));
    }
}


