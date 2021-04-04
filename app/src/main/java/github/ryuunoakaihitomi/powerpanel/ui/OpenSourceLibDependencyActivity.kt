package github.ryuunoakaihitomi.powerpanel.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Category
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import com.drakeet.about.License as L
import com.drakeet.about.License.APACHE_2 as AL2

class OpenSourceLibDependencyActivity : AbsAboutActivity() {

    private val platformSupportList = listOf(
        /**
        本应用程序作为在Android平台及其相关支持组件下开发的产物，
        受到AOSP协议的约束。（目前将Jetpack也算入AOSP中）
         */
        L(
            "Android Open Source Project", "Google LLC",
            strOf(R.string.url_aosp_license),
            strOf(R.string.url_aosp_home)
        ),
        L(
            "CyanogenMod Platform SDK",
            "CyanogenMod",
            AL2,
            "https://github.com/CyanogenMod/cm_platform_sdk"
        ),
    )

    /* 在发布产品中包含的库 */
    @Suppress("SpellCheckingInspection")
    private val libraryList = listOf(
        L("PowerAct", "ZQY", AL2, "https://github.com/ryuunoakaihitomi/PowerAct"),
        L("libsu", "John Wu", AL2, "https://github.com/topjohnwu/libsu"),
        L("Shizuku", "Rikka", AL2, "https://shizuku.rikka.app"),
        L("ReToast", "ZQY", AL2, "https://github.com/ryuunoakaihitomi/ReToast"),
        L("Toasty", "GrenderG", "GNU LGPL v3", "https://github.com/GrenderG/Toasty"),
        L("Markwon", "Dimitry Ivanov", AL2, "https://noties.io/Markwon"),
        L("about-page", "Drakeet", AL2, "https://github.com/PureWriter/about-page"),
        L("Commons IO", "Apache", AL2, "http://commons.apache.org/proper/commons-io"),
        L("Timber", "Jake Wharton", AL2, "https://github.com/JakeWharton/timber"),
        L("ZXing", "Google LLC", AL2, "https://github.com/zxing/zxing"),
    )

    /* debug用，发布产品中排除的库 */
    @Suppress("SpellCheckingInspection")
    private val debugLibraryList = listOf(
        L("LeakCanary", "Square, Inc.", AL2, "https://square.github.io/leakcanary"),
        L("Pandora", "linjiang", AL2, "https://github.com/whataa/pandora"),
    )

    /* 使用的Gradle插件 */
    @Suppress("SpellCheckingInspection")
    private val gradlePluginList = listOf(
        L("AndResGuard", "shwenzhang", AL2, "https://github.com/shwenzhang/AndResGuard")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        val badge = ContextCompat.getDrawable(this, android.R.drawable.ic_lock_power_off)
        badge?.setTint(ResourcesCompat.getColor(resources, R.color.colorIconBackground, null))
        icon.setImageDrawable(badge)
        icon.setOnLongClickListener {
            Toast.makeText(this, BuildConfig.BUILD_TIME, Toast.LENGTH_LONG).show()
            true
        }
        slogan.visibility = View.GONE
        version.text = BuildConfig.VERSION_NAME
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.add(Category("Powered by…"))
        platformSupportList.all { items.add(it) }
        items.add(Category("implementation"))
        libraryList.forEach { items.add(it) }
        items.add(Category("debugImplementation"))
        debugLibraryList.forEach { items.add(it) }
        items.add(Category("Gradle plugin"))
        gradlePluginList.forEach { items.add(it) }
    }

//    /**
//     * 在Android 12 DP中似乎无法自动[finish]掉，[DonateActivity]亦如是。
//     */
//    override fun onBackPressed() {
//        Timber.d("onBackPressed")
//        super.onBackPressed()
//        finish()
//    }
}

private fun strOf(@StringRes id: Int) = BlackMagic.getGlobalApp().getString(id)