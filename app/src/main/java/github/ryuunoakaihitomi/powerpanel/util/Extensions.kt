package github.ryuunoakaihitomi.powerpanel.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Browser
import android.service.quicksettings.Tile
import android.text.Spannable
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.set
import com.google.android.material.R
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import timber.log.Timber

typealias RC = ResourcesCompat

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

operator fun Spannable.set(range: IntRange, spans: Array<Any>) {
    for (span in spans) this[range] = span
}

/**
 * @link https://stackoverflow.com/a/59472972/16091156
 */
fun Snackbar.allowInfiniteLines() = apply {
    view.findViewById<TextView>(R.id.snackbar_text).isSingleLine = false
}