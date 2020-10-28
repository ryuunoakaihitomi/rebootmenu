package github.ryuunoakaihitomi.powerpanel.util

import android.util.Log
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import timber.log.Timber

class MyLogTree : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        StatisticsUtils.log(levelToString(priority), tag ?: "empty tag", message)
        if (BuildConfig.DEBUG) super.log(priority, tag, message, t)
    }

    private fun levelToString(level: Int) = when (level) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        else -> level.toString()
    }
}