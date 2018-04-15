package com.ryuunoakaihitomi.rebootmenu.util;

import android.content.Context;

import java.io.File;

/**
 * 配置管理器
 * Created by ZQY on 2018/2/8.
 */

public class ConfigManager {

    //设置选项列表
    public static final String WHITE_THEME = "wt";
    public static final String NO_NEED_TO_COMFIRM = "nntc";
    public static final String CANCELABLE = "c";
    public static final String DO_NOT_CHECK_ROOT = "dncr";
    public static final String UNROOT_MODE = "urm";
    static final String DEBUG_LOG = "dl";

    //父目录
    private static String path;

    //初始化外部files目录
    @SuppressWarnings({"ConstantConditions", "ResultOfMethodCallIgnored"})
    public static void initDir(Context context) {
        path = context.getExternalFilesDir(null).getPath();
        //除了内置存储外，总是试图往外置存储创建目录，但貌似并无权限，因此总返回假
        context.getExternalFilesDir(null).mkdirs();
    }

    public static boolean get(String key) {
        return new File(path + "/" + key).exists();
    }
}
