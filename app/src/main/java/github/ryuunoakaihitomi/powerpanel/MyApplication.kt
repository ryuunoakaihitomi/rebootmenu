package github.ryuunoakaihitomi.powerpanel

import android.content.Context
import android.os.Build
import android.os.StrictMode
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.receiver.ShutdownReceiver
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.util.MyLogTree
import org.lsposed.hiddenapibypass.HiddenApiBypass
import timber.log.Timber

class MyApplication : MultiDexApplication() {

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

        /* KitKat系统主题为黑色，因此使用夜间模式作为搭配 */
        /**
         * WONT FIX 无法使用values-night资源
         *
         * Note: On API 22 and below,
         * changes to the night mode are only effective when the car or desk mode is enabled on a device.
         *
         * @see [android.app.UiModeManager.setNightMode](https://developer.android.google.cn/reference/android/app/UiModeManager#setNightMode(int))
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}