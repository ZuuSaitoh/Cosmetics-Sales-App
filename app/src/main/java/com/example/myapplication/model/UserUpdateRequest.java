package com.example.myapplication.model;

public class UserUpdateRequest {
    private String phoneNumber;
    private String address;
    private String role;
    private String email;

    public UserUpdateRequest(String phoneNumber, String address, String role, String email) {
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.email = email;
    }

    // getters
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
}
