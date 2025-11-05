package com.example.myapplication.auth;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {
    private final AuthManager authManager;

    public TokenInterceptor(AuthManager authManager) {
        this.authManager = authManager;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        String token = authManager.getToken();
        Request original = chain.request();
        if (token == null || token.isEmpty()) {
            return chain.proceed(original);
        }
        Request withAuth = original.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();
        return chain.proceed(withAuth);
    }
}















