package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.topjohnwu.superuser.Shell
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.util.MyLogTree
import github.ryuunoakaihitomi.powerpanel.util.StatisticsUtils
import timber.log.Timber

class MyApplication : Application() {

    companion object {
        private lateinit var myApplication: MyApplication
        fun getInstance() = myApplication
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Timber.plant(MyLogTree())
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this
        StatisticsUtils.recordEnvInfo()
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults()
            Shell.enableVerboseLogging = true
            // 调试时，使用FirebaseApp#setDataCollectionDefaultEnabled后仍有数据流通，不能保证与firebase的通信绝对被屏蔽
            // 况且setDataCollectionDefaultEnabled不是公开API （含@hide和@KeepForSdk）
            // 只能使用DebugView：adb shell setprop debug.firebase.analytics.app com.ryuunoakaihitomi.rebootmenu
        }
        ExternalUtils.enableLog(BuildConfig.DEBUG)
    }
}