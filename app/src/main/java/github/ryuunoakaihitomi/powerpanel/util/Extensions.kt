package github.ryuunoakaihitomi.powerpanel.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Browser
import android.service.quicksettings.Tile
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import androidx.core.text.set
import es.dmoral.toasty.Toasty
import timber.log.Timber


fun Activity.makeTransparent() {
    window.decorView.alpha = 0f
}

fun Context.openUrlInBrowser(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Timber.e(it)
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

fun CharSequence.emphasize() = let {
    val spannableString = SpannableString(it)
    val range = 0..it.length
    spannableString[range] = StyleSpan(Typeface.BOLD)
    spannableString[range] = TypefaceSpan("monospace")
    spannableString
}

/**
 * 防止无障碍服务攻击
 * 可以使用adb shell uiautomator dump验证效果
 */
fun View.hideFromAccessibilityService() = run {
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

inline fun <reified T : Activity> Activity.teleport() {
    startActivity(Intent(this, T::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    finish()
}