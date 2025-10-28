package com.example.myapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
    
    private List<ChatMessage> messages;
    private SimpleDateFormat timeFormat;
    
    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        if (message.isFromUser()) {
            // Hiển thị tin nhắn từ người dùng (bên phải)
            holder.layoutUserMessage.setVisibility(View.VISIBLE);
            holder.layoutSupportMessage.setVisibility(View.GONE);
            
            holder.textUserMessage.setText(message.getMessage());
            holder.textUserTime.setText(timeFormat.format(message.getTimestamp()));
            
        } else {
            // Hiển thị tin nhắn từ hỗ trợ (bên trái)
            holder.layoutUserMessage.setVisibility(View.GONE);
            holder.layoutSupportMessage.setVisibility(View.VISIBLE);
            
            holder.textSupportMessage.setText(message.getMessage());
            holder.textSupportTime.setText(timeFormat.format(message.getTimestamp()));
        }
    }
    
    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }
    
    public void addMessage(ChatMessage message) {
        if (messages != null) {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUserMessage, layoutSupportMessage;
        TextView textUserMessage, textUserTime;
        TextView textSupportMessage, textSupportTime;
        ImageView imageUserAvatar, imageSupportAvatar;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            
            layoutUserMessage = itemView.findViewById(R.id.layout_user_message);
            layoutSupportMessage = itemView.findViewById(R.id.layout_support_message);
            
            textUserMessage = itemView.findViewById(R.id.text_user_message);
            textUserTime = itemView.findViewById(R.id.text_user_time);
            
            textSupportMessage = itemView.findViewById(R.id.text_support_message);
            textSupportTime = itemView.findViewById(R.id.text_support_time);
            
            imageUserAvatar = itemView.findViewById(R.id.image_user_avatar);
            imageSupportAvatar = itemView.findViewById(R.id.image_support_avatar);
        }
    }
}
