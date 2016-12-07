package com.ryuunoakaihitomi.rebootmenu;

import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;

import java.lang.Process;

public class RootMode extends Activity 
{
	boolean state;
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		final  AlertDialog.Builder ab=SameModule.LoadDialog(this);
		ab.setTitle("高级电源菜单↓");
		final String[]rebootText={"重启", "svc power reboot","关机","svc power shutdown","Recovery恢复模式","svc power reboot recovery","Bootloader启动引导模式(Fastboot)","svc power reboot bootloader","热重启","setprop ctl.restart zygote","重启用户界面","busybox pkill com.android.systemui","安全模式","setprop persist.sys.safemode 1","锁屏","input keyevent 26"};
		final String[]rebootText2={"重启*", "reboot","关机*","reboot -p","Recovery恢复模式*","reboot recovery","Bootloader启动引导模式(Fastboot)*","reboot bootloader","热重启","setprop ctl.restart zygote","重启用户界面","busybox pkill com.android.systemui","安全模式","setprop persist.sys.safemode 1","锁屏","input keyevent 26"};
		final String[]s=new String[8];
		final String[]s2=new String[8];
		final DialogInterface.OnCancelListener e=new DialogInterface.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface p1)
			{
				Toast.makeText(getApplicationContext(), "点击了界面外或按下了返回键，程序退出", Toast.LENGTH_SHORT).show();
				finish();
			}
		};
		for (int i=0;i < 8;i++)
		{
			s[i] = rebootText[2 * i];
			s2[i] = rebootText2[2 * i];
		}
		final DialogInterface.OnClickListener l = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, final int which)
			{
				if (which != 7 & !ReadConfig.normalDo())
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
								exeKernel(rebootText, rebootText2, which);
							}
						}
					};
					String qr3[]={qr[1],qr[2]};
					ab.setItems(qr3, qr2);
					ab.setNeutralButton(null, null);
					ab.setNegativeButton(null, null);
					ab.setPositiveButton(null, null);
					SameModule.alphaShow(ab.create(), 0.9f);
				}
				else
				{
					exeKernel(rebootText, rebootText2, which);
				}
			}
		};
		ab.setItems(s, l);
		if (!ReadConfig.cancelable())
		{
			ab.setPositiveButton("退出", new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						finish();
					}
				});
		}
		ab.setNegativeButton("帮助", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					SameModule.helpDialog(RootMode.this, ab);
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
					SameModule.alphaShow(ab.create(), 0.75f);
				}
			});
		ab.setCancelable(ReadConfig.cancelable());
		ab.setOnCancelListener(e);
		SameModule.alphaShow(ab.create(), 0.75f);
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
	private void exeKernel(String[] rt, String[] rt2, Integer w)
	{
		String c;
		if (!state)
		{
			c = rt[w * 2 + 1];
		}
		else
		{
			c = rt2[w * 2 + 1];
		}
		cmdExec(c);
		if (c == rt[13])
		{
			c = rt[1];
			cmdExec(c);
		}
		Toast.makeText(getApplicationContext(), "命令已发送。。。如果这片烤面包片消失之后手机还是没有反应，应该是执行失败了。请到帮助那里查看解决方法", Toast.LENGTH_LONG).show();
		finish();
	}
}
