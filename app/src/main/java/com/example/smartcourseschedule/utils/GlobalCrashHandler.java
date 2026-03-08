package com.example.smartcourseschedule.utils;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

public class GlobalCrashHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public GlobalCrashHandler(Context context) {
        this.context = context;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        // 1. 这里可以收集错误日志，发送到你的服务器（如 Bugly 或 Firebase）
        Log.e("CRASH", "检测到严重 Bug: " + e.getMessage());

        // 2. 让 App 优雅地“凉掉”，而不是卡死
        // 商业项目中，这里通常会跳转到一个专门的“错误反馈页面”
        defaultHandler.uncaughtException(t, e);
    }
}
