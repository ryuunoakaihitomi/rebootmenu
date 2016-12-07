package com.ryuunoakaihitomi.rebootmenu;

import android.app.*;
import android.app.admin.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.text.*;
import android.widget.*;

public class UnRootMode extends Activity 
{
	AlertDialog.Builder ab;
	DevicePolicyManager policyManager;  
	ComponentName componentName;  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (0 == requestCode)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				policyManager.lockNow();
				if (!ReadConfig.normalDo())
				{
					policyManager.removeActiveAdmin(componentName);
				}
				finish();
			}
			else
			{
				Toast.makeText(getApplicationContext(), "未开启设备管理器，锁屏失败！", Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		ab = SameModule.LoadDialog(this);
		ab.setTitle("高级电源菜单↓(免root模式)");
		final String[]rebootText={"锁屏","打开系统电源菜单"};
		DialogInterface.OnClickListener l=new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface p1, int which)
			{
				if (which == 0)
				{
					lockscreen();
				}
				else
				{
					accessbilityon();
				}
			}
		};
		ab.setItems(rebootText, l);
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
					SameModule.helpDialog(UnRootMode.this, ab);
				}
			});
		ab.setNeutralButton(" ", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface p1, int p2)
				{
					Toast.makeText(UnRootMode.this, "_(´Дˋ」∠)З|📱🔞(≧ω≦)☕。。(嫐)|8='''',D--\n彩蛋：我的个人主页", Toast.LENGTH_SHORT).show();
					Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.coolapk.com/u/532521"));
					startActivity(i);
					finish();
				}
			});
		ab.setCancelable(ReadConfig.cancelable());
		ab.setOnCancelListener(new DialogInterface.OnCancelListener(){

				@Override
				public void onCancel(DialogInterface p1)
				{
					Toast.makeText(getApplicationContext(), "点击了界面外或按下了返回键，程序退出", Toast.LENGTH_SHORT).show();
					finish();
				}
			});
		SameModule.alphaShow(ab.create(), 0.75f);

	}
	private void lockscreen()
	{
		policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);    
		componentName = new ComponentName(this, AdminReceiver.class);  
		boolean active = policyManager.isAdminActive(componentName);  
		if (!active)
		{
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);  
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);  
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "这里是高级电源菜单的锁屏确认选项(可以这么理解吧)，打开设备管理器锁定屏幕。");
			startActivityForResult(intent, 0);
		}  
		if (active)
		{  
			policyManager.lockNow();
			if (!ReadConfig.normalDo())
			{
				policyManager.removeActiveAdmin(componentName);
			}
			android.os.Process.killProcess(android.os.Process.myPid());   
		}  
	}
	private void accessbilityon()
	{
		if (!isAccessibilitySettingsOn(getApplicationContext()))
		{
			Toast.makeText(getApplicationContext(), "你未开启辅助服务...请开启辅助服务之后再打开菜单选择此选项", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
			startActivity(intent);
		}
		else
		{
			sendBroadcast(new Intent("com.ryuunoakaihitomi.rebootmenu.SPD_broadcast"));
		}
		finish();
	}

	private boolean isAccessibilitySettingsOn(Context mContext)
	{
		int accessibilityEnabled = 0;
		final String service = getPackageName() + "/" + SystemPowerDialog.class.getCanonicalName();
		try
		{
			accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
		}
		catch (Settings.SettingNotFoundException e)
		{
		}
		TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');
		if (accessibilityEnabled == 1)
		{
			String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if (settingValue != null)
			{
				mStringColonSplitter.setString(settingValue);
				while (mStringColonSplitter.hasNext())
				{
					String accessibilityService = mStringColonSplitter.next();
					if (accessibilityService.equalsIgnoreCase(service))
					{
						return true;
					}
				}
			}
		}
		else
		{}
		return false;
	}
}
