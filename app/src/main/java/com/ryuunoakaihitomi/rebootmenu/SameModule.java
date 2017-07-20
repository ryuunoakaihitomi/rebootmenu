package com.ryuunoakaihitomi.rebootmenu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.Window;
import android.view.WindowManager;

public class SameModule
{
	public static AlertDialog.Builder LoadDialog(Context activityThis)
	{
		if (ReadConfig.whiteTheme())
		{
			return new AlertDialog.Builder(activityThis, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		}
		else
		{
			return new AlertDialog.Builder(activityThis, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
		}
	}
	public static void helpDialog(final Context activityThis, final AlertDialog.Builder returnTo)
	{
		AlertDialog.Builder h=LoadDialog(activityThis);
		h.setTitle("帮助");
		h.setMessage(HelpText.get() + "版本：" + getAppVersionName(activityThis) + "\n20170720");
		h.setOnCancelListener(new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface p1)
				{
					SameModule.alphaShow(returnTo.create(), 0.75f);
				}
			});
		h.setNeutralButton("官方下载链接", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://coolapk.com/apk/com.ryuunoakaihitomi.rebootmenu"));
					activityThis.startActivity(i);
					System.exit(0);
				}
			});
		if (!ReadConfig.cancelable())
		{
			h.setPositiveButton("退出", new AlertDialog.OnClickListener(){

					@Override
					public void onClick(DialogInterface p1, int p2)
					{
						SameModule.alphaShow(returnTo.create(), 0.75f);
					}
				});
			h.setCancelable(false);
		}
		alphaShow(h.create(), 0.8f);
	}
	public static void alphaShow(AlertDialog w, Float f)
	{
		Window window = w.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = f;
		window.setAttributes(lp);
		w.show();
	}
	private static String getAppVersionName(Context context)
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
