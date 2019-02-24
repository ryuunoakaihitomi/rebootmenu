package com.android.internal.os;

/**
 * Android 隐藏私有API 存根
 * <p>
 * -> 28
 */

@SuppressWarnings("unused")
public final class Zygote {

    /**
     * Executes "/system/bin/sh -c &lt;command&gt;" using the exec() system call.
     * This method throws a runtime exception if exec() failed, otherwise, this
     * method never returns.
     *
     * @param command The shell command to execute.
     */
    public static void execShell(String command) {
    }
}