package com.example.smartcourseschedule.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CourseWithTimes {
    @Embedded
    public Course course; // 嵌入课程主表信息

    @Relation(
            parentColumn = "courseId",
            entityColumn = "courseId"
    )
    public List<CourseTime> times; // 关联的上课时间列表
}
