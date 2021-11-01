package github.ryuunoakaihitomi.powerpanel.stat

import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import rikka.shizuku.Shizuku
import java.util.*

/**
 * 统计工具
 *
 * **为方便迁移请务必在此处创建接口并且不在此类外直接调用统计SDK**
 * **修改及实现[InternalDoer]以分离floss版本**
 */
object Statistics {

    /* 记录电源操作，评估万一某个功能有bug所带来的影响范围 */
    private const val EVENT_PWR_OP = "power_operation"
    private const val KEY_SRC = "source"
    private const val KEY_TIME_HOUR = "time_hour"
    private const val KEY_TYPE = "type"
    private const val KEY_FORCE_MODE = "force_mode"
    private const val KEY_DONE = "done"
    private const val KEY_SHIZUKU = "shizuku"

    /* 记录特权模式二次操作取消 */
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
            // 作为数字，也就是用putInt()，在Events面板只能看到平均值和总数
            putString(KEY_TIME_HOUR, Calendar.getInstance()[Calendar.HOUR_OF_DAY].toString())
            putString(KEY_TYPE, labelResId.toLabel())
            /* FirebaseAnalytics不支持Boolean (String, long and double param types are supported.) */
            putString(KEY_FORCE_MODE, forceMode.toString())
            putString(KEY_DONE, done.toString())
            putString(KEY_SHIZUKU, Shizuku.pingBinder().toString())
        }
        InternalDoerImpl.logEvent(EVENT_PWR_OP, bundle)
    }

    fun logDialogCancel(@StringRes labelResId: Int, cancelled: Boolean) {
        val bundle = Bundle().apply {
            putString(KEY_TYPE, labelResId.toLabel())
            putString(KEY_CANCELLED, cancelled.toString())
        }
        InternalDoerImpl.logEvent(EVENT_DIALOG_CANCEL, bundle)
    }

    fun initConfig(app: Application) {
        recordEnvInfo()
        InternalDoerImpl.initialize(app)
    }

    private fun recordEnvInfo() {
        arrayOf(Build::class, Build.VERSION::class).forEach { clz ->
            clz.java.fields.forEach enumProps@{
                val name = it.name
                if (name == "UNKNOWN") return@enumProps
                @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                InternalDoerImpl.setCustomKey("${clz.simpleName} $name", it[null])
            }
        }
    }

    private fun @receiver:StringRes Int.toLabel() =
        BlackMagic.getGlobalApp().resources.getResourceEntryName(this)
}