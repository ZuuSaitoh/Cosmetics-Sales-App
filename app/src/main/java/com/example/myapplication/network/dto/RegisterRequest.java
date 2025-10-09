package com.example.myapplication.network.dto;

public class RegisterRequest {
    private final String username;
    private final String password;
    private final String confirm_password;
    private final String email;
    private final String role;

    public RegisterRequest(String username, String password, String confirmPassword, String email) {
        this.username = username;
        this.password = password;
        this.confirm_password = confirmPassword;
        this.email = email;
        this.role = "User";
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getConfirm_password() { return confirm_password; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}


