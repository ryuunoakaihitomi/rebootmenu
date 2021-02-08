package github.ryuunoakaihitomi.powerpanel.util

import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import github.ryuunoakaihitomi.powerpanel.MyApplication
import timber.log.Timber
import java.util.*

/**
 * 在floss版中用日志替代
 */
object StatisticsUtils {

    private const val EVENT_PWR_OP = "power_operation"
    private const val KEY_SRC = "source"
    private const val KEY_TIME_HOUR = "time_hour"
    private const val KEY_TYPE = "type"
    private const val KEY_FORCE_MODE = "force_mode"
    private const val KEY_DONE = "done"

    private const val EVENT_DIALOG_CANCEL = "dialog_cancel"
    private const val KEY_CANCELLED = "cancelled"

    fun logPowerOperation(
        activity: AppCompatActivity,
        @StringRes labelResId: Int,
        forceMode: Boolean,
        done: Boolean
    ) {
        val bundle = Bundle().apply {
            val split = activity.localClassName.split('.')
            putString(KEY_SRC, split[split.size - 1])
            putString(KEY_TIME_HOUR, Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString())
            putString(KEY_TYPE, labelResId.toLabel())
            putString(KEY_FORCE_MODE, forceMode.toString())
            putString(KEY_DONE, done.toString())
        }
        logEvent(EVENT_PWR_OP, bundle)
    }

    fun logDialogCancel(@StringRes labelResId: Int, cancelled: Boolean) {
        val bundle = Bundle().apply {
            putString(KEY_TYPE, labelResId.toLabel())
            putString(KEY_CANCELLED, cancelled.toString())
        }
        logEvent(EVENT_DIALOG_CANCEL, bundle)
    }

    fun recordEnvInfo() {
        arrayOf(Build::class, Build.VERSION::class).forEach { clz ->
            clz.java.fields.forEach {
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                setCustomKey("${clz.simpleName} ${it.name}", it[null])
            }
        }
    }

    private fun setCustomKey(key: String, v: Any) {
        val value = if (v is Array<*>) v.contentToString() else v
        Timber.i("Custom Key: $key, $value")
    }

    private fun logEvent(tag: String, bundle: Bundle) {
        Timber.i("event -> $tag $bundle")
    }

    private fun Int.toLabel() = MyApplication.getInstance().resources.getResourceEntryName(this)
}