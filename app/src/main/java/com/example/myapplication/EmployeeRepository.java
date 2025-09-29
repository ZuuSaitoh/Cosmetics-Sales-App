package com.example.myapplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class EmployeeRepository {
    private static final EmployeeRepository INSTANCE = new EmployeeRepository();
    private final List<Employee> employees = new ArrayList<>();

    private EmployeeRepository() {
        // Seed sample data
        employees.add(new Employee(UUID.randomUUID().toString(), "Nguyen Van A", "a@example.com", "Nhân viên"));
        employees.add(new Employee(UUID.randomUUID().toString(), "Tran Thi B", "b@example.com", "Quản lý"));
    }

    public static EmployeeRepository getInstance() {
        return INSTANCE;
    }

    public List<Employee> getAll() {
        return Collections.unmodifiableList(employees);
    }

    public Employee add(String name, String email, String role) {
        Employee e = new Employee(UUID.randomUUID().toString(), name, email, role);
        employees.add(e);
        return e;
    }

    public void update(Employee employee, String name, String email, String role) {
        employee.setName(name);
        employee.setEmail(email);
        employee.setRole(role);
    }

    public void deleteById(String id) {
        Iterator<Employee> it = employees.iterator();
        while (it.hasNext()) {
            if (it.next().getId().equals(id)) {
                it.remove();
                return;
            }
        }
    }
}
