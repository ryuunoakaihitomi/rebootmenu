package com.ryuunoakaihitomi.rebootmenu.activity.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.activity.LockScreenAssist;
import com.ryuunoakaihitomi.rebootmenu.activity.Shortcut;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import androidx.annotation.Nullable;

/**
 * 自定义Activity：亮屏监听，设备管理员申请回调
 * Created by ZQY on 2018/4/15.
 */

@SuppressLint("Registered")
public class MyActivity extends Activity {
    private static final String TAG = "MyActivity";

    //防止helpDialog造成的WindowLeaked
    public static AlertDialog helpDialogReference;
    //主要Dialog防止WindowLeaked
    protected AlertDialog dialogInstance;
    protected ComponentName componentName;
    protected DevicePolicyManager devicePolicyManager;
    protected int requestCode;
    private boolean isBroadcastRegistered;
    //若是Shortcut就不用监听亮屏
    private boolean isShortcut;
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
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        //分割线，有些环境在onCreate时log过多
        new DebugLog(TAG, "onPostCreate() -- START", DebugLog.LogLevel.W);
        super.onPostCreate(savedInstanceState);
        new DebugLog(TAG, "onPostCreate() -- END", DebugLog.LogLevel.W);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new DebugLog("MyActivity.onStart", DebugLog.LogLevel.V);
        if (checkScreenOnListenerUnnecessary())
            return;
        if (!isBroadcastRegistered) {
            //亮屏监听，防止在应用开启熄屏又亮屏时显示警告toast
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            registerReceiver(broadcastReceiver, intentFilter);
            isBroadcastRegistered = true;
        }
    }

    //目前已知的问题有启动失败和主题应用失败
    //由于没有重建Dialog,主题应用问题可以忽视.启用失败可能是Android 7.1中Toast的原因而非后台原因
    @Override
    protected void onRestart() {
        new DebugLog("MyActivity.onRestart", DebugLog.LogLevel.V);
        if (checkScreenOnListenerUnnecessary())
            return;
        //由于onRestart比SCREEN_ON更早执行，因此在此设置延迟
        //不确定哪个更早调用，所以Toast的显示时机也会出现问题
        //综上两条，不再显示Toast，但为了调试仍保留相关代码
        new Handler().postDelayed(() -> {
            if (!isScreenOn)
                new DebugLog(TAG, "activity_onRestart_notice", DebugLog.LogLevel.I);
            isScreenOn = false;
        }, 1000);
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new DebugLog("MyActivity.onDestroy", DebugLog.LogLevel.V);
        if (checkScreenOnListenerUnnecessary())
            return;
        unregisterReceiver(broadcastReceiver);
        //清掉dialog防止WindowLeaked
        if (helpDialogReference != null && helpDialogReference.isShowing()) {
            new DebugLog(getClass().getSimpleName(), "helpDialogReference*", DebugLog.LogLevel.V);
            helpDialogReference.dismiss();
            helpDialogReference = null;
        }
        if (dialogInstance != null) {
            dialogInstance.dismiss();
            //https://stackoverflow.com/questions/11590382/android-view-windowleaked
            /*
            Simply dismissing the dialog was not enough to get rid of the error for me. It turns out my code was holding onto a reference to the dialog even after it was dismissed. The key for me was to set that reference to null after dismissing the dialog.
             */
            dialogInstance = null;
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
    protected void URLockScrInit(boolean isShortcut, int requestCode, DevicePolicyManager devicePolicyManager, ComponentName componentName) {
        new DebugLog("URLockScrInit: isShortcut:" + isShortcut + " requestCode:" + requestCode + " ..", DebugLog.LogLevel.V);
        this.isShortcut = isShortcut;
        this.componentName = componentName;
        this.devicePolicyManager = devicePolicyManager;
        this.requestCode = requestCode;
    }

    private boolean checkScreenOnListenerUnnecessary() {
        //若是Shortcut或者LockScreenAssist等存活时间极短的活动就不用监听亮屏
        Class[] shortTermSurvivalActivities = new Class[]{Shortcut.class, LockScreenAssist.class};
        for (Class c : shortTermSurvivalActivities)
            if (this.getClass().equals(c)) {
                new DebugLog("checkScreenOnListenerUnnecessary: " + c);
                return true;
            }
        return false;
    }
}
