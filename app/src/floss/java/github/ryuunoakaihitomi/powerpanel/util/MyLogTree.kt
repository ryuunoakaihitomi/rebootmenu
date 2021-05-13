package github.ryuunoakaihitomi.powerpanel.util

import timber.log.Timber

/**
 * floss版本完全保留日志
 */
class MyLogTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, /* Free PowerPanel */ "FPP#$tag", message, t)
    }
}