package com.ryuunoakaihitomi.rebootmenu.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.activity.DebugInterface;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;
import com.ryuunoakaihitomi.rebootmenu.util.hook.RMPowerActionManager;
import com.ryuunoakaihitomi.rebootmenu.util.hook.SuJavaPlugin;
import com.ryuunoakaihitomi.rebootmenu.util.hook.XposedUtils;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

import java.util.Objects;

/**
 * 神秘代码调用试验性功能，不开放在帮助文档
 * <p>
 * Created by ZQY on 2018/12/24.
 */

public class SecretCodeListener extends BroadcastReceiver {
    private static final String TAG = "SecretCodeListener";
    //rebootmenu
    private static final String CODE_SIMPLE_TEST = "7326686368";
    //rebootmenu+m
    private static final String CODE_MULTIPLE_TEST = "73266863686";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || context == null) {
            DebugLog.e(TAG, "Intent or context or both are null.");
            return;
        }
        if ("android.provider.Telephony.SECRET_CODE".equals(intent.getAction())) {
            String code = Objects.requireNonNull(intent.getData()).getHost();
            new TextToast(context, true, context.getString(R.string.hidden_function_description));
            new DebugLog(TAG, "code:" + code, null);
            assert code != null;
            switch (code) {
                case CODE_SIMPLE_TEST:
                    if (XposedUtils.isActive)
                        RMPowerActionManager.getInstance().safeMode();
                    else
                        ShellUtils.runSuJavaWithAppProcess(context, SuJavaPlugin.class, SuJavaPlugin.ARG_SHUT_DOWN_DIALOG);
                    break;
                case CODE_MULTIPLE_TEST:
                    context.startActivity(new Intent(context, DebugInterface.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    break;
                default:
                    Log.e(TAG, "onReceive: ", new IllegalAccessException("Unknown code:" + code));
            }
        }
    }
}
