package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.EmployeeAdapter;
import com.example.myapplication.model.Employee;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity implements EmployeeAdapter.EmployeeActionListener {

    private RecyclerView recyclerView;
    private EmployeeAdapter adapter;
    private final List<Employee> data = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quản lý nhân viên");
        }

        recyclerView = findViewById(R.id.recyclerEmployees);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        data.addAll(EmployeeRepository.getInstance().getAll());
        adapter = new EmployeeAdapter(data, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmployeeForm(null);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onEdit(Employee employee) {
        showEmployeeForm(employee);
    }

    @Override
    public void onDelete(Employee employee) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Xóa nhân viên")
                .setMessage("Bạn có chắc muốn xóa " + employee.getName() + "?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (d, w) -> {
                    EmployeeRepository.getInstance().deleteById(employee.getId());
                    reloadData();
                    Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showEmployeeForm(@Nullable Employee employee) {
        View content = getLayoutInflater().inflate(R.layout.dialog_employee_form, null, false);
        TextInputEditText etName = content.findViewById(R.id.etName);
        TextInputEditText etEmail = content.findViewById(R.id.etEmail);
        TextInputEditText etRole = content.findViewById(R.id.etRole);

        boolean isEdit = employee != null;
        if (isEdit) {
            etName.setText(employee.getName());
            etEmail.setText(employee.getEmail());
            etRole.setText(employee.getRole());
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(isEdit ? "Sửa nhân viên" : "Thêm nhân viên")
                .setView(content)
                .setNegativeButton("Hủy", null)
                .setPositiveButton(isEdit ? "Lưu" : "Thêm", (d, w) -> {
                    String name = String.valueOf(etName.getText()).trim();
                    String email = String.valueOf(etEmail.getText()).trim();
                    String role = String.valueOf(etRole.getText()).trim();
                    if (name.isEmpty() || email.isEmpty() || role.isEmpty()) {
                        Toast.makeText(this, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isEdit) {
                        EmployeeRepository.getInstance().update(employee, name, email, role);
                    } else {
                        EmployeeRepository.getInstance().add(name, email, role);
                    }
                    reloadData();
                })
                .show();
    }

    private void reloadData() {
        data.clear();
        data.addAll(EmployeeRepository.getInstance().getAll());
        adapter.notifyDataSetChanged();
    }
}
