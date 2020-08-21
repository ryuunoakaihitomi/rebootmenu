package github.ryuunoakaihitomi.powerpanel.ui

import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import github.ryuunoakaihitomi.powerpanel.util.Log

/**
 * 这个打开音量面板不属于主要功能
 * 但是这个用习惯了比较顺手
 * 所以予以保留
 */
class VolumeControlActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "VolumeControlActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
        getSystemService<AudioManager>()?.adjustVolume(
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI
        )
        finish()
    }
}