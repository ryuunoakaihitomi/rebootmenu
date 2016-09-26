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
public class MainActivity extends Activity 
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		final AlertDialog a ;
		final AlertDialog.Builder ab = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		ab.setTitle("高级电源菜单↓");
		final String[]rebootText={"快速重启","reboot", "调用系统重启", "svc power reboot","快速关机","reboot -p","调用系统关机","svc power shutdown","Recovery恢复模式","reboot recovery","Bootloader启动引导模式(Fastboot)","reboot bootloader","热重启","setprop ctl.restart zygote","重启用户界面","busybox pkill com.android.systemui","安全模式","setprop persist.sys.safemode 1","锁屏","input keyevent 26"};
		final String[]s=new String[10];
		for (int i=0;i < 10;i++)
		{
			s[i] = rebootText[2 * i];
		}
		final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, final int which)
			{
				String qr[]={"确认操作？","是，吾已决意","否，我按错了"};
				ab.setTitle(qr[0] + " " + s[which]);
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
							String c=rebootText[which * 2 + 1];
							cmdExec(c);
							if (c == rebootText[17])
							{
								cmdExec(rebootText[3]);
							}
							Toast.makeText(getApplicationContext(), "命令已发送。。。如果这片烤面包片消失之后手机还是没有反应，应该是执行失败了。请到帮助那里查看解决方法", Toast.LENGTH_LONG).show();
							finish();
						}
					}
				};
				String qr3[]={qr[1],qr[2]};
				ab.setItems(qr3, qr2);
				ab.setCancelable(false);
				AlertDialog ax = ab.create();
				Window window = ax.getWindow();
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.alpha = 0.9f;
				window.setAttributes(lp);
				ax.show();
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
					Toast.makeText(getApplicationContext(), "关于：\n一个重启菜单应用.包括重启和关机(快速和调用系统模式),重启至recovery和bootloader,热重启,重启用户界面,安全模式和锁屏.\nby酷安基佬　@龙红瞳\n", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), " 帮助：\n如果执行失败,手机没有任何反应,请检查本应用是否得到root权限或安装busybox补足Linux二进制试试.\n功能解释：1.快速重启&关机：强制切断设备电源,执行速度快.\n", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "2.调用系统重启&关机：触发系统重启关机事件.速度慢但比较稳妥安全.\n3.recovery恢复模式.bootloader,亦称fastboot模式,线刷模式.\n4.热重启,在保持设备开机的情况下,关闭并重新加载系统.\n", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), " 5.重启用户界面,重启com.android.systemui,用于刷新桌面UI,重新加载桌面小部件或刷新图标显示.\n6.安全模式：重启系统停用所有第三方应用,用于排查有问题的应用并恢复系统.\n7.锁屏：熄灭屏幕\n", Toast.LENGTH_LONG).show();
					Toast.makeText(getApplicationContext(), "ギークにとって、さっきの話全て無駄話だ。\n\n版本" + getAppVersionName(getApplicationContext()) + " 20160925", Toast.LENGTH_SHORT).show();
					AlertDialog a2 = ab.create();
					Window window = a2.getWindow();
					WindowManager.LayoutParams lp = window.getAttributes();
					lp.alpha = 0.75f;
					window.setAttributes(lp);
					a2.show();
				}
			});
		ab.setCancelable(false);
		a = ab.create();
		Window window = a.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.75f;
		window.setAttributes(lp);
		a.show();
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
