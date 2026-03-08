package com.example.smartcourseschedule.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses",
        foreignKeys = @ForeignKey(entity = Schedule.class,
                parentColumns = "scheduleId",
                childColumns = "scheduleId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("scheduleId")})
public class Course {
    @PrimaryKey(autoGenerate = true)
    public int courseId;

    public int scheduleId; // 外键：所属课表ID

    public String courseName;
    public String teacherName;
    public float credits;
    public String colorCode;
}
