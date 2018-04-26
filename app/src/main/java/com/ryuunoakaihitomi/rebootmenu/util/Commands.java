package com.ryuunoakaihitomi.rebootmenu.util;

/**
 * 主要的Shell命令集
 * F:Force
 * Created by ZQY on 2018/4/27.
 */


public class Commands {
    //明明需要public的，AS偏偏就给我个Access can be private.
    @SuppressWarnings("WeakerAccess")
    public static final String
            SVC_POWER_ = "svc power ",
            REBOOT = SVC_POWER_ + "reboot",
            REBOOT_F = "reboot",
            SHUTDOWN = SVC_POWER_ + "shutdown",
            SHUTDOWN_F = REBOOT_F + " -p",
            RECOVERY = SVC_POWER_ + "recovery",
            RECOVERY_F = REBOOT_F + " recovery",
            BOOTLOADER = SVC_POWER_ + "bootloader",
            BOOTLOADER_F = REBOOT_F + " bootloader",
            SETPROP_ = "setprop ",
            HOT_REBOOT = SETPROP_ + "ctl.restart zygote",
            RESTART_SYSTEM_UI = "busybox pkill com.android.systemui",
            RESTART_SYSTEM_UI_ALTERNATIVE = "killall com.android.systemui",
            SAFE_MODE = SETPROP_ + "persist.sys.safemode 1",
            LOCK_SCREEN = "input keyevent 26";
}
