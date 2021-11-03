package github.ryuunoakaihitomi.powerpanel.util

import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Browser
import android.service.quicksettings.Tile
import android.text.Spannable
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.set
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

operator fun Spannable.set(range: IntRange, spans: Array<Any>) {
    for (span in spans) this[range] = span
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