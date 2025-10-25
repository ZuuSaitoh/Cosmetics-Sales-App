package com.example.myapplication.network.dto;

public class ForgotPasswordResponse {
    private int code;
    private String result;

    public ForgotPasswordResponse() {}

    public ForgotPasswordResponse(int code, String result) {
        this.code = code;
        this.result = result;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
