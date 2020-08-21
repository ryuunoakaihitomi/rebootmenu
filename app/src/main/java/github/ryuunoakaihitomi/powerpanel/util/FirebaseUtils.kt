package github.ryuunoakaihitomi.powerpanel.util

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.MyApplication
import java.time.LocalTime

object FirebaseUtils {

    private const val TAG = "FirebaseUtils"

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
            /* 包名不一致需要自行缩短本地类名 */
            val split = activity.localClassName.split('.')
            putString(KEY_SRC, split[split.size - 1])

            putInt(KEY_TIME_HOUR, LocalTime.now().hour)
            putString(KEY_TYPE, labelResId.toLabel())
            putBoolean(KEY_FORCE_MODE, forceMode)
            putBoolean(KEY_DONE, done)
        }
        Log.i(TAG, "logPowerOperation: $bundle")
        logEvent(EVENT_PWR_OP, bundle)
    }

    fun logDialogCancel(@StringRes labelResId: Int, cancelled: Boolean) {
        val bundle = Bundle().apply {
            putString(KEY_TYPE, labelResId.toLabel())
            putBoolean(KEY_CANCELLED, cancelled)
        }
        Log.i(TAG, "logDialogCancel: $bundle")
        logEvent(EVENT_DIALOG_CANCEL, bundle)
    }

    fun disableDataCollection() {
        Firebase.app.setDataCollectionDefaultEnabled(false as Boolean?)
    }

    fun log(level: Int, tag: String, msg: String) {
        val logLine = listOf(Log.levelToString(level), tag, msg).toString()
        if (!BuildConfig.DISABLE_FIREBASE) FirebaseCrashlytics.getInstance().log(logLine)
    }

    private fun logEvent(string: String, bundle: Bundle) {
        if (!BuildConfig.DISABLE_FIREBASE) Firebase.analytics.logEvent(string, bundle)
    }

    private fun Int.toLabel() = MyApplication.getInstance().resources.getResourceEntryName(this)
}