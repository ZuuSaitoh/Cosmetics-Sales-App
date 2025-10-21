package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.CartItem;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    private List<CartItem> items;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());

    // Listeners for item interactions
    private OnItemQuantityChangedListener quantityChangedListener;
    private OnItemRemovedListener itemRemovedListener;

    public interface OnItemQuantityChangedListener {
        void onQuantityChanged(CartItem item, int newQuantity);
    }

    public interface OnItemRemovedListener {
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> items) {
        this.items = items;
    }

    public void updateItems(List<CartItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setOnItemQuantityChangedListener(OnItemQuantityChangedListener listener) {
        this.quantityChangedListener = listener;
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.itemRemovedListener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CartItem item = items.get(position);
        h.title.setText(item.getProduct().getName());
        h.quantity.setText("x" + item.getQuantity());
        h.price.setText(formatPrice(item.getProduct().getPrice()));
        h.total.setText(formatPrice(item.getItemTotal()));
        // TODO: Update to use Glide or Picasso if image URLs are provided by the API
        // h.image.setImageResource(item.getProduct().getImageResId());

        h.btnPlus.setOnClickListener(v -> {
            if (quantityChangedListener != null) {
                quantityChangedListener.onQuantityChanged(item, item.getQuantity() + 1);
            }
        });

        h.btnMinus.setOnClickListener(v -> {
            if (quantityChangedListener != null && item.getQuantity() > 1) {
                quantityChangedListener.onQuantityChanged(item, item.getQuantity() - 1);
            }
        });

        h.btnRemove.setOnClickListener(v -> {
            if (itemRemovedListener != null) {
                itemRemovedListener.onItemRemoved(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String formatPrice(double price) {
        return numberFormat.format((long) price) + " VND";
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title, quantity, price, total;
        final MaterialButton btnPlus, btnMinus, btnRemove;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageProduct);
            title = itemView.findViewById(R.id.textTitle);
            quantity = itemView.findViewById(R.id.textQty);
            price = itemView.findViewById(R.id.textPrice);
            total = itemView.findViewById(R.id.textTotal);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
