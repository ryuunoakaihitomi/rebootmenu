package com.ryuunoakaihitomi.rebootmenu;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.view.*;
import android.widget.*;
import android.app.AlertDialog.*;

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
	public static void helpDialog(Context activityThis, final AlertDialog.Builder returnTo)
	{
		AlertDialog.Builder h=LoadDialog(activityThis);
		h.setTitle("帮助");
		h.setMessage(HelpText.get() + "版本：" + getAppVersionName(activityThis) + "\n20161018");
		h.setOnCancelListener(new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface p1)
				{
					SameModule.alphaShow(returnTo.create(), 0.75f);
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
		alphaShow(h.create(),0.8f);
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
