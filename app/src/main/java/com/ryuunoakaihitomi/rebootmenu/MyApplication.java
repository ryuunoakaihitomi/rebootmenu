package com.ryuunoakaihitomi.rebootmenu;

import android.app.Application;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;

import java.lang.reflect.Field;

/**
 * 自分のアプリケーションクラス
 * Created by ZQY on 2018/2/17.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        new DebugLog("起きます");
        //エクスポスドを無効にしてみて
        try {
            Field field = ClassLoader.getSystemClassLoader()
                    .loadClass("de.robv.android.xposed.XposedBridge")
                    .getDeclaredField("disableHooks");
            field.setAccessible(true);
            field.set(null, true);
        } catch (Throwable ignored) {
        }
    }
}
