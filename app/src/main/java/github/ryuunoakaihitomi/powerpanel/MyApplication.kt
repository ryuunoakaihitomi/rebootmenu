package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.content.Context
import android.os.StrictMode
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
        if (BuildConfig.DEBUG) StrictMode.enableDefaults()
        // 留下PowerAct核心日志用以发布后的调试
        ExternalUtils.enableLog(true)
    }
}