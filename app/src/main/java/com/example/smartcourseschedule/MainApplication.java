package com.example.smartcourseschedule;

import android.app.Application;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 注册全局异常捕获
        Thread.setDefaultUncaughtExceptionHandler(new com.example.smartcourseschedule.utils.GlobalCrashHandler(this));
    }

}

