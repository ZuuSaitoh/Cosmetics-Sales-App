package com.example.myapplication.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutItemAdapter extends RecyclerView.Adapter<CheckoutItemAdapter.ViewHolder> {

    private List<CartItem> items;
    private Context context;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    public CheckoutItemAdapter(List<CartItem> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = items.get(position);

        if (item.getProduct() != null) {
            holder.tvProductName.setText(item.getProduct().getName());
            holder.tvProductPrice.setText(numberFormat.format(item.getItemTotal()) + "đ");
        } else {
            holder.tvProductName.setText("Sản phẩm không có");
            holder.tvProductPrice.setText("0đ");
        }
        holder.tvProductQuantity.setText("x" + item.getQuantity());

        // TODO: Load ảnh sản phẩm nếu có dùng Glide/Picasso
        // Ví dụ:
        // if (item.getProduct() != null && item.getProduct().getImageURL() != null) {
        //     Glide.with(context)
        //          .load(item.getProduct().getImageURL())
        //          .placeholder(R.drawable.img_no_product) // Ảnh chờ
        //          .into(holder.ivProductImage);
        // }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvProductQuantity, tvProductPrice;
        // ImageView ivProductImage; // Bật nếu bạn dùng ImageView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            // ivProductImage = itemView.findViewById(R.id.ivProductImage); // Bật nếu bạn dùng ImageView
        }
    }
}