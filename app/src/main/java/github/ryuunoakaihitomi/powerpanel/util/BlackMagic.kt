package github.ryuunoakaihitomi.powerpanel.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.app.StatusBarManager
import android.content.Context
import android.os.Build
import android.widget.TextView
import androidx.core.content.getSystemService
import org.joor.Reflect.on
import org.joor.Reflect.onClass

object BlackMagic {

    /**
     * 取得AlertDialog里的MessageView
     * 要事先调用[AlertDialog.Builder.setMessage]占位并在[AlertDialog.show]后调用本fun
     *
     * 警告：系统自带的[android.app.AlertDialog]已经不允许这样做，除非绕过反射限制
     */
    fun getAlertDialogMessageView(d: AlertDialog): TextView =
        on(d).field("mAlert").field("mMessageView").get()

    fun getGlobalApp(): Application =
        onClass("android.app.ActivityThread").call("currentApplication").get()

    @SuppressLint("WrongConstant")
    fun collapseStatusBarPanels(context: Context) {
        // https://developer.android.com/about/versions/12/reference/compat-framework-changes#lock_down_collapse_status_bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
        @Suppress("SpellCheckingInspection")
        val statusBarManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.getSystemService<StatusBarManager>()!!
            else context.getSystemService("statusbar")
        on(statusBarManager).call("collapsePanels")
    }

    fun getSystemProperties(key: String): String =
        onClass("android.os.SystemProperties").call("get", key).get()

    fun getBooleanSystemProperties(key: String): Boolean =
        onClass("android.os.SystemProperties").call("getBoolean", key, false).get()
}