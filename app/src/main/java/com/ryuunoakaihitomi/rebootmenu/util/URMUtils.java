package com.ryuunoakaihitomi.rebootmenu.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.SystemPowerDialog;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * 本应用中免（无需）root模式的工具集合
 * Created by ZQY on 2018/4/15.
 *
 * @author ZQY
 */

public class URMUtils {
    /**
     * 用辅助功能锁屏
     *
     * @param activity            1
     * @param componentName       2
     * @param requestCode         3
     * @param devicePolicyManager 4
     * @param needConfig          是否需要 配置管理员
     */

    public static void lockScreen(@NonNull Activity activity, ComponentName componentName, int requestCode, DevicePolicyManager devicePolicyManager, boolean needConfig) {
        new DebugLog("lockScreen", DebugLog.LogLevel.V);
        //设备管理器是否启用
        boolean active = devicePolicyManager.isAdminActive(componentName);
        //Android P
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
            //自动移除不必要的管理员，避免在卸载时造成困扰
            if (active) devicePolicyManager.removeActiveAdmin(componentName);
            //请求打开辅助服务设置
            if (!isAccessibilitySettingsOn(activity.getApplicationContext())) {
                new TextToast(activity.getApplicationContext(), activity.getString(R.string.service_disabled));
                activity.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else
                LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(SystemPowerDialog.LOCK_SCREEN_ACTION));
            activity.finish();
        } else {
            if (!active) {
                //请求启用
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, activity.getString(R.string.service_explanation));
                activity.startActivityForResult(intent, requestCode);
            } else {
                devicePolicyManager.lockNow();
                if (needConfig)
                    //如果需要二次确认，禁用设备管理器。（这里的策略和root模式的锁屏无需确认不同）
                    if (!ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM)) {
                        devicePolicyManager.removeActiveAdmin(componentName);
                    }
                activity.finish();
            }
        }
    }

    /**
     * 打开辅助服务设置或者发送执行广播
     *
     * @param activity 1
     */
    public static void accessibilityOn(Activity activity) {
        new DebugLog("accessibilityOn", DebugLog.LogLevel.V);
        if (!isAccessibilitySettingsOn(activity.getApplicationContext())) {
            new TextToast(activity.getApplicationContext(), activity.getString(R.string.service_disabled));
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            activity.startActivity(intent);
        } else {
            LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent(SystemPowerDialog.POWER_DIALOG_ACTION));
        }
        activity.finish();
    }


    /**
     * 检查辅助服务是否打开
     *
     * @param mContext 1
     * @return boolean
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isAccessibilitySettingsOn(Context mContext) {
        //注意：不要使用以下注释掉的代码取无障碍服务开启状态！disableSelf()之后仍返回true
        //noinspection ConstantConditions
        //return ((AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE)).isEnabled();
        new DebugLog("isAccessibilitySettingsOn", DebugLog.LogLevel.V);
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

    /**
     * 用设备政策管理器实现重启
     *
     * @param devicePolicyManager 1
     * @param componentName       2
     * @param activity            3
     */
    @TargetApi(Build.VERSION_CODES.N)
    public static void rebootWithDevicePolicyManager(DevicePolicyManager devicePolicyManager, ComponentName componentName, Activity activity) {
        new DebugLog("rebootWithDevicePolicyManager", DebugLog.LogLevel.V);
        try {
            devicePolicyManager.reboot(componentName);
        } catch (Throwable t) {
            new TextToast(activity.getApplicationContext(), true, activity.getString(R.string.dpm_reboot_error));
            new DebugLog(t, "rebootWithDevicePolicyManager", true);
        }
        activity.finish();
    }

    /**
     * 尝试调用PowerManager的reboot方法，一般来说只有系统应用能用。
     *
     * @param context 1
     * @param reason  参数
     */
    @SuppressWarnings("ConstantConditions")
    public static void rebootWithPowerManager(Context context, String reason) {
        new DebugLog("rebootWithPowerManager: reason:" + reason, DebugLog.LogLevel.V);
        ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).reboot(reason);
    }
}
