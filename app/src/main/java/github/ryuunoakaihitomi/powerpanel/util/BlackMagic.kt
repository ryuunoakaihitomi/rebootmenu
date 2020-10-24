package github.ryuunoakaihitomi.powerpanel.util

import android.annotation.SuppressLint
import android.os.Build
import android.os.Build.VERSION_CODES.N_MR1
import android.os.Build.VERSION_CODES.Q
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.view.WindowManager.BadTokenException
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.util.TimeUtils
import java.lang.reflect.Proxy

object BlackMagic {

    private const val TAG = "BlackMagic"

    @SuppressLint("PrivateApi")
    fun toastBugFix() {
        if (Build.VERSION.SDK_INT < Q) {
            runCatching {
                val getService = Toast::class.java.getDeclaredMethod("getService")
                getService.isAccessible = true
                val iNotificationManager = getService.invoke(null)
                val iNotificationManagerProxy = Proxy.newProxyInstance(
                    Toast::class.java.classLoader,
                    arrayOf(Class.forName("android.app.INotificationManager"))
                ) { _, method, args ->
                    if ("enqueueToast" == method.name) {
                        Log.d(TAG, "toastBugsFix: duration = ${args[2]}")
                        args[0] = "android"
                        if (Build.VERSION.SDK_INT == N_MR1) {
                            val tn = args[1]
                            val mHandler = tn.javaClass.getDeclaredField("mHandler")
                            mHandler.isAccessible = true
                            @Suppress("DEPRECATION") // Handler(), since 30
                            class HandlerProxy(private val handler: Handler) : Handler() {
                                override fun handleMessage(msg: Message) {
                                    try {
                                        handler.handleMessage(msg)
                                    } catch (e: BadTokenException) {
                                        // 在Toast之后做阻塞操作会BTE，日志会显示阻塞时间
                                        val sb = StringBuilder()
                                        @Suppress("RestrictedApi")
                                        TimeUtils.formatDuration(
                                            SystemClock.uptimeMillis() - msg.`when`,
                                            sb
                                        )
                                        Log.e(TAG, "handleMessage: BTE caught! Stuck in $sb.")
                                    }
                                }
                            }
                            mHandler.set(tn, HandlerProxy(mHandler[tn] as Handler))
                        }
                    }
                    return@newProxyInstance method.invoke(iNotificationManager, *args)
                }
                val sService = Toast::class.java.getDeclaredField("sService")
                sService.isAccessible = true
                sService.set(null, iNotificationManagerProxy)
            }.onFailure {
                Log.e(TAG, "toastBugFix: ", it)
            }
        }
    }

    /**
     * 取得AlertDialog里的MessageView
     * 要事先调用[AlertDialog.Builder.setMessage]占位并在[AlertDialog.show]后调用本fun
     *
     * 注意：系统自带的[android.app.AlertDialog]已经不允许这样做，
     * 必须使用[AlertDialog]。
     *
     * @param alertDialog [AlertDialog]
     */
    fun getAlertDialogMessageView(alertDialog: AlertDialog): TextView {
        val mAlert = AlertDialog::class.java.getDeclaredField("mAlert")
        mAlert.isAccessible = true
        val mAlertController = mAlert[alertDialog]
        val mMessageView = mAlertController.javaClass.getDeclaredField("mMessageView")
        mMessageView.isAccessible = true
        return mMessageView[mAlertController] as TextView
    }
}