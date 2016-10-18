package com.ryuunoakaihitomi.rebootmenu;
import android.app.*;
import android.content.*;
import android.os.*;
import android.widget.*;
import java.io.*;

import java.lang.Process;
public class MainActivity extends Activity 
{
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		String s;
		s = "载入中...";
		if (ReadConfig.whiteTheme())
		{
			s = s + "\n√ 白色主题";
		}
		if (ReadConfig.cancelable())
		{
			s = s + "\n√ 返回键退出";
		}
		if (ReadConfig.normalDo())
		{
			s = s + "\n√ 无需二次确认";
		}
		Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
		if (isRoot())
		{
			startActivity(new Intent(this, RootMode.class));
			finish();
		}
		else
		{
			startActivity(new Intent(this, UnRootMode.class));
			finish();
		}
	}
	public synchronized boolean isRoot()  
	{  
		Process process = null;  
		DataOutputStream os = null;  
		try  
		{  
			process = Runtime.getRuntime().exec("su");  
			os = new DataOutputStream(process.getOutputStream());  
			os.writeBytes("exit\n");  
			os.flush();  
			int exitValue = process.waitFor();  
			if (exitValue == 0)  
			{  
				return true;  
			}
			else  
			{  
				return false;  
			}  
		}
		catch (Exception e)  
		{  

			return false;  
		}
		finally  
		{  
			try  
			{  
				if (os != null)  
				{  
					os.close();  
				}  
				process.destroy();  
			}
			catch (Exception e)  
			{  
				e.printStackTrace();  
			}  
		}  
	}  

}
