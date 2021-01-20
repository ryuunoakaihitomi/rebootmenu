package github.ryuunoakaihitomi.powerpanel.ui.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import github.ryuunoakaihitomi.powerpanel.desc.PowerExecution
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
            startActivityAndCollapse(PowerExecution.toPowerControlPanel(this))
        }
    }

    // 时刻保持“活跃状态”（对比突出图标）以防止误操作，但在锁屏时没有使用的理由
    override fun onStartListening() {
        qsTile?.run { if (!isLocked) updateState(Tile.STATE_ACTIVE) }
    }
}