package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    public interface EmployeeActionListener {
        void onEdit(Employee employee);

        void onDelete(Employee employee);
    }

    private final List<Employee> employees;
    private final EmployeeActionListener listener;

    public EmployeeAdapter(List<Employee> employees, EmployeeActionListener listener) {
        this.employees = employees;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee employee = employees.get(position);
        holder.name.setText(employee.getName());
        holder.email.setText(employee.getEmail());
        holder.role.setText(employee.getRole());
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(employee));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(employee));
    }

    @Override
    public int getItemCount() {
        return employees.size();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView email;
        TextView role;
        ImageButton btnEdit;
        ImageButton btnDelete;

        EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            email = itemView.findViewById(R.id.tvEmail);
            role = itemView.findViewById(R.id.tvRole);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
