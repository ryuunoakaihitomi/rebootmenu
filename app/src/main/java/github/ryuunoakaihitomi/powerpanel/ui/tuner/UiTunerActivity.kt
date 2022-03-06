package github.ryuunoakaihitomi.powerpanel.ui.tuner

import android.os.Bundle
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import kotlin.system.exitProcess

class UiTunerActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        @Keep
        private val hookedByXposed = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, UiTunerFragment())
            .commit()
        if (hookedByXposed) {
            Statistics.logXposedEnabled()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)  // 自杀让lsposed实现生效
    }
}