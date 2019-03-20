package com.ryuunoakaihitomi.rebootmenu.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ListView;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.activity.base.Constants;
import com.ryuunoakaihitomi.rebootmenu.activity.base.MyActivity;
import com.ryuunoakaihitomi.rebootmenu.csc_compat.AdImpl;
import com.ryuunoakaihitomi.rebootmenu.receiver.AdminReceiver;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;
import com.ryuunoakaihitomi.rebootmenu.util.URMUtils;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.ui.UIUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * 免root模式活动
 * Created by ZQY on 2018/2/8.
 */

public class UnRootMode extends MyActivity {
    private static final String TAG = "UnRootMode";

    private AlertDialog.Builder mainDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DebugLog("UnRootMode.onCreate", DebugLog.LogLevel.V);
        //广告
        AdImpl.initialize(this);
        AdImpl.showAdView();
        URLockScrInit(false, Constants.UN_ROOT_MODE_LOCK_SCREEN_REQUEST_CODE, (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE), new ComponentName(this, AdminReceiver.class));
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
                        URMUtils.lockScreen(UnRootMode.this, componentName, requestCode, devicePolicyManager, true);
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
                                    URMUtils.rebootWithDevicePolicyManager(devicePolicyManager, componentName, UnRootMode.this);
                            });
                            mainDialog.setNeutralButton(null, null);
                            mainDialog.setPositiveButton(null, null);
                            mainDialog.setNegativeButton(null, null);
                            dialogInstance = mainDialog.create();
                            UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.CONFIRM);
                        } else
                            URMUtils.rebootWithDevicePolicyManager(devicePolicyManager, componentName, UnRootMode.this);
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
                //WearOS可能不支持Device Owner
                //java.lang.UnsupportedOperationException: The operation is not supported on Wear.
                if (!SpecialSupport.isAndroidWearOS(this))
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
        //节省手表屏幕空间，而且因为容易看出，这也不算是一个彩蛋了
        if (!SpecialSupport.isAndroidWearOS(this))
            //egg
            mainDialog.setNeutralButton(" ", (dialogInterface, i) -> {
                new TextToast(getApplicationContext(), true, "はなび");
                try {
                    //准备
                    //noinspection ConstantConditions
                    final String fileName = "fluid.html",
                            path = getExternalFilesDir(null).getAbsolutePath() + "/" + fileName;
                    File f = new File(path);
                    //释放
                    new DebugLog(TAG, String.valueOf(f.exists() ? f.delete() : f.createNewFile()), null);
                    InputStream is = getAssets().open(fileName, AssetManager.ACCESS_BUFFER);
                    FileOutputStream fos = new FileOutputStream(f);
                    byte[] buffer = new byte[1 << 10];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                    //打开
                    DebugLog.i(TAG, "Fluid opened:" + UIUtils.openFile(this, path));
                } catch (Throwable t) {
                    new DebugLog(t, TAG, true);
                }
                finish();
            });
        dialogInstance = mainDialog.create();
        //NPE:AQUOS PHONE ZETA SH-01F (4.4.2)
        if (dialogInstance == null) return;
        AdImpl.setFlagNotFocusable(dialogInstance);
        dialogInstance.setOnShowListener(dialog -> {
            ListView listView = dialogInstance.getListView();
            listView.setOnItemLongClickListener((parent, view, position, id) -> {
                DebugLog.w(TAG, "onCreate: OnItemLongClickListener " + position);
                switch (position) {
                    case 0:
                        UIUtils.addLauncherShortcut(this, R.string.lockscreen_unroot, android.R.drawable.ic_menu_slideshow, Shortcut.UR_LOCKSCREEN, false);
                        break;
                    case 1:
                        UIUtils.addLauncherShortcut(this, R.string.sys_power_dialog_tile_label, android.R.drawable.ic_menu_preferences, Shortcut.UR_POWERDIALOG, false);
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
            UIUtils.addMagnifier(listView);
        });
        UIUtils.alphaShow(dialogInstance, UIUtils.TransparentLevel.NORMAL);
        new DebugLog("UnRootMode.onCreate -- END", DebugLog.LogLevel.V);
    }
}
