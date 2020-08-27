package github.ryuunoakaihitomi.powerpanel.util

import android.util.Log
import github.ryuunoakaihitomi.powerpanel.BuildConfig

object Log {

    fun v(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg)
        }
        StatisticsUtils.log(Log.VERBOSE, tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg)
        }
        StatisticsUtils.log(Log.DEBUG, tag, msg)
    }

    fun i(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg)
        }
        StatisticsUtils.log(Log.INFO, tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg)
        }
        StatisticsUtils.log(Log.WARN, tag, msg)

    }

    fun e(tag: String, msg: String) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg)
        }
        StatisticsUtils.log(Log.ERROR, tag, msg)
    }

    fun e(tag: String, msg: String, throwable: Throwable) {
        e(tag, msg + "\n" + Log.getStackTraceString(throwable))
    }

    fun levelToString(level: Int) = when (level) {
        Log.VERBOSE -> "V"
        Log.DEBUG -> "D"
        Log.INFO -> "I"
        Log.WARN -> "W"
        Log.ERROR -> "E"
        else -> level.toString()
    }
}