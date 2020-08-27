package github.ryuunoakaihitomi.powerpanel.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.topjohnwu.superuser.Shell
import es.dmoral.toasty.Toasty
import java.util.*


private const val TAG = "Utils"

fun Activity.makeTransparent() {
    this.window.decorView.alpha = 0f
}

fun restartSysUi() {
    @Suppress("SpellCheckingInspection") val sysUiPkgName = "com.android.systemui"
    val processCmd = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "ps -A" else "ps"
    Shell.su(processCmd).submit {
        var line = ""
        it.out.forEach { s ->
            if (s.contains(sysUiPkgName)) {
                line = s
                return@forEach
            }
        }
        val stringTokenizer = StringTokenizer(line)
        var count = 0
        var pid = ""
        while (stringTokenizer.hasMoreTokens()) {
            count++
            pid = stringTokenizer.nextToken()
            if (count == 2) break
        }
        Log.d(TAG, "restartSysUi: pid=$pid")
        Shell.su("kill $pid").submit()
    }
}

fun Context.openUrlInBrowser(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Log.e(TAG, "openUrlInBrowser: ", it)
        Toasty.info(this, url).show()
    }
}