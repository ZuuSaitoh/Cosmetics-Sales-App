package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.widget.Filter;
import android.widget.Filterable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Product;
import com.example.myapplication.ProductDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {
    private final Context context;
    private final List<Product> originalProducts;
    private final List<Product> filteredProducts;


    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.originalProducts = new ArrayList<>(products);
        this.filteredProducts = new ArrayList<>(products);
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
        holder.priceText.setText(String.format("$%.2f", product.getPrice()));
        holder.imageView.setImageResource(product.getImageResId());


        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
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
