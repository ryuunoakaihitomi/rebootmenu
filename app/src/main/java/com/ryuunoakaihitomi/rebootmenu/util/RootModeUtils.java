package com.ryuunoakaihitomi.rebootmenu.util;

import android.content.Context;

import com.ryuunoakaihitomi.rebootmenu.util.hook.RMPowerActionManager;
import com.ryuunoakaihitomi.rebootmenu.util.hook.SuJavaPlugin;

/**
 * 与URMUtils相对,本应用中（需）root模式的工具集合
 * Created by ZQY on 2018/12/29.
 *
 * @author ZQY
 */

public class RootModeUtils {

    public static void rebootSystemUIAlternativeMethod() {
        new DebugLog("rebootSystemUIAlternativeMethod", DebugLog.LogLevel.V);
        ShellUtils.suCmdExec(Commands.RESTART_SYSTEM_UI_ALTERNATIVE);
        ShellUtils.killShKillProcess("com.android.systemui");
    }

    public static void lockScreen(Context context) {
        try {
            RMPowerActionManager.getInstance().lockScreen();
        } catch (Throwable throwable) {
            ShellUtils.runSuJavaWithAppProcess(context, SuJavaPlugin.class.getName(), SuJavaPlugin.ARG_LOCK_SCREEN);
            throwable.printStackTrace();
        }
    }
}
