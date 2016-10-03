package com.ryuunoakaihitomi.rebootmenu;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;
import java.lang.Process;
import android.content.pm.PackageManager.*;
public class MainActivity extends Activity 
{
	boolean state;
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		final AlertDialog.Builder ab=new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);	
		ab.setTitle("高级电源菜单↓");
		final String[]rebootText={"重启", "svc power reboot","关机","svc power shutdown","Recovery恢复模式","svc power reboot recovery","Bootloader启动引导模式(Fastboot)","svc power reboot bootloader","热重启","setprop ctl.restart zygote","重启用户界面","busybox pkill com.android.systemui","安全模式","setprop persist.sys.safemode 1","锁屏","input keyevent 26"};
		final String[]rebootText2={"重启*", "reboot","关机*","reboot -p","Recovery恢复模式*","reboot recovery","Bootloader启动引导模式(Fastboot)*","reboot bootloader","热重启","setprop ctl.restart zygote","重启用户界面","busybox pkill com.android.systemui","安全模式","setprop persist.sys.safemode 1","锁屏","input keyevent 26"};
		final String[]s=new String[8];
		final String[]s2=new String[8];
		for (int i=0;i < 8;i++)
		{
			s[i] = rebootText[2 * i];
			s2[i] = rebootText2[2 * i];
		}
		final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, final int which)
			{
				String qr[]={"确认操作？","是，吾已决意","否，手动滑机"};
				if (!state)
				{
					ab.setTitle(qr[0] + " " + s[which]);
				}
				else
				{
					ab.setTitle(qr[0] + " " + s2[which]);
				}
				DialogInterface.OnClickListener qr2=new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						if (p2 == 1)
						{
							Toast.makeText(getApplicationContext(), "请重新打开此应用进行选择", Toast.LENGTH_SHORT).show();
							finish();
						}
						else
						{
							String c;
							if (!state)
							{
								c = rebootText[which * 2 + 1];
							}
							else
							{
								c = rebootText2[which * 2 + 1];
							}
							cmdExec(c);
							if (c == rebootText[13])
							{
								c = rebootText[1];
								cmdExec(c);
							}
							Toast.makeText(getApplicationContext(), "命令已发送。。。如果这片烤面包片消失之后手机还是没有反应，应该是执行失败了。请到帮助那里查看解决方法", Toast.LENGTH_LONG).show();
							finish();
						}
					}
				};
				String qr3[]={qr[1],qr[2]};
				ab.setItems(qr3, qr2);
				ab.setCancelable(false);
				ab.setNeutralButton("", null);
				ab.setNegativeButton("", null);
				ab.setPositiveButton("", null);
				alphaShow(ab.create(), 0.9f);
			}
		};
		ab.setItems(s, l);
		ab.setPositiveButton("退出", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					finish();
				}
			});
		ab.setNegativeButton("帮助", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					Toast.makeText(getApplicationContext(), "关于：\n一个重启菜单应用.包括重启和关机(默认和强制模式),重启至recovery和bootloader,热重启,重启用户界面,安全模式和锁屏.\nby酷安基佬　@龙红瞳\n", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "帮助：\n如果执行失败,手机没有任何反应,请检查本应用是否得到root权限或安装busybox补足Linux二进制试试,还可以切换到强制模式再操作.\n功能解释：0.切换模式:切换至强制模式或默认模式,使用强制模式的功能加上*标记.", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "1.强制模式中的重启&关机：调用linux中的命令.强制切断设备电源,执行速度快,成功率高,但是有极小的可能性会导致文件损坏甚至系统崩溃.", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "2.默认模式中的重启&关机：调用Android系统电源管理模块.速度慢,有一点执行失败的可能性(一直显示正在xx的对话),但是更加安全.", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "3.recovery:恢复模式.默认模式中会先调用默认重启.\n4：bootloader,亦称fastboot模式,线刷模式.默认模式中会先调用默认重启.\n5.热重启,在保持设备开机的情况下,关闭并重新加载系统.", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "6.重启用户界面,重启com.android.systemui,用于刷新桌面UI,重新加载桌面小部件或刷新图标显示.\n7.安全模式：重启系统停用所有第三方应用,用于排查有问题的应用并恢复系统.\n8.锁屏：熄灭屏幕", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "(≧∇≦)/\n\n版本" + getAppVersionName(getApplicationContext()) + " 20161003", Toast.LENGTH_SHORT).show();
					alphaShow(ab.create(), 0.75f);
				}
			});
		ab.setNeutralButton("切换模式", new AlertDialog.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					if (!state)
					{
						ab.setItems(s2, l);
						state = true;
						Toast.makeText(getApplicationContext(), "转换成强制模式", Toast.LENGTH_SHORT).show();
					}
					else
					{
						ab.setItems(s, l);
						state = false;
						Toast.makeText(getApplicationContext(), "转换成默认模式", Toast.LENGTH_SHORT).show();
					}
					ab.setCancelable(false);
					alphaShow(ab.create(), 0.75f);
				}
			});
		ab.setCancelable(false);
		alphaShow(ab.create(), 0.75f);
    }
	private static void cmdExec(String command)
	{
		try
		{
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream d = new DataOutputStream(p.getOutputStream());
			d.writeBytes(command + "\n");
			d.writeBytes("exit\n");
			d.flush();
			p.getErrorStream().close();
		}
		catch (IOException s)
		{
		}
	}

	public void alphaShow(AlertDialog w, Float f)
	{
		Window window = w.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = f;
		window.setAttributes(lp);
		w.show();
	}
	public static String getAppVersionName(Context context)
	{
        String versionName = "";
        try
		{
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0)
			{
                return "";
            }
        }
		catch (Exception e)
		{
        }
        return versionName;
    }
}
