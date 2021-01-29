package github.ryuunoakaihitomi.powerpanel.util

import timber.log.Timber

/**
 * floss版本保留日志
 */
class MyLogTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        super.log(priority, "floss#$tag", message, t)
    }
}