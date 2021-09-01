package github.ryuunoakaihitomi.powerpanel.stat

import android.content.Context
import android.os.Bundle
import timber.log.Timber
import kotlin.system.exitProcess

/**
 * 在floss版中用日志替代
 */
object InternalDoerImpl : InternalDoer {

    init {
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            Timber.e(e, "${e.javaClass.simpleName} in $t")
            exitProcess(-1)
        }
    }

    override fun initialize(ctx: Context) {
        Timber.i("init: $ctx")
    }

    override fun setCustomKey(k: String, v: Any) {
        val value = if (v is Array<*>) v.contentToString() else v
        Timber.i("Custom Key: $k, $value")
    }

    override fun logEvent(tag: String, bundle: Bundle) {
        Timber.i("event -> $tag $bundle")
    }
}