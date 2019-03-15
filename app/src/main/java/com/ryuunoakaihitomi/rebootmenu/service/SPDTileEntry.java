package com.ryuunoakaihitomi.rebootmenu.service;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;
import com.ryuunoakaihitomi.rebootmenu.util.URMUtils;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;


/**
 * 直接调出系统电源菜单的磁贴入口
 */

@TargetApi(Build.VERSION_CODES.N)
public class SPDTileEntry extends TileService {

    @Override
    public void onCreate() {
        new DebugLog("SPDTileEntry.onCreate");
        super.onCreate();
        //Wear OS没有自定义Tile，所以用来执行通知Action
        if (SpecialSupport.isAndroidWearOS(this)) {
            new DebugLog(getClass().getSimpleName(), "Executing WearOS Action...", null);
            accessibilityOnImpl();
            stopSelf();
        }
    }

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
        new DebugLog("accessibilityOnImpl", DebugLog.LogLevel.V);
        if (!URMUtils.isAccessibilitySettingsOn(getApplicationContext())) {
            new TextToast(getApplicationContext(), getString(R.string.service_disabled));
            //service -> activity
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                //收起状态栏
                startActivityAndCollapse(intent);
            } catch (ActivityNotFoundException e) {
                new TextToast(this, true, getString(R.string.accessibility_settings_not_found), true);
            }
        } else {
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(UnRootAccessibility.POWER_DIALOG_ACTION));
        }
    }
}