package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.os.ServiceManager;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Xposed执行入口，计划实现更多功能
 * Created by ZQY on 2018/12/30.
 *
 * @author ZQY
 */

public class XposedMain implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static final String TAG = "XposedMain";
    @SuppressLint("StaticFieldLeak")
    private static RMPowerActionService rmPowerActionService;

    @Override
    public void initZygote(StartupParam startupParam) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            XposedBridge.log("rebootmenu:Not support for Android 7.0-");
            return;
        }
        XposedBridge.hookAllMethods(ActivityThread.class, "systemMain", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Class amsClass = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", classLoader);
                XposedBridge.hookAllConstructors(amsClass, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        rmPowerActionService = new RMPowerActionService(context);
                        XposedHelpers.callStaticMethod(ServiceManager.class, "addService",
                                XposedUtils.getServiceName(RMPowerActionService.TAG), rmPowerActionService, true);
                    }
                });
                XposedBridge.hookAllMethods(amsClass, "systemReady", new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        rmPowerActionService.allServicesInitialised();
                    }
                });
            }
        });
        //...
        XposedBridge.log("rebootmenu:enabled...");
        Log.d(TAG, "initZygote: zygote " + SpecialSupport.varArgsToString(Build.VERSION.SDK_INT, Process.myPid(), Process.myUid(), Process.myTid()));
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (lpparam.packageName.equals("com.ryuunoakaihitomi.rebootmenu")) {
            Class<?> utilsClass = XposedHelpers.findClass("com.ryuunoakaihitomi.rebootmenu.util.hook.XposedUtils", lpparam.classLoader);
            XposedHelpers.setStaticBooleanField(utilsClass, "isActive", true);
        }
    }
}
