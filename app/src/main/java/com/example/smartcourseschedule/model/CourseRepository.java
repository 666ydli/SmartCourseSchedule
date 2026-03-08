package com.example.smartcourseschedule.model;

import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CourseRepository {
    private final CourseDao courseDao;
    private final ExecutorService executorService;

    @Inject
    public CourseRepository(CourseDao courseDao) {
        this.courseDao = courseDao;
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
        });
    }

    public void deleteCourse(Course course) {
        executorService.execute(() -> courseDao.deleteCourse(course));
    }
    public void updateCourseWithTime(Course course, CourseTime courseTime) {
        executorService.execute(() -> {
            courseDao.updateCourse(course);
            courseDao.updateCourseTime(courseTime);
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
}
