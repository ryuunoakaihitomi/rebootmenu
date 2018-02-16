package com.ryuunoakaihitomi.rebootmenu;

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
import android.provider.Settings;
import android.text.TextUtils;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
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

    //亮屏监听用变量和接收器
    boolean isScreenOn;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new DebugLog("亮屏", DebugLog.V);
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
        mainDialog = UIUtils.LoadDialog(ConfigManager.get(ConfigManager.WHITE_THEME), this);
        mainDialog.setTitle(getString(R.string.unroot_title));
        final String[] uiTextList = {getString(R.string.lockscreen), getString(R.string.system_power_dialog)};
        DialogInterface.OnClickListener mainListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i == 0)
                    lockscreen();
                else
                    //调用系统电源菜单无需二次确认
                    accessbilityon();
            }
        };
        //Android5.0以下不支持系统电源菜单
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mainDialog.setItems(uiTextList, mainListener);
        else {
            mainDialog.setItems(new String[]{getString(R.string.lockscreen)}, mainListener);
            new TextToast(getApplicationContext(), getString(R.string.lollipop_notice));
        }
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
                new TextToast(getApplicationContext(), true, "生きて");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.bilibili.com/video/av19384384/"));
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
        //亮屏监听，防止在应用开启熄屏又亮屏时显示警告toast
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    //目前已知的问题有启动失败和主题应用失败
    @Override
    protected void onRestart() {
        //由于onRestart比SCREEN_ON更早执行，因此在此设置延迟
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
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, AdminReceiver.class);
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
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    //打开辅助服务设置或者发送执行广播
    private void accessbilityon() {
        if (!isAccessibilitySettingsOn(getApplicationContext())) {
            new TextToast(getApplicationContext(), getString(R.string.service_disabled));
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        } else {
            sendBroadcast(new Intent(getString(R.string.service_action_key)));
        }
        finish();
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
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
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
}
