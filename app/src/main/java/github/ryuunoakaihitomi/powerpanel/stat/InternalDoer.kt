package github.ryuunoakaihitomi.powerpanel.stat

import android.app.Application
import android.os.Bundle

/**
 * 区分不同变体的统计行为
 * 不得在[Statistics]以外调用它的子类
 */
interface InternalDoer {
    fun initialize(app: Application)
    fun setCustomKey(k: String, v: Any)
    fun logEvent(tag: String, bundle: Bundle)
    fun log(level: Int, tag: String, msg: String) {}
}