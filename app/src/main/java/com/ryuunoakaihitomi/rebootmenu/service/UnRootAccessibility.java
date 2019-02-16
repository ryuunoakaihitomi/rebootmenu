package com.ryuunoakaihitomi.rebootmenu.service;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.activity.LockScreenAssist;
import com.ryuunoakaihitomi.rebootmenu.activity.RootMode;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.LocalBroadcastManager;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import java.util.ArrayList;
import java.util.List;

/**
 * 辅助服务
 * Created by ZQY on 2018/2/12.
 */

public class UnRootAccessibility extends AccessibilityService {

    public static final String
            POWER_DIALOG_ACTION = "com.ryuunoakaihitomi.rebootmenu.POWER_DIALOG_ACTION",
            LOCK_SCREEN_ACTION = "com.ryuunoakaihitomi.rebootmenu.LOCK_SCREEN_ACTION";
    private final BroadcastReceiver mPowerDialogBroadcastReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            new DebugLog("onReceive: will perform AccessibilityService.GLOBAL_ACTION_POWER_DIALOG");
            //调用系统电源菜单核心代码
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
            new TextToast(getApplicationContext(), getString(R.string.spd_showed));
        }
    };
    private final BroadcastReceiver mLockScreenBroadcastReceiver = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.P)
        @Override
        public void onReceive(Context context, Intent intent) {
            new DebugLog("GLOBAL_ACTION_LOCK_SCREEN -> " + performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN));
        }
    };
    private boolean isBroadcastRegistered;
    private LocalBroadcastManager localBroadcastManager;

    @Override
    public void onCreate() {
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        super.onCreate();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        new DebugLog("SystemPowerDialog.onServiceConnected", DebugLog.LogLevel.V);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            loadNoticeBar();
            registerBroadcastReceiver(mPowerDialogBroadcastReceiver, POWER_DIALOG_ACTION);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                registerBroadcastReceiver(mLockScreenBroadcastReceiver, LOCK_SCREEN_ACTION);
            isBroadcastRegistered = true;
        } else {
            new DebugLog("onServiceConnected: Build.VERSION_CODES.LOLLIPOP?", DebugLog.LogLevel.E);
            System.exit(-1);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopForeground(true);
        new DebugLog("SystemPowerDialog.onUnbind", DebugLog.LogLevel.V);
        if (isBroadcastRegistered) {
            isBroadcastRegistered = false;
            localBroadcastManager.unregisterReceiver(mPowerDialogBroadcastReceiver);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1)
                localBroadcastManager.unregisterReceiver(mLockScreenBroadcastReceiver);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        String className = accessibilityEvent.getClassName().toString();
        new DebugLog("SystemPowerDialog.onAccessibilityEvent className:" + className, DebugLog.LogLevel.V);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            //使用root模式就没有必要保留辅助服务
            if (RootMode.class.getName().equals(className))
                disableSelf();
                //Wear OS H 以下也没有必要
            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P && SpecialSupport.isAndroidWearOS(this))
                disableSelf();
    }

    @Override
    public void onInterrupt() {
    }

    //目前大部分环境中都无效的保活方式
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    //常驻通知
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void loadNoticeBar() {
        Notification.Builder builder;
        final String CHANNEL_ID = "URA";
        final int NOTIFICATION_ID = 1;
        //Oreo以上适配通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //尽管IMPORTANCE_MIN在26中无效...
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.service_name), NotificationManager.IMPORTANCE_MIN);
            //noinspection ConstantConditions
            getSystemService(NotificationManager.class).createNotificationChannel(notificationChannel);
            builder = new Notification.Builder(this, CHANNEL_ID);
            //根据文档：如果非要用一个来代替的话，使用免root的锁屏
            //理解错误，这个方法的作用是使用通知替代一个Shortcut，当通知出现时Shortcut一定会被隐藏
            //builder.setShortcutId("ur_l");
        } else
            builder = new Notification.Builder(this);
        builder
                .setContentIntent(PendingIntent.getActivity(this, 0, getPackageManager().getLaunchIntentForPackage(getPackageName()), 0))
                .setOngoing(true)
                //尽管在26上不能折叠通知（需要手动设置），但可以将其放置在较低的位置（已废弃）
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(R.drawable.small_icon)
                //没有必要显示在锁屏上
                .setVisibility(Notification.VISIBILITY_SECRET)
                //秒表指示
                .setUsesChronometer(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //启用自动折叠
            builder.setStyle(new Notification.BigTextStyle()
                    .bigText(getString(R.string.notification_notice))
                    .setSummaryText(getString(R.string.accessibility_service_sunnary_hint))
                    .setBigContentTitle(getString(R.string.service_simple_name)));
        } else {
            //在Android5.1中BigTextStyle的方法可能无法显示，作为兼容
            builder.setContentText(getString(R.string.notification_notice))
                    .setContentTitle(getString(R.string.service_simple_name));
        }
        //Wear OS不需要太复杂的通知
        if (SpecialSupport.isAndroidWearOS(this)) {
            builder.setUsesChronometer(false)
                    .setStyle(null)
                    .setContentText(getString(R.string.accessibility_service_sunnary_hint));
        }
        //Wear OS快捷操作
        //---
        List<Notification.Action> wearActions = new ArrayList<>();
        //锁屏
        Notification.Action.Builder lockScreenActionBuilder = new Notification.Action.Builder(android.R.drawable.ic_menu_slideshow, getString(R.string.lockscreen)
                , PendingIntent.getActivity(this, 0, new Intent(this, LockScreenAssist.class)
                .setAction(Intent.ACTION_ASSIST)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0));
        Notification.Action.WearableExtender lockScreenWearableExtender = new Notification.Action.WearableExtender();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            lockScreenActionBuilder.extend(lockScreenWearableExtender.setHintLaunchesActivity(true));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            //setHintDisplayActionInline在P中没有明显作用，在N和O中可以给通知卡片加action，仅支持一个
            lockScreenActionBuilder.extend(lockScreenWearableExtender.setHintDisplayActionInline(true));
        }
        wearActions.add(lockScreenActionBuilder.build());
        //电源菜单
        //Wear OS没有24的API Level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
            wearActions.add(new Notification.Action.Builder(Icon.createWithResource(this, android.R.drawable.ic_menu_preferences)
                    , getString(R.string.sys_power_dialog_tile_label)
                    , PendingIntent.getService(this, 0, new Intent(this, SPDTileEntry.class), 0))
                    .extend(new Notification.Action.WearableExtender().setHintDisplayActionInline(true))
                    .build());
        builder.extend(new Notification.WearableExtender()
                .addActions(wearActions));
        //---
        if (SpecialSupport.isMIUI()) {
            //MIUI会把秒表和发出时间一同显示
            builder.setShowWhen(false);
        }
        Notification notification = builder.build();
        startForeground(NOTIFICATION_ID, notification);
    }

    //注册广播接收器
    private void registerBroadcastReceiver(BroadcastReceiver broadcastReceiver, String action) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        localBroadcastManager.registerReceiver(broadcastReceiver, intentFilter);
    }
}
