package com.ryuunoakaihitomi.rebootmenu.csc_compat;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.ryuunoakaihitomi.rebootmenu.activity.SendBugFeedback;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 对崩溃报告组件的二次封装和处理
 * Created by ZQY on 2019/3/10.
 */

public class CrashReport {
    private static final String TAG = "CrashReport";

    private CrashReport() {
    }

    /**
     * 开始错误报告
     * 逻辑：如果自动上传配置为假，自动捕捉，否则不做处理（由Crashlytics捕捉）
     *
     * @param context {@link Context}
     * @param handler {@link java.lang.Thread.UncaughtExceptionHandler}
     */
    public static void start(Context context, Thread.UncaughtExceptionHandler handler) {
        long launchTimes = ConfigManager.getPrivateLong(context, ConfigManager.APP_LAUNCH_TIMES, 0);
        /*
         * 第一条件：用户不是一般用户 -> !(安装了酷安，是MIUI系统)
         * 第二条件：自动上报功能关闭，或条件不足以加载csc
         */
        if (!CoolapkCompat.hasCoolapk(context) && !SpecialSupport.isMIUI()) {
            if (!ConfigManager.getPrivateBoolean(context, ConfigManager.AUTO_CRASH_REPORT, false) || !MainCompat.shouldLoadCSC())
                Thread.setDefaultUncaughtExceptionHandler(handler);
        } else {
            //Firebase Crashlytics抓取的机型信息不全面，这里再手动抓一次
            try {
                JSONObject object = new JSONObject(SendBugFeedback.getRawBuildEnvInfo());
                Iterator iterator = object.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    Crashlytics.setString(key, object.getString(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Crashlytics.setLong("launchTimes", launchTimes);
            //小米设备的话看看是不是原版系统
            if ("Xiaomi".equals(Build.MANUFACTURER))
                Crashlytics.setBool("isMIUI", SpecialSupport.isMIUI());
        }
        ConfigManager.setPrivateLong(context, ConfigManager.APP_LAUNCH_TIMES, ++launchTimes);
    }

    /**
     * 设置自动上传
     *
     * @param context   {@link Context}
     * @param crashInfo 包含在日志信息中的崩溃信息
     */
    public static void setAutoSendReport(Context context, String crashInfo) {
        if (!MainCompat.shouldLoadCSC()) return;
        ConfigManager.setPrivateBoolean(context, ConfigManager.AUTO_CRASH_REPORT, true);
        Crashlytics.log(Log.ERROR, TAG, "UncaughtException:" + crashInfo);
    }

    /**
     * 记录非严重异常
     *
     * @param throwable {@link Throwable}
     */
    public static void logNonFatalExceptions(Throwable throwable) {
        if (!MainCompat.shouldLoadCSC()) return;
        Crashlytics.logException(throwable);
    }

    /**
     * crash时附带的log信息
     *
     * @param tag tag
     * @param msg message
     */
    public static void log(String tag, String msg) {
        if (!MainCompat.shouldLoadCSC()) return;
        Crashlytics.log(tag + " : " + msg);
    }

    /**
     * Test exception
     */
    public static void crashEmulator(boolean type) {
        if (type) Crashlytics.getInstance().crash();
        throw new IllegalStateException("Test Exception");
    }
}
