package com.ryuunoakaihitomi.rebootmenu.service;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.LocalBroadcastManager;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.URMUtils;


/**
 * 直接调出系统电源菜单的磁贴入口
 */

@TargetApi(Build.VERSION_CODES.N)
public class SPDTileEntry extends TileService {
    @Override
    public void onClick() {
        new DebugLog("SPDTileEntry isLocked:" + isLocked() + " isSecure:" + isSecure());
        if (!isLocked()) {
            accessibilityOnImpl();
        } else {
            if (isSecure())
                //什么都不做只是弹出密码界面表示要先解锁，因为实际上无法在锁屏输入密码后调出电源菜单
                unlockAndRun(() -> {
                });
            else
                //如果在锁屏状态，但没有设置密码（不安全），则调出
                accessibilityOnImpl();
        }
    }

    //URMUtils.accessibilityOn()的实现,针对Tile做了必要的修改
    private void accessibilityOnImpl() {
        new DebugLog("SPDTileEntry.onClick", DebugLog.LogLevel.V);
        if (!URMUtils.isAccessibilitySettingsOn(getApplicationContext())) {
            new TextToast(getApplicationContext(), getString(R.string.service_disabled));
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            //收起状态栏
            startActivityAndCollapse(intent);
        } else {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(UnRootAccessibility.POWER_DIALOG_ACTION));
        }
    }
}