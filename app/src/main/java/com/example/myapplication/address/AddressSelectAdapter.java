package com.example.myapplication.address;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class AddressSelectAdapter extends RecyclerView.Adapter<AddressSelectAdapter.VH> {
    public interface Listener {
        void onSelect(int position);
        void onSetDefault(int position);
        void onEdit(int position);
        void onDelete(int position);
    }

    private final List<AddressEntry> data;
    private int selectedIndex;
    private final Listener listener;

    public AddressSelectAdapter(List<AddressEntry> data, int selectedIndex, Listener listener) {
        this.data = data;
        this.selectedIndex = selectedIndex;
        this.listener = listener;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_select, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AddressEntry e = data.get(position);
        // Hiển thị name, nếu null thì dùng "Người nhận"
        h.txtName.setText(e.name != null && !e.name.trim().isEmpty() ? e.name : "Người nhận");
        // Hiển thị phone, nếu null thì ẩn hoặc hiển thị "Chưa có SĐT"
        if (e.phone != null && !e.phone.trim().isEmpty()) {
            h.txtPhone.setText(e.phone);
            h.txtPhone.setVisibility(View.VISIBLE);
        } else {
            h.txtPhone.setText("");
            h.txtPhone.setVisibility(View.GONE);
        }
        // Hiển thị address
        h.txtAddress.setText(e.address != null ? e.address : "");
        h.txtDefault.setVisibility(e.isDefault ? View.VISIBLE : View.GONE);
        h.radio.setChecked(position == selectedIndex);

        h.itemView.setOnClickListener(v -> listener.onSelect(h.getBindingAdapterPosition()));
        h.radio.setOnClickListener(v -> listener.onSelect(h.getBindingAdapterPosition()));
        h.btnSetDefault.setOnClickListener(v -> listener.onEdit(h.getBindingAdapterPosition()));
        h.btnDelete.setOnClickListener(v -> listener.onDelete(h.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtPhone, txtAddress, txtDefault;
        RadioButton radio;
        ImageButton btnSetDefault, btnDelete;
        VH(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtPhone = itemView.findViewById(R.id.txt_phone);
            txtAddress = itemView.findViewById(R.id.txt_address);
            txtDefault = itemView.findViewById(R.id.txt_default);
            radio = itemView.findViewById(R.id.radio_select);
            btnSetDefault = itemView.findViewById(R.id.btn_set_default);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}


