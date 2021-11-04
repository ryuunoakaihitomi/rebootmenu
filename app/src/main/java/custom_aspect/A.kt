package custom_aspect

import android.content.ActivityNotFoundException
import android.view.View
import com.drakeet.about.ClickableViewHolder
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect

@Suppress("unused", "SpellCheckingInspection")
@Aspect
class A {

    /**
     * 修复about-page在Wear OS上的崩溃
     * 原因：在WearOS上打开链接会抛出不为[ActivityNotFoundException]的异常
     *
     * `Permission Denial: starting Intent { act=... dat=... cmp=com.google.android.wearable.app/com.google.android.clockwork.wcs.remoteintent.UriRedirectActivity } from ProcessRecord ... requires com.google.android.wearable.READ_SETTINGS`
     * 后记：似乎在Android 11 Wear OS中得以修复，不再处理链接
     * @see ClickableViewHolder
     */
    @Around("call(* setOnClickListener(..)) && this(com.drakeet.about.ClickableViewHolder)")
    fun fixAboutPageCrashOnWatch(pjp: ProceedingJoinPoint) {
        val rawListener = pjp.args[0] as View.OnClickListener
        pjp.proceed(arrayOf(OnClickListenerSafeProxy(rawListener)))
    }

    private class OnClickListenerSafeProxy(private val l: View.OnClickListener) :
        View.OnClickListener {
        override fun onClick(v: View?) {
            runCatching { l.onClick(v) }.onFailure { it.printStackTrace() }
        }
    }
}