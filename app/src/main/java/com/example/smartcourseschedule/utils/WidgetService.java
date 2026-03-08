package com.example.smartcourseschedule.utils;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.smartcourseschedule.R;
import com.example.smartcourseschedule.model.AppDatabase;
import com.example.smartcourseschedule.model.CourseTime;
import com.example.smartcourseschedule.model.CourseWithTimes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CourseRemoteViewsFactory(this.getApplicationContext());
    }
}

class CourseRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private List<CourseWithTimes> data = new ArrayList<>();

    public CourseRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Log.d("WidgetService", "onDataSetChanged called");

        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int dbDay = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
        data = AppDatabase.getInstance(context).courseDao().getTodayCoursesSync(dbDay);

        Log.d("WidgetService", "today course count = " + (data == null ? 0 : data.size()));
    }

    @Override
    public void onDestroy() {
        data.clear();
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (data == null || position < 0 || position >= data.size()) return null;

        CourseWithTimes item = data.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item_course);

        views.setTextViewText(R.id.widget_item_name, item.course.courseName);

        if (item.times != null && !item.times.isEmpty()) {
            CourseTime t = item.times.get(0);
            views.setTextViewText(R.id.widget_item_time, t.startPeriod + "-" + t.endPeriod + "节");
            views.setTextViewText(R.id.widget_item_loc,
                    (t.location == null || t.location.trim().isEmpty()) ? "No location" : t.location);
        } else {
            views.setTextViewText(R.id.widget_item_time, "--");
            views.setTextViewText(R.id.widget_item_loc, "No location");
        }

        Intent fillInIntent = new Intent();
        fillInIntent.putExtra("schedule_id", item.course.scheduleId);
        fillInIntent.putExtra("schedule_name", "我的课表");
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        if (data == null || position < 0 || position >= data.size()) return position;
        return data.get(position).course.courseId;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
