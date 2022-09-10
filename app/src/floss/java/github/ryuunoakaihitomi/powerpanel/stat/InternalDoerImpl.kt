package github.ryuunoakaihitomi.powerpanel.stat

import android.app.Application
import android.os.Bundle
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R
import org.acra.ACRA
import org.acra.config.toast
import org.acra.ktx.initAcra
import timber.log.Timber

/**
 * 在floss版中用日志和ACRA替代
 *
 * ACRA作为一种替代选择，毕竟完整的错误报告可能包含隐私信息
 * 不过在一些麻烦的情况下仍有需要错误报告作为补充的可能
 */
object InternalDoerImpl : InternalDoer {

    init {
        ACRA.DEV_LOGGING = BuildConfig.DEBUG
    }

    override fun initialize(app: Application) {
        Timber.i("init: $app")
        app.initAcra {
            buildConfigClass = BuildConfig::class.java
            toast { text = app.getString(R.string.acra_toast_text) }
        }
    }

    override fun setCustomKey(k: String, v: Any) {
        val value = if (v is Array<*>) v.contentToString() else v
        Timber.i("Custom Key: $k, $value")
        // 无需继续用ACRA记录了，ACRA默认的报告中就包含了丰富的环境信息
    }

    override fun logEvent(tag: String, bundle: Bundle) {
        Timber.i("event -> $tag $bundle")
        // https://www.acra.ch/docs/AdvancedUsage#adding-your-own-custom-variables-or-traces-in-crash-reports-breadcrumbs
        ACRA.errorReporter.putCustomData("${System.currentTimeMillis()}, $tag", bundle.toString())
    }
}