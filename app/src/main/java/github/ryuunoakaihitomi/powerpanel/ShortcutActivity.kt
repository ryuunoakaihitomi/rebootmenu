package github.ryuunoakaihitomi.powerpanel

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import github.ryuunoakaihitomi.poweract.PowerActX

class ShortcutActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LABEL_RES_ID = "extraLabelResId"
        const val EXTRA_FORCE_MODE = "extraForceMode"

        fun getActionIntent(@StringRes labelResId: Int, forceMode: Boolean = false) = run {
            Intent(MyApplication.getInstance(), ShortcutActivity::class.java).run {
                action = Intent.ACTION_VIEW
                putExtra(EXTRA_LABEL_RES_ID, labelResId)
                putExtra(EXTRA_FORCE_MODE, forceMode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeTransparent()
        val callback = getGlobalCallback(this)
        val forceMode = intent.getBooleanExtra(EXTRA_FORCE_MODE, false)
        when (intent.getIntExtra(EXTRA_LABEL_RES_ID, -1)) {
            R.string.func_lock_screen -> lockScreenWithTip(this, callback)
            R.string.func_sys_pwr_menu -> showPowerDialogWithTip(this, callback)
            R.string.func_reboot -> PowerActX.reboot(callback, forceMode)
            R.string.func_shutdown -> PowerActX.shutdown(callback, forceMode)
            R.string.func_recovery -> PowerActX.recovery(callback, forceMode)
            R.string.func_bootloader -> PowerActX.bootloader(callback, forceMode)
            R.string.func_soft_reboot -> PowerActX.softReboot(callback)
            R.string.func_restart_sys_ui -> {
                restartSysUi()
                callback.done()
            }
            R.string.func_safe_mode -> PowerActX.safeMode(callback, forceMode)
            R.string.func_lock_screen_privileged -> PowerActX.lockScreen(callback)
        }
    }
}