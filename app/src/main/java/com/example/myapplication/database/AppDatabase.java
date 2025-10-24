package com.example.myapplication.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.myapplication.model.Notification;
import com.example.myapplication.notification.NotificationDao;

/**
 * Room Database class cho ứng dụng
 * Quản lý tất cả các entities và DAOs
 */
@Database(
    entities = {Notification.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    // Tên database
    private static final String DATABASE_NAME = "cosmetics_sales_app.db";
    
    // Singleton instance
    private static volatile AppDatabase INSTANCE;
    
    // ============ DAO ABSTRACT METHODS ============
    
    /**
     * Lấy NotificationDao để thao tác với Notification table
     */
    public abstract NotificationDao notificationDao();
    
    // ============ SINGLETON PATTERN ============
    
    /**
     * Lấy instance của database (Singleton)
     * Thread-safe với double-checked locking
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    // Cho phép queries trên main thread (chỉ dùng cho testing/development)
                    // Production nên xóa dòng này và dùng async operations
                    // .allowMainThreadQueries()
                    
                    // Thêm callback khi database được tạo
                    .addCallback(roomCallback)
                    
                    // Thêm migrations nếu cần
                    // .addMigrations(MIGRATION_1_2)
                    
                    // Fallback: destroy và rebuild database nếu migration fails
                    // CHÚ Ý: Chỉ dùng trong development, production nên có migration strategy
                    .fallbackToDestructiveMigration()
                    
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Destroy instance (dùng cho testing)
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }
    
    // ============ DATABASE CALLBACKS ============
    
    /**
     * Callback khi database được tạo lần đầu
     */
    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            // Có thể thêm logic khởi tạo data mẫu ở đây
            // Ví dụ: insert default notifications, categories, etc.
        }
        
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // Logic khi database được mở
        }
    };
    
    // ============ MIGRATIONS ============
    
    /**
     * Migration từ version 1 sang version 2
     * Ví dụ: Thêm column mới vào table
     */
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Ví dụ: Thêm column mới
            // database.execSQL("ALTER TABLE notifications ADD COLUMN new_column TEXT");
        }
    };
    
    /**
     * Migration từ version 2 sang version 3
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Logic migration
        }
    };
}

