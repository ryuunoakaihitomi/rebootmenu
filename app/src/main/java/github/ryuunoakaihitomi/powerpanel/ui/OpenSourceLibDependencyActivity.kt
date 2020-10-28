package github.ryuunoakaihitomi.powerpanel.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Category
import com.drakeet.about.License
import github.ryuunoakaihitomi.powerpanel.BuildConfig

class OpenSourceLibDependencyActivity : AbsAboutActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        icon.setImageResource(android.R.drawable.ic_lock_power_off)
        slogan.visibility = View.GONE
        version.text = BuildConfig.VERSION_NAME
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        @Suppress("SpellCheckingInspection")
        listOf(
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
                "Toasty", "GrenderG", "GNU Lesser General Public License version 3",
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
        ).forEach { items.add(it) }

        items.add(Category("debugImplementation"))
        items.add(
            License(
                "LeakCanary", "Square, Inc.", License.APACHE_2,
                "https://square.github.io/leakcanary"
            )
        )
    }
}