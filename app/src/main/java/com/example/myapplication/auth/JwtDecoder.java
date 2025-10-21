package com.example.myapplication.auth;

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JwtDecoder {
    private static final String TAG = "JwtDecoder";

    /**
     * Decode JWT token và lấy userId từ claims
     * @param token JWT token
     * @return userId nếu tìm thấy, null nếu không
     */
    public static Long getUserIdFromToken(String token) {
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Token is null or empty");
            return null;
        }

        try {
            // JWT token có format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.w(TAG, "Invalid JWT format - expected 3 parts, got: " + parts.length);
                return null;
            }

            // Decode payload (phần thứ 2) mà không cần verify signature
            String payload = parts[1];

            // Decode base64
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            Log.d(TAG, "Decoded JWT payload: " + decodedPayload);

            // Parse JSON để lấy userId
            return extractUserIdFromPayload(decodedPayload);

        } catch (Exception e) {
            Log.e(TAG, "Error decoding JWT token", e);
            return null;
        }
    }

    /**
     * Extract userId từ JSON payload
     */
    private static Long extractUserIdFromPayload(String payload) {
        try {
            // Tìm userId trong JSON payload
            // Có thể có các format khác nhau: "userId": 1, "user_id": 1, "id": 1, "sub": "1"

            // Thử tìm "userId"
            String userIdPattern = "\"userId\"\\s*:\\s*(\\d+)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(userIdPattern);
            java.util.regex.Matcher matcher = pattern.matcher(payload);

            if (matcher.find()) {
                String userIdStr = matcher.group(1);
                Long userId = Long.parseLong(userIdStr);
                Log.d(TAG, "Found userId in JWT: " + userId);
                return userId;
            }

            // Thử tìm "user_id"
            String userIdPattern2 = "\"user_id\"\\s*:\\s*(\\d+)";
            pattern = java.util.regex.Pattern.compile(userIdPattern2);
            matcher = pattern.matcher(payload);

            if (matcher.find()) {
                String userIdStr = matcher.group(1);
                Long userId = Long.parseLong(userIdStr);
                Log.d(TAG, "Found user_id in JWT: " + userId);
                return userId;
            }

            // Thử tìm "id"
            String idPattern = "\"id\"\\s*:\\s*(\\d+)";
            pattern = java.util.regex.Pattern.compile(idPattern);
            matcher = pattern.matcher(payload);

            if (matcher.find()) {
                String userIdStr = matcher.group(1);
                Long userId = Long.parseLong(userIdStr);
                Log.d(TAG, "Found id in JWT: " + userId);
                return userId;
            }

            // Thử tìm "sub" (subject - thường là userId trong JWT)
            String subPattern = "\"sub\"\\s*:\\s*\"?(\\d+)\"?";
            pattern = java.util.regex.Pattern.compile(subPattern);
            matcher = pattern.matcher(payload);

            if (matcher.find()) {
                String userIdStr = matcher.group(1);
                Long userId = Long.parseLong(userIdStr);
                Log.d(TAG, "Found sub in JWT: " + userId);
                return userId;
            }

            Log.w(TAG, "No userId found in JWT payload: " + payload);
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Error extracting userId from payload", e);
            return null;
        }
    }

    private static String extractRoleFromPayload(String payload) {
        try {
            Pattern pattern = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(payload);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting role from payload", e);
        }
        return null;
    }

    public static String getRoleFromToken(String token) {
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Token is null or empty");
            return null;
        }

        try {
            // JWT token có format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                Log.w(TAG, "Invalid JWT format - expected 3 parts, got: " + parts.length);
                return null;
            }

            // Decode payload (phần thứ 2) mà không cần verify signature
            String payload = parts[1];

            // Decode base64
            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            Log.d(TAG, "Decoded JWT payload: " + decodedPayload);

            // Parse JSON để lấy role
            return extractRoleFromPayload(decodedPayload);

        } catch (Exception e) {
            Log.e(TAG, "Error decoding JWT token", e);
            return null;
        }
    }


    /**
     * Kiểm tra token có phải JWT không
     */
    public static boolean isJwtToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String[] parts = token.split("\\.");
        return parts.length == 3;
    }

    /**
     * Lấy thông tin cơ bản từ JWT token
     */
    public static JwtInfo getJwtInfo(String token) {
        if (!isJwtToken(token)) {
            return null;
        }

        try {
            String[] parts = token.split("\\.");
            String payload = parts[1];

            byte[] decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE);
            String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);

            Long userId = extractUserIdFromPayload(decodedPayload);

            return new JwtInfo(userId, decodedPayload);

        } catch (Exception e) {
            Log.e(TAG, "Error getting JWT info", e);
            return null;
        }
    }

    /**
     * Class để chứa thông tin JWT
     */
    public static class JwtInfo {
        public final Long userId;
        public final String payload;

        public JwtInfo(Long userId, String payload) {
            this.userId = userId;
            this.payload = payload;
        }
    }
}
