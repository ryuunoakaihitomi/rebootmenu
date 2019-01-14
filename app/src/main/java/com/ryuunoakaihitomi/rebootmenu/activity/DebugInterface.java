package com.ryuunoakaihitomi.rebootmenu.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SELinux;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.EditText;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import androidx.annotation.Nullable;

/**
 * 调试界面
 * Created by ZQY on 2019/1/14.
 */

public class DebugInterface extends Activity {

    /**
     * 系统权限锁屏
     * 警告：priv-app权限也不会生效
     *
     * @param context {@link "android.permission.DEVICE_POWER"}
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    private static void lockScreenPM(Context context) {
        new DebugLog("lockScreenPM", DebugLog.LogLevel.V);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            Method goToSleep = PowerManager.class.getMethod("goToSleep", long.class);
            goToSleep.invoke(pm, SystemClock.uptimeMillis());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_DeviceDefault);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_debug);
        EditText paramET = findViewById(R.id.param);
        Button execBtn = findViewById(R.id.exec);
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
                default:
                    print(param);
            }
        });
    }

    private void print(Object text) {
        new TextToast(this, String.valueOf(text));
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.exit(0);
    }
}
