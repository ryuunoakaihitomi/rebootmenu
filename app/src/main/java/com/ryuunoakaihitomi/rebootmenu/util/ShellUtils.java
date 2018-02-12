package com.ryuunoakaihitomi.rebootmenu.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * svc兼容性检查
     * <p>
     * 检查当前Android环境中svc命令是否支持重启关机电源操作
     * 原理：目前尚未知晓svc是从Android的哪一个版本支持此功能，所以在非root模式下尝试执行svc power取用法列表。当返回值包括reboot时判断支持。
     *
     * @return 真：支持
     */
    public static boolean svcCompatibilityCheck() {
        StringBuilder sb = new StringBuilder();
        try {
            Process p = Runtime.getRuntime().exec("sh");
            DataOutputStream d = new DataOutputStream(p.getOutputStream());
            d.writeBytes("svc power\n");
            d.writeBytes("exit\n");
            d.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String l = "";
            while ((l = br.readLine()) != null) {
                sb.append(l);
            }
            p.getErrorStream().close();
        } catch (IOException s) {
            s.printStackTrace();
            return false;
        }
        return sb.toString().contains("reboot");
    }

    /**
     * 在root权限下执行命令
     *
     * @param command 一条所要执行的命令
     */
    public static void suCmdExec(String command) {
        new DebugLog("执行的命令为：" + command, DebugLog.I);
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
}
