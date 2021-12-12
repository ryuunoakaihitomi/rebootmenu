package github.ryuunoakaihitomi.powerpanel.desc

import android.app.*
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.os.postDelayed
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.powerpanel.R

object AccessibilityHint {

    // 打开无障碍服务的提示信息
    fun request(activity: AppCompatActivity, work: () -> Unit) {
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
                    // 不延迟防止被识别为后台Toast造成无法显示
                    showEashToast(activity, false)
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
                        Intent(activity, Activity::class.java),
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
                showEashToast(activity)
                mainHandler.postDelayed(toastDelay) { showEashToast(activity) }
            }
        }
        work()
    }

    @Suppress("SpellCheckingInspection")
    private fun showEashToast(activity: Activity, isCustom: Boolean = true) {
        val app = activity.application
        val msgRes = R.string.enable_accessibility_service_hint
        val duration = Toast.LENGTH_LONG

        if (isCustom) Toasty.info(app, msgRes, duration).show()
        else Toast.makeText(app, msgRes, duration).show()
    }
}