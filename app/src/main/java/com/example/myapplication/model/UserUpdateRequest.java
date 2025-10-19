package com.example.myapplication.model;

public class UserUpdateRequest {
    private String phoneNumber;
    private String address;
    private String role;
    private String email;
    private String username; // Thêm field username

    public UserUpdateRequest(String phoneNumber, String address, String role, String email) {
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.email = email;
        this.username = ""; // Default empty
    }
    
    public UserUpdateRequest(String phoneNumber, String address, String role, String email, String username) {
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.email = email;
        this.username = username;
    }

    // getters
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
}
