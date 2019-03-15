package com.ryuunoakaihitomi.rebootmenu.csc_compat;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Map;

/**
 * 对统计事件组件的二次封装
 * Created by ZQY on 2018/6/2.
 */

public class EventStatistics {

    //eventID选项
    @SuppressWarnings("WeakerAccess")
    public final static String
            //配置
            CONFIG_DATA = "configData",
    //有xposed
    HAS_XPOSED = "hasXposed",
    //选项选择
    OPTION_SELECTION = "optionSelection",
    //AD加载状态码
    AD_STATUS_CODE = "adErrorCode";
    private static FirebaseAnalytics analytics;

    private EventStatistics() {
    }

    /**
     * 初始化统计组件
     *
     * @param context {@link Context}
     */
    public static void initComponent(Context context) {
        if (MainCompat.shouldLoadCSC())
            analytics = FirebaseAnalytics.getInstance(context);
    }

    public static void record(String tag, String msg) {
        if (analytics == null) return;
        Bundle bundle = new Bundle();
        bundle.putString("string", msg);
        analytics.logEvent(tag, bundle);
    }

    public static void record(String tag, Map<String, String> map) {
        if (analytics != null) {
            Bundle bundle = new Bundle();
            for (String key : map.keySet()) bundle.putString(key, map.get(key));
            analytics.logEvent(tag, bundle);
        }
    }
}
