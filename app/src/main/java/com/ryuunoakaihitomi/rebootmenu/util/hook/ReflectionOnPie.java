package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 参考
 * http://weishu.me/2018/06/07/free-reflection-above-android-p/
 * 的第二个条件和
 * https://github.com/tiann/FreeReflection
 * <p>
 * 规避Android P的隐私API调用限制
 * <p>
 * Note:不知道谷歌会将此作为一个安全风险，
 * 但是至少在正式版本的Android P上此法有效
 * <p>
 * Created by ZQY on 2019/1/15.
 */

public class ReflectionOnPie {

    private static final String TAG = "ReflectionOnPie";

    /**
     * HiddenApiEnforcementPolicy置零
     *
     * @param context {@link ApplicationInfo}
     */
    public static void zeroHAEP(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clearClassLoaderInClass(ReflectionOnPie.class);
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            synchronized (ReflectionOnPie.class) {
                try {
                    @SuppressWarnings("JavaReflectionMemberAccess")
                    @SuppressLint("PrivateApi")
                    Method setHiddenApiEnforcementPolicy = ApplicationInfo.class
                            .getDeclaredMethod("setHiddenApiEnforcementPolicy", int.class);
                    setHiddenApiEnforcementPolicy.invoke(applicationInfo, 0);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else
            Log.w(TAG, "zeroHAEP: SDK_INT<28,skipping...");
    }

    /**
     * 用Unsafe清除classloader
     * 该方法仅对同一个类生效，所以必须在需要的类中添加使用，不可以跨类调用
     *
     * @param cls 需要清除Loader的类
     */
    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
    public static void clearClassLoaderInClass(Class cls) {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeInstanceField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeInstanceField.setAccessible(true);
            Object unsafeInstance = unsafeInstanceField.get(null);
            Method objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
            Field classLoaderField = Class.class.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            Method putObject = unsafeClass.getMethod("putObject", Object.class, long.class, Object.class);
            long offset = (long) objectFieldOffset.invoke(unsafeInstance, classLoaderField);
            Log.i(TAG, "clearClassLoaderInClass: classLoader offset=" + offset);
            putObject.invoke(unsafeInstance, cls, offset, null);
        } catch (Throwable throwable) {
            Log.e(TAG, "clearClassLoaderInClass: ", throwable);
        }
    }
}
