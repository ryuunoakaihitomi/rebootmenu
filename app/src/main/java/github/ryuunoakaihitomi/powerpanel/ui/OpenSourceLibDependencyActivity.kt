package github.ryuunoakaihitomi.powerpanel.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Category
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R
import com.drakeet.about.License as L

class OpenSourceLibDependencyActivity : AbsAboutActivity() {

    /* 在发布产品中包含的库 */
    @Suppress("SpellCheckingInspection")
    private val libraryList = listOf(
        L("PowerAct", "ZQY", L.APACHE_2, "https://github.com/ryuunoakaihitomi/PowerAct"),
        L("libsu", "John Wu", L.APACHE_2, "https://github.com/topjohnwu/libsu"),
        L("Shizuku", "Rikka", L.APACHE_2, "https://shizuku.rikka.app"),
        L("ReToast", "ZQY", L.APACHE_2, "https://github.com/ryuunoakaihitomi/ReToast"),
        L("Toasty", "GrenderG", "GNU LGPL v3", "https://github.com/GrenderG/Toasty"),
        L("Markwon", "Dimitry Ivanov", L.APACHE_2, "https://noties.io/Markwon"),
        L("about-page", "Drakeet", L.APACHE_2, "https://github.com/PureWriter/about-page"),
        L("Commons IO", "Apache", L.APACHE_2, "http://commons.apache.org/proper/commons-io"),
        L("Timber", "Jake Wharton", L.APACHE_2, "https://github.com/JakeWharton/timber"),
        L("ZXing", "Google LLC", L.APACHE_2, "https://github.com/zxing/zxing"),
    )

    /* debug用，发布产品中排除的库 */
    @Suppress("SpellCheckingInspection")
    private val debugLibraryList = listOf(
        L("LeakCanary", "Square, Inc.", L.APACHE_2, "https://square.github.io/leakcanary"),
        L("Pandora", "linjiang", L.APACHE_2, "https://github.com/whataa/pandora"),
    )

    /* 使用的Gradle插件 */
    @Suppress("SpellCheckingInspection")
    private val gradlePluginList = listOf(
        L("AndResGuard", "shwenzhang", L.APACHE_2, "https://github.com/shwenzhang/AndResGuard")
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

        /*
        本应用程序作为在Android平台及其相关支持组件下开发的产物，
        受到AOSP协议的约束。（目前将Jetpack也算入AOSP中）
         */
        items.add(Category("Powered by…"))
        items.add(
            L(
                "Android Open Source Project", "Google LLC",
                getString(R.string.url_aosp_license),
                getString(R.string.url_aosp_home)
            )
        )
        // CyanogenMod系统平台支持
        items.add(
            L(
                "CyanogenMod Platform SDK",
                "CyanogenMod",
                L.APACHE_2,
                "https://github.com/CyanogenMod/cm_platform_sdk"
            )
        )

        items.add(Category("implementation"))
        libraryList.forEach { items.add(it) }
        items.add(Category("debugImplementation"))
        debugLibraryList.forEach { items.add(it) }
        items.add(Category("Gradle plugin"))
        gradlePluginList.forEach { items.add(it) }
    }
}