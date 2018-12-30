package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.os.Process;
import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;

/**
 * Xposed执行入口，计划实现一些功能
 * Created by ZQY on 2018/12/30.
 * <p>
 *
 * @author ZQY
 */

public class XposedMain implements IXposedHookZygoteInit {
    private static final String TAG = "XposedMain";

    @Override
    public void initZygote(StartupParam startupParam) {
        //...
        XposedBridge.log("rebootmenu");
        Log.d(TAG, "initZygote: zygote " + Arrays.toString(new int[]{Process.myPid(), Process.myUid(), Process.myTid()}));
    }
}