package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.IRMPowerActionService;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;

import java.lang.reflect.Method;

/**
 * IRMPowerActionService AIDL的具体实现
 * Created by ZQY on 2019/1/3.
 */

class RMPowerActionService extends IRMPowerActionService.Stub {

    static final String TAG = "RMPowerActionService";

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    private PowerManager mPowerManager;

    RMPowerActionService(Context context) {
        Log.d(TAG, "Constructor: " + context);
        mContext = context;
    }

    @Override
    public void lockScreen() {
        Log.d(TAG, "lockScreen");
        injectSystemThread(() -> {
            //API比aidl稳定，但是导包是个问题，现在暂时使用反射将就下
            try {
                @SuppressWarnings("JavaReflectionMemberAccess") Method goToSleep =
                        PowerManager.class.getMethod("goToSleep", long.class);
                goToSleep.invoke(mPowerManager, SystemClock.uptimeMillis());
            } catch (Throwable throwable) {
                Log.e(TAG, "run: ", throwable);
            }
        });
    }

    @Override
    public void reboot(String reason) {
        injectSystemThread(() -> mPowerManager.reboot(reason));
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public void safeMode() {
        injectSystemThread(() -> {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess") Method rebootSafeMode =
                        PowerManager.class.getMethod("rebootSafeMode");
                rebootSafeMode.invoke(mPowerManager);
            } catch (Throwable throwable) {
                Log.e(TAG, "run: ", throwable);
            }
        });
    }

    //测试服务状态
    @Override
    public void ping() {
        Log.i(TAG, "ping: " + SpecialSupport.varArgsToString(mPowerManager, mContext, Process.myUid(), getCallingPid(), getCallingUid()));
    }

    //插入到系统线程才有系统权限
    private void injectSystemThread(Runnable r) {
        new Handler(mContext.getMainLooper()).post(r);
    }

    //所有的系统服务都已经初始化完成
    void allServicesInitialised() {
        Log.d(TAG, "allServicesInitialised");
        mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
    }
}
