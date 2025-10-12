package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.adapter.BannerAdapter;
import com.example.myapplication.adapter.ProductAdapter;
import com.example.myapplication.map.MapsActivity;
import com.example.myapplication.model.Banner;
import com.example.myapplication.model.Product;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView bannerRecyclerView;
    private RecyclerView productRecyclerView;
    private BannerAdapter bannerAdapter;
    private ProductAdapter productAdapter;
    
    // Search and Cart
    private EditText searchEditText;
    private ImageView cartIcon;
    private TextView cartBadge;
    private ImageView locationIcon;
    private int cartItemCount = 0;

    private Handler autoScrollHandler = new Handler();
    private Runnable autoScrollRunnable;
    private int currentBannerPosition = 0;
    private int scrollDelay = 5000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        
        bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView);
        productRecyclerView = view.findViewById(R.id.productRecyclerView);
        
        // Initialize search and cart
        searchEditText = view.findViewById(R.id.searchEditText);
        cartIcon = view.findViewById(R.id.cartIcon);
        cartBadge = view.findViewById(R.id.cartBadge);
        locationIcon = view.findViewById(R.id.locationIcon);
        
        setupSearch();
        setupCart();
        setupBannerList();
        setupProductList();
        startAutoScroll();
        setupLocation();
        
        return view;
    }

    private void setupLocation() {
        locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Context ctx = getContext();
                if (ctx != null) {
                    android.content.Intent i = new android.content.Intent(ctx, MapsActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (productAdapter != null) {
                    productAdapter.getFilter().filter(s);
                }
                boolean hasQuery = s != null && s.length() > 0;
                if (bannerRecyclerView != null) {
                    bannerRecyclerView.setVisibility(hasQuery ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCart() {
        updateCartBadge();
        cartIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.content.Context ctx = getContext();
                if (ctx != null) {
                    android.content.Intent i = new android.content.Intent(ctx, com.example.myapplication.cart.CartActivity.class);
                    startActivity(i);
                }
            }
        });
    }

    // filter handled by adapter's Filterable implementation

    public void addToCart() {
        updateCartBadge();
        Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void updateCartBadge() {
        int total = com.example.myapplication.data.CartManager.getInstance().getTotalQuantity();
        if (total > 0) {
            cartBadge.setText(String.valueOf(total));
            cartBadge.setVisibility(View.VISIBLE);
        } else {
            cartBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (autoScrollRunnable != null) {
            startAutoScroll();
        }
        // refresh badge on return
        updateCartBadge();
    }

    private void setupBannerList() {
        bannerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<Banner> banners = new ArrayList<>();
        banners.add(new Banner(R.drawable.banner));
        banners.add(new Banner(R.drawable.banner));
        banners.add(new Banner(R.drawable.banner));
        banners.add(new Banner(R.drawable.banner));
        
        bannerAdapter = new BannerAdapter(banners);
        bannerRecyclerView.setAdapter(bannerAdapter);
    }

    private void setupProductList() {
        productRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        List<Product> products = new ArrayList<>();

        products.add(new Product("Lipstick", "High-quality lipstick", 25.99, R.drawable.banner));
        products.add(new Product("Foundation", "Perfect coverage foundation", 35.99, R.drawable.banner));
        products.add(new Product("Eyeshadow", "Beautiful eyeshadow palette", 29.99, R.drawable.banner));
        products.add(new Product("Mascara", "Long-lasting mascara", 19.99, R.drawable.banner));
        products.add(new Product("Blush", "Natural blush color", 22.99, R.drawable.banner));
        products.add(new Product("Concealer", "Full coverage concealer", 24.99, R.drawable.banner));
        
        productAdapter = new ProductAdapter(getContext(), products);
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
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoScroll();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoScroll();
    }
}
