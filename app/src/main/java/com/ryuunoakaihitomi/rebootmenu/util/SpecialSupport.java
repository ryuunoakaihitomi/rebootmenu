package com.ryuunoakaihitomi.rebootmenu.util;

import android.annotation.TargetApi;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

/**
 * 为特殊的Android环境提供支持
 * Created by ZQY on 2018/12/31.
 *
 * @author ZQY
 * <p>
 * Note:特殊环境数量较少，调试困难
 * 所以，为了找bug，在任何时候都保持logcat输出
 * 不适用{@link DebugLog}，改用{@link android.util.Log}
 */

public class SpecialSupport {
    private static final String TAG = "SpecialSupport";

    /**
     * 检测是否是WearOS(Android Wear)系统
     * Note:com.google.android.wearable.app，希望有更好的方法
     *
     * @param context {@link android.content.Context}
     * @return bool
     */
    public static boolean isAndroidWearOS(Context context) {
        try {
            PackageManager mgr = context.getPackageManager();
            PackageInfo info = mgr.getPackageInfo("com.google.android.wearable.app", 0);
            Log.i(TAG, "isAndroidWearOS: App Version:" + info.versionName);
            return (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }

    /**
     * 检测是否是MIUI系统
     *
     * @return bool
     */
    public static boolean isMIUI() {
        //android.os.SystemProperties.get("ro.miui.ui.version.name", "");
        // 如果返回值是「V10」，就是 MIUI 10
        String miuiVersionName;
        try {
            miuiVersionName = SystemProperties.get("ro.miui.ui.version.name", "");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return false;
        }
        Log.d(TAG, "isMIUI: miuiVersionName:" + miuiVersionName);
        return !TextUtils.isEmpty(miuiVersionName);
    }

    /**
     * 检测是否支持Live TV这个feature
     *
     * @param context {@link PackageManager}
     * @return boolean
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean hasTvFeature(Context context) {
        return context.getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_LIVE_TV);
    }

    /**
     * 检测是否在Android TV环境下
     *
     * @param context {@link PackageManager}
     * @return boolean
     * @see <a href="https://developer.android.com/training/tv/start/hardware.html#check-features">处理 TV 硬件 - 检查硬件功能</a>
     */
    public static boolean isAndroidTV(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;
    }

    //静态集合不需要实例化
    private SpecialSupport() {
    }

    /**
     * 通过zxing规范应用生成QRCode二维码
     * <p>
     * Reference:
     * https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/Intents.java
     * https://github.com/zxing/zxing/blob/master/android/src/com/google/zxing/client/android/Contents.java
     *
     * @param context          打开活动所需context
     * @param data             需要编码的文本内容
     * @param needToDisplayTxt 如果需要显示结果
     * @return 是否成功启动zxing规范二维码解析应用
     */
    public static boolean showQRCodeWithZxingApp(Context context, String data, boolean needToDisplayTxt) {
        Log.d(TAG, "showQRCodeWithZxingApp: data=" + Arrays.toString(new Object[]{data, needToDisplayTxt}));
        Intent intent = new Intent();

        /*
         * Send this intent to encode a piece of data as a QR code and display it full screen, so
         * that another person can scan the barcode from your screen.
         */
        intent.setAction("com.google.zxing.client.android.ENCODE");

        /*
         * The data to encode. Use {@link android.content.Intent#putExtra(String, String)} or
         * {@link android.content.Intent#putExtra(String, android.os.Bundle)},
         * depending on the type and format specified. Non-QR Code formats should
         * just use a String here. For QR Code, see Contents for details.
         */
        intent.putExtra("ENCODE_DATA", data);

        /*
         * The type of data being supplied if the format is QR Code. Use
         * {@link android.content.Intent#putExtra(String, String)} with one of {@link Contents.Type}.
         */
        //ENCODE_TYPE
        /*
         * Plain text. Use Intent.putExtra(DATA, string). This can be used for URLs too, but string
         * must include "http://" or "https://".
         */
        //TEXT_TYPE
        intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");

        /*
         * Normally the contents of the barcode are displayed to the user in a TextView. Setting this
         * boolean to false will hide that TextView, showing only the encode barcode.
         */
        intent.putExtra("ENCODE_SHOW_CONTENTS", needToDisplayTxt);

        //Start Activity
        try {
            context.startActivity(intent);
            return true;
        }
        //ActivityNotFoundException | other
        catch (Throwable ignored) {
            return false;
        }
    }
}
