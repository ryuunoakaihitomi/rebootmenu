package com.ryuunoakaihitomi.rebootmenu;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Process;
import android.os.SystemClock;

import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;

import java.lang.reflect.Field;

/**
 * 自定义Application：禁用xposed，异常捕捉，调试/系统属性
 * Created by ZQY on 2018/2/17.
 */


@SuppressWarnings("WeakerAccess")
public class MyApplication extends Application implements Thread.UncaughtExceptionHandler {

    public static boolean isDebug, isSystemApp;

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化属性值
        isDebug = isDebuggable();
        isSystemApp = isSystemApp();
        //应该等到isDebug初始化完了再log要不然查看到的isDebug值恒为假（又一个粗心造成的惨案）
        new DebugLog("MyApplication.onCreate: (尾北) isSystem:" + isSystemApp, DebugLog.LogLevel.I);
        //捕捉异常
        Thread.setDefaultUncaughtExceptionHandler(this);
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
        new DebugLog(throwable, "FATAL:uncaughtException tid:" + thread.getId(), true);
        //用户友好：阻止弹出已停止运行窗口
        SystemClock.sleep(1000);
        Process.killProcess(Process.myPid());
    }

    /**
     * 判断本应用是否为debug包
     *
     * @return boolean
     */
    boolean isDebuggable() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * 判断是否是系统应用
     *
     * @return boolean
     */
    boolean isSystemApp() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }
}
