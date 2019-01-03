package com.ryuunoakaihitomi.rebootmenu.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import com.ryuunoakaihitomi.rebootmenu.util.DebugLog;
import com.ryuunoakaihitomi.rebootmenu.util.ui.TextToast;

/**
 * To set the volume.(Hidden feature)
 * Created by ZQY on 2018/3/20.
 */

public class TileLongPressEntry extends Activity {

    @SuppressWarnings("ConstantConditions")
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new DebugLog("TileLongPressEntry.onCreate", DebugLog.LogLevel.V);
        //Emoji:Volume Control
        new TextToast(this, true, "\uD83C\uDF9A");
        getSystemService(AudioManager.class)
                .adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        finish();
    }
}
