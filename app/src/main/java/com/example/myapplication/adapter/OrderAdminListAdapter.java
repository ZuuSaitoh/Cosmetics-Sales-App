package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.Order;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdminListAdapter extends RecyclerView.Adapter<OrderAdminListAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onConfirmClick(Order order);
        void onCancelClick(Order order);
    }

    public OrderAdminListAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_admin_list, parent, false);
        return new OrderViewHolder(view);
    }

    private SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    private SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        if (order == null) return;

        // Order Code
        holder.tvOrderCodeAdmin.setText("Mã đơn hàng: " + order.getOrderID());

        // Order Date
        try {
            String isoDate = order.getOrderDate();
            if (isoDate != null && !isoDate.isEmpty()) {
                Date date = isoFormat.parse(isoDate);
                holder.tvOrderDate.setText(outputFormat.format(date));
            } else {
                holder.tvOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate() : "");
            }
        } catch (Exception e) {
            holder.tvOrderDate.setText(order.getOrderDate() != null ? order.getOrderDate() : "");
        }

        // Order Status với màu sắc động - Chuyển sang tiếng Việt
        String status = order.getOrderStatus() != null ? order.getOrderStatus() : "";
        String statusVietnamese = getStatusInVietnamese(status);
        holder.tvOrderStatus.setText(statusVietnamese);
        applyStatusColors(holder, status);

        // Order Quantity (tạm thời để "1 sản phẩm" vì cần fetch từ CartItem)
        holder.tvOrderQuantity.setText("1 sản phẩm"); // TODO: Fetch actual quantity from CartItem

        // Total Price
        double totalPrice = order.getCart() != null ? order.getCart().getTotalPrice() : 0;
        holder.tvTotalPrice.setText(String.format("%,.0f đ", totalPrice));

        // Hide multi-item badge for now
        holder.layoutMultiItemBadge.setVisibility(View.GONE);

        // Kiểm tra status để enable/disable nút Xác nhận
        // Enable cho cả Processing (để chuyển sang Shipped) và Shipped (để chuyển sang Delivered)
        boolean canConfirm = isProcessingStatus(status) || isShippedStatus(status);
        holder.btnConfirmOrder.setEnabled(canConfirm);
        holder.btnConfirmOrder.setAlpha(canConfirm ? 1.0f : 0.5f); // Visual feedback

        // Kiểm tra status để enable/disable nút Hủy
        // Chỉ enable khi đơn hàng ở trạng thái "Đang xử lý" (Processing)
        // Vô hiệu hóa khi đã "Đang giao" (Shipped) hoặc "Đã giao" (Delivered)
        boolean canCancel = isProcessingStatus(status) && !isShippedStatus(status) && !isDeliveredStatus(status);
        holder.btnCancelOrder.setEnabled(canCancel);
        holder.btnCancelOrder.setAlpha(canCancel ? 1.0f : 0.5f); // Visual feedback

        // Sự kiện click
        holder.btnConfirmOrder.setOnClickListener(v -> {
            if (canConfirm && listener != null) {
                listener.onConfirmClick(order);
            }
        });

        holder.btnCancelOrder.setOnClickListener(v -> {
            if (canCancel && listener != null) {
                listener.onCancelClick(order);
            }
        });
    }

    /**
     * Kiểm tra xem status có phải là "Processing" không
     */
    private boolean isProcessingStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String statusLower = status.trim().toLowerCase();
        return statusLower.contains("processing") || statusLower.contains("đang xử");
    }

    /**
     * Kiểm tra xem status có phải là "Shipped" (Đang giao) không
     */
    private boolean isShippedStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String statusLower = status.trim().toLowerCase();
        return statusLower.contains("shipped") || statusLower.contains("đang giao") || statusLower.contains("shipping");
    }

    /**
     * Kiểm tra xem status có phải là "Delivered" (Đã giao) không
     */
    private boolean isDeliveredStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String statusLower = status.trim().toLowerCase();
        return statusLower.contains("delivered") || statusLower.contains("đã giao");
    }

    /**
     * Chuyển đổi status từ tiếng Anh sang tiếng Việt
     */
    private String getStatusInVietnamese(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Không xác định";
        }
        
        String statusLower = status.trim().toLowerCase();
        
        if (statusLower.contains("processing")) {
            return "Đang xử lý";
        } else if (statusLower.contains("shipped") || statusLower.contains("shipping")) {
            return "Đang giao";
        } else if (statusLower.contains("delivered")) {
            return "Đã giao";
        } else if (statusLower.contains("cancelled") || statusLower.contains("canceled")) {
            return "Đã hủy";
        } else if (statusLower.contains("đang xử")) {
            return "Đang xử lý";
        } else if (statusLower.contains("đang giao")) {
            return "Đang giao";
        } else if (statusLower.contains("đã giao")) {
            return "Đã giao";
        } else if (statusLower.contains("đã hủy") || statusLower.contains("da huy")) {
            return "Đã hủy";
        }
        
        // Nếu không match, trả về status gốc (có thể đã là tiếng Việt)
        return status;
    }

    /**
     * Áp dụng màu sắc cho status badge dựa trên trạng thái đơn hàng
     */
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

    public void setOrderList(List<Order> orders) {
        this.orderList = orders;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderCodeAdmin, tvOrderDate, tvOrderStatus, tvOrderQuantity, tvTotalPrice, tvMultiItemBadge;
        LinearLayout layoutMultiItemBadge;
        com.google.android.material.button.MaterialButton btnCancelOrder, btnConfirmOrder;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);

            tvOrderCodeAdmin = itemView.findViewById(R.id.tvOrderCodeAdmin);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderQuantity = itemView.findViewById(R.id.tvOrderQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvMultiItemBadge = itemView.findViewById(R.id.tvMultiItemBadge);
            layoutMultiItemBadge = itemView.findViewById(R.id.layoutMultiItemBadge);
            btnCancelOrder = itemView.findViewById(R.id.btnCancelOrder);
            btnConfirmOrder = itemView.findViewById(R.id.btnConfirmOrder);
        }
    }
}
