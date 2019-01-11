package com.ryuunoakaihitomi.rebootmenu.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

/**
 * 为特殊的Android环境提供支持
 * Created by ZQY on 2018/12/31.
 *
 * @author ZQY
 * <p>
 * Note:特殊环境数量较少，调试困难
 * 所以，为了找bug，在任何时候都保持logcat输出
 * 不适用{@link DebugLog}，改用{@link android.util.Log}
 */

public class SpecialSupport {
    private static final String TAG = "SpecialSupport";

    /**
     * 检测是否是WearOS(Android Wear)系统
     * Note:com.google.android.wearable.app，希望有更好的方法
     *
     * @param context {@link android.content.Context}
     * @return bool
     */
    public static boolean isAndroidWearOS(Context context) {
        try {
            PackageManager mgr = context.getPackageManager();
            PackageInfo info = mgr.getPackageInfo("com.google.android.wearable.app", 0);
            Log.i(TAG, "isAndroidWearOS: App Version:" + info.versionName);
            return (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 检测是否是MIUI系统
     *
     * @return bool
     */
    public static boolean isMIUI() {
        //android.os.SystemProperties.get("ro.miui.ui.version.name", "");
        // 如果返回值是「V10」，就是 MIUI 10
        String miuiVersionName;
        try {
            miuiVersionName = SystemProperties.get("ro.miui.ui.version.name", "");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
        Log.d(TAG, "isMIUI: miuiVersionName:" + miuiVersionName);
        return !TextUtils.isEmpty(miuiVersionName);
    }

    /**
     * 检测是不是Android TV设备
     *
     * @param context {@link PackageManager}
     * @return boolean
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasTvFeature(Context context) {
        return context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LIVE_TV);
    }
}
