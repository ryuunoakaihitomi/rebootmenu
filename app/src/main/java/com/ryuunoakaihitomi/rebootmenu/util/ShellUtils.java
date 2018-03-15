package com.ryuunoakaihitomi.rebootmenu.util;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 本应用中关于shell操作的工具集合
 * Created by ZQY on 2018/2/10.
 *
 * @author ZQY
 */

public class ShellUtils {

    /**
     * 检查是否获得root权限
     *
     * @return 如已获得root权限，返回为真，反之为假
     */
    public static synchronized boolean isRoot() {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                assert process != null;
                process.destroy();
            } catch (Exception ignored) {
                //不再打印堆栈以提高性能
            }
        }
    }

    /**
     * 在root权限下执行命令
     *
     * @param command 一条所要执行的命令
     */
    public static void suCmdExec(String command) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream d = new DataOutputStream(p.getOutputStream());
            d.writeBytes(command + "\n");
            d.writeBytes("exit\n");
            d.flush();
            p.getErrorStream().close();
        } catch (IOException ignored) {
        }
    }

    /**
     * 普通权限shell
     *
     * @param command 一条所要执行的命令
     */
    public static void shCmdExec(String command) {
        try {
            Runtime.getRuntime().exec(command).getErrorStream().close();
        } catch (IOException ignored) {
        }
    }
}