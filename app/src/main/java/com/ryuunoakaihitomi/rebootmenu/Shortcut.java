package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.UIUtils;
import com.ryuunoakaihitomi.rebootmenu.util.URMUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * 在Android API level 25中对Shortcut的处理
 * Created by ZQY on 2018/2/15.
 */

public class Shortcut extends MyActivity {

    static final int ROOT = 0;
    static final int UNROOT = 1;
    public static final String extraTag = "shortcut";
    static final String action = "com.ryuunoakaihitomi.rebootmenu.SHORTCUT_ACTION";
    private boolean isSysApp;

    //shortcut功能选项
    static final int REBOOT = 2,
            SHUTDOWN = 3,
            RECOVERY = 8,
            FASTBOOT = 9,
            HOT_REBOOT = 12,
            REBOOT_UI = 4,
            SAFEMODE = 11,
            LOCKSCREEN = 5,
            UR_LOCKSCREEN = 6,
            UR_POWERDIALOG = 7,
            UR_REBOOT = 10;

    //来自UnRootMode.java -- 结束

    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.N_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DebugLog("Shortcut.onCreate", DebugLog.LogLevel.V);
        componentName = new ComponentName(this, AdminReceiver.class);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        requestCode = 1729;
        assert devicePolicyManager != null;
        URLockScrInit(true, requestCode, devicePolicyManager, componentName);
        boolean isN_MR1 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;
        isSysApp = MyApplication.isSystemApp;
        int param = getIntent().getIntExtra(extraTag, -1);
        new DebugLog("onCreate: param=" + param + " isSysApp=" + isSysApp, DebugLog.LogLevel.I);
        ShortcutManager shortcutManager = null;
        if (isN_MR1)
            shortcutManager = getSystemService(ShortcutManager.class);
        switch (param) {
            //Root模式下的快捷方式
            case ROOT:
                ShortcutInfo fastboot = new ShortcutInfo.Builder(this, "r_ffb")
                        .setShortLabel("*" + getString(R.string.fastboot_short))
                        //命令行
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_sort_by_size))
                        .setIntent(new Intent(action).putExtra(extraTag, FASTBOOT))
                        .setRank(5)
                        .build();
                ShortcutInfo recovery = new ShortcutInfo.Builder(this, "r_frr")
                        //超出长度文本将截断
                        .setShortLabel("*" + getString(R.string.recovery_short))
                        //分割成六块的工具箱
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_today))
                        .setIntent(new Intent(action).putExtra(extraTag, RECOVERY))
                        .setRank(4)
                        .build();
                ShortcutInfo reboot = new ShortcutInfo.Builder(this, "r_fr")
                        .setShortLabel("*" + getString(R.string.reboot))
                        //回转箭头中心一点
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_rotate))
                        .setIntent(new Intent(action).putExtra(extraTag, REBOOT))
                        .setRank(3)
                        .build();
                ShortcutInfo shutdown = new ShortcutInfo.Builder(this, "r_fs")
                        .setShortLabel("*" + getString(R.string.shutdown))
                        //垃圾桶
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_delete))
                        .setIntent(new Intent(action).putExtra(extraTag, SHUTDOWN))
                        .setRank(2)
                        .build();
                ShortcutInfo rebootui = new ShortcutInfo.Builder(this, "r_ru")
                        .setShortLabel(getString(R.string.rebootui_short))
                        //眼睛（看见的就是UI）
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_view))
                        .setIntent(new Intent(action).putExtra(extraTag, REBOOT_UI))
                        .setRank(1)
                        .build();
                ShortcutInfo lockscreen = new ShortcutInfo.Builder(this, "r_l")
                        .setShortLabel(getString(R.string.lockscreen))
                        //方框围住播放标志
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_slideshow))
                        .setIntent(new Intent(action).putExtra(extraTag, LOCKSCREEN))
                        .setRank(0)
                        .build();
                //无论如何，没有root不能用shell锁屏和重启UI，所以在不检查root模式下的无root模式改为增加fast boot。
                if (ShellUtils.isRoot())
                    shortcutManager.setDynamicShortcuts(Arrays.asList(recovery, reboot, shutdown, rebootui, lockscreen));
                else
                    shortcutManager.setDynamicShortcuts(Arrays.asList(fastboot, recovery, reboot, shutdown));
                finish();
                break;
            //免root模式下的快捷方式
            case UNROOT:
                boolean isDevOwner = devicePolicyManager.isDeviceOwnerApp(getPackageName());
                ShortcutInfo ur_reboot = null;
                if (isDevOwner)
                    ur_reboot = new ShortcutInfo.Builder(this, "ur_r")
                            .setShortLabel(getString(R.string.reboot_unroot))
                            .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_rotate))
                            .setIntent(new Intent(action).putExtra(extraTag, UR_REBOOT))
                            .setRank(2)
                            .build();
                ShortcutInfo ur_lockscreen = new ShortcutInfo.Builder(this, "ur_l")
                        .setShortLabel(getString(R.string.lockscreen_unroot))
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_slideshow))
                        .setIntent(new Intent(action).putExtra(extraTag, UR_LOCKSCREEN))
                        .setRank(1)
                        .build();
                ShortcutInfo ur_powerdialog = new ShortcutInfo.Builder(this, "ur_p")
                        //"电源菜单"
                        .setShortLabel(getString(R.string.tile_label))
                        //扳手
                        .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_preferences))
                        .setIntent(new Intent(action).putExtra(extraTag, UR_POWERDIALOG))
                        .setRank(0)
                        .build();
                if (isDevOwner)
                    shortcutManager.setDynamicShortcuts(Arrays.asList(ur_lockscreen, ur_powerdialog, ur_reboot));
                else
                    shortcutManager.setDynamicShortcuts(Arrays.asList(ur_lockscreen, ur_powerdialog));
                finish();
                break;
            //快捷方式使用强制执行
            case FASTBOOT:
                rebootExec("bootloader");
                break;
            case RECOVERY:
                rebootExec("recovery");
                break;
            case REBOOT:
                rebootExec(null);
                break;
            case SHUTDOWN:
                rebootExec("-p");
                break;
            case REBOOT_UI:
                if (!ShellUtils.suCmdExec("busybox pkill com.android.systemui"))
                    RootMode.rebootSystemUIAlternativeMethod();
                finish();
                break;
            case HOT_REBOOT:
                ShellUtils.suCmdExec("setprop ctl.restart zygote");
                break;
            case SAFEMODE:
                //由于活动会短暂可见
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    UIUtils.transparentStatusBar(this);
                ShellUtils.suCmdExec("setprop persist.sys.safemode 1");
                ShellUtils.suCmdExec("svc power reboot");
                break;
            case LOCKSCREEN:
                ShellUtils.suCmdExec("input keyevent 26");
                finish();
                break;
            //免root模式
            case UR_LOCKSCREEN:
                URMUtils.lockscreen(this, componentName, requestCode, devicePolicyManager, false);
                break;
            case UR_POWERDIALOG:
                URMUtils.accessbilityon(Shortcut.this);
                break;
            case UR_REBOOT:
                if (devicePolicyManager.isDeviceOwnerApp(getPackageName()))
                    URMUtils.reboot(devicePolicyManager, componentName, this);
                else {
                    if (isN_MR1)
                        shortcutManager.removeDynamicShortcuts(Collections.singletonList("ur_r"));
                    int random = new Random().nextInt(99);
                    new DebugLog("DEVICE_OWNER_DISABLED from SHORTCUT random " + random, DebugLog.LogLevel.V);
                    new TextToast(getApplicationContext(), random > 50, getString(R.string.device_owner_disabled));
                }
                finish();
            default:
                finish();
        }
    }

    //只有rebooot系才有可能免root执行
    @SuppressWarnings("StringEquality")
    private void rebootExec(String arg) {
        new DebugLog("rebootExec: arg:" + arg, DebugLog.LogLevel.V);
        if (isSysApp && arg != "-p")
            URMUtils.rebootedByPowerManager(this, arg);
        else {
            String cmd = arg == null ? "reboot" : "reboot " + arg;
            ShellUtils.shCmdExec(cmd);
            ShellUtils.suCmdExec(cmd);
        }
        finish();
    }
}
