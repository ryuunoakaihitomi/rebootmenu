package xp.hook

import com.github.kyuubiran.ezxhelper.utils.Log
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import github.ryuunoakaihitomi.powerpanel.R
import xp.common.KEY_RENAME_LOCK_SCREEN_ITEM
import xp.common.pref

object RenameLockScreenPrivilegedHook : IXposedHookInitPackageResources {
    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam) {
        if (pref.getBoolean(KEY_RENAME_LOCK_SCREEN_ITEM, false)) {
            Log.d("RenameLockScreenPrivilegedHook")
            resparam.res.run {
                setReplacement(
                    R.string.func_lock_screen_privileged,
                    getString(R.string.func_lock_screen)
                )
            }
        }
    }
}