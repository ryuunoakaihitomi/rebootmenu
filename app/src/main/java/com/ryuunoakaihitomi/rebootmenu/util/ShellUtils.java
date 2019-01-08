package com.ryuunoakaihitomi.rebootmenu.util;

import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;

import androidx.annotation.NonNull;

/**
 * 本应用中关于shell操作的工具集合
 * Created by ZQY on 2018/2/10.
 *
 * @author ZQY
 */

public class ShellUtils {

    private static final String TAG = "ShellUtils";

    /**
     * 检查是否获得root权限
     *
     * @return 如已获得root权限，返回为真，反之为假
     */
    public static synchronized boolean isRoot() {
        new DebugLog(TAG, "isRoot", DebugLog.LogLevel.V);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (process == null)
            return false;
        try (DataOutputStream os = new DataOutputStream(process.getOutputStream())) {
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            return exitValue == 0;
        } catch (Exception e) {
            new DebugLog(e, "isRoot", true);
            return false;
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                process.destroyForcibly();
            else
                process.destroy();
        }
    }

    /**
     * 在root权限下执行命令
     *
     * @param command 一条所要执行的命令
     */
    public static boolean suCmdExec(String command) {
        boolean ret = false;
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream d = new DataOutputStream(p.getOutputStream());
            d.writeBytes(command + "\n");
            d.writeBytes("exit\n");
            d.flush();
            p.getErrorStream().close();
            ret = true;
        } catch (IOException ignored) {
        } finally {
            new DebugLog("suCmdExec: cmd:" + command + " ret:" + ret, DebugLog.LogLevel.I);
        }
        return ret;
    }

    /**
     * 普通权限shell
     *
     * @param command 一条所要执行的命令
     */
    public static void shCmdExec(String command) {
        new DebugLog(TAG, "shCmdExec: " + command, DebugLog.LogLevel.I);
        try {
            Runtime.getRuntime().exec(command).getErrorStream().close();
        } catch (IOException e) {
            new DebugLog(e, "shCmdExec", false);
        }
    }

    /**
     * 仅使用kill命令杀死指定包名的进程
     * 贡献者：酷安用户 @lyblyblyblin http://www.coolapk.com/u/517089 （已经在原来的代码基础上经过修改）
     * 注意：从Android Oreo开始ps只显示sh和ps信息，需要加-A。
     *
     * @param packageName 包名
     */
    public static void killShKillProcess(String packageName) {
        new DebugLog("killShKillProcess: " + packageName);
        String processId = "";
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(p.getOutputStream());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                dos.writeBytes("ps -A");
            else
                dos.writeBytes("ps");
            dos.writeBytes("\nexit\n");
            dos.flush();
            BufferedReader br = new BufferedReader(new InputStreamReader(p
                    .getInputStream()));
            String inline;
            while ((inline = br.readLine()) != null)
                if (inline.contains(packageName))
                    break;
            br.close();
            if (inline == null) return;
            StringTokenizer processInfoTokenizer = new StringTokenizer(inline);
            int count = 0;
            while (processInfoTokenizer.hasMoreTokens()) {
                count++;
                processId = processInfoTokenizer.nextToken();
                if (count == 2)
                    break;
            }
            //部分机型不支持su -c这样的写法
            suCmdExec("kill " + processId);
        } catch (IOException e) {
            new DebugLog(e, "killShKillProcess", false);
        }
    }

    /**
     * 用app_process执行带root权限的Java命令
     *
     * @param context context.getPackageResourcePath()所需
     * @param cls     class.getName()
     * @param args    传入参数
     */
    public static void runSuJavaWithAppProcess(Context context, Class cls, @NonNull String... args) {
        final String packageResourcePath = context.getPackageResourcePath();
        final String className = cls.getName();
        try {
            String argLine = "";
            for (String s : args)
                //noinspection StringConcatenationInLoop
                argLine += s + " ";
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream stream = new DataOutputStream(process.getOutputStream());
            stream.writeBytes("export CLASSPATH=" + packageResourcePath + '\n');
            stream.writeBytes("exec app_process /system/bin " + className + " " + argLine + '\n');
            stream.flush();
        } catch (IOException ignored) {
        } finally {
            new DebugLog("runSuJavaWithAppProcess: packageResourcePath:" + packageResourcePath +
                    " className:" + className + " args:" + Arrays.toString(args));
        }
    }
}
