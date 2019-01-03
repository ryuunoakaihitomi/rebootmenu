package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.content.Context;
import android.os.Build;
import android.os.RemoteException;
import android.os.ServiceManager;

import com.ryuunoakaihitomi.rebootmenu.IRMPowerActionService;

/**
 * RMPowerActionService的对外API
 * Created by ZQY on 2019/1/3.
 */

public class RMPowerActionManager {

    private static RMPowerActionManager mInstance;
    private static IRMPowerActionService mService;

    //getServiceName
    private static String SERVICE_NAME = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? RMPowerActionService.TAG :
            (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? Context.TV_INPUT_SERVICE : "user." + RMPowerActionService.TAG);

    private RMPowerActionManager() {
        mService = IRMPowerActionService.Stub.asInterface(ServiceManager.getService(SERVICE_NAME));
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
}
