package com.example.myapplication.network.dto;

public class RegisterRequest {
    private final String fullName;
    private final String emailOrPhone;
    private final String password;
    private final String birthDate; // ISO-8601 (yyyy-MM-dd) or empty

    public RegisterRequest(String fullName, String emailOrPhone, String password, String birthDate) {
        this.fullName = fullName;
        this.emailOrPhone = emailOrPhone;
        this.password = password;
        this.birthDate = birthDate;
    }

    public String getFullName() { return fullName; }
    public String getEmailOrPhone() { return emailOrPhone; }
    public String getPassword() { return password; }
    public String getBirthDate() { return birthDate; }
}


