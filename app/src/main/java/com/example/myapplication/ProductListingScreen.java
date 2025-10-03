package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapter.BannerAdapter;
import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.model.Banner;
import com.example.myapplication.model.Product;


import java.util.ArrayList;
import java.util.List;

public class ProductListingScreen extends AppCompatActivity {
    private RecyclerView bannerRecyclerView;
    private RecyclerView productRecyclerView;
    private BannerAdapter bannerAdapter;
    private ProductAdapter productAdapter;


    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private int currentBannerPosition = 0;
    private int scrollDelay = 5000; 




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bannerRecyclerView = findViewById(R.id.bannerRecyclerView);
        productRecyclerView = findViewById(R.id.productRecyclerView);

        setupBannerList();
        setupProductList();
        startAutoScroll();
    }

    private void setupBannerList() {
        bannerRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner(R.drawable.banner));
        banners.add(new Banner(R.drawable.banner));
        banners.add(new Banner(R.drawable.banner));
        banners.add(new Banner(R.drawable.banner));
        
        bannerAdapter = new BannerAdapter(banners);
        bannerRecyclerView.setAdapter(bannerAdapter);
    }

    private void setupProductList() {
        productRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        List<Product> products = new ArrayList<>();

        products.add(new Product("Lipstick", "High-quality lipstick", 25.99, R.drawable.banner));
        products.add(new Product("Foundation", "Perfect coverage foundation", 35.99, R.drawable.banner));
        products.add(new Product("Eyeshadow", "Beautiful eyeshadow palette", 29.99, R.drawable.banner));
        products.add(new Product("Mascara", "Long-lasting mascara", 19.99, R.drawable.banner));
        products.add(new Product("Blush", "Natural blush color", 22.99, R.drawable.banner));
        products.add(new Product("Concealer", "Full coverage concealer", 24.99, R.drawable.banner));
        
        productAdapter = new ProductAdapter(products);
        productRecyclerView.setAdapter(productAdapter);
    }

    private void startAutoScroll() {
        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerAdapter != null && bannerAdapter.getItemCount() > 0) {
                    currentBannerPosition = (currentBannerPosition + 1) % bannerAdapter.getItemCount();
                    bannerRecyclerView.smoothScrollToPosition(currentBannerPosition);
                }
                autoScrollHandler.postDelayed(this, scrollDelay);
            }
        };
        autoScrollHandler.postDelayed(autoScrollRunnable, scrollDelay);
    }

    private void stopAutoScroll() {
        if (autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoScroll();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAutoScroll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (autoScrollRunnable != null) {
            startAutoScroll();
        }
    }
}
