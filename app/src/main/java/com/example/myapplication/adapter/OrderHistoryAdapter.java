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
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        //  Hiển thị ngày đặt hàng
        //  Hiển thị ngày đặt hàng
        try {
            String isoDate = order.getOrderDate();
            Date date = isoFormat.parse(isoDate);
            holder.tvOrderDate.setText("Ngày đặt: " + outputFormat.format(date));
        } catch (Exception e) {
            holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());
        }

        //  Hiển thị trạng thái đơn hàng
        String status = order.getOrderStatus();
        holder.tvOrderStatus.setText("Trạng thái: " + (status != null ? status : "N/A"));

        if (status != null) {
            if (status.equalsIgnoreCase("Đã giao") || status.equalsIgnoreCase("Hoàn thành")) {
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else if (status.equalsIgnoreCase("Đang xử lý") || status.equalsIgnoreCase("Processing") || status.equalsIgnoreCase("Đang giao")) {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#FF9800")); // Cam
            } else if (status.equalsIgnoreCase("Đã hủy")) {
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
            } else {
                holder.tvOrderStatus.setTextColor(Color.GRAY);
            }
        }

        //  Hiển thị tổng tiền
        if (order.getCart() != null) {
            double totalPrice = order.getCart().getTotalPrice();
            holder.tvTotalPrice.setText("Tổng tiền: " + currencyFormat.format(totalPrice));
        } else {
            holder.tvTotalPrice.setText("Tổng tiền: N/A");
        }

        // Mặc định ảnh rỗng
        holder.ivOrderThumbnail.setImageResource(R.drawable.img_no_product);
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
                        if (!items.isEmpty()) {
                            String imageUrl = items.get(0).getProduct().getImageURL();

                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.img_no_product)
                                    .error(R.drawable.img_no_product)
                                    .into(holder.ivOrderThumbnail);

                            //  Nếu có nhiều sản phẩm -> hiện badge +n
                            if (items.size() > 1) {
                                holder.tvMultiItemBadge.setVisibility(View.VISIBLE);
                                holder.tvMultiItemBadge.setText("+" + (items.size() - 1));
                            } else {
                                holder.tvMultiItemBadge.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<CartItemsResponse> call, Throwable t) {
                    // Không crash, chỉ log
                }
            });
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });


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






    //  ViewHolder khớp với item_order.xml
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOrderThumbnail;
        TextView tvOrderDate, tvOrderStatus, tvTotalPrice, tvMultiItemBadge;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrderThumbnail = itemView.findViewById(R.id.ivOrderThumbnail);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvMultiItemBadge = itemView.findViewById(R.id.tvMultiItemBadge);
        }
    }
}
