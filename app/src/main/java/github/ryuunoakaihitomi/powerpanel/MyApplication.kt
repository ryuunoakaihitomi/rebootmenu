package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.os.StrictMode
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import github.ryuunoakaihitomi.powerpanel.util.FirebaseUtils

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
            FirebaseUtils.disableDataCollection()
        }
        if (BuildConfig.DISABLE_FIREBASE) {
            FirebaseUtils.disableDataCollection()
        }
        BlackMagic.toastBugFix()
        ExternalUtils.enableLog(BuildConfig.DEBUG)
    }
}