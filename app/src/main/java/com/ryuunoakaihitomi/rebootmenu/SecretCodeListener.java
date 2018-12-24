package com.ryuunoakaihitomi.rebootmenu;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;
import com.ryuunoakaihitomi.rebootmenu.util.SuPlugin;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;

/**
 * 神秘代码调用试验性功能，不开放在帮助文档
 * <p>
 * Created by ZQY on 2018/12/24.
 */

public class SecretCodeListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SECRET_CODE".equals(intent.getAction())) {
            new TextToast(context, true, context.getString(R.string.hidden_function_description));
            ShellUtils.runSuJavaWithAppProcess(context, SuPlugin.class.getName(), SuPlugin.ARG_LOCK_SHUT_DOWN_DIALOG);
        }
    }
}
