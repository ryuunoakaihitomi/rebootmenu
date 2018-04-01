package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.provider.Settings;
import android.text.TextUtils;

import com.ryuunoakaihitomi.rebootmenu.util.TextToast;
import com.ryuunoakaihitomi.rebootmenu.util.UIUtils;

/**
 * 免root模式活动
 * Created by ZQY on 2018/2/8.
 */

public class UnRootMode extends Activity {
    AlertDialog.Builder mainDialog;
    DevicePolicyManager devicePolicyManager;
    ComponentName componentName;

    boolean isScreenOn;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isScreenOn = true;
        }
    };

    //辅助服务申请回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (0 == requestCode) {
            //若同意
            if (resultCode == Activity.RESULT_OK) {
                devicePolicyManager.lockNow();
                //如果需要二次确认，禁用设备管理器。
                if (!ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM))
                    devicePolicyManager.removeActiveAdmin(componentName);
            } else
                new TextToast(this, getString(R.string.lockscreen_failed));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            UIUtils.transparentStatusBar(this);
        mainDialog = UIUtils.LoadDialog(ConfigManager.get(ConfigManager.WHITE_THEME), this);
        mainDialog.setTitle(getString(R.string.unroot_title));
        String[] uiTextList;
        DialogInterface.OnClickListener mainListener = new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    case 0:
                        lockscreen();
                        break;
                    case 1:
                        //调用系统电源菜单无需二次确认
                        accessbilityon(UnRootMode.this);
                        break;
                    case 2:
                        initDPMCN();
                        try {
                            devicePolicyManager.reboot(componentName);
                        } catch (Throwable ignored) {
                            new TextToast(getApplicationContext(), true, getString(R.string.dpm_reboot_error));
                        }
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            uiTextList = new String[]{getString(R.string.lockscreen), getString(R.string.system_power_dialog), getString(R.string.reboot)};
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
        //是否需要退出键
        if (!ConfigManager.get(ConfigManager.CANCELABLE))
            mainDialog.setPositiveButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
        //帮助
        mainDialog.setNegativeButton(R.string.help, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UIUtils.helpDialog(UnRootMode.this, mainDialog, ConfigManager.get(ConfigManager.CANCELABLE), ConfigManager.get(ConfigManager.WHITE_THEME));
            }
        });
        //egg
        mainDialog.setNeutralButton(" ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new TextToast(getApplicationContext(), true, "とまれかくもあはれ\nほたるほたるおいで");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://music.163.com/#/song?id=22765874"));
                startActivity(intent);
                finish();
            }
        });
        //不按退出的退出监听
        mainDialog.setCancelable(ConfigManager.get(ConfigManager.CANCELABLE));
        mainDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface p1) {
                new TextToast(getApplicationContext(), false, getString(R.string.exit_notice));
                finish();
            }
        });
        UIUtils.alphaShow(mainDialog.create(), 0.75f);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onRestart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isScreenOn)
                    new TextToast(getApplicationContext(), getString(R.string.activity_onrestart_notice));
                isScreenOn = false;
            }
        }, 1000);
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    //用辅助功能锁屏
    private void lockscreen() {
        initDPMCN();
        //设备管理器是否启用
        boolean active = devicePolicyManager.isAdminActive(componentName);
        if (!active) {
            //请求启用
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.service_explanation));
            startActivityForResult(intent, 0);
        } else {
            devicePolicyManager.lockNow();
            //如果需要二次确认，禁用设备管理器。（这里的策略和root模式的锁屏无需确认不同）
            if (!ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM)) {
                devicePolicyManager.removeActiveAdmin(componentName);
            }
            //自杀退出
            Process.killProcess(Process.myPid());
        }
    }

    /**
     * 打开辅助服务设置或者发送执行广播
     *
     * @param activity a
     */
    static void accessbilityon(Activity activity) {
        if (!isAccessibilitySettingsOn(activity.getApplicationContext())) {
            new TextToast(activity.getApplicationContext(), activity.getString(R.string.service_disabled));
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            activity.startActivity(intent);
        } else {
            activity.sendBroadcast(new Intent(activity.getString(R.string.service_action_key)));
        }
        activity.finish();
    }


    /**
     * 检查辅助服务是否打开
     *
     * @param mContext 上下文
     * @return 返回值
     */
    static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + SystemPowerDialog.class.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
        if (accessibilityEnabled == 1) {
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //初始化设备政策管理者和组件名称
    void initDPMCN() {
        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);
    }
}
