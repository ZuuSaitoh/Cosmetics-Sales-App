package com.example.myapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

import com.mapbox.core.utils.TextUtils;

public class AuthManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_ROLE = "user_role";

    private final SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        // Dùng getApplicationContext() để đảm bảo SharedPreferences dùng 1 bản duy nhất toàn app
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuth(String token, String role) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Kiểm tra trạng thái đăng nhập dựa trên sự tồn tại của token.
     */
    public boolean isLoggedIn() {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        return token != null && !token.isEmpty();
    }
}




