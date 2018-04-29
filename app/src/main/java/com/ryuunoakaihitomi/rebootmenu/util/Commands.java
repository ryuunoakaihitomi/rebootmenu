package com.ryuunoakaihitomi.rebootmenu.util;

/**
 * 主要的Shell命令集
 * F:Force
 * Created by ZQY on 2018/4/27.
 */

public class Commands {
    private static final String _RECOVERY = " recovery",
            _BOOTLOADER = " bootloader",
            SVC_POWER_ = "svc power ",
            SETPROP_ = "setprop ";
    //明明需要public的，AS偏偏就给我个Access can be private.
    @SuppressWarnings("WeakerAccess")
    public static final String
            REBOOT = SVC_POWER_ + "reboot",
            REBOOT_F = "reboot",
            SHUTDOWN = SVC_POWER_ + "shutdown",
            SHUTDOWN_F = REBOOT_F + " -p",
            RECOVERY = SVC_POWER_ + REBOOT_F + _RECOVERY,
            RECOVERY_F = REBOOT_F + _RECOVERY,
            BOOTLOADER = SVC_POWER_ + REBOOT_F + _BOOTLOADER,
            BOOTLOADER_F = REBOOT_F + _BOOTLOADER,
            HOT_REBOOT = SETPROP_ + "ctl.restart zygote",
            RESTART_SYSTEM_UI = "busybox pkill com.android.systemui",
            RESTART_SYSTEM_UI_ALTERNATIVE = "killall com.android.systemui",
            SAFE_MODE = SETPROP_ + "persist.sys.safemode 1",
            LOCK_SCREEN = "input keyevent 26";
}
