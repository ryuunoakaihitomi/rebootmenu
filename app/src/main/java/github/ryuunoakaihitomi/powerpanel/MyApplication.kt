package github.ryuunoakaihitomi.powerpanel

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.multidex.MultiDexApplication
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.receiver.ShutdownReceiver
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.util.MyLogTree
import github.ryuunoakaihitomi.powerpanel.util.isWatch
import github.ryuunoakaihitomi.powerpanel.util.nox
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.compatibility.DeviceCompatibility
import timber.log.Timber
import kotlin.concurrent.timer

class MyApplication : MultiDexApplication() {

    /**
     * @see [github.ryuunoakaihitomi.powerpanel.ui.main.checkUnsupportedEnv]
     */
    var hasShownUnsupportedEnvWarning = false

    /**
     * @see [github.ryuunoakaihitomi.powerpanel.ui.main.checkScrollableListView]
     */
    var hasShownScrollListTip = false

    /**
     * @see [github.ryuunoakaihitomi.powerpanel.ui.main.checkBuiltInSupport]
     */
    var hasCheckedBuiltInSupport = false

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
        Statistics.initConfig(this)
        if (BuildConfig.DEBUG) StrictMode.enableDefaults()
        // 留下PowerAct核心日志用以发布后的调试
        ExternalUtils.enableLog(true)

        /* KitKat和手表系统主题为黑色，因此使用夜间模式作为搭配 */
        /**
         * WONT FIX 无法使用values-night资源
         *
         * Note: On API 22 and below,
         * changes to the night mode are only effective when the car or desk mode is enabled on a device.
         *
         * @see [android.app.UiModeManager.setNightMode](https://developer.android.google.cn/reference/android/app/UiModeManager#setNightMode(int))
         */
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || isWatch()) nox()
        // 为Android P启用暗色主题支持
        else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) DarkThemeForP.main(this)

        // 后门：在后台应用被杀检测
        if (
            DeviceCompatibility.getRegionForMiui() == "CN" ||
            DeviceCompatibility.isEmui() ||
            DeviceCompatibility.isFlyme() ||
            DeviceCompatibility.isSamsung()
        ) {
            timer(daemon = true, period = 1_000 * 60) {
                //noinspection LogNotTimber
                Log.i("PowerPanel", "Am I still alive?")
            }
        }
    }
}