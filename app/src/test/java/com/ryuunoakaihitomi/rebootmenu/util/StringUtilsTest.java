package com.ryuunoakaihitomi.rebootmenu.util;

import android.os.Build;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StringUtilsTest {

    @BeforeAll
    static void checkEnv() {
        try {
            int sdk = Build.VERSION.SDK_INT;
            assertEquals(0, sdk, "DO NOT RUN IT IN ANDROID!");
        } catch (Throwable ignored) {
        }
    }

    @Test
    void varArgsToString() {
        String s = StringUtils.varArgsToString("A", 2, true, 4.0f);
        assertEquals("[A, 2, true, 4.0]", s);
    }

    @Test
    @DisplayName("堆栈跟踪打印")
    void printStackTraceToString() {
        String s = StringUtils.printStackTraceToString(new NullPointerException());
        assertNotNull(s);
        System.out.println(s);
    }
}
