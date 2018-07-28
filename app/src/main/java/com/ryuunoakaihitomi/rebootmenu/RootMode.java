package com.ryuunoakaihitomi.rebootmenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import com.ryuunoakaihitomi.rebootmenu.util.Commands;
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
    private AlertDialog dialogInstance;

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
                Commands.REBOOT,
                Commands.SHUTDOWN,
                Commands.RECOVERY,
                Commands.BOOTLOADER,
                Commands.HOT_REBOOT,
                Commands.RESTART_SYSTEM_UI,
                Commands.SAFE_MODE,
                Commands.LOCK_SCREEN
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
                Commands.REBOOT_F,
                Commands.SHUTDOWN_F,
                Commands.RECOVERY_F,
                Commands.BOOTLOADER_F,
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
                dialogInstance = mainDialog.create();
                UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.CONFIRM);
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
                dialogInstance = mainDialog.create();
                UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.NORMAL);
            });
        } else {
            //不能兼容就只能选择强制
            mainDialog.setItems(uiTextListForce, mainListener);
            isForceMode = true;
            new TextToast(getApplicationContext(), getString(R.string.normal_not_support));
        }
        //长按监听 来自https://stackoverflow.com/questions/9145628/add-onlongclick-listener-to-an-alertdialog/14163293#14163293
        AlertDialog mainDialogCreate = mainDialog.create();
        mainDialogCreate.setOnShowListener(dialog -> {
            ListView listView = mainDialogCreate.getListView();
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                switch (position) {
                    case 0:
                        UIUtils.addLauncherShortcut(this, R.string.reboot, android.R.drawable.ic_menu_rotate, Shortcut.REBOOT, true);
                        break;
                    case 1:
                        UIUtils.addLauncherShortcut(this, R.string.shutdown, android.R.drawable.ic_menu_delete, Shortcut.SHUTDOWN, true);
                        break;
                    case 2:
                        UIUtils.addLauncherShortcut(this, R.string.recovery_short, android.R.drawable.ic_menu_today, Shortcut.RECOVERY, true);
                        break;
                    case 3:
                        UIUtils.addLauncherShortcut(this, R.string.fastboot_short, android.R.drawable.ic_menu_sort_by_size, Shortcut.FASTBOOT, true);
                        break;
                    case 4:
                        //热重启是一种永远都不被推荐的重启方式，它造成系统不稳定的可能性很大，所以使用截图警示图标
                        UIUtils.addLauncherShortcut(this, R.string.hot_reboot, android.R.drawable.ic_menu_report_image, Shortcut.HOT_REBOOT, false);
                        break;
                    case 5:
                        UIUtils.addLauncherShortcut(this, R.string.rebootui_short, android.R.drawable.ic_menu_view, Shortcut.REBOOT_UI, false);
                        break;
                    case 6:
                        //用于定点清除故障应用的安全模式，使用位置图标
                        UIUtils.addLauncherShortcut(this, R.string.safety, android.R.drawable.ic_menu_mylocation, Shortcut.SAFEMODE, false);
                        break;
                    case 7:
                        UIUtils.addLauncherShortcut(this, R.string.lockscreen, android.R.drawable.ic_menu_slideshow, Shortcut.LOCKSCREEN, false);
                }
                new TextToast(this, true, String.format(getString(R.string.launcher_shortcut_added), uiTextList[position]));
                return true;
            });
        });
        dialogInstance = mainDialogCreate;
        UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.NORMAL);
    }

    private void exeKernel(String[] shellList, String[] shellListForce, int i) {
        new DebugLog("exeKernel: i:" + i + " isForceMode:" + isForceMode);
        //是系统应用，且是reboot系，且不是关机
        if (MyApplication.isSystemApp && i != 1 && i < 4) {
            final String[] rebootResList = {
                    null, null, "recovery", "bootloader"
            };
            URMUtils.rebootWithPowerManager(this, rebootResList[i]);
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
        ShellUtils.suCmdExec(Commands.RESTART_SYSTEM_UI_ALTERNATIVE);
        ShellUtils.killShKillProcess("com.android.systemui");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dialogInstance != null) {
            dialogInstance.dismiss();
            //https://stackoverflow.com/questions/11590382/android-view-windowleaked
            /*
            Simply dismissing the dialog was not enough to get rid of the error for me. It turns out my code was holding onto a reference to the dialog even after it was dismissed. The key for me was to set that reference to null after dismissing the dialog.
             */
            dialogInstance = null;
        }
    }
}
