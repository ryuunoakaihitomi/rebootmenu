package github.ryuunoakaihitomi.powerpanel.stat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import timber.log.Timber

/**
 * 目前使用的是`Firebase`
 */
object InternalDoerImpl : InternalDoer {

    init {
        disableFirebaseInDebug()
    }

    /**
     * 使用DebugView过滤上传的统计信息比较麻烦
     */
    @SuppressLint("LogNotTimber")
    private fun disableFirebaseInDebug() {
        if (BuildConfig.DEBUG) {
            Log.e(javaClass.simpleName, "!!! NOTICE: Crash analytics is disabled in debug variant.")
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(false)
            Firebase.analytics.setAnalyticsCollectionEnabled(false)
        }
    }

    override fun setCustomKey(k: String, v: Any) {
        Firebase.crashlytics.apply {
            when (v) {
                is String -> setCustomKey(k, v)
                is Boolean -> setCustomKey(k, v)
                is Int -> setCustomKey(k, v)
                is Long -> setCustomKey(k, v)
                is Float -> setCustomKey(k, v)
                is Double -> setCustomKey(k, v)
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