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
        this.sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuth(String token, String role, Long userId) {
        android.util.Log.d("AuthManager", "Saving auth - Token: " + (token != null ? "exists" : "null"));
        android.util.Log.d("AuthManager", "Saving auth - Role: " + role);
        android.util.Log.d("AuthManager", "Saving auth - UserID: " + userId);

        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_ROLE, role)
                .putLong(KEY_USER_ID, userId != null ? userId : -1L)
                .apply();

        android.util.Log.d("AuthManager", "Auth saved successfully");
    }

    // Overload method để backward compatibility
    public void saveAuth(String token, String role) {
        saveAuth(token, role, null);
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public Long getUserId() {
        long userId = sharedPreferences.getLong(KEY_USER_ID, -1L);
        Long result = userId == -1L ? null : userId;
        android.util.Log.d("AuthManager", "Getting UserID from SharedPreferences: " + result);

        // Nếu không có userId trong SharedPreferences, thử decode từ token
        if (result == null) {
            String token = getToken();
            if (token != null && !token.isEmpty()) {
                android.util.Log.d("AuthManager", "Trying to decode userId from JWT token");
                result = JwtDecoder.getUserIdFromToken(token);
                android.util.Log.d("AuthManager", "Decoded UserID from JWT: " + result);

                // Lưu userId đã decode vào SharedPreferences để lần sau không cần decode lại
                if (result != null) {
                    sharedPreferences.edit()
                            .putLong(KEY_USER_ID, result)
                            .apply();
                    android.util.Log.d("AuthManager", "Saved decoded userId to SharedPreferences");
                }
            }
        }

        return result;
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
        android.util.Log.d("AuthManager", "Cleared all auth data");
    }

    /**
     * Clear chỉ userId để force decode lại từ token
     */
    public void clearUserId() {
        sharedPreferences.edit()
                .remove(KEY_USER_ID)
                .apply();
        android.util.Log.d("AuthManager", "Cleared userId, will decode from token next time");
    }

    /**
     * Force refresh userId từ token
     */
    public Long refreshUserId() {
        android.util.Log.d("AuthManager", "Force refreshing userId from token");
        clearUserId();
        return getUserId();
    }

    public boolean isLoggedIn() {
        String token = sharedPreferences.getString(KEY_TOKEN, null);
        return token != null && !token.isEmpty();
    }
}
