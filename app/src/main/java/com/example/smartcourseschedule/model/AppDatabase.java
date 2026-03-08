package com.example.smartcourseschedule.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. 必须把 Schedule.class 加进去
// 2. 版本号必须提升到 2
@Database(entities = {Course.class, CourseTime.class, Homework.class, Schedule.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract CourseDao courseDao();

    private static volatile AppDatabase INSTANCE;
    static final androidx.room.migration.Migration MIGRATION_1_2 = new androidx.room.migration.Migration(1, 2) {
        @Override
        public void migrate(@NonNull androidx.sqlite.db.SupportSQLiteDatabase database) {
            // 1. 创建新表 schedules
            database.execSQL("CREATE TABLE IF NOT EXISTS `schedules` (`scheduleId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `scheduleName` TEXT)");

            // 2. 给 courses 表增加 scheduleId 字段（并设置默认值，防止旧数据出错）
            database.execSQL("ALTER TABLE `courses` ADD COLUMN `scheduleId` INTEGER NOT NULL DEFAULT 0");

            // 3. 为外键列创建索引（优化性能）
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_courses_scheduleId` ON `courses` (`scheduleId`) ");
        }
    };

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "smart_course_database")
                            .fallbackToDestructiveMigration() // 重点：找不到迁移路径时，允许直接删除旧表重建
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
