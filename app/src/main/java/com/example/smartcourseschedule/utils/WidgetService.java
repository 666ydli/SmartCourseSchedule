package com.example.smartcourseschedule.utils;

import android.content.Context;
import android.content.Intent;
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
    public void onDataSetChanged() {
        // 核心逻辑：从数据库同步获取今日课程
        int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        // Calendar 周日是1，周一是2... 转换为我们数据库的 1-7
        int dbDay = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;

        // 这里不使用注入，直接获取数据库单例
        data = AppDatabase.getInstance(context).courseDao().getTodayCoursesSync(dbDay);
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (data == null || position >= data.size()) return null;

        CourseWithTimes item = data.get(position);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_item_course);

        views.setTextViewText(R.id.widget_item_name, item.course.courseName);
        if (!item.times.isEmpty()) {
            CourseTime t = item.times.get(0);
            views.setTextViewText(R.id.widget_item_time, t.startPeriod + "-" + t.endPeriod + "节");
            views.setTextViewText(R.id.widget_item_loc, "📍 " + t.location);
        }

        // 设置点击此项跳转 App 的意图
        Intent fillInIntent = new Intent();
        views.setOnClickFillInIntent(R.id.widget_item_name, fillInIntent);

        return views;
    }

    @Override public int getCount() { return data.size(); }
    @Override public void onCreate() {}
    @Override public void onDestroy() { data.clear(); }
    @Override public RemoteViews getLoadingView() { return null; }
    @Override public int getViewTypeCount() { return 1; }
    @Override public long getItemId(int position) { return position; }
    @Override public boolean hasStableIds() { return true; }
}
