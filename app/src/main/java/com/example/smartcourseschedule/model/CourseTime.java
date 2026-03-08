package com.example.smartcourseschedule.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// 定义外键，关联 Course 表的 courseId。
// onDelete = ForeignKey.CASCADE 意味着：如果一门课被删了，它对应的上课时间也会被自动全删掉，防止产生垃圾数据。
@Entity(tableName = "course_times",
        foreignKeys = @ForeignKey(entity = Course.class,
                parentColumns = "courseId",
                childColumns = "courseId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("courseId")})
public class CourseTime {
    @PrimaryKey(autoGenerate = true)
    public int timeId;

    public int courseId; // 外键，对应 Course 表的 courseId

    public int weeksBitmask; // 上课周次（利用位运算，极大地优化存储和查询）
    public int dayOfWeek; // 星期几 (1-7)
    public int startPeriod; // 开始节次 (如：1)
    public int endPeriod; // 结束节次 (如：2)
    public String location; // 教室地点
}
