package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.ProductAdmin;

import java.util.List;

public class TopProductAdapter extends RecyclerView.Adapter<TopProductAdapter.ViewHolder> {

    private List<ProductAdmin> productList;

    public TopProductAdapter(List<ProductAdmin> productList) {
        this.productList = productList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ProductAdmin product = productList.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvSalesCount.setText("Đã bán: " + product.getSalesCount());
        holder.imgProduct.setImageResource(R.drawable.img_no_product);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvSalesCount;

        public ViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvSalesCount = itemView.findViewById(R.id.tv_sales_count);
        }
    }
}
