package com.ryuunoakaihitomi.rebootmenu;

import android.os.Environment;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ShellUtils;

import java.io.File;

/**
 * 配置管理器
 * Created by ZQY on 2018/2/8.
 */

class ConfigManager {

    //设置选项列表
    static final String WHITE_THEME = "wt";
    static final String NO_NEED_TO_COMFIRM = "nntc";
    static final String CANCELABLE = "c";
    static final String DO_NOT_CHECK_ROOT = "dncr";
    static final String UNROOT_MODE = "urm";

    //父目录
    private static String path = Environment.getExternalStorageDirectory().getPath() + "/Android/data/com.ryuunoakaihitomi.rebootmenu/files/";

    //如果files目录不存在
    static {
        ShellUtils.suCmdExec("mkdir -p " + path);
    }

    static boolean get(String key) {
        boolean isExist = new File(path + key).exists();
        new DebugLog("配置管理:" + key + " " + isExist);
        return isExist;
    }
}
