package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.content.Context
import android.os.StrictMode
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.util.MyLogTree
import timber.log.Timber

class MyApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Timber.plant(MyLogTree())
    }

    override fun onCreate() {
        super.onCreate()
        Statistics.recordEnvInfo()
        if (BuildConfig.DEBUG) StrictMode.enableDefaults()
        // 留下PowerAct核心日志用以发布后的调试
        ExternalUtils.enableLog(true)
    }
}