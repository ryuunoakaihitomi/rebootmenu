package github.ryuunoakaihitomi.powerpanel.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.app.StatusBarManager
import android.content.Context
import android.os.Build
import android.widget.TextView
import androidx.core.content.getSystemService
import org.apache.commons.lang.reflect.FieldUtils
import java.lang.Class as C

object BlackMagic {

    /**
     * 取得AlertDialog里的MessageView
     * 要事先调用[AlertDialog.Builder.setMessage]占位并在[AlertDialog.show]后调用本fun
     *
     * 警告：系统自带的[android.app.AlertDialog]已经不允许这样做，除非绕过反射限制
     */
    @SuppressLint("PrivateApi")
    fun getAlertDialogMessageView(alertDialog: AlertDialog): TextView {
        val mAlert = AlertDialog::class.java.getDeclaredField("mAlert")
        mAlert.isAccessible = true
        val mAlertController = mAlert[alertDialog]
        // 为Wear OS中使用的MicroAlertController做适配
        val mMessageView = FieldUtils.getField(mAlertController.javaClass, "mMessageView", true)
        mMessageView.isAccessible = true
        return mMessageView[mAlertController] as TextView
    }

    @SuppressLint("PrivateApi")
    fun getGlobalApp() = C.forName("android.app.ActivityThread").getMethod("currentApplication")
        .invoke(null) as Application

    @SuppressLint("WrongConstant")
    fun collapseStatusBarPanels(context: Context) {
        // https://developer.android.com/about/versions/12/reference/compat-framework-changes#lock_down_collapse_status_bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return
        @Suppress("SpellCheckingInspection")
        val statusBarManager =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) context.getSystemService<StatusBarManager>()!!
            else context.getSystemService("statusbar")
        val collapsePanels = statusBarManager.javaClass.getMethod("collapsePanels")
        collapsePanels.invoke(statusBarManager)
    }
}