package com.ryuunoakaihitomi.rebootmenu.util;

import android.util.Log;

/**
 * 日志输出调试工具
 * Created by ZQY on 2018/2/11.
 *
 * @author ZQY
 * @version 1.2
 * @see android.util.Log
 */

@SuppressWarnings("ConstantConditions")
public class DebugLog {

    //标签
    public static final String TAG = "rebootmenu";

    //总输出开关
    private final boolean isLog = true;

    /**
     * debug级日志输出
     *
     * @param msg 输出内容
     */
    public DebugLog(String msg) {
        if (isLog)
            Log.d(TAG, msg);
    }

    /**
     * 其他等级日志输出
     *
     * @param msg      输出内容
     * @param logLevel 日志等级
     */
    public DebugLog(String msg, LogLevel logLevel) {
        if (isLog)
            switch (logLevel) {
                case V:
                    Log.v(TAG, msg);
                    break;
                case D:
                    Log.d(TAG, msg);
                    break;
                case I:
                    Log.i(TAG, msg);
                    break;
                case W:
                    Log.w(TAG, msg);
                    break;
                case E:
                    Log.e(TAG, msg);
                    break;
                case WTF:
                    Log.wtf(TAG, msg);
                    break;
                default:
                    Log.w(TAG, "(level?)" + msg);
            }
    }

    //日志等级
    public enum LogLevel {
        V, D, I, W, E, WTF
    }
}
