package github.ryuunoakaihitomi.powerpanel.ui.tuner

import android.os.Bundle
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import com.github.kyuubiran.ezxhelper.utils.showToast
import github.ryuunoakaihitomi.powerpanel.databinding.ActivityTunerPlaceholderBinding
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
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
            setContentView(ActivityTunerPlaceholderBinding.inflate(layoutInflater).root)
            showToast("Xposed required")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exitProcess(0)  // 自杀让lsposed实现重读配置
    }
}