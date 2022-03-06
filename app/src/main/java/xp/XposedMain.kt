package xp

import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import xp.hook.CheckXposedEnabledHook
import xp.hook.DisableDonationHook
import xp.hook.HideListItemHook
import xp.hook.RenameLockScreenPrivilegedHook

@Keep
class XposedMain : IXposedHookLoadPackage, IXposedHookInitPackageResources {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam?.packageName == BuildConfig.APPLICATION_ID) {
            initEzXHelper(lpparam)
            initPkgHook(CheckXposedEnabledHook, HideListItemHook, DisableDonationHook)
        }
    }

    override fun handleInitPackageResources(resparam: XC_InitPackageResources.InitPackageResourcesParam?) {
        if (resparam?.packageName == BuildConfig.APPLICATION_ID) {
            initResHook(resparam, RenameLockScreenPrivilegedHook)
        }
    }
}