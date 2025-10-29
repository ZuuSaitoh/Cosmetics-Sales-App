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
        
        // NotificationId
        if (dto.getNotificationId() != null) {
            notification.setNotificationId(dto.getNotificationId().intValue());
        }
        
        // UserId - convert Long to String
        if (dto.getUserId() != null) {
            notification.setUserId(String.valueOf(dto.getUserId()));
        } else {
            notification.setUserId("");  // Default empty string
        }
        
        // Title
        notification.setTitle(dto.getTitle());
        
        // Message
        if (dto.getMessage() != null) {
            notification.setMessage(dto.getMessage());
        } else {
            notification.setMessage("");  // Default empty string
        }
        
        // NotificationType
        notification.setNotificationType(dto.getNotificationType());
        
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
     * (Dùng khi cần gửi data lên server)
     */
    public static NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        NotificationDTO dto = new NotificationDTO();
        
        dto.setNotificationId((long) notification.getNotificationId());
        
        // Parse userId from String to Long
        try {
            dto.setUserId(Long.parseLong(notification.getUserId()));
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid userId format: " + notification.getUserId());
            dto.setUserId(0L);
        }
        
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setNotificationType(notification.getNotificationType());
        dto.setIsRead(notification.isRead());
        
        // Convert timestamp to ISO datetime string
        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        dto.setCreatedAt(isoFormat.format(new Date(notification.getCreatedAt())));
        
        dto.setDataPayload(notification.getDataPayload());
        
        return dto;
    }
}

