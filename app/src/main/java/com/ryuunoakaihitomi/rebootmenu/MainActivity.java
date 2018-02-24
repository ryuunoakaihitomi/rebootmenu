package com.ryuunoakaihitomi.rebootmenu;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;
import com.ryuunoakaihitomi.rebootmenu.util.TextToast;

/**
 * 主活动，(免)Root模式的加载
 * Created by ZQY on 2018/2/8.
 */

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigManager.initDir(this);
        //配置选项
        String configView = getString(R.string.loading);
        if (ConfigManager.get(ConfigManager.WHITE_THEME))
            configView += getString(R.string.r_whitetheme);
        if (ConfigManager.get(ConfigManager.CANCELABLE))
            configView += getString(R.string.r_cancelable);
        if (ConfigManager.get(ConfigManager.NO_NEED_TO_COMFIRM))
            configView += getString(R.string.r_normal_do);
        if (ConfigManager.get(ConfigManager.DO_NOT_CHECK_ROOT)) {
            configView += getString(R.string.r_no_root_check);
            if (ConfigManager.get(ConfigManager.UNROOT_MODE))
                configView += getString(R.string.r_unroot_mode);
        }
        new TextToast(this, configView);
        if (!ConfigManager.get(ConfigManager.DO_NOT_CHECK_ROOT))
            activitySwitch(ShellUtils.isRoot());
        else    //如果不检查root权限，则检查“手动免root模式”配置
            activitySwitch(!ConfigManager.get(ConfigManager.UNROOT_MODE));
    }

    void activitySwitch(boolean isRootMode) {
        //在加载相应模式的窗口时加载对应的shortcut
        if (isRootMode) {
            startActivity(new Intent(this, RootMode.class));
            startActivity(new Intent(Shortcut.action).putExtra(Shortcut.extraTag, Shortcut.ROOT));
        } else {
            startActivity(new Intent(this, UnRootMode.class));
            startActivity(new Intent(Shortcut.action).putExtra(Shortcut.extraTag, Shortcut.UNROOT));
        }
        finish();
    }
}
