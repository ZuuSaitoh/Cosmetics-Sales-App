package com.example.myapplication.notification;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.model.Notification;

import java.util.List;

/**
 * Data Access Object (DAO) cho Notification
 * Định nghĩa các phương thức CRUD để tương tác với database
 */
@Dao
public interface NotificationDao {
    
    // ============ INSERT OPERATIONS ============
    
    /**
     * Thêm một notification mới vào database
     * @param notification Notification cần thêm
     * @return ID của notification vừa thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertNotification(Notification notification);
    
    /**
     * Thêm nhiều notifications cùng lúc
     * @param notifications Danh sách notifications
     * @return Mảng IDs của các notifications vừa thêm
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertNotifications(List<Notification> notifications);
    
    // ============ UPDATE OPERATIONS ============
    
    /**
     * Cập nhật thông tin notification
     * @param notification Notification cần cập nhật
     * @return Số lượng records đã cập nhật
     */
    @Update
    int updateNotification(Notification notification);
    
    /**
     * Đánh dấu notification là đã đọc
     * @param notificationId ID của notification
     */
    @Query("UPDATE notifications SET is_read = 1 WHERE notification_id = :notificationId")
    void markAsRead(int notificationId);
    
    /**
     * Đánh dấu tất cả notifications của user là đã đọc
     * @param userId ID của user
     */
    @Query("UPDATE notifications SET is_read = 1 WHERE user_id = :userId")
    void markAllAsRead(String userId);
    
    /**
     * Đánh dấu notification là chưa đọc
     * @param notificationId ID của notification
     */
    @Query("UPDATE notifications SET is_read = 0 WHERE notification_id = :notificationId")
    void markAsUnread(int notificationId);
    
    // ============ DELETE OPERATIONS ============
    
    /**
     * Xóa một notification
     * @param notification Notification cần xóa
     */
    @Delete
    void deleteNotification(Notification notification);
    
    /**
     * Xóa notification theo ID
     * @param notificationId ID của notification cần xóa
     */
    @Query("DELETE FROM notifications WHERE notification_id = :notificationId")
    void deleteNotificationById(int notificationId);
    
    /**
     * Xóa tất cả notifications của một user
     * @param userId ID của user
     */
    @Query("DELETE FROM notifications WHERE user_id = :userId")
    void deleteAllNotificationsByUser(String userId);
    
    /**
     * Xóa tất cả notifications đã đọc của user
     * @param userId ID của user
     */
    @Query("DELETE FROM notifications WHERE user_id = :userId AND is_read = 1")
    void deleteReadNotifications(String userId);
    
    /**
     * Xóa tất cả notifications
     */
    @Query("DELETE FROM notifications")
    void deleteAllNotifications();
    
    // ============ SELECT/QUERY OPERATIONS ============
    
    /**
     * Lấy tất cả notifications của một user (LiveData - tự động update UI)
     * Sắp xếp theo thời gian tạo (mới nhất trước)
     * @param userId ID của user
     * @return LiveData chứa danh sách notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC")
    LiveData<List<Notification>> getAllNotifications(String userId);
    
    /**
     * Lấy tất cả notifications của user (không phải LiveData)
     * @param userId ID của user
     * @return Danh sách notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC")
    List<Notification> getAllNotificationsSync(String userId);
    
    /**
     * Lấy các notifications chưa đọc của user (LiveData)
     * @param userId ID của user
     * @return LiveData chứa danh sách notifications chưa đọc
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND is_read = 0 ORDER BY created_at DESC")
    LiveData<List<Notification>> getUnreadNotifications(String userId);
    
    /**
     * Lấy số lượng notifications chưa đọc (LiveData)
     * @param userId ID của user
     * @return LiveData chứa số lượng notifications chưa đọc
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    LiveData<Integer> getUnreadCount(String userId);
    
    /**
     * Lấy số lượng notifications chưa đọc (sync)
     * @param userId ID của user
     * @return Số lượng notifications chưa đọc
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND is_read = 0")
    int getUnreadCountSync(String userId);
    
    /**
     * Lấy một notification theo ID
     * @param notificationId ID của notification
     * @return Notification object
     */
    @Query("SELECT * FROM notifications WHERE notification_id = :notificationId")
    Notification getNotificationById(int notificationId);
    
    /**
     * Lấy notification theo ID (LiveData)
     * @param notificationId ID của notification
     * @return LiveData chứa Notification
     */
    @Query("SELECT * FROM notifications WHERE notification_id = :notificationId")
    LiveData<Notification> getNotificationByIdLive(int notificationId);
    
    /**
     * Lấy notifications theo loại
     * @param userId ID của user
     * @param type Loại notification (CART, ORDER, etc.)
     * @return LiveData chứa danh sách notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND notification_type = :type ORDER BY created_at DESC")
    LiveData<List<Notification>> getNotificationsByType(String userId, String type);
    
    /**
     * Lấy các cart notifications chưa đọc
     * @param userId ID của user
     * @return LiveData chứa danh sách cart notifications chưa đọc
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId AND notification_type = 'CART' AND is_read = 0 ORDER BY created_at DESC")
    LiveData<List<Notification>> getUnreadCartNotifications(String userId);
    
    /**
     * Lấy số lượng cart notifications chưa đọc
     * @param userId ID của user
     * @return Số lượng cart notifications chưa đọc
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId AND notification_type = 'CART' AND is_read = 0")
    int getUnreadCartNotificationCount(String userId);
    
    /**
     * Lấy N notifications mới nhất
     * @param userId ID của user
     * @param limit Số lượng notifications cần lấy
     * @return Danh sách notifications
     */
    @Query("SELECT * FROM notifications WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit")
    List<Notification> getRecentNotifications(String userId, int limit);
    
    /**
     * Xóa các notifications cũ (quá X ngày)
     * @param timestamp Timestamp cutoff (notifications cũ hơn sẽ bị xóa)
     */
    @Query("DELETE FROM notifications WHERE created_at < :timestamp")
    void deleteOldNotifications(long timestamp);
    
    /**
     * Lấy tổng số notifications của user
     * @param userId ID của user
     * @return Tổng số notifications
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE user_id = :userId")
    int getTotalNotificationCount(String userId);
}

