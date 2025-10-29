package com.example.myapplication.network.dto;

import android.util.Log;

import com.example.myapplication.model.Notification;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Mapper để convert giữa NotificationDTO (API) và Notification (Entity)
 */
public class NotificationMapper {
    
    private static final String TAG = "NotificationMapper";
    
    // Các format datetime có thể có từ backend
    private static final SimpleDateFormat[] DATE_FORMATS = {
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()),
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()),
        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    };
    
    /**
     * Convert NotificationDTO sang Notification entity
     */
    public static Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Notification notification = new Notification();
        
        // NotificationId - LOGGING để trace conversion
        if (dto.getNotificationId() != null) {
            Long dtoId = dto.getNotificationId();
            int entityId = dtoId.intValue();
            
            Log.d(TAG, "Converting NotificationId - DTO(Long): " + dtoId + " → Entity(int): " + entityId);
            
            notification.setNotificationId(entityId);
        } else {
            Log.w(TAG, "DTO notificationId is NULL!");
        }
        
        // UserId - extract từ nested user object
        Long userId = dto.getUserId();  // Helper method đã extract từ user.getUserID()
        if (userId != null) {
            notification.setUserId(String.valueOf(userId));
            Log.d(TAG, "Extracted userId from nested user object: " + userId);
        } else {
            notification.setUserId("");  // Default empty string
            Log.w(TAG, "UserId is null - user object may be missing");
        }
        
        // Title - backend không trả về, tạo default
        String title = dto.getTitle();
        if (title == null || title.isEmpty()) {
            // Tạo default title từ message hoặc "Thông báo"
            String message = dto.getMessage();
            if (message != null && !message.isEmpty()) {
                // Lấy 30 ký tự đầu làm title
                title = message.length() > 30 ? message.substring(0, 30) + "..." : message;
            } else {
                title = "Thông báo";  // Default title
            }
            Log.d(TAG, "Title is null, using default: " + title);
        }
        notification.setTitle(title);
        
        // Message
        if (dto.getMessage() != null) {
            notification.setMessage(dto.getMessage());
        } else {
            notification.setMessage("");  // Default empty string
        }
        
        // NotificationType - backend không trả về, default SYSTEM
        String type = dto.getNotificationType();
        if (type == null || type.isEmpty()) {
            type = "SYSTEM";  // Default type
            Log.d(TAG, "NotificationType is null, using default: SYSTEM");
        }
        notification.setNotificationType(type);
        
        // IsRead
        if (dto.getIsRead() != null) {
            notification.setRead(dto.getIsRead());
        } else {
            notification.setRead(false);  // Default false
        }
        
        // CreatedAt - parse String datetime to long timestamp
        long timestamp = parseCreatedAt(dto.getCreatedAt());
        notification.setCreatedAt(timestamp);
        
        // DataPayload
        notification.setDataPayload(dto.getDataPayload());
        
        return notification;
    }
    
    /**
     * Convert danh sách NotificationDTO sang Notification entities
     */
    public static List<Notification> toEntityList(List<NotificationDTO> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }
        
        List<Notification> notifications = new ArrayList<>();
        for (NotificationDTO dto : dtoList) {
            try {
                Notification notification = toEntity(dto);
                if (notification != null) {
                    notifications.add(notification);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error converting DTO to Entity: " + dto, e);
                // Continue với các items khác
            }
        }
        
        return notifications;
    }
    
    /**
     * Parse createdAt từ String sang long timestamp
     * Hỗ trợ nhiều format datetime khác nhau
     */
    private static long parseCreatedAt(String createdAtStr) {
        if (createdAtStr == null || createdAtStr.isEmpty()) {
            return System.currentTimeMillis();  // Default to now
        }
        
        // Thử parse as number (nếu backend trả về timestamp)
        try {
            return Long.parseLong(createdAtStr);
        } catch (NumberFormatException e) {
            // Not a number, try parse as date string
        }
        
        // Thử parse as datetime string
        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                Date date = format.parse(createdAtStr);
                if (date != null) {
                    return date.getTime();
                }
            } catch (ParseException e) {
                // Try next format
            }
        }
        
        // Nếu không parse được, log warning và return current time
        Log.w(TAG, "Could not parse createdAt: " + createdAtStr + ", using current time");
        return System.currentTimeMillis();
    }
    
    /**
     * Convert Notification entity sang NotificationDTO
     * (KHÔNG CẦN DÙNG - chỉ cần DTO → Entity để đọc từ API)
     * 
     * Method này được comment vì backend response có nested user object,
     * không thể convert ngược lại từ Entity (userId String) → DTO (user Object)
     */
    /*
    public static NotificationDTO toDTO(Notification notification) {
        // Not implemented - backend response structure không support
        // Backend expect nested user object, không phải userId đơn thuần
        throw new UnsupportedOperationException(
            "Cannot convert Entity to DTO - backend uses nested user object"
        );
    }
    */
}

