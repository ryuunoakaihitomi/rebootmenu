package github.ryuunoakaihitomi.powerpanel.ui.tuner

import android.os.Bundle
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.util.uiLog
import kotlin.system.exitProcess

class UiTunerActivity : AppCompatActivity() {

    companion object {
        @Keep
        private val hookedByXposed = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hookedByXposed) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, UiTunerFragment())
                .commit()
            Statistics.logXposedEnabled()
        } else {
            uiLog("Xposed required")
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (hookedByXposed) exitProcess(0)  // 自杀让lsposed实现生效
    }
}