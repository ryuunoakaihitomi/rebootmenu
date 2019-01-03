package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.IRMPowerActionService;

import java.lang.reflect.Method;

/**
 * IRMPowerActionService AIDL的具体实现
 * Created by ZQY on 2019/1/3.
 */

public class RMPowerActionService extends IRMPowerActionService.Stub {

    static final String TAG = "RMPowerActionService";

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    RMPowerActionService(Context context) {
        Log.d(TAG, "Constructor: " + context);
        mContext = context;
    }

    @Override
    public void lockScreen() {
        injectSystemThread(() -> {
            PowerManager powerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            //API比aidl稳定，但是导包是个问题，现在暂时使用反射将就下
            try {
                @SuppressWarnings("JavaReflectionMemberAccess") Method goToSleep =
                        PowerManager.class.getMethod("goToSleep", long.class);
                goToSleep.invoke(powerManager, SystemClock.uptimeMillis());
            } catch (Throwable throwable) {
                Log.e(TAG, "run: ", throwable);
            }
        });
    }

    //插入到系统线程才有系统权限
    private void injectSystemThread(Runnable r) {
        new Handler(mContext.getMainLooper()).post(r);
    }
}
