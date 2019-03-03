package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.app.ActivityThread;
import android.content.Context;
import android.os.Looper;
import android.os.ServiceManager;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 以root权限启动RMPowerActionService
 * Created by ZQY on 2019/3/3.
 */

public class SuServiceStarter {
    private static final String TAG = "SuServiceStarter";

    private SuServiceStarter() {
    }

    public static void main(String[] args) {
        new SuServiceStarter().hang();
    }

    /**
     * 调用：使用root权限加载服务
     *
     * @param context         {@link Context#getPackageResourcePath()}
     * @param onlyKillService 若为真则只是杀死服务
     */
    public static void invoke(Context context, boolean onlyKillService) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream stream = new DataOutputStream(process.getOutputStream());
            stream.writeBytes("killall " + RMPowerActionService.TAG + '\n');
            if (!onlyKillService) {
                stream.writeBytes("export CLASSPATH=" + context.getPackageResourcePath() + '\n');
                stream.writeBytes("exec app_process /system/bin --nice-name=" + RMPowerActionService.TAG + " " + SuServiceStarter.class.getName() + /*Daemon Mark*/" &\n");
            }
            stream.flush();
        } catch (IOException e) {
            Log.e(TAG, "invoke: ", e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void hang() {
        Looper.prepareMainLooper();
        RMPowerActionService service = new RMPowerActionService(ActivityThread.systemMain().getSystemContext());
        ServiceManager.addService(XposedUtils.getServiceName(RMPowerActionService.TAG), service);
        service.allServicesInitialised();
        Looper.loop();
        throw new RuntimeException("Main thread loop unexpectedly exited");
    }
}
