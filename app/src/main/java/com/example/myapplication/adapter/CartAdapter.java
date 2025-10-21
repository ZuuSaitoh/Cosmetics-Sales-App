package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.CartManager;
import com.example.myapplication.model.CartItem;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.VH> {
    private final List<CartItem> items;
    private final NumberFormat numberFormat;

    public CartAdapter(List<CartItem> items) {
        this.items = items;
        this.numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
    }

    private String formatPrice(double price) {
        return numberFormat.format((long) price) + " VND";
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
        h.image.setImageResource(item.getProduct().getImageResId());

        h.btnPlus.setOnClickListener(v -> {
            CartManager.getInstance().updateQuantity(item.getProduct(), item.getQuantity() + 1);
            refreshBinding(h.getAdapterPosition());
        });

        h.btnMinus.setOnClickListener(v -> {
            int newQty = Math.max(1, item.getQuantity() - 1);
            CartManager.getInstance().updateQuantity(item.getProduct(), newQty);
            refreshBinding(h.getAdapterPosition());
        });

        h.btnRemove.setOnClickListener(v -> {
            CartManager.getInstance().remove(item.getProduct());
            int idx = h.getAdapterPosition();
            if (idx != RecyclerView.NO_POSITION) {
                items.remove(idx);
                notifyItemRemoved(idx);
            }
            if (onCartChangedListener != null) onCartChangedListener.onChanged();
        });
    }

    private void refreshBinding(int position) {
        if (position == RecyclerView.NO_POSITION) return;
        notifyItemChanged(position);
        if (onCartChangedListener != null) onCartChangedListener.onChanged();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView quantity;
        final TextView price;
        final TextView total;
        final MaterialButton btnPlus;
        final MaterialButton btnMinus;
        final MaterialButton btnRemove;

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

    public interface OnCartChangedListener {
        void onChanged();
    }

    private OnCartChangedListener onCartChangedListener;

    public void setOnCartChangedListener(OnCartChangedListener listener) {
        this.onCartChangedListener = listener;
    }
}


