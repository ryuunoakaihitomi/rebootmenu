package com.ryuunoakaihitomi.rebootmenu;

import android.app.Application;
import android.os.Process;
import android.os.SystemClock;

import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;

import java.lang.reflect.Field;

/**
 * 自定义Application：禁用xposed，异常捕捉
 * Created by ZQY on 2018/2/17.
 */


@SuppressWarnings("WeakerAccess")
public class MyApplication extends Application implements Thread.UncaughtExceptionHandler {
    @Override
    public void onCreate() {
        super.onCreate();
        //捕捉异常
        Thread.setDefaultUncaughtExceptionHandler(this);
        new DebugLog("MyApplication.onCreate: (尾北)", DebugLog.LogLevel.I);
        ConfigManager.initDir(this);
        //尝试禁用xposed
        try {
            Field field = ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedBridge")
                    .getDeclaredField("disableHooks");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Throwable t) {
            new DebugLog(t, "disableXposed", false);
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        new DebugLog(throwable, "FATAL:uncaughtException", true);
        //用户友好：阻止弹出已停止运行窗口
        SystemClock.sleep(1000);
        Process.killProcess(Process.myPid());
    }
}
