package com.ryuunoakaihitomi.rebootmenu.activity;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;
import com.ryuunoakaihitomi.rebootmenu.util.hook.XposedUtils;

import java.util.Objects;

import androidx.annotation.Nullable;

/**
 * 显示Xposed模块兼容性问题的通知
 * 由于{@link android.content.BroadcastReceiver}传入的Context不能直接显示通知，所以借助一个Activity的上下文实现
 * <p>
 * Created by ZQY on 2019/1/6.
 */

public class XposedWarning extends Activity {

    private static final String CHANNEL_ID = "XW";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Notification.Builder builder = null;
        //Xposed模块开启才值得显示通知，这是一切的前提
        if (XposedUtils.isActive) {
            if (XposedUtils.isSELinuxPatrolling() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, getString(R.string.xposed_warning), NotificationManager.IMPORTANCE_HIGH);
                Objects.requireNonNull(getSystemService(NotificationManager.class)).createNotificationChannel(notificationChannel);
                builder = new Notification.Builder(this, CHANNEL_ID);
                builder.setContentTitle(getString(R.string.xposed_warning)).setSmallIcon(android.R.drawable.ic_delete);
                //TV设备会影响功能，所以不兼容
                if (SpecialSupport.hasTvFeature(this))
                    builder.setContentText(getString(R.string.xposed_not_support))
                            .setSubText(getString(R.string.disabled_xposed_module_notice))
                            .setColor(Color.RED);
                else
                    //TvInputService冲突
                    builder.setContentText(getString(R.string.xposed_conflict_with_tv_ser))
                            .setColor(Color.YELLOW);
            }//不兼容（没有设备无法测试）
            else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                builder = new Notification.Builder(this);
                builder.setSmallIcon(android.R.drawable.ic_delete);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder.setColor(Color.BLACK);
                }
                builder.setContentTitle(getString(R.string.app_name) + getString(R.string.xposed_warning));
                builder.setContentText(getString(R.string.xposed_not_support));
            }
            if (builder != null) {
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.notify(0, builder.build());
            }
        }
        finish();
    }
}
