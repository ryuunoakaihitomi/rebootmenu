package github.ryuunoakaihitomi.powerpanel.ui.tile

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.JobIntentService
import cyanogenmod.app.CMStatusBarManager
import cyanogenmod.app.CustomTile
import cyanogenmod.os.Build
import github.ryuunoakaihitomi.powerpanel.MyApplication
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.ui.ShortcutActivity
import github.ryuunoakaihitomi.powerpanel.ui.VolumeControlActivity
import timber.log.Timber

/**
 * 用CyanogenMod平台在Android N之前添加图块
 *
 * 只在LineageOS 13.0上测试过，那时应该还只是改了下UI中的名字
 *
 * @see <a href="https://cyanogenmod.github.io/cm_platform_sdk/reference/packages.html">CM Platform SDK Reference</a>
 */
class CmCustomTileService : JobIntentService() {

    companion object {

        private val publish by lazy {
            Timber.i("Starting to publish tile...")
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N &&
                Build.CM_VERSION.SDK_INT >= Build.CM_VERSION_CODES.APRICOT
            ) {
                val context = MyApplication.getInstance()
                context.startService(Intent(context, CmCustomTileService::class.java))
            }
            true
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

    private fun Intent.pend() = PendingIntent.getActivity(
        application, 0, this, PendingIntent.FLAG_UPDATE_CURRENT
    )

    override fun onHandleWork(intent: Intent) {
        CMStatusBarManager.getInstance(this).publishTile(0,
            /* 目前也就加一个“电源面板”的瓷块，锁屏的话双击状态栏比瓷块更方便 */
            CustomTile.Builder(this).run {
                setLabel(R.string.tile_pwr_menu)
                // 由于长按后才显示，和setOnLongClickIntent()冲突。保留以兼容更早期不支持OnLongClickIntent的CM
                setContentDescription(R.string.app_name)
                // CM有自己瓷块“音量面板”，但是点击不收起快捷设置面板，长按弹出的是占满屏幕的声音设置
                // 这套SDK似乎做了适配，提供类似于appcompat的兼容性
                setOnLongClickIntent(
                    Intent(application, VolumeControlActivity::class.java).pend()
                )
                setOnClickIntent(
                    ShortcutActivity.getActionIntent(R.string.func_sys_pwr_menu).pend()
                )
                setIcon(android.R.drawable.ic_lock_power_off)
                build()
            })
        Timber.d("tile published")
    }
}