package github.ryuunoakaihitomi.powerpanel

import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService

/**
 * 这个打开音量面板不属于主要功能
 * 但是这个用习惯了有时会很顺手
 * 所以予以保留
 */
class VolumeControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSystemService<AudioManager>()?.adjustVolume(
            AudioManager.ADJUST_SAME,
            AudioManager.FLAG_SHOW_UI
        )
        finish()
    }
}