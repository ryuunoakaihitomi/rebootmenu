package com.ryuunoakaihitomi.rebootmenu.util.hook;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.SystemService;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.util.DumpUtils;
import com.ryuunoakaihitomi.rebootmenu.BuildConfig;
import com.ryuunoakaihitomi.rebootmenu.IRMPowerActionService;
import com.ryuunoakaihitomi.rebootmenu.util.StringUtils;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.O;

/**
 * IRMPowerActionService AIDL的具体实现
 * Created by ZQY on 2019/1/3.
 */

@TargetApi(M)
final class RMPowerActionService extends IRMPowerActionService.Stub {

    static final String TAG = "RMPowerActionService";

    private final Context mContext;
    private PackageManager mPackageManager;
    private PowerManager mPowerManager;
    //API比aidl稳定，但是导包是个问题，现在暂时使用反射将就下
    private Method goToSleep, rebootSafeMode, shutdown;

    private final RMPowerActionRecord record;

    RMPowerActionService(Context context) {
        Log.d(TAG, "Constructor: " + context);
        mContext = context;
        record = RMPowerActionRecord.instance;
    }

    @Override
    public void lockScreen() {
        preliminaryPreparation("lockScreen");
        injectSystemThread(() -> invokeNoThrowAndReturn(goToSleep, mPowerManager, SystemClock.uptimeMillis()));
    }

    @Override
    public void reboot(String reason) {
        preliminaryPreparation("reboot", reason);
        injectSystemThread(() -> mPowerManager.reboot(reason));
    }

    @TargetApi(M)
    @Override
    public void safeMode() {
        preliminaryPreparation("safeMode");
        injectSystemThread(() -> {
            if (SDK_INT < N) {
                //ShutdownThread.java
                // Indicates whether we are rebooting into safe mode
                //public static final String REBOOT_SAFEMODE_PROPERTY = "persist.sys.safemode";
                SystemProperties.set("persist.sys.safemode", "1");
                mPowerManager.reboot(null);
                return;
            }
            invokeNoThrowAndReturn(rebootSafeMode, mPowerManager);
        });
    }

    @TargetApi(M)
    @Override
    public void shutdown() {
        preliminaryPreparation("shutdown");
        injectSystemThread(() -> {
            Object[] params = SDK_INT >= N ?
                    new Object[]{false, null, false} : new Object[]{false, false};
            invokeNoThrowAndReturn(shutdown, mPowerManager, params);
        });
    }

    @Override
    public void hotReboot() {
        preliminaryPreparation("hotReboot");
        injectSystemThread(() -> {
            //throw new IllegalStateException("Hot Reboot Request");    /*在此抛出异常也会导致热重启*/
            SystemService.restart("zygote");
        });
    }

    //测试服务状态
    @Override
    public void ping() {
        preliminaryPreparation("ping");
        Log.i(TAG, "ping: " + StringUtils.varArgsToString(mPowerManager, mPackageManager, mContext, Process.myUid(), getCallingPid(), getCallingUid()));
        injectSystemThread(() ->
                Log.i(TAG, "ping: " + StringUtils.varArgsToString(pingBinder(), getCallingPid(), getCallingUid())));
    }

    //For "dumpsys"
    @Override
    protected void dump(@NonNull FileDescriptor fd, @NonNull PrintWriter fOut, @Nullable String[] args) {
        if (!enforceDumpPermission(fOut)) return;
        if (args != null) {
            if (args.length == 0) {
                dumpInfo(fOut);
                return;
            }
            for (String arg : args) {
                switch (arg) {
                    //Help
                    case "-h":
                        fOut.println("RMPowerActionService dump options:");
                        fOut.println("  -c");
                        fOut.println("    Clear operation record.");
                        fOut.println(" [-d]");
                        fOut.println("    Print dump information.");
                        fOut.println("  -h");
                        fOut.println("    Print this help text.");
                        fOut.println("  -r");
                        fOut.println("    Print operation record.");
                        break;
                    //Clear record
                    case "-c":
                        record.clear();
                        fOut.println("Record cleared.");
                        break;
                    //show Record
                    case "-r":
                        fOut.println("PowerActionRecord:");
                        for (Map.Entry<Long, String> entry : record.traversal().entrySet()) {
                            fOut.println(entry.getKey() + "\t" + entry.getValue());
                        }
                        break;
                    //Dump info
                    case "-d":
                        dumpInfo(fOut);
                        break;
                    default:
                        fOut.println("Unknown arguments: " + arg);
                }
            }
        }
    }

