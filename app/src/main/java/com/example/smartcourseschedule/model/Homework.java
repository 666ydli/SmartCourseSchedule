package com.example.smartcourseschedule.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "homeworks",
        foreignKeys = @ForeignKey(entity = Course.class,
                parentColumns = "courseId",
                childColumns = "courseId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("courseId")})
public class Homework {
    @PrimaryKey(autoGenerate = true)
    public int homeworkId;

    public int courseId;
    public String content;
    public long reminderTime; // 存储提醒的毫秒数，0 表示不提醒
    public boolean isCompleted;

    public Homework(int courseId, String content, long reminderTime) {
        this.courseId = courseId;
        this.content = content;
        this.reminderTime = reminderTime;
        this.isCompleted = false;
    }
}
