package com.example.myapplication.network.dto;

public class ForgotPasswordRequest {
    private final String mail;
    private final String newPassword;

    public ForgotPasswordRequest(String mail, String newPassword) {
        this.mail = mail;
        this.newPassword = newPassword;
    }

    public String getMail() {
        return mail;
    }

    public String getNewPassword() {
        return newPassword;
    }
}
