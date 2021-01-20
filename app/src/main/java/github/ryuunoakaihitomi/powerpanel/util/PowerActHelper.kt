package github.ryuunoakaihitomi.powerpanel.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import github.ryuunoakaihitomi.poweract.ExternalUtils
import github.ryuunoakaihitomi.poweract.internal.pa.PaReceiver
import github.ryuunoakaihitomi.poweract.internal.pa.PaService

object PowerActHelper {

    /**
     * 关于启用外置组件，由于完全可以在外部环境实现，不考虑在PowerAct中添加接口
     */
    fun toggleExposedComponents(context: Context, enabled: Boolean) {
        if (enabled) {
            PaReceiver::class.java.enable(context)
            PaService::class.java.enable(context)
        } else {
            ExternalUtils.disableExposedComponents(context)
        }
    }

    private fun Class<*>.enable(context: Context) {
        val pm = context.packageManager
        val cn = ComponentName(context, this)
        val def = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
        if (pm.getComponentEnabledSetting(cn) != def)
            pm.setComponentEnabledSetting(cn, def, PackageManager.DONT_KILL_APP)
    }
}