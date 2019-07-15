package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 参考
 * http://weishu.me/2018/06/07/free-reflection-above-android-p/
 * 的第二个条件和
 * https://github.com/tiann/FreeReflection
 * <p>
 * 至Android Q Beta 4
 * http://weishu.me/2019/03/16/another-free-reflection-above-android-p/
 * <p>
 * 规避Android P的隐私API调用限制
 * <p>
 * Note:不知道谷歌会将此作为一个安全风险，
 * 但是至少在正式版本的Android P上此法有效
 * <p>
 * Created by ZQY on 2019/1/15.
 */

@SuppressWarnings("JavaReflectionMemberAccess")
public class ReflectionOnPie {

    private static final String TAG = "ReflectionOnPie";

    /**
     * 初始化Android非公开API限制规避
     *
     * @param context {@link ApplicationInfo}
     */
    public static void init(Context context) {
        //Android Q
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            Class<?> vmRuntimeCls = null;
            try {
                //目前的限制策略似乎并不包括对类的访问限制，所以先这样写
                vmRuntimeCls = Class.forName("dalvik.system.VMRuntime");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "init: dalvik.system.VMRuntime not found!");
            }
            if (vmRuntimeCls != null) {
                Object vmRuntimeIns = invokeDeclaredMethod(vmRuntimeCls, "getRuntime", null, null);
                invokeDeclaredMethod(vmRuntimeCls
                        , "setHiddenApiExemptions"
                        , new Class[]{String[].class}
                        , vmRuntimeIns
                        , (Object) new String[]{"L"});
            }
        }
        //Android P
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            clearClassLoaderInClass(ReflectionOnPie.class);
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            synchronized (ReflectionOnPie.class) {
                try {
                    @SuppressLint("PrivateApi")
                    Method setHiddenApiEnforcementPolicy = ApplicationInfo.class
                            .getDeclaredMethod("setHiddenApiEnforcementPolicy", int.class);
                    setHiddenApiEnforcementPolicy.invoke(applicationInfo, 0);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        } else Log.w(TAG, "init: SDK_INT<28,skipping...");
    }

    /**
     * 用Unsafe清除classloader
     * 该方法仅对同一个类生效，所以必须在需要的类中添加使用，不可以跨类调用
     *
     * @param cls 需要清除Loader的类
     */
    @SuppressWarnings("unchecked")
    public static void clearClassLoaderInClass(Class cls) {
        try {
            Class unsafeClass = Class.forName("sun.misc.Unsafe");
            Field unsafeInstanceField = unsafeClass.getDeclaredField("theUnsafe");
            unsafeInstanceField.setAccessible(true);
            Object unsafeInstance = unsafeInstanceField.get(null);
            Method objectFieldOffset = unsafeClass.getMethod("objectFieldOffset", Field.class);
            // 警告：至少在Java 10上可能无效
            // 查看源码发现，classLoader已经修饰上了final，并伴随以下注释
            // Initialized in JVM not by private constructor
            // This field is filtered from reflection access, i.e. getDeclaredField
            // will throw NoSuchFieldException
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

    /**
     * 恢复classloader:清除loader可能会造成问题
     * (java.lang.NoClassDefFoundError: Class not found using the boot class loader; no stack trace available)
     * 反射完成后恢复loader
     *
     * @param cls 需要恢复Loader的类
     */
    public static void restoreLoaderInClass(Class cls) {
        try {
            Field classLoaderField = Class.class.getDeclaredField("classLoader");
            classLoaderField.setAccessible(true);
            //If this object represents a primitive type or void, null is returned.
            if (cls != null && !cls.isPrimitive() && classLoaderField.get(cls) == null) {
                Log.w(TAG, "restoreLoaderInClass: classloader is null!");
                // 已经阅读过相关源码（Java 10）。class.getClassLoader()
                // 除了途径SecurityManager的权限检查（Android上不适用）,返回的就是classLoader对象
                // 不要使用上述方法！可能会和当前线程所运行的类加载器不一致（JVM本身就是多线程的），
                // 导致仅能够完整显示错误堆栈但仍无法找到包内的类。用getContextClassLoader加载外部资源
                // ClassLoader.getSystemClassLoader():AppClassLoader
                classLoaderField.set(cls, Thread.currentThread().getContextClassLoader());
            }
        } catch (Exception e) {
            Log.e(TAG, "restoreLoaderInClass: ", e);
        }
    }

    /**
     * 通过双重反射，实现在Android Q上重新调用被拉清单的API
     *
     * @param cls        待调用的类
     * @param methodName 方法名
     * @param argsType   参数类型列表
     * @param instance   实例
     * @param args       参数实体列表
     * @return 返回调用方法返回值，若找不到则返回getDeclaredMethod方法对象或空
     */
    @Nullable
    private static Object invokeDeclaredMethod(Class<?> cls, String methodName, Class<?>[] argsType, Object instance, Object... args) {
        Method sysMethod = null;
        try {
            sysMethod = Class.class.getDeclaredMethod("getDeclaredMethod", String.class, Class[].class);
            Method myMethod = (Method) sysMethod.invoke(cls, methodName, argsType);
            return myMethod.invoke(instance, args);
        } catch (Throwable e) {
            //NoSuchMethodException | IllegalAccessException | InvocationTargetException
            Log.e(TAG, "invokeDeclaredMethod: ", e);
        }
        return sysMethod;
    }

    /*
    //之后可能像这样要重写一遍反射相关的接口
    public static Field getDeclaredField(Class cls, String fieldName) {
        try {
            Method sysMethod = Class.class.getDeclaredMethod("getDeclaredField", String.class);
            return (Field) sysMethod.invoke(cls, fieldName);
        } catch (Throwable ignored) {
        }
        return Class.class.getFields()[0];
    }
    */
}
