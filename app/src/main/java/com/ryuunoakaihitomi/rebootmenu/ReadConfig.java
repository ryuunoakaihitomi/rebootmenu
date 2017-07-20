package com.ryuunoakaihitomi.rebootmenu;

import java.io.File;

public class ReadConfig
{
	public static boolean whiteTheme()
	{
		return fileIsExists("/sdcard/dreapm/wt");
	}
	public static boolean normalDo()
	{
		return fileIsExists("/sdcard/dreapm/nd");
	}
	public static boolean cancelable()
	{
		return fileIsExists("/sdcard/dreapm/c");
	}
	public static boolean noRootCheck()
	{
		return fileIsExists("/sdcard/dreapm/nrc");
	}
	public static boolean unRootMode()
	{
		return fileIsExists("/sdcard/dreapm/urm");
	}
	private static boolean fileIsExists(String strFile)
    {
        try
        {
            File f=new File(strFile);
            if (!f.exists())
            {
				return false;
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
}
