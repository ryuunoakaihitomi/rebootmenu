package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;

/**
 * 自定义Activity：亮屏监听，设备管理员申请回调
 * Created by ZQY on 2018/4/15.
 */

@SuppressLint("Registered")
public class MyActivity extends Activity {

    private boolean isBroadcastRegistered;
    //若是Shortcut就不用监听亮屏
    private boolean isShortcut;
    //防止helpDialog造成的WindowLeaked
    public static AlertDialog helpDialogReference;
    ComponentName componentName;
    DevicePolicyManager devicePolicyManager;
    int requestCode;

    //亮屏监听用变量和接收器
    private boolean isScreenOn;
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new DebugLog("onReceive: Intent.ACTION_SCREEN_ON", DebugLog.LogLevel.V);
            isScreenOn = true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        new DebugLog("MyActivity.onStart", DebugLog.LogLevel.V);
        if (checkScreenOnListenerUnnecessary())
            return;
        if (!isShortcut && !isBroadcastRegistered) {
            //亮屏监听，防止在应用开启熄屏又亮屏时显示警告toast
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(broadcastReceiver, intentFilter);
            isBroadcastRegistered = true;
        }
    }

    //目前已知的问题有启动失败和主题应用失败
    @Override
    protected void onRestart() {
        new DebugLog("MyActivity.onRestart", DebugLog.LogLevel.V);
        if (checkScreenOnListenerUnnecessary())
            return;
        if (!isShortcut)
            //由于onRestart比SCREEN_ON更早执行，因此在此设置延迟
            new Handler().postDelayed(() -> {
                if (!isScreenOn)
                    new TextToast(getApplicationContext(), getString(R.string.activity_onrestart_notice));
                isScreenOn = false;
            }, 1000);
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (checkScreenOnListenerUnnecessary())
            return;
        new DebugLog("MyActivity.onDestroy", DebugLog.LogLevel.V);
        if (!isShortcut)
            unregisterReceiver(broadcastReceiver);
        if (helpDialogReference != null && helpDialogReference.isShowing()) {
            new DebugLog(getClass().getSimpleName(), "helpDialogReference*", DebugLog.LogLevel.V);
            helpDialogReference.dismiss();
            helpDialogReference = null;
        }
    }

    //辅助服务申请回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new DebugLog("MyActivity.onActivityResult", DebugLog.LogLevel.V);
        if (this.requestCode == requestCode) {
            //若同意
            if (resultCode == Activity.RESULT_OK) {
                devicePolicyManager.lockNow();
                /*如果是在App Shortcut中调用不要解锁
                 如果需要二次确认，禁用设备管理器。*/
                if (!isShortcut && !ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM))
                    devicePolicyManager.removeActiveAdmin(componentName);
            } else
                new TextToast(this, getString(R.string.lockscreen_failed));
            finish();
        }
    }

    //免root模式下锁屏重启的初始化
    void URLockScrInit(boolean isShortcut, int requestCode, DevicePolicyManager devicePolicyManager, ComponentName componentName) {
        new DebugLog("URLockScrInit: isShortcut:" + isShortcut + " requestCode:" + requestCode + " ..", DebugLog.LogLevel.V);
        this.isShortcut = isShortcut;
        this.componentName = componentName;
        this.devicePolicyManager = devicePolicyManager;
        this.requestCode = requestCode;
    }

    private boolean checkScreenOnListenerUnnecessary() {
        Class[] shortTermSurvivalActivities = new Class[]{Shortcut.class, LockScreenAssist.class};
        for (Class c : shortTermSurvivalActivities)
            if (this.getClass().equals(c)) {
                new DebugLog("checkScreenOnListenerUnnecessary: " + c);
                return true;
            }
        return false;
    }
}
