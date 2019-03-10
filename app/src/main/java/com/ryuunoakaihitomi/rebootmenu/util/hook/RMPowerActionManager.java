package com.ryuunoakaihitomi.rebootmenu.util.hook;


import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.IRMPowerActionService;

/**
 * RMPowerActionService的对外API
 * Created by ZQY on 2019/1/3.
 */

public final class RMPowerActionManager {
    private static final String TAG = "RMPowerActionManager";

    private static volatile RMPowerActionManager mInstance;
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
        if (mInstance == null)
            synchronized (RMPowerActionManager.class) {
                if (mInstance == null) mInstance = new RMPowerActionManager();
            }
        return mInstance;
    }

    /**
     * 检查服务是否可用
     * 注意：由于单例模式，需要重启应用才能看到状态改变
     *
     * @return 若可用返回真
     */
    public boolean isServiceAvailable() {
        if (mService != null) {
            try {
                mService.ping();
                return true;
            } catch (RemoteException e) {
                return false;
            }
        } else return false;
    }

    /**
     * 锁屏
     */
    public void lockScreen() throws Throwable {
        mService.lockScreen();
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
     * 关机
     */
    public void shutdown() {
        try {
            mService.shutdown();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * 热重启
     */
    public void hotReboot() {
        try {
            mService.hotReboot();
        } catch (RemoteException | NullPointerException e) {
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
