package github.ryuunoakaihitomi.powerpanel.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import es.dmoral.toasty.Toasty


private const val TAG = "Utils"

fun Activity.makeTransparent() {
    this.window.decorView.alpha = 0f
}

fun Context.openUrlInBrowser(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Log.e(TAG, "openUrlInBrowser: ", it)
        Toasty.info(this, url).show()
    }
}