package github.ryuunoakaihitomi.powerpanel.util

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.service.quicksettings.Tile
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import timber.log.Timber

typealias RC = ResourcesCompat

fun Context.openUrlInBrowser(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toasty.info(this, url).show()
        // 分享到其他地方，这个方法catch了ActivityNotFoundException，所以不用担心崩溃问题
        Browser.sendString(this, url)
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun Tile.updateState(state: Int) {
    if (this.state != state) {
        this.state = state
        updateTile()
    }
}

fun Context.isWatch() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
    Configuration.UI_MODE_TYPE_WATCH == getSystemService<UiModeManager>()?.currentModeType
} else {
    false
}

fun Context.uiLog(msg: String) {
    Timber.i("UILog -> $msg")
    Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
}

/**
 * @link https://stackoverflow.com/a/59472972/16091156
 */
fun Snackbar.allowInfiniteLines() = apply {
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).isSingleLine = false
}

/**
 * 防止无障碍服务攻击
 * 可以使用adb shell uiautomator dump验证效果
 */
fun View.emptyAccessibilityDelegate() = run {
    accessibilityDelegate = object : View.AccessibilityDelegate() {
        override fun addExtraDataToAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfo,
            extraDataKey: String,
            arguments: Bundle?
        ) {
        }

        override fun dispatchPopulateAccessibilityEvent(
            host: View?,
            event: AccessibilityEvent?
        ) = true

        override fun getAccessibilityNodeProvider(host: View?) = null
        override fun onInitializeAccessibilityEvent(host: View?, event: AccessibilityEvent?) {}
        override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {}
        override fun onPopulateAccessibilityEvent(host: View?, event: AccessibilityEvent?) {}
        override fun onRequestSendAccessibilityEvent(
            host: ViewGroup?,
            child: View?,
            event: AccessibilityEvent?
        ) = false

        override fun performAccessibilityAction(host: View?, action: Int, args: Bundle?) = true
        override fun sendAccessibilityEvent(host: View?, eventType: Int) {}
        override fun sendAccessibilityEventUnchecked(host: View?, event: AccessibilityEvent?) {}
    }
}

fun nox() = AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)