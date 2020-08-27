package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.os.StrictMode
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import github.ryuunoakaihitomi.powerpanel.util.StatisticsUtils

class MyApplication : Application() {

    companion object {
        private lateinit var myApplication: MyApplication
        fun getInstance(): MyApplication {
            return myApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
            // 也可以使用DebugView：adb shell setprop debug.firebase.analytics.app com.ryuunoakaihitomi.rebootmenu
            StatisticsUtils.disableDataCollection()
        }
        if (BuildConfig.DISABLE_FIREBASE) {
            StatisticsUtils.disableDataCollection()
        }
        BlackMagic.toastBugFix()
        ExternalUtils.enableLog(BuildConfig.DEBUG)
    }
}