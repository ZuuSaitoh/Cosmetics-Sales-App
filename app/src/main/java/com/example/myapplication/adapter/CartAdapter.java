package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.CartItem;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    private List<CartItem> items;
    private final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    private java.util.Set<Long> selectedItems = new java.util.HashSet<>();

    // Listeners for item interactions
    private OnItemQuantityChangedListener quantityChangedListener;
    private OnItemRemovedListener itemRemovedListener;
    private OnItemSelectionChangedListener selectionChangedListener;

    public interface OnItemQuantityChangedListener {
        void onQuantityChanged(CartItem item, int newQuantity);
    }

    public interface OnItemRemovedListener {
        void onItemRemoved(CartItem item);
    }

    public interface OnItemSelectionChangedListener {
        void onSelectionChanged(CartItem item, boolean isSelected);
    }

    public CartAdapter(List<CartItem> items) {
        this.items = items;
    }

    public void updateItems(List<CartItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setSelectedItems(java.util.Set<Long> selectedItems) {
        this.selectedItems = selectedItems;
        notifyDataSetChanged();
    }

    public void setOnItemQuantityChangedListener(OnItemQuantityChangedListener listener) {
        this.quantityChangedListener = listener;
    }

    public void setOnItemRemovedListener(OnItemRemovedListener listener) {
        this.itemRemovedListener = listener;
    }

    public void setOnItemSelectionChangedListener(OnItemSelectionChangedListener listener) {
        this.selectionChangedListener = listener;
    }

    public List<CartItem> getItems() {
        return items;
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
        h.quantity.setText(String.valueOf(item.getQuantity()));
        h.total.setText(formatPrice(item.getItemTotal()));
        h.checkbox.setChecked(selectedItems.contains(item.getCartItemID()));
        
        // Load image using Glide
        String imageUrl = item.getProduct().getImageURL();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(h.image.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.img_no_product)
                    .error(R.drawable.img_no_product)
                    .into(h.image);
        } else {
            // Fallback to default image if no URL
            h.image.setImageResource(R.drawable.img_no_product);
        }

        h.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (selectionChangedListener != null) {
                selectionChangedListener.onSelectionChanged(item, isChecked);
            }
        });

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
        final TextView title, quantity, total;
        final ImageButton btnPlus, btnMinus;
        final TextView btnRemove;
        final CheckBox checkbox;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageProduct);
            title = itemView.findViewById(R.id.textTitle);
            quantity = itemView.findViewById(R.id.textQty);
            total = itemView.findViewById(R.id.textTotal);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            checkbox = itemView.findViewById(R.id.checkbox_select);
        }
    }
}
