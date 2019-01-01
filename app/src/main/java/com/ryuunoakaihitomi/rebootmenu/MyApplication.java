package com.ryuunoakaihitomi.rebootmenu;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Process;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.util.LogPrinter;

import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 自定义Application：禁用xposed，异常捕捉，调试/系统属性
 * Created by ZQY on 2018/2/17.
 */


public class MyApplication extends Application implements Thread.UncaughtExceptionHandler {

    public static boolean isDebug, isSystemApp;
    private static final String TAG = "MyApplication";

    //
    private void coolapkOrMe() {
        final String CA_PKG_NAME = "com.coolapk.market";
        final String CA_URL = "https://www.coolapk.com/apk/com.ryuunoakaihitomi.rebootmenu";
        LogPrinter printer = new LogPrinter(Log.VERBOSE, TAG);
        try {
            printer.println("Coolapk, versionName:"
                    + getPackageManager().getPackageInfo(CA_PKG_NAME, 0).versionName);
            Intent toCoolForum = new Intent()
                    .setData(Uri.parse("market://details?id=" + getPackageName()))
                    //按back键从这个任务返回的时候会回到home，防止返回重复进入
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                    .setPackage(CA_PKG_NAME);
            startActivity(toCoolForum);
            System.exit(0);
        } catch (PackageManager.NameNotFoundException ignored) {
            printer.println("Me");
        } catch (ActivityNotFoundException ignored) {
            printer.println("ActivityNotFoundException,CA is still here but frozen.");
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CA_URL))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME));
                System.exit(0);
            } catch (ActivityNotFoundException | SecurityException ignore) {
                printer.println(CA_URL);
                ShellUtils.suCmdExec("pm uninstall " + getPackageName());
                startActivity(new Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.fromParts("package", getPackageName(), null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
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
    void logcatHolder() {
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
    public void onCreate() {
        super.onCreate();
        coolapkOrMe();
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
}
