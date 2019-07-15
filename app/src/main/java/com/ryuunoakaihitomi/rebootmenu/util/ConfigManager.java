package com.ryuunoakaihitomi.rebootmenu.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 配置管理器
 * Created by ZQY on 2018/2/8.
 */

public class ConfigManager {

    //设置选项列表
    public static final String
            WHITE_THEME = "wt",
            NO_NEED_TO_CONFIRM = "nntc",
            CANCELABLE = "c",
            DO_NOT_CHECK_ROOT = "dncr",
            UNROOT_MODE = "urm",
            LATEST_RELEASE_DOWNLOAD_ID = "lrdi",
            AUTO_CRASH_REPORT = "acr",
            APP_LAUNCH_TIMES = "alt",
            AUTO_NOAD = "ana";

    private static SharedPreferences pref;
    private static SharedPreferences.Editor editor;

    public static boolean get(@DefCfgOptions String key) {
        boolean isExists = new File(path + "/" + key).exists();
        new DebugLog("ConfigManager.get: key:" + key + " isExists:" + isExists, DebugLog.LogLevel.I);
        return isExists;
    }

    //父目录
    private static String path;

    //初始化外部files目录
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void initDir(@NonNull Context context) {
        File file = context.getExternalFilesDir(null);
        if (file != null) {
            path = file.getPath();
            //除了内置存储外，总是试图往外置存储创建目录，但貌似并无权限，因此总返回假
            file.mkdirs();
        }
        new DebugLog("initDir: path=" + path, DebugLog.LogLevel.I);
    }

    /**
     * @param key   键
     * @param value 值
     * @return 是否成功
     */
    public static boolean set(@DefCfgOptions String key, boolean value) {
        File f = new File(path + "/" + key);
        return value ? f.mkdirs() : f.delete();
    }

    public static long getPrivateLong(Context context, @DefCfgOptions String key, long def) {
        initSharedPreferencesAndEditor(context);
        return pref.getLong(key, def);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean getPrivateBoolean(Context context, @DefCfgOptions String key, boolean def) {
        initSharedPreferencesAndEditor(context);
        return pref.getBoolean(key, def);
    }

    public static void setPrivateBoolean(Context context, @DefCfgOptions String key, boolean val) {
        initSharedPreferencesAndEditor(context);
        editor.putBoolean(key, val).apply();
    }

    private ConfigManager() {
    }

    public static void setPrivateLong(Context context, @DefCfgOptions String key, long val) {
        initSharedPreferencesAndEditor(context);
        editor.putLong(key, val).apply();
    }

    @SuppressLint("CommitPrefEdits")
    private static void initSharedPreferencesAndEditor(Context context) {
        if (pref == null || editor == null) {
            pref = context.getSharedPreferences(null, Context.MODE_PRIVATE);
            editor = pref.edit();
        }
    }

    //默认配置选项注解
    @StringDef({WHITE_THEME, NO_NEED_TO_CONFIRM, CANCELABLE,
            DO_NOT_CHECK_ROOT, UNROOT_MODE, LATEST_RELEASE_DOWNLOAD_ID,
            AUTO_CRASH_REPORT, APP_LAUNCH_TIMES, AUTO_NOAD})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DefCfgOptions {
    }
}
