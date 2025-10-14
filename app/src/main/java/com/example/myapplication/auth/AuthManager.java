package com.example.myapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_ROLE = "user_role";
    private static final String KEY_USER_ID = "user_id";

    private final SharedPreferences sharedPreferences;

    public AuthManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuth(String token, String role) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, role)
                .apply();
    }

    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}




