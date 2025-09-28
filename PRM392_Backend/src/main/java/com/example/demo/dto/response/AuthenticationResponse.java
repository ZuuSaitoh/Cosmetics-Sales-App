package com.example.demo.dto.response;

public class AuthenticationResponse {
    private String token;
    private boolean authenticated;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public AuthenticationResponse(String token, boolean authenticated) {
        this.token = token;
        this.authenticated = authenticated;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
