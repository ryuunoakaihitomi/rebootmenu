package com.ryuunoakaihitomi.rebootmenu;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.SELinux;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.activity.SendBugFeedback;
import com.ryuunoakaihitomi.rebootmenu.csc_compat.CrashReport;
import com.ryuunoakaihitomi.rebootmenu.csc_compat.EventStatistics;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;
import com.ryuunoakaihitomi.rebootmenu.util.StringUtils;
import com.ryuunoakaihitomi.rebootmenu.util.hook.ReflectionOnPie;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 自定义Application：禁用xposed，异常捕捉，调试/系统属性
 * Created by ZQY on 2018/2/17.
 */


public class MyApplication extends Application implements Thread.UncaughtExceptionHandler {

    public static boolean isDebug, isSystemApp;
    private static final String TAG = "MyApplication";

    //keep reference
    private static void checkSELinuxStatus() {
        String context = null;
        boolean isEnabled = false, isEnforced = false;
        try {
            context = SELinux.getContext();
            isEnabled = SELinux.isSELinuxEnabled();
            isEnforced = SELinux.isSELinuxEnforced();
        } catch (Throwable throwable) {
            Log.w(TAG, "checkSELinuxStatus: ", throwable);
        }
        //noinspection ConstantConditions
        Log.i(TAG, "checkSELinuxStatus: Security Context:" + context + " is(Enabled/Enforced):" + StringUtils.varArgsToString(isEnabled, isEnforced));
    }

    /**
     * 判断本应用是否为debug包
     *
     * @return boolean
     */
    private boolean isDebuggable() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    /**
     * 判断是否是系统应用
     *
     * @return boolean
     */
    private boolean isSystemApp() {
        return (getApplicationInfo().flags & ApplicationInfo.FLAG_SYSTEM) > 0;
    }

    /**
     * 日志记录线程保持
     * 注意：会长时间连续写入，不用时请及时关闭
     */
    private void logcatHolder() {
        new Thread(() -> {
            String logFileName = "rbm_" + System.currentTimeMillis() + Base64.encodeToString(
                    (Build.FINGERPRINT).getBytes(), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP) + ".log";
            Log.d(TAG, "logFN=" + logFileName);
            try {
                //刷新缓冲，防止进入之前的日志混入
                Runtime.getRuntime().exec("logcat -c");
                java.lang.Process process = Runtime.getRuntime().exec("logcat -v threadtime -f " + getExternalFilesDir("logcat") + "/" + logFileName);
                int code = process.waitFor();
                //除了read: unexpected EOF!导致的一般不可能退出
                Log.e(TAG, "exit! code:" + code);
            } catch (IOException e) {
                Log.e(TAG, "IOException");
            } catch (InterruptedException e) {
                Log.e(TAG, "InterruptedException");
            }
        }).start();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        DebugLog.d(TAG, "uncaughtException");
        String crashTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS"
                , Locale.getDefault()).format(new Date());
        new DebugLog(throwable, "FATAL:uncaughtException tid:" + thread.getId(), true);
        //用户友好：阻止弹出已停止运行窗口
        SystemClock.sleep(1000);
        SendBugFeedback.actionStart(getApplicationContext()
                , crashTime, StringUtils.printStackTraceToString(throwable));
        Process.killProcess(Process.myPid());
        System.exit(-1);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        checkSELinuxStatus();
        //按需记录自身日志
        if (new File(Environment.getExternalStorageDirectory().getPath() + "/rbmLogWriter").exists())
            logcatHolder();
        //初始化属性值
        isDebug = isDebuggable();
        isSystemApp = isSystemApp();
        //应该等到isDebug初始化完了再log要不然查看到的isDebug值恒为假（又一个粗心造成的惨案）
        new DebugLog("MyApplication.onCreate: (尾北) isSystem:" + isSystemApp, DebugLog.LogLevel.I);
        //包检测
        Log.i(TAG, "APK_PACK_INFO: " + BuildConfig.APK_PACK_TIME + ' ' + (isDebug ? "dbg" : "rls"));
        //捕捉异常
        CrashReport.start(this, this);
        //初始化统计组件
        EventStatistics.initComponent(this);
        ConfigManager.initDir(this);
        //尝试禁用xposed，并统计是否安装Xposed
        boolean hasXposed = false;
        try {
            Field field = ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedBridge")
                    .getDeclaredField("disableHooks");
            field.setAccessible(true);
            field.set(null, true);
            hasXposed = true;
        } catch (Throwable t) {
            new DebugLog(t, "disableXposed", false);
        }
        EventStatistics.record(EventStatistics.HAS_XPOSED, String.valueOf(hasXposed));
        //无障碍服务的保活用通知只能让系统屏蔽，所以要特别注意让Toast不会因此消失
        //只有MIUI已经修复了这个问题
        // 从Android Q开始也修复了这个问题，而且反射会出错：
        // java.lang.SecurityException: Calling uid 10087 gave package android which is owned by uid 1000
        //TODO 傻Android Q Beta 1似乎还没有把API Level升上去(仍然是28)...
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !SpecialSupport.isMIUI())
            TextToast.defineSystemToast();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ReflectionOnPie.zeroHAEP(base);
    }
}
