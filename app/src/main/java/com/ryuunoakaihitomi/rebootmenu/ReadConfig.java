package com.ryuunoakaihitomi.rebootmenu;

import java.io.*;

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
