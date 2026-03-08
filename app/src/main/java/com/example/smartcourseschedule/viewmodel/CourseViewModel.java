package com.example.smartcourseschedule.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.smartcourseschedule.model.Course;
import com.example.smartcourseschedule.model.CourseRepository;
import com.example.smartcourseschedule.model.CourseTime;
import com.example.smartcourseschedule.model.CourseWithTimes;
import com.example.smartcourseschedule.model.Schedule;

import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CourseViewModel extends ViewModel {
    private final CourseRepository repository;

    @Inject
    public CourseViewModel(CourseRepository repository) {
        this.repository = repository;
    }

    // --- 新增：关联 Schedule 的方法 ---

    public LiveData<List<Schedule>> getAllSchedules() {
        return repository.getAllSchedules();
    }

    public void insertSchedule(String name) {
        repository.insertSchedule(name);
    }

    public LiveData<List<CourseWithTimes>> getCoursesBySchedule(int scheduleId) {
        return repository.getCoursesBySchedule(scheduleId);
    }

    // --- 原有方法 ---

    public void insertCourseWithTime(Course course, CourseTime courseTime) {
        repository.insertCourseWithTime(course, courseTime);
    }

    public void deleteCourse(Course course) {
        repository.deleteCourse(course);
    }
    public void updateCourseWithTime(Course course, CourseTime courseTime) {
        repository.updateCourseWithTime(course, courseTime);
    }
    public void updateSchedule(Schedule schedule) {
        repository.updateSchedule(schedule);
    }
    public void insertHomework(com.example.smartcourseschedule.model.Homework homework) {
        repository.insertHomework(homework);
    }
    public LiveData<List<com.example.smartcourseschedule.model.Homework>> getHomeworkByCourse(int courseId) {
        return repository.getHomeworkByCourse(courseId);
    }
    public void deleteHomework(com.example.smartcourseschedule.model.Homework homework) {
        repository.deleteHomework(homework);
    }
    public void deleteSchedule(Schedule schedule) {
        repository.deleteSchedule(schedule);
    }
}
