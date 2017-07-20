package com.ryuunoakaihitomi.rebootmenu;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class SystemPowerDialog extends AccessibilityService
{

	@Override
	public void onAccessibilityEvent(AccessibilityEvent p1)
	{}
	@Override
	public void onInterrupt()
	{}
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
	public boolean onUnbind(Intent intent)
	{
		unregisterReceiver(a);
		return super.onUnbind(intent);
	}
}

