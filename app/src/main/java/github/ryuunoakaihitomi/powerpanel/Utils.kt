package github.ryuunoakaihitomi.powerpanel

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.topjohnwu.superuser.Shell
import es.dmoral.toasty.Toasty
import java.util.*


private const val TAG = "Utils"

fun addLauncherShortcut(
    context: Context,
    label: CharSequence,
    bitmap: Bitmap,
    intent: Intent
) {
    val unspannedLabel = label.toString()
    // 使用UUID有两种后果：可以重复添加Icon，修复图标无法变色的bug
    val shortcut = ShortcutInfoCompat.Builder(context, UUID.randomUUID().toString())
        .setShortLabel(unspannedLabel)
        .setLongLabel(unspannedLabel)
        .setIcon(IconCompat.createWithBitmap(bitmap))
        .setIntent(intent)
        .build()
    ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
    // 在Android8.0以下和一些自定义系统（自动拒绝）可能没有反馈
    Toasty.info(context, R.string.toast_shortcut_added).show()
}

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
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (t: Throwable) {
        Log.e(TAG, "openUrlInBrowser: ", t)
    }
}