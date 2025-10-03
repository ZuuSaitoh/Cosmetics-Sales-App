package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private final List<Product> products;

    public ProductAdapter(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.titleText.setText(product.getName());
//        holder.subtitleText.setText(product.getDescription());
        holder.priceText.setText(String.format("$%.2f", product.getPrice()));
        holder.imageView.setImageResource(product.getImageResId());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView titleText;
//        final TextView subtitleText;
        final TextView priceText;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageProduct);
            titleText = itemView.findViewById(R.id.textTitle);
//            subtitleText = itemView.findViewById(R.id.textSubtitle);
            priceText = itemView.findViewById(R.id.textPrice);
        }
    }
}


