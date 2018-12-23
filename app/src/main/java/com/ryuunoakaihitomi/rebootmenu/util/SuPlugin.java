package com.ryuunoakaihitomi.rebootmenu.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IPowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

/**
 * 本应用中可以root权限执行的Java代码
 * Created by ZQY on 2018/12/23.
 * <p>
 * 注意:此处环境要与外部完全隔离
 *
 * @author ZQY
 */

public class SuPlugin {
    /**
     * 参数锁屏
     */
    public static final String ARG_LOCK_SCREEN = "ls";
    /**
     * 参数关机确认菜单
     */
    public static final String ARG_LOCK_SHUT_DOWN_DIALOG = "sdd";
    private static final String TAG = "SuPlugin";

    //main入口
    public static void main(String[] args) {
        Log.d(TAG, "main: id=" + (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? Process.myUid() : (Os.getuid() + "|" + Os.geteuid())));
        if (args != null && args.length > 0 && !TextUtils.isEmpty(args[0])) {
            switch (args[0]) {
                case ARG_LOCK_SCREEN:
                    try {
                        lockScreenWithIPowerManager();
                    } catch (Throwable e) {
                        Log.e(TAG, "main: lockScreenWithIPowerManager()", e);
                        String CMD_LOCK_SCREEN = "input keyevent 26";
                        shell(CMD_LOCK_SCREEN);
                    }
                    break;
                case ARG_LOCK_SHUT_DOWN_DIALOG:
                    //"userrequested"待研究 (@param reason code to pass to android_reboot() (e.g. "userrequested"), or null.)
                    shutdownWithIPowerManager(true, "userrequested", false);
                default:
            }
        } else {
            Log.w(TAG, "main: arg?");
            System.exit(-1);
        }
    }

    private static void lockScreenWithIPowerManager() throws RemoteException {
        Log.v(TAG, "lockScreenWithIPowerManager()");
        IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        long uptimeMillis = SystemClock.uptimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            iPowerManager.goToSleep(uptimeMillis, 0, 0);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            iPowerManager.goToSleep(uptimeMillis, 0);
        else
            iPowerManager.goToSleep(uptimeMillis);
    }

    @SuppressWarnings("SameParameterValue")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void shutdownWithIPowerManager(boolean confirm, String reason, boolean wait) {
        Log.v(TAG, "lockScreenWithIPowerManager(...)");
        IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            iPowerManager.shutdown(confirm, reason, wait);
            return;
        }
        iPowerManager.shutdown(confirm, wait);
    }

    private static void shell(String command) {
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            Log.w(TAG, "shell: ", e);
        }
    }
}