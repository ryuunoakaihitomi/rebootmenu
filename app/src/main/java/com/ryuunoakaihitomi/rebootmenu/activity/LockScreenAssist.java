package com.ryuunoakaihitomi.rebootmenu.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ryuunoakaihitomi.rebootmenu.activity.base.MyActivity;
import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;

/**
 * 占用搜索助手的位置快捷锁屏
 * Created by ZQY on 2018/11/25.
 */

public class LockScreenAssist extends MyActivity {
    private static final String TAG = "LockScreenAssist";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Intent.ACTION_ASSIST.equals(getIntent().getAction())) {
            //避免故意抛出的异常
            Log.e(TAG, "onCreate: ", new IllegalAccessError("Activity for Intent.ACTION_ASSIST!"));
            return;
        }
        int flag = ShellUtils.isRoot() ? Shortcut.LOCKSCREEN : Shortcut.UR_LOCKSCREEN;
        startActivity(new Intent(Shortcut.action).putExtra(Shortcut.extraTag, flag));
        finish();
    }
}
