package com.ryuunoakaihitomi.rebootmenu.util;

/**
 * 主要的Shell命令集
 * F:Force
 * Created by ZQY on 2018/4/27.
 */

public class Commands {
    @SuppressWarnings("SpellCheckingInspection")
    //保持可读性
    public static final String
            REBOOT = "svc power reboot",
            REBOOT_F = "reboot",
            SHUTDOWN = "svc power shutdown",
            SHUTDOWN_F = "reboot -p",
            RECOVERY = "svc power reboot recovery",
            RECOVERY_F = "reboot recovery",
            BOOTLOADER = "svc power reboot bootloader",
            BOOTLOADER_F = "reboot bootloader",
            HOT_REBOOT = "setprop ctl.restart zygote",
            RESTART_SYSTEM_UI = "busybox pkill com.android.systemui",
            RESTART_SYSTEM_UI_ALTERNATIVE = "killall com.android.systemui",
            SAFE_MODE = "setprop persist.sys.safemode 1",
            LOCK_SCREEN = "input keyevent KEYCODE_POWER",
            START_BROADCAST_BY_ACTION = "am start -a %s";
}
