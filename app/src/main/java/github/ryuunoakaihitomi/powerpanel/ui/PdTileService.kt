package github.ryuunoakaihitomi.powerpanel.ui

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import github.ryuunoakaihitomi.poweract.internal.pa.PaService
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.ui.main.MainActivity
import github.ryuunoakaihitomi.powerpanel.util.Log

/**
 * Power Dialog Tile Service
 */
@RequiresApi(Build.VERSION_CODES.N)
class PdTileService : TileService() {

    companion object {
        private const val TAG = "PdTileService"
    }

    override fun onClick() {
        // 在锁屏时给用户“无效”的反馈
        if (isLocked) {
            Log.w(TAG, "onClick: locked!")
            qsTile?.run {
                state = Tile.STATE_UNAVAILABLE
                updateTile()
            }
        } else {
            /* 打开电源菜单 */
            val isAccessibilityServiceDisable = packageManager.getComponentEnabledSetting(
                ComponentName(this, PaService::class.java)
            ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            val intent = if (isAccessibilityServiceDisable) {
                Intent(this, MainActivity::class.java)
            } else {
                ShortcutActivity.getActionIntent(R.string.func_sys_pwr_menu)
            }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
        }
    }

    // 这是一个随时可用的磁贴，因此应该在可见时时刻保持“活跃状态”（对比突出图标），但在锁屏时没有使用的理由
    override fun onStartListening() {
        qsTile?.run {
            if (!isLocked && state != Tile.STATE_ACTIVE) {
                state = Tile.STATE_ACTIVE
                updateTile()
            }
        }
    }
}