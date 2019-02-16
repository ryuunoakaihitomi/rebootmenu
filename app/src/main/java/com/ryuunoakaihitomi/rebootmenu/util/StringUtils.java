package com.ryuunoakaihitomi.rebootmenu.util;

import android.os.Build;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import androidx.annotation.NonNull;

/**
 * 字符串工具
 * Created by ZQY on 2019/2/14.
 *
 * @author ZQY
 */

public class StringUtils {

    private StringUtils() {
    }

    /**
     * 可变长参数转字符串
     *
     * @param objects Arrays.toString
     * @return {@link String}
     */
    public static String varArgsToString(Object... objects) {
        return Arrays.toString(objects);
    }

    /**
     * 字符串数组转字符串
     *
     * @param in  数组
     * @param dot 分割符
     * @return 字符串
     */
    public static String strArr2String(@NonNull String[] in, @NonNull String dot) {
        //Java 8+ Throws:NullPointerException - If delimiter or elements is null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return String.join(dot, in);
        StringBuilder out = new StringBuilder();
        int i = 0;
        for (String string : in) {
            i++;
            out.append(string).append(i == in.length ? "" : dot);
        }
        return out.toString();
    }

    /**
     * 将异常的完整堆栈追踪信息保存到字符串中
     *
     * @param t {@link Throwable}
     * @return {@link String}
     */
    public static String printStackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw, true));
        return sw.getBuffer().toString();
    }
}
