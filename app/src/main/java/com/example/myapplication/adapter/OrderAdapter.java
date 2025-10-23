package com.example.myapplication.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private final OnOrderClickListener listener;

    // Interface để xử lý sự kiện click
    public interface OnOrderClickListener {
        void onClick(Order order);
    }

    public OrderAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        if (order == null) return;

        // Hiển thị mã đơn hàng
        holder.tvOrderCode.setText("#ORD" + order.getOrderID());

        // Hiển thị ngày đặt
        holder.tvOrderDate.setText("Ngày đặt: " + order.getOrderDate());

        // Hiển thị trạng thái
        String status = order.getOrderStatus();
        if (status != null) {
            switch (status.toUpperCase()) {
                case "PROCESSING":
                    holder.tvOrderStatus.setText("Trạng thái: Đang xử lý");
                    holder.tvOrderStatus.setTextColor(Color.parseColor("#FFA726")); // Cam
                    break;
                case "DELIVERED":
                    holder.tvOrderStatus.setText("Trạng thái: Đã giao");
                    holder.tvOrderStatus.setTextColor(Color.parseColor("#388E3C")); // Xanh lá
                    break;
                case "CANCELLED":
                    holder.tvOrderStatus.setText("Trạng thái: Đã hủy");
                    holder.tvOrderStatus.setTextColor(Color.parseColor("#D32F2F")); // Đỏ
                    break;
                default:
                    holder.tvOrderStatus.setText("Trạng thái: " + status);
                    holder.tvOrderStatus.setTextColor(Color.parseColor("#555555"));
                    break;
            }
        } else {
            holder.tvOrderStatus.setText("Trạng thái: Không xác định");
        }

        // Hiển thị phương thức thanh toán (hoặc tổng tiền nếu backend trả về)
        holder.tvOrderTotal.setText("Phương thức: " + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "Không có"));

        // Sự kiện khi nhấn vào item
        holder.itemView.setOnClickListener(v -> listener.onClick(order));
    }

    @Override
    public int getItemCount() {
        return (orderList != null) ? orderList.size() : 0;
    }

    // ✅ ViewHolder
    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCode, tvOrderDate, tvOrderStatus, tvOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}
