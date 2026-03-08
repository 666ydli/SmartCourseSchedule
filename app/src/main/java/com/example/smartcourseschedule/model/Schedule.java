package com.example.smartcourseschedule.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "schedules")
public class Schedule {
    @PrimaryKey(autoGenerate = true)
    public int scheduleId;

    public String scheduleName; // 例如 "2024秋季学期"

    public Schedule(String scheduleName) {
        this.scheduleName = scheduleName;
    }
}
