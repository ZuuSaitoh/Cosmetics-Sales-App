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

import com.bumptech.glide.Glide; // Import thư viện Glide
import com.example.myapplication.R;
import com.example.myapplication.model.CartItem;
import com.example.myapplication.model.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
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

        // Set ngày
        if (order.getOrderDate() != null) {
            holder.tvOrderDate.setText("Ngày đặt: " + dateFormat.format(order.getOrderDate()));
        } else {
            holder.tvOrderDate.setText("Ngày đặt: N/A");
        }

        // Set trạng thái và màu sắc
        String status = order.getOrderStatus();
        holder.tvOrderStatus.setText("Trạng thái: " + (status != null ? status : "N/A"));

        if (status != null) {
            if (status.equalsIgnoreCase("Đã giao") || status.equalsIgnoreCase("Hoàn thành")) {
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
            } else if (status.equalsIgnoreCase("Đang xử lý") || status.equalsIgnoreCase("Đang giao")) {
                holder.tvOrderStatus.setTextColor(Color.parseColor("#FF9800")); // Cam
            } else if (status.equalsIgnoreCase("Đã hủy")) {
                holder.tvOrderStatus.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
            } else {
                holder.tvOrderStatus.setTextColor(Color.GRAY);
            }
        }

        // Set ảnh bằng Glide
//        String imageUrl = order.getProduct().getImageURL();
//        Glide.with(holder.ivOrderThumbnail.getContext())
//                .load(imageUrl)
//                .placeholder(R.drawable.img_no_product)
//                .error(R.drawable.img_no_product)
//                .into(holder.ivOrderThumbnail);

        // TODO: Thêm OnClickListener cho item nếu cần
        // holder.itemView.setOnClickListener(v -> { ... });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Cập nhật danh sách khi có dữ liệu mới
    public void setOrderList(List<Order> newOrderList) {
        this.orderList.clear();
        this.orderList.addAll(newOrderList);
        notifyDataSetChanged();
    }

    // ViewHolder khớp với item_order.xml
    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivOrderThumbnail;
        TextView tvOrderDate;
        TextView tvOrderStatus;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOrderThumbnail = itemView.findViewById(R.id.ivOrderThumbnail);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
        }
    }
}