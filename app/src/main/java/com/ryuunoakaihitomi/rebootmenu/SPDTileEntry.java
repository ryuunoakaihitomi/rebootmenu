package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.service.quicksettings.TileService;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.URMUtils;

@TargetApi(Build.VERSION_CODES.N)
public class SPDTileEntry extends TileService {
    @Override
    public void onClick() {
        //源URMUtils.accessbilityon(),针对Tile做了必要的修改
        new DebugLog("SPDTileEntry.onClick", DebugLog.LogLevel.V);
        if (!URMUtils.isAccessibilitySettingsOn(getApplicationContext())) {
            new TextToast(getApplicationContext(), getString(R.string.service_disabled));
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            //收起状态栏
            startActivityAndCollapse(intent);
        } else {
            sendBroadcast(new Intent(SystemPowerDialog.POWER_DIALOG_ACTION));
        }
    }
}