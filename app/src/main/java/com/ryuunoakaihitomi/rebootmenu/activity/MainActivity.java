package com.ryuunoakaihitomi.rebootmenu.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.ryuunoakaihitomi.rebootmenu.R;
import com.ryuunoakaihitomi.rebootmenu.util.ConfigManager;
import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

/**
 * 主活动，(免)Root模式的加载
 * Created by ZQY on 2018/2/8.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DebugLog("MainActivity.onCreate", DebugLog.LogLevel.V);
        //对于本应用的性质而言，Monkey Test没有必要，而且也容易造成测试机器电源状态改变
        if (ActivityManager.isUserAMonkey()) {
            new TextToast(this, true, getString(R.string.monkey_test_notice));
            ShellUtils.killShKillProcess("monkey");
            finish();
            return;
        }
        //配置选项
        String configView = getString(R.string.loading);
        final String PREFIX = "\n√ ";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
            configView += getString(R.string.kitkat_compat);
        if (ConfigManager.get(ConfigManager.WHITE_THEME))
            configView += PREFIX + getString(R.string.r_whitetheme);
        if (ConfigManager.get(ConfigManager.CANCELABLE))
            configView += PREFIX + getString(R.string.r_cancelable);
        if (ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM))
            configView += PREFIX + getString(R.string.r_normal_do);
        if (ConfigManager.get(ConfigManager.DO_NOT_CHECK_ROOT)) {
            configView += PREFIX + getString(R.string.r_no_root_check);
            if (ConfigManager.get(ConfigManager.UNROOT_MODE))
                configView += PREFIX + getString(R.string.r_unroot_mode);
        }
        new TextToast(this, configView);
        if (!ConfigManager.get(ConfigManager.DO_NOT_CHECK_ROOT))
            activitySwitch(ShellUtils.isRoot());
        else    //如果不检查root权限，则检查“手动免root模式”配置
            activitySwitch(!ConfigManager.get(ConfigManager.UNROOT_MODE));
    }

    private void activitySwitch(boolean isRootMode) {
        new DebugLog("activitySwitch: " + isRootMode, DebugLog.LogLevel.D);
        //在加载相应模式的窗口时加载对应的shortcut
        if (isRootMode) {
            startActivity(new Intent(this, RootMode.class));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                startActivity(new Intent(Shortcut.action).putExtra(Shortcut.extraTag, Shortcut.ROOT));
        } else {
            startActivity(new Intent(this, UnRootMode.class));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1)
                startActivity(new Intent(Shortcut.action).putExtra(Shortcut.extraTag, Shortcut.UNROOT));
        }
        finish();
    }
}
