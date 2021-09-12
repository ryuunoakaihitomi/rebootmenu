package github.ryuunoakaihitomi.powerpanel.stat

import android.content.Context
import android.os.Build
import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsTools
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import timber.log.Timber

/**
 * 目前使用的是`Firebase`和`Huawei AppGallery Connect`
 */
object InternalDoerImpl : InternalDoer {

    override fun initialize(ctx: Context) {
        if (BuildConfig.DEBUG) {
            HiAnalyticsTools.enableLog()
        }
        // FIXME 等待华为分析服务适配Android 12
        // https://developer.huawei.com/consumer/cn/doc/development/HMSCore-Guides/version-change-history-0000001050705116
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            HiAnalytics.getInstance(ctx)
        }
    }

    override fun setCustomKey(k: String, v: Any) {
        Firebase.crashlytics.apply {
            when (v) {
                is String -> setCustomKey(k, v)
                is Int -> setCustomKey(k, v)
                is Long -> setCustomKey(k, v)
                is Float -> setCustomKey(k, v)
                is Double -> setCustomKey(k, v)
                is Boolean -> setCustomKey(k, v)
                is Array<*> -> setCustomKey(k, v.contentToString())
                else -> Timber.w("Undefined type: $k, $v")
            }
        }
    }

    override fun logEvent(tag: String, bundle: Bundle) {
        Timber.i(bundle.toString())
        Firebase.analytics.logEvent(tag, bundle)
        Firebase.crashlytics.setCustomKey(tag, bundle.toString())
    }

    override fun log(level: String, tag: String, msg: String) {
        val logLine = listOf(level, tag, msg).toString()
        Firebase.crashlytics.log(logLine)
    }
}