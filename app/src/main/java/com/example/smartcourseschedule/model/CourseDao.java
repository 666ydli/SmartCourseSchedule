package com.example.smartcourseschedule.model;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CourseDao {

    // --- 课表库 (Schedule) 操作 ---

    @Insert
    long insertSchedule(Schedule schedule);

    @Query("SELECT * FROM schedules")
    LiveData<List<Schedule>> getAllSchedules();

    @Query("SELECT * FROM schedules LIMIT 1")
    Schedule getFirstScheduleSync(); // 用于初始化默认课表

    // --- 课程 (Course) 操作 ---

    @Insert
    long insertCourse(Course course);

    @Delete
    void deleteCourse(Course course);

    // 核心：查询特定课表下的所有课程及其时间
    @Transaction
    @Query("SELECT * FROM courses WHERE scheduleId = :scheduleId")
    LiveData<List<CourseWithTimes>> getCoursesBySchedule(int scheduleId);

    // --- 上课时间 (CourseTime) 操作 ---

    @Insert
    void insertCourseTime(CourseTime courseTime);

    // --- 作业 (Homework) 操作 ---

    @Insert
    void insertHomework(Homework homework);
    @Transaction
    @Query("SELECT * FROM courses JOIN course_times ON courses.courseId = course_times.courseId WHERE course_times.dayOfWeek = :dayOfWeek")
    List<CourseWithTimes> getTodayCoursesSync(int dayOfWeek);

    @Update
    void updateCourse(Course course);

    @Update
    void updateCourseTime(CourseTime courseTime);
    @Update
    void updateSchedule(Schedule schedule);
    @Query("SELECT * FROM homeworks WHERE courseId = :courseId ORDER BY homeworkId DESC")
    LiveData<List<Homework>> getHomeworkByCourse(int courseId);

    @Delete
    void deleteHomework(Homework homework);

    @Update
    void updateHomework(Homework homework);
    @Delete
    void deleteSchedule(Schedule schedule);

}
