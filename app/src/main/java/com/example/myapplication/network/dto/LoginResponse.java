package com.example.myapplication.network.dto;

public class LoginResponse {
    private String token;
    private String userId;
    private String role;
    
    // Thêm field để hỗ trợ userId dưới dạng số
    private Integer userID;

    public String getToken() {
        return token;
    }

    public String getUserId() {
        // Nếu userId là null nhưng userID có giá trị, chuyển đổi
        if (userId == null && userID != null) {
            return String.valueOf(userID);
        }
        return userId;
    }

    public String getRole() {
        return role;
    }
    
    // Getter cho userID (Integer)
    public Integer getUserID() {
        return userID;
    }
    
    // Setter cho userID
    public void setUserID(Integer userID) {
        this.userID = userID;
    }
    
    // Getter cho userIdInt (backward compatibility)
    public Integer getUserIdInt() {
        return userID;
    }
    
    // Setter cho userIdInt (backward compatibility)
    public void setUserIdInt(Integer userIdInt) {
        this.userID = userIdInt;
    }
}




