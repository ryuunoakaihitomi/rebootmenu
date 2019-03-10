package com.ryuunoakaihitomi.rebootmenu.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

/**
 * 配置管理器
 * Created by ZQY on 2018/2/8.
 */

public class ConfigManager {

    //设置选项列表
    public static final String
            WHITE_THEME = "wt",
            NO_NEED_TO_COMFIRM = "nntc",
            CANCELABLE = "c",
            DO_NOT_CHECK_ROOT = "dncr",
            UNROOT_MODE = "urm",
            LASTEST_RELEASE_DOWNLOAD_ID = "lrdi";

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
        SharedPreferences pref = context.getSharedPreferences(null, Context.MODE_PRIVATE);
        return pref.getLong(key, def);
    }

    private ConfigManager() {
    }

    public static void setPrivateLong(Context context, @DefCfgOptions String key, long val) {
        context.getSharedPreferences(null, Context.MODE_PRIVATE).edit().putLong(key, val).apply();
    }

    //默认配置选项注解
    @StringDef({WHITE_THEME, NO_NEED_TO_COMFIRM, CANCELABLE, DO_NOT_CHECK_ROOT, UNROOT_MODE, LASTEST_RELEASE_DOWNLOAD_ID})
    @Retention(RetentionPolicy.SOURCE)
    private @interface DefCfgOptions {
    }
}
