package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.os.StrictMode
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.poweract.Callback
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.poweract.PowerAct

class MyApplication : Application() {

    companion object {
        private lateinit var myApplication: MyApplication
        fun getInstance(): MyApplication {
            return myApplication
        }
    }

    override fun onCreate() {
        super.onCreate()
        myApplication = this
        if (BuildConfig.DEBUG) StrictMode.enableDefaults()
        BlackMagic.toastBugsFix()
    }
}

/* --- 应用内部共享资源 --- */

fun getGlobalCallback(activity: AppCompatActivity): Callback {
    return object : Callback {

        override fun done() {
            activity.finish()
        }

        override fun failed() {
            val error = Toasty.error(
                activity,
                R.string.toast_op_failed
            )
            error.setGravity(Gravity.CENTER, 0, 0)
            error.show()
            activity.finish()
        }
    }
}

// 打开无障碍服务的提示信息
private fun requestAccessibilityService(work: () -> Unit) {
    ExternalUtils.setUserGuideRunnable {
        Toasty.info(
            MyApplication.getInstance(),
            R.string.toast_enable_accessibility_service_hint,
            Toasty.LENGTH_LONG
        ).show()
    }
    work()
}

fun lockScreenWithTip(activity: AppCompatActivity, callback: Callback) {
    requestAccessibilityService {
        PowerAct.lockScreen(activity, callback)
    }
}

fun showPowerDialogWithTip(activity: AppCompatActivity, callback: Callback) {
    requestAccessibilityService {
        PowerAct.showPowerDialog(activity, callback)
    }
}