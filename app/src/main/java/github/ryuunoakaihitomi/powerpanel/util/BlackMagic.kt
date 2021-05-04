package github.ryuunoakaihitomi.powerpanel.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.widget.TextView
import java.lang.Class as C

object BlackMagic {

    /**
     * 取得AlertDialog里的MessageView
     * 要事先调用[AlertDialog.Builder.setMessage]占位并在[AlertDialog.show]后调用本fun
     *
     * 警告：系统自带的[android.app.AlertDialog]已经不允许这样做，除非绕过反射限制
     */
    fun getAlertDialogMessageView(alertDialog: AlertDialog): TextView {
        val mAlert = AlertDialog::class.java.getDeclaredField("mAlert")
        mAlert.isAccessible = true
        val mAlertController = mAlert[alertDialog]
        val mMessageView = mAlertController.javaClass.getDeclaredField("mMessageView")
        mMessageView.isAccessible = true
        return mMessageView[mAlertController] as TextView
    }

    @SuppressLint("PrivateApi")
    fun getGlobalApp() = C.forName("android.app.ActivityThread").getMethod("currentApplication")
        .invoke(null) as Application
}