package com.example.smartcourseschedule.viewmodel;

import com.example.smartcourseschedule.model.CourseTime;

public class ConflictUtils {

    // 基础方法：对比两个对象
    public static boolean isConflicting(CourseTime t1, CourseTime t2) {
        if (t1.dayOfWeek != t2.dayOfWeek) return false;
        return t1.startPeriod <= t2.endPeriod && t2.startPeriod <= t1.endPeriod;
    }

    // 重载方法：直接对比数值（用于编辑和添加逻辑）
    public static boolean isConflicting(int day, int start, int end, CourseTime ct) {
        if (day != ct.dayOfWeek) return false;
        // 逻辑：(A开始 <= B结束) 且 (B开始 <= A结束) 说明有交集
        return start <= ct.endPeriod && ct.startPeriod <= end;
    }
}
