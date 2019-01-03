package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.app.ActivityThread;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.os.ServiceManager;
import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Xposed执行入口，计划实现更多功能
 * Created by ZQY on 2018/12/30.
 *
 * @author ZQY
 */

public class XposedMain implements IXposedHookZygoteInit {

    private static final String TAG = "XposedMain";

    private static void addSystemService(final Context context) {
        RMPowerActionService rmPowerActionService = new RMPowerActionService(context);
        XposedHelpers.callStaticMethod(ServiceManager.class, "addService", getServiceName(RMPowerActionService.TAG), rmPowerActionService, true);
    }

    //受SELinux的制约
    @SuppressWarnings("SameParameterValue")
    private static String getServiceName(String baseName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return baseName;
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                XposedBridge.log("rebootmenu " + Context.TV_INPUT_SERVICE);
                return Context.TV_INPUT_SERVICE;
            } else
                return "user." + baseName;
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) {
        XposedBridge.hookAllMethods(ActivityThread.class, "systemMain", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class amsClass = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", classLoader);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    XposedBridge.hookAllConstructors(amsClass, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            addSystemService(context);
                        }
                    });
                } else {
                    XposedBridge.hookAllMethods(amsClass, "main", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            addSystemService((Context) param.getResult());
                        }
                    });
                }
            }
        });
        //...
        XposedBridge.log("rebootmenu enabled...");
        Log.d(TAG, "initZygote: zygote " + Arrays.toString(new int[]{Process.myPid(), Process.myUid(), Process.myTid()}));
    }
}
