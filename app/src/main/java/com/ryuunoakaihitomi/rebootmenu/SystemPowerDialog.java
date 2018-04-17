package com.ryuunoakaihitomi.rebootmenu;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;

/**
 * 辅助服务
 * Created by ZQY on 2018/2/12.
 */

public class SystemPowerDialog extends AccessibilityService {

    private boolean isBroadcastRegistered;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            new DebugLog("onReceive: will perform AccessibilityService.GLOBAL_ACTION_POWER_DIALOG");
            //调用系统电源菜单核心代码
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
            new TextToast(getApplicationContext(), getString(R.string.spd_showed));
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        new DebugLog("SystemPowerDialog.onServiceConnected", DebugLog.LogLevel.V);
        new DebugLog("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(getString(R.string.service_action_key));
            registerReceiver(broadcastReceiver, intentFilter);
            isBroadcastRegistered = true;
        } else {
            new DebugLog("onServiceConnected: Build.VERSION_CODES.LOLLIPOP?", DebugLog.LogLevel.E);
            System.exit(-1);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        new DebugLog("SystemPowerDialog.onUnbind", DebugLog.LogLevel.V);
        if (isBroadcastRegistered) {
            isBroadcastRegistered = false;
            unregisterReceiver(broadcastReceiver);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
    }
}
