package com.ryuunoakaihitomi.rebootmenu;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import com.ryuunoakaihitomi.rebootmenu.util.TextToast;

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
        new TextToast(this, true, "音量調節");
        getSystemService(AudioManager.class)
                .adjustVolume(AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
        finish();
    }
}
