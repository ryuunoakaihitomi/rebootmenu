package com.ryuunoakaihitomi.rebootmenu.util;

import android.content.Context;
import android.widget.Toast;

/**
 * 快速生成默认文本烤面包片的工具类
 * Created by ZQY on 2018/2/8.
 *
 * @author ZQY
 * @version 1.0
 * @see android.widget.Toast
 */

public class TextToast {

    /**
     * 文本toast生成
     *
     * @param context 上下文
     * @param isLong  是否是持续时间较长的toast
     * @param message 显示的文本内容
     */
    public TextToast(Context context, boolean isLong, String message) {
        Toast.makeText(context, message, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    /**
     * 短暂文本toast生成
     *
     * @param context 上下文
     * @param message 文本内容
     */
    public TextToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
