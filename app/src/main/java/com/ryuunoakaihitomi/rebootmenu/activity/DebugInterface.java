package com.ryuunoakaihitomi.rebootmenu.activity;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.PowerManager;
import android.os.Process;
import android.os.SELinux;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.EditText;

import com.android.server.SystemConfig;
import com.ryuunoakaihitomi.rebootmenu.BuildConfig;
import com.ryuunoakaihitomi.rebootmenu.MyApplication;
import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.activity.base.Constants;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.SpecialSupport;
import com.ryuunoakaihitomi.rebootmenu.util.hook.XposedUtils;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;
import static com.ryuunoakaihitomi.rebootmenu.util.StringUtils.varArgsToString;

/**
 * 调试界面
 * Created by ZQY on 2019/1/14.
 */

public class DebugInterface extends Activity {
    private static final String TAG = "DebugInterface";

    /**
     * 系统权限锁屏
     * 警告：priv_app权限也不会生效
     *
     * @param context {@link "android.permission.DEVICE_POWER"}
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void lockScreenPM(Context context) {
        new DebugLog("lockScreenPM", DebugLog.LogLevel.V);
        try {
            PowerManager.class.
                    getMethod("goToSleep", long.class)
                    .invoke(context.getSystemService(Context.POWER_SERVICE), SystemClock.uptimeMillis());
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void biometricPromptTest() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            BiometricPrompt bp = new BiometricPrompt.Builder(this)
                    .setTitle("Authenticate Test")
                    .setDescription("setDescription")
                    .setNegativeButton(getString(android.R.string.cancel), getMainExecutor(), (dialogInterface, i) -> print("canceled"))
                    .setSubtitle("setSubtitle")
                    .build();
            CancellationSignal signal = new CancellationSignal();
            signal.setOnCancelListener(() -> print("canceled from CancellationSignal"));
            bp.authenticate(signal, getMainExecutor(), new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, CharSequence errString) {
                    print("onAuthenticationError:" + varArgsToString(errorCode, errString));
                }

                @Override
                public void onAuthenticationFailed() {
                    print("onAuthenticationFailed");
                    finish();
                }

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    print("onAuthenticationSucceeded");
                }

                @Override
                public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                    print("onAuthenticationHelp:" + varArgsToString(helpCode, helpString));
                }
            });
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(signal::cancel);
                }
            }, 5000);
        }
    }

    /**
     * 取应用VersionName
     * （姑且放在这里）
     *
     * @param context c
     * @return vn
     */
    private static String getAppVersionName(@NonNull Context context) {
        new DebugLog("getAppVersionName: " + context, DebugLog.LogLevel.I);
        String versionName = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0)
                return "";
        } catch (Exception e) {
            new DebugLog(e, "getAppVersionName", true);
        }
        return versionName;
    }

    private void print(Object text) {
        new DebugLog(TAG, "print: " + text, DebugLog.LogLevel.I);
        new TextToast(this, String.valueOf(text));
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.exit(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (Build.VERSION.SDK_INT < M) return;
        if (Constants.DEBUG_INTERFACE_WRITE_EXTERNAL_STORAGE_REQUEST_CODE == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                print("granted");
            } else {
                print("denied " + shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE));
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_debug);
        EditText paramET = findViewById(R.id.param);
        Button execBtn = findViewById(R.id.exec);
        paramET.setOnEditorActionListener((textView, i, keyEvent) -> {
            if ((keyEvent != null && KeyEvent.KEYCODE_ENTER == keyEvent.getKeyCode() && KeyEvent.ACTION_DOWN == keyEvent.getAction())) {
                return execBtn.post(() -> {
                    //模拟出按钮点击UI效果
                    long now = SystemClock.uptimeMillis();
                    int[] pos = new int[2];
                    execBtn.getLocationOnScreen(pos);
                    float x = pos[0], y = pos[1];
                    Log.d(TAG, "onCreate: execBtn pos=" + varArgsToString(x, y));
                    ViewDebug.dumpCapturedView(TAG, execBtn);
                    MotionEvent beforeClickEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, x, y, 0);
                    now++;
                    MotionEvent afterClickEvent = MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, x, y, 0);
                    execBtn.onTouchEvent(beforeClickEvent);
                    execBtn.onTouchEvent(afterClickEvent);
                    beforeClickEvent.recycle();
                    afterClickEvent.recycle();
                });
                //execBtn.performClick();
            }
            return false;
        });
        execBtn.setOnClickListener(view -> {
            String param = paramET.getText().toString();
            switch (param.charAt(0)) {
                case '0':
                    print(SELinux.isSELinuxEnforced());
                    break;
                case '1':
                    print(SELinux.getContext());
                    break;
                case '2':
                    lockScreenPM(this);
                    break;
                case '3':
                    try {
                        print("logcat");
                        //保留
                        ArraySet<String> arraySet = SystemConfig.getInstance().getHiddenApiWhitelistedApps();
                        for (Object s : arraySet.toArray())
                            Log.d(TAG, "onCreate: getHiddenApiWhitelistedApps:" + s);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                    break;
                case '4':
                    print(XposedUtils.isActive);
                    break;
                case 'b':
                    biometricPromptTest();
                    break;
                //调试日志标记
                case 'd':
                    File tokenFile = new File(Environment.getExternalStorageDirectory().getPath() + "/" + DebugLog.TOKEN_TAG);
                    boolean exists = tokenFile.exists();
                    boolean succeeded = false;
                    if (exists)
                        succeeded = tokenFile.delete();
                    else {
                        try {
                            succeeded = tokenFile.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            int uid = Process.myUid();
                            print("createNewFile failed. " + varArgsToString(
                                    checkPermission(WRITE_EXTERNAL_STORAGE, Process.myPid(), uid),
                                    M <= Build.VERSION.SDK_INT ?
                                            ((AppOpsManager) Objects.requireNonNull(getSystemService(APP_OPS_SERVICE))).checkOpNoThrow(AppOpsManager.OPSTR_WRITE_EXTERNAL_STORAGE, uid, getPackageName()) :
                                            "?"));
                            if (Build.VERSION.SDK_INT >= M) {
                                requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, Constants.DEBUG_INTERFACE_WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                            }
                        }
                    }
                    print("debug: " + varArgsToString(!exists, succeeded));
                    break;
                //版本检查
                case 'V':
                case 'v':
                    print(BuildConfig.APK_PACK_TIME + "(" + getAppVersionName(this) + ")\ndebug:" + MyApplication.isDebug);
                    break;
                //测试异常
                case 'c':
                    throw new IllegalStateException("Test Exception");
                case 'Q':
                case 'q':
                    SpecialSupport.showQRCodeWithZxingApp(this, param.substring(1), Character.isUpperCase(param.charAt(0)));
                    break;
                default:
                    print(param);
            }
        });
        ViewDebug.dumpCapturedView(TAG, getWindow().getDecorView());
    }
}
