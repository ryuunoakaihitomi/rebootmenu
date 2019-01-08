package com.ryuunoakaihitomi.rebootmenu.util.hook;

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

import com.android.internal.os.Zygote;
import com.ryuunoakaihitomi.rebootmenu.util.Commands;

/**
 * 本应用中可以root权限执行的Java代码
 * Created by ZQY on 2018/12/23.
 * <p>
 * 注意:此处环境要与外部完全隔离
 *
 * @author ZQY
 */

public class SuJavaPlugin {

    /**
     * 参数 锁屏
     */
    public static final String ARG_LOCK_SCREEN = "ls";

    public static final String ARG_SHUT_DOWN_DIALOG = "sdd";

    private static final String TAG = "SuJavaPlugin";

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
                        //Zygote.execShell()必须在root权限下使用！system_server权限都不行。否则会出现如下错误导致热重启！
                        //E/JavaBinder: *** Uncaught remote exception!  (Exceptions are not yet supported across processes.)
                        //android.system.ErrnoException: execv failed: EACCES (Permission denied)
                        //Zygote.execShell(shell);
                        Zygote.execShell(Commands.LOCK_SCREEN);
                    }
                    break;
                case ARG_SHUT_DOWN_DIALOG:
                    shutdownWithIPowerManager(true, false);
                    break;
                default:
                    throw new IllegalArgumentException("unknown arg[0]:" + args[0]);
            }
        } else {
            Log.w(TAG, "main: arg?");
            throw new IllegalArgumentException("arg == null");
        }
    }

    private static void lockScreenWithIPowerManager() throws RemoteException {
        Log.v(TAG, "lockScreenWithIPowerManager()");
        IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        //Go to sleep reason code: Going to sleep due by application request.
        final int GO_TO_SLEEP_REASON_APPLICATION = 0;
        long uptimeMillis = SystemClock.uptimeMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            iPowerManager.goToSleep(uptimeMillis, GO_TO_SLEEP_REASON_APPLICATION, 0);
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            iPowerManager.goToSleep(uptimeMillis, GO_TO_SLEEP_REASON_APPLICATION);
        else
            iPowerManager.goToSleep(uptimeMillis);
    }

    @SuppressWarnings("SameParameterValue")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private static void shutdownWithIPowerManager(boolean confirm, boolean wait) {
        Log.v(TAG, "shutdownWithIPowerManager(...)");
        IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // @param reason code to pass to android_reboot() (e.g. "userrequested"), or null.
            // @hide The value to pass as the 'reason' argument to android_reboot().
            @SuppressWarnings("SpellCheckingInspection") final String SHUTDOWN_USER_REQUESTED = "userrequested";
            iPowerManager.shutdown(confirm, SHUTDOWN_USER_REQUESTED, wait);
            return;
        }
        iPowerManager.shutdown(confirm, wait);
    }
}