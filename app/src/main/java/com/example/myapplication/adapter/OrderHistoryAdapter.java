package com.example.myapplication.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.model.CartItem;
import com.example.myapplication.model.Order;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthService;
import com.example.myapplication.network.dto.CartItemsResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderClickListener listener;


    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderHistoryAdapter(Context context, List<Order> orderList, OnOrderClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_with_delete, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        // Mã đơn hàng (sử dụng cartID nếu có)
        if (order.getCart() != null && order.getCart().getCartID() != null) {
            holder.tvOrderCode.setText("#" + order.getCart().getCartID());
        } else {
            holder.tvOrderCode.setText("#-");
        }

        //  Hiển thị ngày đặt hàng
        try {
            String isoDate = order.getOrderDate();
            Date date = isoFormat.parse(isoDate);
            holder.tvOrderDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            holder.tvOrderDate.setText(order.getOrderDate());
        }

        //  Hiển thị trạng thái đơn hàng
        String status = order.getOrderStatus();
        holder.tvOrderStatus.setText(status != null ? status : "N/A");

        applyStatusColors(holder, status);

        //  Hiển thị tổng số lượng (tạm thời từ CartItem nếu có)
        if (order.getCart() != null && order.getCart().getCartItem() != null) {
            holder.tvOrderQuantity.setText(String.valueOf(order.getCart().getCartItem().getQuantity()));
        } else {
            holder.tvOrderQuantity.setText("-");
        }

        //  Hiển thị tổng tiền
        if (order.getCart() != null) {
            double totalPrice = order.getCart().getTotalPrice();
            holder.tvTotalPrice.setText(currencyFormat.format(totalPrice));
        } else {
            holder.tvTotalPrice.setText("Tổng tiền: N/A");
        }

        // Mặc định ảnh rỗng (nếu có ImageView)
        if (holder.ivOrderThumbnail != null) {
            holder.ivOrderThumbnail.setImageResource(R.drawable.img_no_product);
        }
        holder.tvMultiItemBadge.setVisibility(View.GONE);

        // Gọi API phụ để lấy danh sách sản phẩm trong giỏ hàng
        if (order.getCart() != null && order.getCart().getCartID() != null) {
            Long cartID = order.getCart().getCartID();

            AuthService authService = ApiClient.getRetrofit(context).create(AuthService.class);
            authService.getCartItems(cartID).enqueue(new Callback<CartItemsResponse>() {
                @Override
                public void onResponse(Call<CartItemsResponse> call, Response<CartItemsResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getResult() != null) {
                        List<CartItem> items = response.body().getResult();
                        if (!items.isEmpty() && items.get(0).getProduct() != null) {
                            String imageUrl = items.get(0).getProduct().getImageURL();

                            if (holder.ivOrderThumbnail != null) {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.img_no_product)
                                        .error(R.drawable.img_no_product)
                                        .into(holder.ivOrderThumbnail);
                            }

                            //  Nếu có nhiều sản phẩm -> hiện badge +n
                            if (items.size() > 1) {
                                holder.tvMultiItemBadge.setVisibility(View.VISIBLE);
                                holder.tvMultiItemBadge.setText("+" + (items.size() - 1));
                            } else {
                                holder.tvMultiItemBadge.setVisibility(View.GONE);
                            }

                            // Cập nhật số lượng từ danh sách items 
                            holder.tvOrderQuantity.setText(String.valueOf(items.size()));
                        }
                    }
                }

                @Override
                public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                    // Không crash, chỉ log
                }
            });
        }

        // Click vào item để mở chi tiết đơn hàng
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });


    }

    private void applyStatusColors(@NonNull OrderViewHolder holder, String statusRaw) {
        if (statusRaw == null) {
            return;
        }
        String status = statusRaw.trim().toLowerCase();

        int bgColor;
        int textColor;
        if (status.contains("processing") || status.contains("đang xử")) {
            bgColor = ContextCompat.getColor(context, R.color.status_processing_bg);
            textColor = ContextCompat.getColor(context, R.color.status_processing_text);
        } else if (status.contains("shipped") || status.contains("đang giao") || status.contains("shipping")) {
            bgColor = ContextCompat.getColor(context, R.color.status_shipping_bg);
            textColor = ContextCompat.getColor(context, R.color.status_shipping_text);
        } else if (status.contains("delivered") || status.contains("đã giao")) {
            bgColor = ContextCompat.getColor(context, R.color.status_delivered_bg);
            textColor = ContextCompat.getColor(context, R.color.status_delivered_text);
        } else if (status.contains("cancelled") || status.contains("canceled") || status.contains("đã hủy") || status.contains("da huy")) {
            bgColor = ContextCompat.getColor(context, R.color.status_cancelled_bg);
            textColor = ContextCompat.getColor(context, R.color.status_cancelled_text);
        } else {
            bgColor = ContextCompat.getColor(context, R.color.divider_light);
            textColor = ContextCompat.getColor(context, R.color.text_secondary);
        }

        holder.tvOrderStatus.setTextColor(textColor);
        android.graphics.drawable.GradientDrawable badge = new android.graphics.drawable.GradientDrawable();
        badge.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        badge.setColor(bgColor);
        float radiusPx = holder.itemView.getResources().getDisplayMetrics().density * 12f;
        badge.setCornerRadius(radiusPx);
        holder.tvOrderStatus.setBackground(badge);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    //  Cập nhật danh sách khi có dữ liệu mới
    public void setOrderList(List<Order> newOrderList) {
        this.orderList.clear();
        this.orderList.addAll(newOrderList);
        notifyDataSetChanged();
    }






    //  ViewHolder khớp với item_order_with_delete.xml
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        public View foregroundView;
        public View deleteBackground;
        ImageView ivOrderThumbnail;
        TextView tvOrderDate, tvOrderStatus, tvTotalPrice, tvMultiItemBadge, tvOrderCode, tvOrderQuantity;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            foregroundView = itemView.findViewById(R.id.foregroundView);
            deleteBackground = itemView.findViewById(R.id.deleteBackground);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvOrderQuantity = itemView.findViewById(R.id.tvOrderQuantity);
            ivOrderThumbnail = itemView.findViewById(R.id.ivOrderThumbnail);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvMultiItemBadge = itemView.findViewById(R.id.tvMultiItemBadge);
        }
    }
}