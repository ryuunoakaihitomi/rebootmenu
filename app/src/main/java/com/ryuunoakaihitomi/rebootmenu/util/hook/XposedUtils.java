package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

public class XposedUtils {
    private static final String TAG = "XposedUtils";

    /**
     * 检测本应用Xposed模块部分是否已经被起启用
     * <p>
     * Note:
     * 使用Method不仅代码赘余，而且可能会hook失败
     */
    public static boolean isActive = false;

    /**
     * 受SELinux的制约，从L开始，服务名称要加user.前缀
     * 从O开始不允许服务注入，用使用频率较少的TvInputService代替
     *
     * @param baseName tag
     * @return string
     */
    @SuppressWarnings("SameParameterValue")
    static String getServiceName(String baseName) {
        String ret;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            ret = baseName;
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                ret = Context.TV_INPUT_SERVICE;
            else
                ret = "user." + baseName;
        }
        Log.d(TAG, "getServiceName: " + ret);
        return ret;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasTvFeature(Context context) {
        PackageManager pm = context.getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_LIVE_TV);
    }
}
