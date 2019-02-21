package com.ryuunoakaihitomi.rebootmenu.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

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
