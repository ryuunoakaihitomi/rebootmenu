package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.UIUtils;
import com.ryuunoakaihitomi.rebootmenu.util.URMUtils;

/**
 * 免root模式活动
 * Created by ZQY on 2018/2/8.
 */

public class UnRootMode extends MyActivity {
    private final int requestCode = 1989;
    private AlertDialog.Builder mainDialog;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName componentName;
    private AlertDialog dialogInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DebugLog("UnRootMode.onCreate", DebugLog.LogLevel.V);
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);
        URLockScrInit(false, requestCode, devicePolicyManager, componentName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            UIUtils.transparentStatusBar(this);
        mainDialog = UIUtils.LoadDialog(ConfigManager.get(ConfigManager.WHITE_THEME), this);
        UIUtils.setExitStyleAndHelp(UnRootMode.this, mainDialog);
        mainDialog.setTitle(getString(R.string.unroot_title));
        String[] uiTextList;
        @SuppressWarnings("Convert2Lambda") DialogInterface.OnClickListener mainListener = new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        URMUtils.lockscreen(UnRootMode.this, componentName, requestCode, devicePolicyManager, true);
                        break;
                    case 1:
                        //调用系统电源菜单无需二次确认
                        URMUtils.accessibilityOn(UnRootMode.this);
                        break;
                    case 2:
                        if (!ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM)) {
                            mainDialog.setTitle(getString(R.string.confirm_operation));
                            mainDialog.setItems(new String[]{getString(R.string.yes), getString(R.string.no)}, (dialogInterface1, iConfirm) -> {
                                if (iConfirm == 1)
                                    finish();
                                else
                                    URMUtils.reboot(devicePolicyManager, componentName, UnRootMode.this);
                            });
                            mainDialog.setNeutralButton(null, null);
                            mainDialog.setPositiveButton(null, null);
                            mainDialog.setNegativeButton(null, null);
                            dialogInstance = mainDialog.create();
                            UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.CONFIRM);
                        } else
                            URMUtils.reboot(devicePolicyManager, componentName, UnRootMode.this);
                        break;
                    case 3:
                        mainDialog.setTitle(getString(R.string.confirm_operation));
                        mainDialog.setItems(null, null);
                        mainDialog.setNeutralButton(R.string.yes, (dialog, which) -> {
                            //警告：在26中弃用，之后可能只能使用wipeData解除。在今后可能要移除重启功能。
                            devicePolicyManager.clearDeviceOwnerApp(getPackageName());
                            new TextToast(getApplicationContext(), getString(R.string.clear_owner_notice));
                            finish();
                        });
                        dialogInstance = mainDialog.create();
                        UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.CONFIRM);
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //检查Device Owner
            if (devicePolicyManager.isDeviceOwnerApp(getPackageName()))
                uiTextList = new String[]{getString(R.string.lockscreen), getString(R.string.system_power_dialog), getString(R.string.reboot), getString(R.string.clear_owner)};
            else {
                uiTextList = new String[]{getString(R.string.lockscreen), getString(R.string.system_power_dialog)};
                new TextToast(getApplicationContext(), true, getString(R.string.device_owner_disabled));
            }
        }
        //Android7.0以下不支持设备管理器重启
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uiTextList = new String[]{getString(R.string.lockscreen), getString(R.string.system_power_dialog)};
            new TextToast(getApplicationContext(), getString(R.string.nougat_notice));
        } else {
            //Android5.0以下不支持系统电源菜单
            uiTextList = new String[]{getString(R.string.lockscreen)};
            new TextToast(getApplicationContext(), getString(R.string.lollipop_notice));
        }
        mainDialog.setItems(uiTextList, mainListener);
        //egg
        mainDialog.setNeutralButton(" ", (dialogInterface, i) -> {
            new TextToast(getApplicationContext(), true, "とまれかくもあはれ\nほたるほたるおいで");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://music.163.com/#/song?id=22765874")));
            finish();
        });
        dialogInstance = mainDialog.create();
        dialogInstance.setOnShowListener(dialog -> {
            ListView listView = dialogInstance.getListView();
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                switch (position) {
                    case 0:
                        UIUtils.addLauncherShortcut(this, R.string.lockscreen_unroot, android.R.drawable.ic_menu_slideshow, Shortcut.UR_LOCKSCREEN, false);
                        break;
                    case 1:
                        UIUtils.addLauncherShortcut(this, R.string.tile_label, android.R.drawable.ic_menu_preferences, Shortcut.UR_POWERDIALOG, false);
                        break;
                    case 2:
                        UIUtils.addLauncherShortcut(this, R.string.reboot_unroot, android.R.drawable.ic_menu_rotate, Shortcut.UR_REBOOT, false);
                        break;
                    case 3:
                        //解除设备所有者不可轻易设置回，因此不予添加
                        return false;
                }
                new TextToast(this, true, String.format(getString(R.string.launcher_shortcut_added), uiTextList[position]));
                return true;
            });
        });
        UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.NORMAL);
        new DebugLog("UnRootMode.onCreate -- END", DebugLog.LogLevel.V);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //清掉dialog防止WindowLeaked
        if (dialogInstance != null) {
            dialogInstance.dismiss();
            dialogInstance = null;
        }
    }
}
