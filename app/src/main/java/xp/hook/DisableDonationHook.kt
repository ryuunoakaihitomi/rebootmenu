package xp.hook

import android.app.Activity
import android.app.AlertDialog
import com.github.kyuubiran.ezxhelper.utils.Log
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import com.github.kyuubiran.ezxhelper.utils.hookBefore
import github.ryuunoakaihitomi.powerpanel.R
import xp.common.KEY_DISABLE_DONATION
import xp.common.pref

object DisableDonationHook : Runnable {
    override fun run() {
        if (pref.getBoolean(KEY_DISABLE_DONATION, false)) {
            findMethod(AlertDialog.Builder::class.java) { name == "setNegativeButton" }.hookBefore {
                it.run {
                    if ((args[0] as Int) == R.string.donate) {
                        Log.d("DisableDonationHook")
                        result = thisObject
                    }
                }
            }
            findMethod("github.ryuunoakaihitomi.powerpanel.ui.DonateActivity") { name == "onCreate" }.hookAfter {
                Log.toast("DonateActivity disabled")
                (it.thisObject as Activity).finish()
            }
        }
    }
}