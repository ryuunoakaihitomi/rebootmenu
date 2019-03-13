package com.ryuunoakaihitomi.rebootmenu.csc_compat;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.util.LogPrinter;

/**
 * MainCompat
 * Created by ZQY on 2019/3/11.
 */

public class MainCompat {
    private static final String TAG = "MainCompat";

    /**
     * 是否应该加载闭源组件
     * 注意：闭源组件行为无法控制，
     * 要加载在常用的系统平台上以便保持稳定
     *
     * @return {@link Boolean}
     */
    static boolean shouldLoadCSCModule() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * 酷安
     *
     * @param context {@link Context}
     * @return hasCoolApk
     */
    public static boolean openCoolapk(Context context) {
        final String CA_PKG_NAME = "com.coolapk.market";
        final String CA_URL = "https://www.coolapk.com/apk/com.ryuunoakaihitomi.rebootmenu";
        LogPrinter printer = new LogPrinter(Log.VERBOSE, TAG);
        try {
            printer.println("Coolapk, versionName:"
                    + context.getPackageManager().getPackageInfo(CA_PKG_NAME, 0).versionName);
            Intent toCoolForum = new Intent()
                    .setData(Uri.parse("market://details?id=" + context.getPackageName()))
                    //按back键从这个任务返回的时候会回到home，防止返回重复进入
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME)
                    .setPackage(CA_PKG_NAME);
            context.startActivity(toCoolForum);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            printer.println("Me");
            return false;
        } catch (ActivityNotFoundException ignored) {
            printer.println("ActivityNotFoundException,CA is still here but frozen.");
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(CA_URL))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_TASK_ON_HOME));
            } catch (ActivityNotFoundException | SecurityException ignore) {
                printer.println("uninstall");
                //💊
                /*
                printer.println(CA_URL);
                ShellUtils.suCmdExec("pm uninstall " + context.getPackageName());
                context.startActivity(new Intent(Intent.ACTION_UNINSTALL_PACKAGE, Uri.fromParts("package", context.getPackageName(), null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        */
            }
            return true;
        }
    }
}
