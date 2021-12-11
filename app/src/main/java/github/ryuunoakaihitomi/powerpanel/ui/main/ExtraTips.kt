package github.ryuunoakaihitomi.powerpanel.ui.main

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.os.UserManager
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ListView
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.powerpanel.MyApplication
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.util.allowInfiniteLines
import github.ryuunoakaihitomi.powerpanel.util.isWatch
import rikka.compatibility.DeviceCompatibility
import timber.log.Timber

fun Activity.checkUnsupportedEnv() {
    if (myApp().hasShownUnsupportedEnvWarning) return
    val isCompatible =
        // KitKat无法长期维护，这次只不过是临时接触了这类设备才给稍微适配
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                // Wear OS存在界面元素无法显示问题
                !isWatch() &&
                // Android TV部分功能无法使用
                !packageManager.hasSystemFeature(
                    @Suppress("DEPRECATION")    // PackageManager.FEATURE_TELEVISION
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        PackageManager.FEATURE_LEANBACK else PackageManager.FEATURE_TELEVISION
                ) &&
                // 工作资料之类的
                getSystemService<UserManager>()!!.userProfiles[0].equals(Process.myUserHandle())
    if (!isCompatible) {
        Timber.i("show unsupported env hint")
        Toasty.error(this, R.string.toast_unsupported_env, Toasty.LENGTH_LONG).show()
        myApp().hasShownUnsupportedEnvWarning = true
    }
}

/**
 * 检测ListView是否可滑动，提醒用户可能有一些项目被隐藏（仅提醒一次）
 */
fun Activity.checkScrollableListView(lv: ListView) {
    if (!myApp().hasShownScrollListTip) {
        lv.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
//                    Timber.d("OnGlobalLayoutListener")
                val lastVisibleChild = lv.getChildAt(lv.lastVisiblePosition)
                if (lastVisibleChild != null && lastVisibleChild.bottom > lv.height) {
                    Timber.d("Tip: scrollable listview")
                    Snackbar.make(lv, R.string.toast_list_scrollable, Snackbar.LENGTH_SHORT)
                        .allowInfiniteLines()
                        .show()
                    myApp().hasShownScrollListTip = true
                    // This ViewTreeObserver is not alive, call getViewTreeObserver() again
                    lv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }
}

/**
 * 检测系统以指点用户尽可能使用更稳定的系统内置选项以替代本应用
 */
fun Activity.checkBuiltInSupport(lv: ListView) {
    if (myApp().hasCheckedBuiltInSupport) return
    else myApp().hasCheckedBuiltInSupport = true

    val configKey = "checkBuiltInSupport"
    val maxCheckCount = 3
    var count: Int
    getPreferences(Context.MODE_PRIVATE).run {
        count = getInt(configKey, 0)
        if (count >= maxCheckCount) {
            Timber.d("checkBuiltInSupport() disabled after $count checks.")
            return
        }
        edit().putInt(configKey, count + 1).apply()
    }

    if (DeviceCompatibility.isMiui() &&
        // 这是已经经过测试的版本: Android M 的 MIUI 10
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    ) {
        lv.topSnackBar(R.string.snack_main_migrate_tip_for_miui)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        lv.topSnackBar(R.string.snack_main_migrate_tip_for_a12)
    }
}

private fun View.topSnackBar(@StringRes resId: Int) {
    val snackBar = Snackbar.make(this, resId, Snackbar.LENGTH_LONG)
    val view = snackBar.view
    val layoutParams = view.layoutParams as FrameLayout.LayoutParams
    layoutParams.gravity = Gravity.TOP
    view.layoutParams = layoutParams
    // 改变动画模式使其看上去不突兀
    snackBar.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    snackBar.show()
}

private fun Activity.myApp() = application as MyApplication