    @Override
    public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        Log.v(TAG, "onTransact: code=" + code);
        return super.onTransact(code, data, reply, flags);
    }

    //插入到系统线程才有系统权限
    private void injectSystemThread(Runnable r) {
        new Handler(mContext.getMainLooper()).post(r);
    }

    //所有的系统服务都已经初始化完成
    @SuppressWarnings({"JavaReflectionMemberAccess", "unchecked"})
    @TargetApi(N)
    void allServicesInitialised() {
        Log.d(TAG, "allServicesInitialised");
        mPowerManager = mContext.getSystemService(PowerManager.class);
        //PackageManager无法用以下方法从system_server的context中获取
        //mPackageManager = mContext.getSystemService(PackageManager.class);
        mPackageManager = mContext.getPackageManager();
        Class pmCls = PowerManager.class;

        /*
         * Forces the device to go to sleep.
         * <p>
         * Overrides all the wake locks that are held.
         * This is what happens when the power key is pressed to turn off the screen.
         * </p><p>
         * Requires the {@link android.Manifest.permission#DEVICE_POWER} permission.
         * </p>
         *
         * @param time The time when the request to go to sleep was issued, in the
         * {@link SystemClock#uptimeMillis()} time base.  This timestamp is used to correctly
         * order the go to sleep request with other power management functions.  It should be set
         * to the timestamp of the input event that caused the request to go to sleep.
         *
         * @see #userActivity
         * @see #wakeUp
         *
         * @removed Requires signature permission.
         */
        goToSleep = getMethodNoThrow(pmCls, "goToSleep", long.class);

        /*
         * Reboot the device. Will not return if the reboot is successful.
         * <p>
         * Requires the {@link android.Manifest.permission#REBOOT} permission.
         * </p>
         * @hide
         */
        if (SDK_INT >= N)
            rebootSafeMode = getMethodNoThrow(pmCls, "rebootSafeMode");

        shutdown = SDK_INT >= N ?

                /*
                 * Turn off the device.
                 *
                 * @param confirm If true, shows a shutdown confirmation dialog.
                 * @param reason code to pass to android_reboot() (e.g. "userrequested"), or null.
                 * @param wait If true, this call waits for the shutdown to complete and does not return.
                 *
                 * @hide
                 */
                getMethodNoThrow(pmCls, "shutdown", boolean.class, String.class, boolean.class) :

                /*
                 * Turn off the device.
                 *
                 * @param confirm If true, shows a shutdown confirmation dialog.
                 * @param wait If true, this call waits for the shutdown to complete and does not return.
                 *
                 * @hide
                 */
                getMethodNoThrow(pmCls, "shutdown", boolean.class, boolean.class);

        Log.d(TAG, "allServicesInitialised: Methods:" + StringUtils.varArgsToString(goToSleep, rebootSafeMode, shutdown));
    }

    /**
     * 检测调用方
     */
    private boolean checkSelfInvoke() {
        int uid = Binder.getCallingUid();
        if (mPackageManager == null) {
            Log.e(TAG, "checkSelfInvoke: ", new IllegalStateException("mPackageManager not initialized"));
            return false;
        }
        if (uid == Process.SYSTEM_UID || uid == 0) {
            Log.i(TAG, "checkSelfInvoke: Invoked by uid(" + uid + ") with system privilege.");
            return true;
        }
        String[] packages = mPackageManager.getPackagesForUid(uid);
        if (packages != null && packages.length > 0) {
            for (String pn : packages) {
                Log.d(TAG, "checkSelfInvoke: pn=" + pn);
                if (!pn.startsWith(BuildConfig.APPLICATION_ID)) {
                    Log.w(TAG, "checkSelfInvoke: May be invoked by an illegal source.");
                    return false;
                }
            }
        } else {
            Log.e(TAG, "checkSelfInvoke: ", new NullPointerException("packages is null"));
            return false;
        }
        return true;
    }

    private boolean enforceDumpPermission(PrintWriter pw) {
        if (Build.VERSION.SDK_INT >= O) return DumpUtils.checkDumpPermission(mContext, TAG, pw);
        else {
            try {
                mContext.enforceCallingOrSelfPermission(Manifest.permission.DUMP, "enforceDumpPermission");
                return true;
            } catch (SecurityException e) {
                pw.println(StringUtils.printStackTraceToString(e));
                Log.e(TAG, "enforceDumpPermission: ", e);
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private Method getMethodNoThrow(@NonNull Class sourceClass, String name, Class<?>... parameterTypes) {
        try {
            return sourceClass.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getMethodNoThrow: ", e);
        }
        return null;
    }

    private void invokeNoThrowAndReturn(@NonNull Method method, Object instance, Object... args) {
        try {
            method.invoke(instance, args);
        } catch (Throwable throwable) {
            //IllegalAccessException|IllegalArgumentException|InvocationTargetException|NullPointerException
            Log.e(TAG, "invokeNoThrowAndReturn: ", throwable);
        }
    }

    private void dumpInfo(PrintWriter pw) {
        pw.print(this);
        pw.print(" dump info:");
        pw.println();
        int longestLen = 0;
        for (Field field : getClass().getDeclaredFields()) {
            try {
                String dmpLine = StringUtils.varArgsToString(field.getName(), field.get(this));
                if (longestLen < dmpLine.length()) longestLen = dmpLine.length();
                pw.println(dmpLine);
            } catch (IllegalAccessException e) {
                pw.println(e);
            }
        }
        //https://stackoverflow.com/questions/1235179/simple-way-to-repeat-a-string-in-java
        pw.println(TextUtils.join("", Collections.nCopies(longestLen, '-')));
    }

    /**
     * 前期准备：包括检查权限，打印信号日志，记录操作
     *
     * @param operationTag 操作标识
     * @param args         附加参数
     */
    private void preliminaryPreparation(String operationTag, Object... args) {
        if (!checkSelfInvoke()) throw new SecurityException("Permission Denied!");
        String reportBody = operationTag;
        if (args != null) reportBody += ' ' + StringUtils.varArgsToString(args);
        Log.i(TAG, reportBody);
        record.add(SystemClock.elapsedRealtime(), reportBody);
    }

    //操作记录
    private enum RMPowerActionRecord {

        instance;
        private @NonNull
        final
        ArrayList<Long> mOpTimes = new ArrayList<>();
        private @NonNull
        final
        ArrayList<String> mOpTags = new ArrayList<>();

        void add(Long time, String tag) {
            mOpTimes.add(time);
            mOpTags.add(tag);
        }

        void clear() {
            mOpTimes.clear();
            mOpTags.clear();
        }

        Map<Long, String> traversal() {
            Map<Long, String> ret = new LinkedHashMap<>();
            for (int i = 0; i < mOpTimes.size(); i++)
                ret.put(mOpTimes.get(i), mOpTags.get(i));
            return ret;
        }
    }
}
