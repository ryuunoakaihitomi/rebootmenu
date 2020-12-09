package github.ryuunoakaihitomi.powerpanel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Category
import com.drakeet.about.License
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R

class OpenSourceLibDependencyActivity : AbsAboutActivity() {

    /* 在发布产品中包含的库 */
    @Suppress("SpellCheckingInspection")
    private val libraryList = listOf(
        License(
            "PowerAct", "ZQY", License.APACHE_2,
            "https://github.com/ryuunoakaihitomi/PowerAct"
        ),
        License(
            "libsu", "John Wu", License.APACHE_2,
            "https://github.com/topjohnwu/libsu"
        ),
        License(
            "Shizuku", "Rikka", License.APACHE_2,
            "https://shizuku.rikka.app"
        ),
        License(
            "ReToast", "ZQY", License.APACHE_2,
            "https://github.com/ryuunoakaihitomi/ReToast"
        ),
        License(
            "Toasty", "GrenderG", "GNU LGPL v3",
            "https://github.com/GrenderG/Toasty"
        ),
        License(
            "Markwon", "Dimitry Ivanov", License.APACHE_2,
            "https://noties.io/Markwon"
        ),
        License(
            "about-page", "Drakeet", License.APACHE_2,
            "https://github.com/PureWriter/about-page"
        ),
        License(
            "Apache Commons IO", "the Apache Software Foundation", License.APACHE_2,
            "http://commons.apache.org/proper/commons-io"
        ),
        License(
            "Timber", "Jake Wharton", License.APACHE_2,
            "https://github.com/JakeWharton/timber"
        ),
        License(
            "ZXing", "Google LLC", License.APACHE_2,
            "https://github.com/zxing/zxing"
        ),
        License(
            "Condom", "Oasis Feng", License.APACHE_2,
            "https://github.com/oasisfeng/condom"
        ),
    )

    /* debug用，发布产品中排除的库 */
    @Suppress("SpellCheckingInspection")
    private val debugLibraryList = listOf(
        License(
            "LeakCanary", "Square, Inc.", License.APACHE_2,
            "https://square.github.io/leakcanary"
        ),
        License(
            "Pandora", "linjiang", License.APACHE_2,
            "https://github.com/whataa/pandora"
        ),
    )

    /* 使用的Gradle插件 */
    @Suppress("SpellCheckingInspection")
    private val gradlePluginList = listOf(
        License(
            "AndResGuard", "shwenzhang", License.APACHE_2,
            "https://github.com/shwenzhang/AndResGuard"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        icon.setImageResource(android.R.drawable.ic_lock_power_off)
        // 由此进入调试界面
        icon.setOnLongClickListener {
            startActivity(Intent(this, DebugActivity::class.java))
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
            License(
                "Android Open Source Project", "Google LLC",
                getString(R.string.url_aosp_license),
                getString(R.string.url_aosp_home)
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