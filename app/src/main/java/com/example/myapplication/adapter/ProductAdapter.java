package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.myapplication.R;
import com.example.myapplication.model.Product;
import com.example.myapplication.ProductDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {
    private final Context context;
    private final List<Product> originalProducts;
    private final List<Product> filteredProducts;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.originalProducts = new ArrayList<>(products);
        this.filteredProducts = new ArrayList<>(products);
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }
    
    public void updateProducts(List<Product> newProducts) {
        this.originalProducts.clear();
        this.originalProducts.addAll(newProducts);
        this.filteredProducts.clear();
        this.filteredProducts.addAll(newProducts);
        notifyDataSetChanged();
    }



    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = filteredProducts.get(position);
        holder.titleText.setText(product.getName());

        String formattedPrice = String.format("%,.0f VND", product.getPrice());
        holder.priceText.setText(formattedPrice);

        String imageURL = product.getImageURL();

        if (imageURL != null && !imageURL.isEmpty()) {
            Glide.with(context)
                    .load(imageURL.trim())
                    .placeholder(R.drawable.img_no_product) // Ảnh chờ
                    .error(R.drawable.img_no_product)       // Ảnh khi lỗi
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.img_no_product);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return filteredProducts.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String query = constraint == null ? "" : constraint.toString().trim().toLowerCase();
                List<Product> result = new ArrayList<>();
                if (query.isEmpty()) {
                    result.addAll(originalProducts);
                } else {
                    for (Product product : originalProducts) {
                        String name = product.getName() == null ? "" : product.getName().toLowerCase();
                        String description = product.getDescription() == null ? "" : product.getDescription().toLowerCase();
                        if (name.contains(query) || description.contains(query)) {
                            result.add(product);
                        }
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = result;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredProducts.clear();
                if (results != null && results.values instanceof List) {
                    filteredProducts.addAll((List<Product>) results.values);
                }
                notifyDataSetChanged();
            }
        };
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView titleText;
        final TextView priceText;
        //final TextView descriptionText;


        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageProduct);
            titleText = itemView.findViewById(R.id.textTitle);
           // descriptionText = itemView.findViewById(R.id.textDescription);
            priceText = itemView.findViewById(R.id.textPrice);
        }
    }
}
