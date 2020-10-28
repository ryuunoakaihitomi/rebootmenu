package github.ryuunoakaihitomi.powerpanel.desc

import android.view.Gravity
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.poweract.Callback
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.poweract.PowerAct
import github.ryuunoakaihitomi.poweract.PowerActX
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.util.StatisticsUtils
import timber.log.Timber

object PowerExecution {

    fun execute(@StringRes labelResId: Int, activity: AppCompatActivity, forceMode: Boolean) {
        val callback = object : Callback {

            override fun done() {
                StatisticsUtils.logPowerOperation(activity, labelResId, forceMode, true)
                activity.finish()
            }

            override fun failed() {
                StatisticsUtils.logPowerOperation(activity, labelResId, forceMode, false)
                Toasty.error(activity, R.string.toast_op_failed).run {
                    setGravity(Gravity.CENTER, 0, 0)
                    show()
                }
                activity.finish()
            }
        }
        when (labelResId) {
            R.string.func_lock_screen -> requestAccessibilityService(activity) {
                PowerAct.lockScreen(activity, callback)
            }
            R.string.func_sys_pwr_menu -> requestAccessibilityService(activity) {
                PowerAct.showPowerDialog(activity, callback)
            }
            R.string.func_reboot -> PowerActX.reboot(callback, forceMode)
            R.string.func_shutdown -> PowerActX.shutdown(callback, forceMode)
            R.string.func_recovery -> PowerActX.recovery(callback, forceMode)
            R.string.func_bootloader -> PowerActX.bootloader(callback, forceMode)
            R.string.func_soft_reboot -> PowerActX.softReboot(callback)
            R.string.func_restart_sys_ui -> PowerActX.restartSystemUi(callback)
            R.string.func_safe_mode -> PowerActX.safeMode(callback, forceMode)
            R.string.func_lock_screen_privileged -> PowerActX.lockScreen(callback)
            else -> {
                Timber.w("Unknown res id($labelResId). Go home...")
                activity.startActivity(
                    activity.packageManager.getLaunchIntentForPackage(BuildConfig.APPLICATION_ID)
                )
                activity.finish()
            }
        }
    }

    // 打开无障碍服务的提示信息
    private fun requestAccessibilityService(activity: AppCompatActivity, work: () -> Unit) {
        ExternalUtils.setUserGuideRunnable {
            Toasty.info(
                activity,
                R.string.toast_enable_accessibility_service_hint,
                Toasty.LENGTH_LONG
            ).show()
        }
        work()
    }
}