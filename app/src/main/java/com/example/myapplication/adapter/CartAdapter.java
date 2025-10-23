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
import com.bumptech.glide.Glide;

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
    
    public CartItem getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
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
        // Hiển thị giá niêm yết (không phải total)
        h.price.setText(formatPrice(item.getProduct().getPrice()));
        h.quantity.setText(String.valueOf(item.getQuantity()));
        
        // Cập nhật hình ảnh sản phẩm
        if (item.getProduct().getImageURL() != null && !item.getProduct().getImageURL().isEmpty()) {
            // Sử dụng Glide để load hình ảnh từ URL
            Glide.with(h.image.getContext())
                .load(item.getProduct().getImageURL().trim())
                .placeholder(R.drawable.img_no_product)
                .error(R.drawable.img_no_product)
                .into(h.image);
        } else {
            // Fallback: sử dụng hình ảnh mặc định
            h.image.setImageResource(R.drawable.img_no_product);
        }

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

        // Cho phép nhập số trực tiếp
        h.quantity.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                try {
                    int newQty = Integer.parseInt(h.quantity.getText().toString().trim());
                    newQty = Math.max(1, newQty);
                    if (quantityChangedListener != null && newQty != item.getQuantity()) {
                        quantityChangedListener.onQuantityChanged(item, newQty);
                    }
                } catch (Exception ignored) { }
                h.quantity.setText(String.valueOf(item.getQuantity()));
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
        final TextView title, price;
        final android.widget.EditText quantity;
        final android.widget.CheckBox checkSelect;
        final MaterialButton btnPlus, btnMinus, btnRemove;

        VH(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.imageProduct);
            title = itemView.findViewById(R.id.textTitle);
            price = itemView.findViewById(R.id.textPrice);
            quantity = itemView.findViewById(R.id.textQty);
            btnPlus = itemView.findViewById(R.id.btn_plus);
            btnMinus = itemView.findViewById(R.id.btn_minus);
            btnRemove = itemView.findViewById(R.id.btn_remove);
            checkSelect = itemView.findViewById(R.id.checkSelect);
        }
    }
}
