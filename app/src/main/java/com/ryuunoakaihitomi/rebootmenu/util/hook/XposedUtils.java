package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.content.Context;
import android.os.Build;
import android.os.SELinux;
import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.SELinuxHelper;

/**
 * 在Xposed内外部都使用的一些工具
 * <p>
 * Created by ZQY on 2019/1/6.
 */

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
        if (!isSELinuxPatrolling()) {
            ret = baseName;
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                ret = baseName;
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ret = Context.TV_INPUT_SERVICE;
                else
                    ret = "user." + baseName;
            }
        }
        Log.d(TAG, "getServiceName: " + ret);
        return ret;
    }

    /**
     * 检查SELinux是否启用或者处于Enforce模式
     * 使用两套API，一套来自Xposed，一套来自隐私API。
     * SELinux必须完全禁用才能保证自由（或者API工作可能不稳定）
     * Note:目前还不知道为什么SELinux.isSELinuxEnforced在untrusted_app权限下enforce状态下仍返回false
     *
     * @return boolean
     */
    public static boolean isSELinuxPatrolling() {
        //安全起见，假设为真
        @SuppressWarnings("UnusedAssignment") boolean ret = true;
        try {
            ret = SELinuxHelper.isSELinuxEnabled() || SELinuxHelper.isSELinuxEnforced();
        } catch (Throwable t) {
            if (!(t instanceof NoClassDefFoundError))
                t.printStackTrace();
            try {
                ret = SELinux.isSELinuxEnabled() || SELinux.isSELinuxEnforced();
            } catch (Throwable ignored) {
            }
        }
        return ret;
    }

    /**
     * 可变长参数转字符串
     *
     * @param objects Arrays.toString
     * @return {@link String}
     */
    public static String varArgsToString(Object... objects) {
        return Arrays.toString(objects);
    }
}
