package com.ryuunoakaihitomi.rebootmenu;

import android.accessibilityservice.*;
import android.view.accessibility.*;
import android.widget.*;
import android.content.*;

public class SystemPowerDialog extends AccessibilityService
{
	private BroadcastReceiver a=new BroadcastReceiver(){
		@Override
		public void onReceive(Context p1, Intent p2)
		{
			performGlobalAction(AccessibilityService.GLOBAL_ACTION_POWER_DIALOG);
			Toast.makeText(getApplicationContext(), "已经调出系统电源菜单...", Toast.LENGTH_SHORT).show(); 
		}
	};
	@Override
	protected void onServiceConnected()
	{
		IntentFilter b=new IntentFilter();
		b.addAction("com.ryuunoakaihitomi.rebootmenu.SPD_broadcast");
		registerReceiver(a, b);
		super.onServiceConnected();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent p1)
	{
	}

	@Override
	public void onInterrupt()
	{
	}
	@Override
	public void onUnbind()
	{
		unregisterReceiver(a);
	}
}

