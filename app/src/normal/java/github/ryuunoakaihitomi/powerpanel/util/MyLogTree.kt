package github.ryuunoakaihitomi.powerpanel.util

import android.util.Log
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.stat.InternalDoerImpl
import timber.log.Timber

/**
 * 从main复制至此以防止Redeclaration错误
 */
class MyLogTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        InternalDoerImpl.log(priority, tag ?: "empty tag", message)
        if (BuildConfig.DEBUG) super.log(priority, tag, message, t)
    }
}