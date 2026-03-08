package com.example.smartcourseschedule.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.smartcourseschedule.utils.TodayCourseWidget;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class CourseRepository {

    private final CourseDao courseDao;
    private final ExecutorService executorService;
    private final Context appContext;

    @Inject
    public CourseRepository(CourseDao courseDao, @ApplicationContext Context appContext) {
        this.courseDao = courseDao;
        this.appContext = appContext;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Schedule>> getAllSchedules() {
        return courseDao.getAllSchedules();
    }

    public void insertSchedule(String name) {
        executorService.execute(() -> courseDao.insertSchedule(new Schedule(name)));
    }

    public LiveData<List<CourseWithTimes>> getCoursesBySchedule(int scheduleId) {
        return courseDao.getCoursesBySchedule(scheduleId);
    }

    public void insertCourseWithTime(Course course, CourseTime courseTime) {
        executorService.execute(() -> {
            long id = courseDao.insertCourse(course);
            courseTime.courseId = (int) id;
            courseDao.insertCourseTime(courseTime);
            notifyWidgetRefresh();
        });
    }

    public void deleteCourse(Course course) {
        executorService.execute(() -> {
            courseDao.deleteCourse(course);
            notifyWidgetRefresh();
        });
    }

    public void updateCourseWithTime(Course course, CourseTime courseTime) {
        executorService.execute(() -> {
            courseDao.updateCourse(course);
            courseDao.updateCourseTime(courseTime);
            notifyWidgetRefresh();
        });
    }

    public void updateSchedule(Schedule schedule) {
        executorService.execute(() -> courseDao.updateSchedule(schedule));
    }

    public void insertHomework(Homework homework) {
        executorService.execute(() -> {
            courseDao.insertHomework(homework);
        });
    }

    public LiveData<List<Homework>> getHomeworkByCourse(int courseId) {
        return courseDao.getHomeworkByCourse(courseId);
    }

    public void deleteHomework(Homework homework) {
        executorService.execute(() -> courseDao.deleteHomework(homework));
    }

    public void deleteSchedule(Schedule schedule) {
        executorService.execute(() -> courseDao.deleteSchedule(schedule));
    }

    private void notifyWidgetRefresh() {
        Log.d("CourseRepository", "send widget refresh broadcast");

        Intent intent = new Intent(appContext, TodayCourseWidget.class);
        intent.setAction(TodayCourseWidget.ACTION_REFRESH_WIDGET);
        appContext.sendBroadcast(intent);
    }
}
