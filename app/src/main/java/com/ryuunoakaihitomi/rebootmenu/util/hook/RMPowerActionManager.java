package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.IRMPowerActionService;

/**
 * RMPowerActionService的对外API
 * Created by ZQY on 2019/1/3.
 */

public class RMPowerActionManager {
    private static final String TAG = "RMPowerActionManager";

    private static RMPowerActionManager mInstance;
    private static IRMPowerActionService mService;

    private RMPowerActionManager() {
        mService = IRMPowerActionService.Stub.asInterface(ServiceManager.getService(XposedUtils.getServiceName(RMPowerActionService.TAG)));
    }

    /**
     * 获取实例
     *
     * @return RMPowerActionManager
     */
    //单例模式
    public static RMPowerActionManager getInstance() {
        if (mInstance == null) {
            mInstance = new RMPowerActionManager();
        }
        return mInstance;
    }

    /**
     * 锁屏
     */
    public void lockScreen() {
        try {
            mService.lockScreen();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重启
     *
     * @param reason 内核参数
     */
    public void reboot(String reason) {
        try {
            mService.reboot(reason);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * 安全模式
     */
    public void safeMode() {
        if (mService == null)
            return;
        try {
            mService.safeMode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试服务
     * Note:这个方法绝对不能抛出异常
     */
    public void testPrint() {
        Log.i(TAG, "testPrint: Test Service ");
        try {
            mService.ping();
        } catch (Throwable t) {
            Log.e(TAG, "testPrint: ", t);
        }
    }
}
