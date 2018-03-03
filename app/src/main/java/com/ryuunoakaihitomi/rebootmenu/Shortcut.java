package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.UIUtils;

import java.util.Arrays;

/**
 * 在Android API level 25中对Shortcut的处理
 * Created by ZQY on 2018/2/15.
 */

public class Shortcut extends Activity {

    static final int ROOT = 0;
    static final int UNROOT = 1;
    static final String extraTag = "shortcut";
    static final String action = "com.ryuunoakaihitomi.rebootmenu.SHORTCUT_ACTION";
    //来自UnRootMode.java -- 开始
    //不使用二次确认直接执行。有意保留的bug:下次在UR活动中执行时要先执行一次才能执行二次确认。
    DevicePolicyManager devicePolicyManager;

    ComponentName componentName;

    //辅助服务申请回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (0 == requestCode) {
            //若同意
            if (resultCode == Activity.RESULT_OK)
                devicePolicyManager.lockNow();
            else
                new TextToast(this, getString(R.string.lockscreen_failed));
            finish();
        }
    }

    //用辅助功能锁屏
    private void lockscreen() {
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);
        if (!devicePolicyManager.isAdminActive(componentName)) {
            //请求启用
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.service_explanation));
            startActivityForResult(intent, 0);
        } else {
            devicePolicyManager.lockNow();
            finish();
        }
    }

    //打开辅助服务设置或者发送执行广播
    private void accessbilityon() {
        if (!UnRootMode.isAccessibilitySettingsOn(getApplicationContext())) {
            new TextToast(getApplicationContext(), getString(R.string.service_disabled));
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } else
            sendBroadcast(new Intent(getString(R.string.service_action_key)));
        finish();
    }

    //来自UnRootMode.java -- 结束

    @TargetApi(Build.VERSION_CODES.N_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //shortcut功能选项
        final int REBOOT = 2;
        final int SHUTDOWN = 3;
        final int RECOVERY = 8;
        final int REBOOT_UI = 4;
        final int LOCKSCREEN = 5;
        final int UR_LOCKSCREEN = 6;
        final int UR_POWERDIALOG = 7;

        int param = getIntent().getIntExtra(extraTag, -1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
            switch (param) {
                //Root模式下的快捷方式
                case ROOT:
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
                            .setShortLabel(getString(R.string.reboot_ui))
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
                    assert shortcutManager != null;
                    shortcutManager.setDynamicShortcuts(Arrays.asList(recovery, reboot, shutdown, rebootui, lockscreen));
                    finish();
                    break;
                //免root模式下的快捷方式
                case UNROOT:
                    ShortcutInfo ur_lockscreen = new ShortcutInfo.Builder(this, "ur_l")
                            .setShortLabel(getString(R.string.lockscreen))
                            .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_slideshow))
                            .setIntent(new Intent(action).putExtra(extraTag, UR_LOCKSCREEN))
                            .setRank(1)
                            .build();
                    ShortcutInfo ur_powerdialog = new ShortcutInfo.Builder(this, "ur_p")
                            .setShortLabel(getString(R.string.system_power_dialog))
                            //扳手
                            .setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_preferences))
                            .setIntent(new Intent(action).putExtra(extraTag, UR_POWERDIALOG))
                            .setRank(0)
                            .build();
                    assert shortcutManager != null;
                    shortcutManager.setDynamicShortcuts(Arrays.asList(ur_lockscreen, ur_powerdialog));
                    finish();
                    break;
                //快捷方式使用强制执行
                case RECOVERY:
                    ShellUtils.suCmdExec("reboot recovery");
                    break;
                case REBOOT:
                    ShellUtils.suCmdExec("reboot");
                    break;
                case SHUTDOWN:
                    ShellUtils.suCmdExec("reboot -p");
                    break;
                case REBOOT_UI:
                    ShellUtils.suCmdExec("busybox pkill com.android.systemui");
                    finish();
                    break;
                case LOCKSCREEN:
                    ShellUtils.suCmdExec("input keyevent 26");
                    finish();
                    break;
                //免root模式
                case UR_LOCKSCREEN:
                    lockscreen();
                    break;
                case UR_POWERDIALOG:
                    accessbilityon();
                    break;
                default:
                    finish();
            }
            //非常规操作：比如root模式下App Shortcut权限拒绝时，活动长期驻留时，保持状态栏透明。
            UIUtils.transparentStatusBar(this);
        }
        //<---注意：不要在这里放finish()，因为还需要等待锁屏时设备管理器申请回调
        else {
            finish();
        }
    }
}
