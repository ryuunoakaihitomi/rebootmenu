package github.ryuunoakaihitomi.powerpanel.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import github.ryuunoakaihitomi.powerpanel.ui.tile.CmCustomTileService

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // 为了使其接近后来在Nougat上的图块的体验。在设备重启之后，自启动并再发布一次。
            CmCustomTileService.start()
        }
    }
}