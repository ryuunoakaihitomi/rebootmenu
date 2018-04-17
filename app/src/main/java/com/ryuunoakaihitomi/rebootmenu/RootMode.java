package com.ryuunoakaihitomi.rebootmenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;

import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.UIUtils;
import com.ryuunoakaihitomi.rebootmenu.util.URMUtils;

/**
 * Root模式活动
 * Created by ZQY on 2018/2/8.
 */

public class RootMode extends MyActivity {
    private boolean isForceMode;

    @SuppressWarnings("DanglingJavadoc")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DebugLog("RootMode.onCreate", DebugLog.LogLevel.V);
        //只有API Level 23或以上才需要做此妥协
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UIUtils.transparentStatusBar(this);
        }

        final AlertDialog.Builder mainDialog = UIUtils.LoadDialog(ConfigManager.get(ConfigManager.WHITE_THEME), this);
        UIUtils.setExitStyleAndHelp(RootMode.this, mainDialog);
        mainDialog.setTitle(getString(R.string.root_title));
        //默认模式功能列表
        final String[] uiTextList = {
                getString(R.string.reboot),
                getString(R.string.shutdown),
                getString(R.string.recovery),
                getString(R.string.fastboot),
                getString(R.string.hot_reboot),
                getString(R.string.reboot_ui),
                getString(R.string.safety),
                getString(R.string.lockscreen)
        };
        //默认模式命令列表
        final String[] shellList = {
                "svc power reboot",
                "svc power shutdown",
                "svc power reboot recovery",
                "svc power reboot bootloader",
                "setprop ctl.restart zygote",
                "busybox pkill com.android.systemui",
                "setprop persist.sys.safemode 1",
                "input keyevent 26"
        };
        //强制模式功能列表
        final String[] uiTextListForce = {
                "*" + uiTextList[0],
                "*" + uiTextList[1],
                "*" + uiTextList[2],
                "*" + uiTextList[3],
                uiTextList[4],
                uiTextList[5],
                "*" + uiTextList[6],
                uiTextList[7]
        };
        //强制模式命令列表
        final String[] shellListForce = {
                "reboot",
                "reboot -p",
                "reboot recovery",
                "reboot bootloader",
                shellList[4],
                shellList[5],
                shellList[6],
                shellList[7]
        };
        //功能监听
        final DialogInterface.OnClickListener mainListener = (dialogInterface, i) -> {
            new DebugLog("RootMode: i:" + i);
            //锁屏就直接锁了，不需要确认。
            if (i != 7 && !ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM)) {
                //确认界面显示（？YN）
                final String[] confirmList = {
                        getString(R.string.confirm_operation),
                        getString(R.string.yes),
                        getString(R.string.no)
                };
                //在后面显示刚刚选择的功能名称
                if (!isForceMode)
                    mainDialog.setTitle(getString(R.string.confirm_operation) + " " + uiTextList[i]);
                else
                    mainDialog.setTitle(getString(R.string.confirm_operation) + " " + uiTextListForce[i]);
                //点击是或者否的监听
                //iConfirm不和i混淆
                DialogInterface.OnClickListener confirmListener = (dialogInterface1, iConfirm) -> {
                    //若否
                    if (iConfirm == 1) {
                        new TextToast(getApplicationContext(), getString(R.string.no_seleted_notice));
                        finish();
                    } else
                        exeKernel(shellList, shellListForce, i);
                };
                //YN
                String[] confirmSelect = {
                        confirmList[1], confirmList[2]
                };
                mainDialog.setItems(confirmSelect, confirmListener);
                //取消之前设置的底部按钮
                mainDialog.setNeutralButton(null, null);
                mainDialog.setPositiveButton(null, null);
                mainDialog.setNegativeButton(null, null);
                UIUtils.alphaShow(mainDialog.create(), UIUtils.TransparentLevel.CONFIRM);
            } else
                //直接执行（无需确认）
                exeKernel(shellList, shellListForce, i);
        };
        mainDialog.setItems(uiTextList, mainListener);
        /**
         * 经代码查阅对比，发现在Android4.3中加入了svc控制关机的功能。
         *
         * @see https://github.com/aosp-mirror/platform_frameworks_base/commit/62aad7f66fcd673831029eb96dd49c95f76b17bd
         *
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mainDialog.setNeutralButton(R.string.mode_switch, (dialogInterface, i) -> {
                //没问题就按照用户的选择
                if (!isForceMode) {
                    mainDialog.setItems(uiTextListForce, mainListener);
                    isForceMode = true;
                    new TextToast(getApplicationContext(), getString(R.string.force_mode));
                } else {
                    mainDialog.setItems(uiTextList, mainListener);
                    isForceMode = false;
                    new TextToast(getApplicationContext(), getString(R.string.normal_mode));
                }
                UIUtils.alphaShow(mainDialog.create(), UIUtils.TransparentLevel.NORMAL);
            });
        } else {
            //不能兼容就只能选择强制
            mainDialog.setItems(uiTextListForce, mainListener);
            isForceMode = true;
            new TextToast(getApplicationContext(), getString(R.string.normal_not_support));
        }
        UIUtils.alphaShow(mainDialog.create(), UIUtils.TransparentLevel.NORMAL);
    }

    private void exeKernel(String[] shellList, String[] shellListForce, int i) {
        new DebugLog("exeKernel: i:" + i + " isForceMode:" + isForceMode);
        //是系统应用，且是reboot系，且不是关机
        if (URMUtils.isSystemApp(this) && i != 1 && i < 4) {
            final String[] rebootResList = {
                    null, null, "recovery", "bootloader"
            };
            URMUtils.rebootedByPowerManager(this, rebootResList[i]);
        } else {
            String command;
            boolean isSucceed;
            //模式选择
            if (!isForceMode)
                command = shellList[i];
            else {
                command = shellListForce[i];
                //首先尝试普通权限shell，只在强制模式可能有效
                ShellUtils.shCmdExec(command);
            }
            isSucceed = ShellUtils.suCmdExec(command);
            //如果是安全模式，MIUI执行完不能立即重启，还得执行一次重启
            //noinspection StringEquality
            if (command == shellList[6])
                if (!isForceMode)
                    ShellUtils.suCmdExec(shellList[0]);
                else {
                    ShellUtils.shCmdExec(shellListForce[0]);
                    ShellUtils.suCmdExec(shellListForce[0]);
                }
                //重启UI：两套备选方案
            else if (shellList[5].equals(command) && !isSucceed) {
                rebootSystemUIAlternativeMethod();
            }
        }
        new TextToast(getApplicationContext(), true, getString(R.string.cmd_send_notice));
        finish();
    }

    static void rebootSystemUIAlternativeMethod() {
        new DebugLog("rebootSystemUIAlternativeMethod", DebugLog.LogLevel.V);
        ShellUtils.suCmdExec("killall com.android.systemui");
        ShellUtils.killShKillProcess("com.android.systemui");
    }
}
