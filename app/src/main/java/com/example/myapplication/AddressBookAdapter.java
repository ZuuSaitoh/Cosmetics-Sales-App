package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddressBookAdapter extends RecyclerView.Adapter<AddressBookAdapter.AddressViewHolder> {

    public interface AddressActionListener {
        void onDelete(int position);
        void onSetDefault(int position);
    }

    private final List<AddressBookActivity.AddressEntry> items;
    private final AddressActionListener listener;

    public AddressBookAdapter(List<AddressBookActivity.AddressEntry> items, AddressActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AddressViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address, parent, false);
        return new AddressViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressViewHolder holder, int position) {
        AddressBookActivity.AddressEntry e = items.get(position);
        holder.txtName.setText(e.name);
        holder.txtPhone.setText(e.phone);
        holder.txtAddress.setText(e.address);
        holder.txtDefault.setVisibility(e.isDefault ? View.VISIBLE : View.GONE);
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(holder.getAdapterPosition()));
        holder.btnSetDefault.setOnClickListener(v -> listener.onSetDefault(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AddressViewHolder extends RecyclerView.ViewHolder {
        TextView txtName;
        TextView txtPhone;
        TextView txtAddress;
        TextView txtDefault;
        ImageButton btnDelete;
        ImageButton btnSetDefault;

        AddressViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtPhone = itemView.findViewById(R.id.txt_phone);
            txtAddress = itemView.findViewById(R.id.txt_address);
            txtDefault = itemView.findViewById(R.id.txt_default);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
        }
    }
}


