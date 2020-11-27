package github.ryuunoakaihitomi.powerpanel.ui.tile

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.ui.ShortcutActivity
import github.ryuunoakaihitomi.powerpanel.util.updateState

/**
 * 目前唯一感觉不顺畅的地方就是如果未授权安全锁屏时要解锁才能操作，但是Activity已经在后台打开。
 * 通过这个行为即使在安全锁屏状态时操作也得以执行并被记录，但是未授权时Activity会滞留在后台。
 * 由于不确定这是不是一个bug，所以不予理会。
 */
@RequiresApi(Build.VERSION_CODES.N)
class LockScreenTileService : TileService() {

    override fun onClick() {
        val intent = ShortcutActivity.getActionIntent(
            if (ExternalUtils.isExposedComponentAvailable(this)) R.string.func_lock_screen else R.string.func_lock_screen_privileged
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(intent)
    }

    override fun onStartListening() {
        qsTile?.updateState(Tile.STATE_INACTIVE)
    }
}