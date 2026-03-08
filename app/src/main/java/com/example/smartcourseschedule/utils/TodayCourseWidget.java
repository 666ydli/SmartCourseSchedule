package com.example.smartcourseschedule.utils;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.smartcourseschedule.R;

public class TodayCourseWidget extends AppWidgetProvider {

    public static final String ACTION_REFRESH_WIDGET =
            "com.example.smartcourseschedule.ACTION_REFRESH_WIDGET";

    private static final String TAG = "TodayCourseWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate called, count = " + appWidgetIds.length);

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, WidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            @SuppressLint("RemoteViewLayout")
            RemoteViews views = new RemoteViews(
                    context.getPackageName(),
                    R.layout.widget_today_schedule
            );

            views.setRemoteAdapter(R.id.widget_list, intent);
            views.setEmptyView(R.id.widget_list, R.id.widget_empty);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent == null) return;

        Log.d(TAG, "onReceive action = " + intent.getAction());

        if (ACTION_REFRESH_WIDGET.equals(intent.getAction())) {
            Log.d(TAG, "ACTION_REFRESH_WIDGET received");

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, TodayCourseWidget.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);

            Log.d(TAG, "widget ids count = " + appWidgetIds.length);

            if (appWidgetIds != null && appWidgetIds.length > 0) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
                onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
    }
}
