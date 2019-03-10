package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Build;
import android.os.IPowerManager;
import android.os.Process;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.os.Zygote;
import com.ryuunoakaihitomi.rebootmenu.util.Commands;

import java.util.Objects;

import androidx.annotation.FloatRange;

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

    /**
     * 设置屏幕显示灰度
     * 1->0
     */
    public static final String ARG_SET_DISPLAY_SATURATION = "sds";

    /**
     * 灰度渐变效果
     */
    public static final String ARG_DISPLAY_SATURATION_GRADIENT = "dsg";

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
                case ARG_SET_DISPLAY_SATURATION:
                    float level = Float.valueOf(args[1]);
                    if (setDisplaySaturationLevel(level))
                        Log.d(TAG, "main: ARG_SET_DISPLAY_SATURATION");
                    break;
                case ARG_DISPLAY_SATURATION_GRADIENT:
                    setDisplaySaturationGradient(Boolean.valueOf(args[1]));
                    break;
                default:
                    throw new IllegalArgumentException("unknown arg[0]:" + args[0]);
            }
        } else {
            Log.w(TAG, "main: arg?");
            throw new IllegalArgumentException("arg == null");
        }
    }

    private static void lockScreenWithIPowerManager() {
        Log.v(TAG, "lockScreenWithIPowerManager()");
        IPowerManager iPowerManager = IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE));
        if (iPowerManager == null) return;
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
        IPowerManager iPowerManager = Objects.requireNonNull(IPowerManager.Stub.asInterface(ServiceManager.getService(Context.POWER_SERVICE)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // @param reason code to pass to android_reboot() (e.g. "userrequested"), or null.
            // @hide The value to pass as the 'reason' argument to android_reboot().
            @SuppressWarnings("SpellCheckingInspection") final String SHUTDOWN_USER_REQUESTED = "userrequested";
            iPowerManager.shutdown(confirm, SHUTDOWN_USER_REQUESTED, wait);
            return;
        }
        iPowerManager.shutdown(confirm, wait);
    }

    /**
     * Set the level of color saturation to apply to the display.
     *
     * @param level The amount of saturation to apply, between 0 and 1 inclusive.
     *              0 produces a grayscale image, 1 is normal.
     *              //@hide
     */
    @TargetApi(Build.VERSION_CODES.P)
    private static boolean setDisplaySaturationLevel(@FloatRange(from = 0, to = 1) float level) {
        boolean ret = false;
        try {
            //Landroid/hardware/display/DisplayManagerGlobal;->setSaturationLevel(F)V,greylist-max-o
            //noinspection ConstantConditions
            DisplayManagerGlobal.getInstance().setSaturationLevel(level);
            ret = true;
        } catch (Throwable t) {
            Log.e(TAG, "setDisplaySaturationLevel: ", t);
        } finally {
            Log.d(TAG, "main: setDisplaySaturationLevel(" + level + "): " + ret);
        }
        return ret;
    }

    /**
     * 屏幕灰度渐变
     *
     * @param isToGrey 是否由彩色转灰度，否则反之
     */
    private static synchronized void setDisplaySaturationGradient(boolean isToGrey) {
        //在{duration 毫秒}中调节{levelCount}级
        final float levelCount = 6, durationMs = 1000;
        try {
            DisplayManagerGlobal dmg = DisplayManagerGlobal.getInstance();
            assert dmg != null;
            float f = isToGrey ? 1 : 0;
            while (isToGrey ? f >= 0 : f <= 1) {
                //Log.v(TAG, "setDisplaySaturationGradient: f=" + f);
                dmg.setSaturationLevel(f);
                f = f + 1 / (isToGrey ? -levelCount : +levelCount);
                Thread.sleep((long) (durationMs / levelCount));
            }
            dmg.setSaturationLevel(isToGrey ? 0 : 1);
        } catch (Throwable t) {
            Log.e(TAG, "setDisplaySaturationGradient: ", t);
        }
    }
}
