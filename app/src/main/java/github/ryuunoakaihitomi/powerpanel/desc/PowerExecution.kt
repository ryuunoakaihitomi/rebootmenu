package github.ryuunoakaihitomi.powerpanel.desc

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.os.postDelayed
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.poweract.Callback
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.poweract.PowerAct
import github.ryuunoakaihitomi.poweract.PowerActX
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.ui.DummyActivity
import github.ryuunoakaihitomi.powerpanel.ui.ShortcutActivity
import github.ryuunoakaihitomi.powerpanel.ui.main.MainActivity
import timber.log.Timber

object PowerExecution {

    fun execute(activity: AppCompatActivity, @StringRes labelResId: Int, forceMode: Boolean) {
        val callback = object : Callback {

            override fun done() {
                Statistics.logPowerOperation(activity, labelResId, forceMode, true)
                activity.finish()
            }

            override fun failed() {
                Statistics.logPowerOperation(activity, labelResId, forceMode, false)
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
            val toastDelay = 1_000L  // 延迟，在播放完进入无障碍设置的动画再显示，防止用户来不及注意滞留的Toast
            val notificationDuration = 5_000L
            val mainHandler = Handler(Looper.getMainLooper())
            /* 在Android 11中无法使用自定义后台Toast，使用全屏通知，或者文本Toast代替Toasty提示用户 */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val manager = activity.getSystemService<NotificationManager>()
                // 开了勿扰或者禁用，使用文本Toast代替
                if (manager?.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL ||
                    !manager.areNotificationsEnabled()
                ) {
                    mainHandler.postDelayed(toastDelay) {
                        Toast.makeText(
                            activity.application,
                            R.string.enable_accessibility_service_hint,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@setUserGuideRunnable
                }
                val cid = "accessibility_service_hint"
                val hint = activity.getText(R.string.enable_accessibility_service_hint)
                val channel =
                    NotificationChannel(cid, hint, NotificationManager.IMPORTANCE_HIGH).apply {
                        setSound(Uri.EMPTY, null)   // 静音
                        setShowBadge(false)
                        //setBypassDnd(true) 受到勿扰模式的影响，这个condition经常忘记
                    }
                val notification = Notification.Builder(activity, cid).apply {
                    setSmallIcon(android.R.drawable.stat_notify_chat)
                    setContentTitle(activity.getText(android.R.string.dialog_alert_title))
                    setContentText(hint)
                    setChannelId(cid)
                    val dummyPendingIntent = PendingIntent.getActivity(
                        activity,
                        0,
                        Intent(activity, DummyActivity::class.java),
                        PendingIntent.FLAG_IMMUTABLE // Android 12必须明确指定可变性
                    )
                    setFullScreenIntent(dummyPendingIntent, true)   // 可点击取消的全屏通知
                    setAutoCancel(true)
                }.build()
                val nid = Int.MAX_VALUE
                manager.run {
                    createNotificationChannel(channel)
                    notify(nid, notification)
                }
                // 延迟一段时间后自动取消通知
                mainHandler.postDelayed(notificationDuration) {
                    manager.run {
                        cancel(nid)
                        deleteNotificationChannel(cid)
                    }
                }
            } else {
                mainHandler.postDelayed(toastDelay) {
                    Toasty.info(
                        activity,
                        R.string.enable_accessibility_service_hint,
                        Toasty.LENGTH_LONG
                    ).show()
                }
            }
        }
        work()
    }

    fun toPowerControlPanel(context: Context) =
        if (ExternalUtils.isExposedComponentAvailable(context)) {
            ShortcutActivity.getActionIntent(R.string.func_sys_pwr_menu)
        } else {
            Intent(context, MainActivity::class.java)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}