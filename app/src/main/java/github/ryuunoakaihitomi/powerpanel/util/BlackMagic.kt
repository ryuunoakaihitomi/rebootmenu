package github.ryuunoakaihitomi.powerpanel.util

import android.annotation.SuppressLint
import android.app.Application
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.lang.Class as C

object BlackMagic {

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

    @SuppressLint("PrivateApi")
    fun getGlobalApp() = C.forName("android.app.ActivityThread").getMethod("currentApplication")
        .invoke(null) as Application
}