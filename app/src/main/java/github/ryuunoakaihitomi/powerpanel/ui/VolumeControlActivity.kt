package github.ryuunoakaihitomi.powerpanel.ui

import android.content.Intent
import android.media.AudioManager
import android.media.AudioManager.ADJUST_SAME
import android.media.AudioManager.FLAG_SHOW_UI
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import timber.log.Timber

/**
 * 这个打开音量面板不属于主要功能
 * 但是这个用习惯了比较顺手
 * 所以予以保留
 */
class VolumeControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("active")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startActivity(Intent(Settings.Panel.ACTION_VOLUME))
        } else {
            getSystemService<AudioManager>()?.adjustVolume(ADJUST_SAME, FLAG_SHOW_UI)
        }
        finish()
    }
}