package github.ryuunoakaihitomi.powerpanel.ui.tile

import android.app.PendingIntent
import android.content.Intent
import cyanogenmod.app.CMStatusBarManager
import cyanogenmod.app.CustomTile
import cyanogenmod.os.Build
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.desc.PowerExecution
import github.ryuunoakaihitomi.powerpanel.ui.VolumeControlActivity
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import timber.log.Timber

/**
 * 用CyanogenMod平台在Android N之前添加图块
 *
 * @see <a href="https://cyanogenmod.github.io/cm_platform_sdk/reference/packages.html">CM Platform SDK Reference</a>
 */
class CmCustomTile {

    companion object {

        private val publish by lazy {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N &&
                Build.CM_VERSION.SDK_INT >= Build.CM_VERSION_CODES.APRICOT
            ) {
                Timber.i("Starting to publish tile...")
                launch()
            }
            true
        }

        private fun launch() {
            CMStatusBarManager.getInstance(app).publishTile(0,
                /* 目前也就加一个“电源面板”的瓷块，锁屏的话双击状态栏比瓷块更方便 */
                CustomTile.Builder(app).run {
                    setLabel(R.string.tile_pwr_menu)
                    // 由于长按后才显示，和setOnLongClickIntent()冲突
                    // 保留以兼容更早期不支持OnLongClickIntent的CM（能在CM12.1显示）
                    setContentDescription(R.string.app_name)
                    // CM有自己的瓷块“音量面板”，但是点击不收起快捷设置面板，长按弹出的是占满屏幕的声音设置
                    // 这套SDK似乎做了适配，提供类似于appcompat的兼容性
                    // CM12.1似乎没有调整Tile的设置（自定义Tile可以移除）
                    val volume = Intent(app, VolumeControlActivity::class.java)
                    setOnLongClickIntent(volume.pend())
                    setOnSettingsClickIntent(volume)    // 以同样的原因和setOnLongClickIntent()冲突，从CM13
                    setOnClickIntent(PowerExecution.toPowerControlPanel(app).pend())
                    setIcon(android.R.drawable.ic_lock_power_off)
                    build()
                })
            Timber.d("tile published")
        }

        /**
         * 就像Notification一样，CM的自定义瓷块（那时的中文翻译是这么叫）必须要APP活着才能显示
         * 如果应用进程不在，那自定义瓷块也将消失
         * 实测静态CustomTileListenerService似乎无效
         */
        fun start() {
            if (publish) Timber.v("start() invoked")
        }
    }
}

private fun Intent.pend() = run {
    var piFlags = PendingIntent.FLAG_UPDATE_CURRENT
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        piFlags = piFlags or PendingIntent.FLAG_IMMUTABLE   // UnspecifiedImmutableFlag
    }
    PendingIntent.getActivity(app, 0, this, piFlags)
}

private val app = BlackMagic.getGlobalApp()