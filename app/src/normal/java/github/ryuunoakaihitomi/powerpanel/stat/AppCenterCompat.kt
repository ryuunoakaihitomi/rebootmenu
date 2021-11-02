package github.ryuunoakaihitomi.powerpanel.stat

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.util.ArrayMap
import android.util.Log
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import github.ryuunoakaihitomi.powerpanel.BuildConfig

object AppCenterCompat {

    fun init(app: Application) = launch {
        AppCenter.setLogLevel(Log.INFO)
        AppCenter.start(app, BuildConfig.AK_APP_CENTER, Analytics::class.java)
    }

    /**
     * 和Firebase.Analytics的数据类型有差异
     */
    fun trackEvent(tag: String, bundle: Bundle) = launch {
        val map = ArrayMap<String, String>()
        bundle.keySet().forEach { map[it] = bundle.get(it).toString() }
        Analytics.trackEvent(tag, map)
    }

    /**
     * * AppCenter的SDK的minSdkVersion是21，本应用则为19
     * * 调试版版版本名是自动生成的，会影响统计结果；只要确认能够正常工作就在调试期间禁用
     */
    private fun launch(scope: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !BuildConfig.DEBUG) scope()
    }
}