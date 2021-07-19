package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.StrictMode
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.receiver.ShutdownReceiver
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.util.MyLogTree
import org.lsposed.hiddenapibypass.HiddenApiBypass
import timber.log.Timber

class MyApplication : Application() {

    /**
     * @see [github.ryuunoakaihitomi.powerpanel.ui.main.MainActivity.compatibilityCheck]
     */
    var hasShownCompatibilityWarning = false

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Timber.plant(MyLogTree())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            println("Happy hacking!")   // println不会触发Timber的lint
            val result = HiddenApiBypass.addHiddenApiExemptions("L")
            println("HiddenApiBypass success: $result")

            ShutdownReceiver.register(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Statistics.recordEnvInfo()
        if (BuildConfig.DEBUG) StrictMode.enableDefaults()
        // 留下PowerAct核心日志用以发布后的调试
        ExternalUtils.enableLog(true)
    }
}