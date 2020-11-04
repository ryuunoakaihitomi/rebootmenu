package github.ryuunoakaihitomi.powerpanel.ui.tile

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.ui.ShortcutActivity
import github.ryuunoakaihitomi.powerpanel.ui.main.MainActivity
import github.ryuunoakaihitomi.powerpanel.util.updateState
import timber.log.Timber

@RequiresApi(Build.VERSION_CODES.N)
class PowerDialogTileService : TileService() {

    override fun onClick() {
        // 在安全锁屏时给用户“无效”的反馈，并请求用户解锁
        if (isLocked && isSecure) {
            Timber.w("locked!")
            qsTile?.run {
                updateState(Tile.STATE_UNAVAILABLE)
                unlockAndRun { updateState(Tile.STATE_ACTIVE) }
            }
        } else {
            /* 打开电源菜单 */
            val intent = if (ExternalUtils.isExposedComponentAvailable(this)) {
                ShortcutActivity.getActionIntent(R.string.func_sys_pwr_menu)
            } else {
                Intent(this, MainActivity::class.java)
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
        }
    }

    // 这是一个随时可用的磁贴，因此应该在可见时时刻保持“活跃状态”（对比突出图标），但在锁屏时没有使用的理由
    override fun onStartListening() {
        qsTile?.run { if (!isLocked) updateState(Tile.STATE_ACTIVE) }
    }
}