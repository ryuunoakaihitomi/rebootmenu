package xp

import android.app.Application
import android.content.Context
import com.github.kyuubiran.ezxhelper.init.EzXHelperInit
import com.github.kyuubiran.ezxhelper.utils.findMethod
import com.github.kyuubiran.ezxhelper.utils.hookAfter
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.callbacks.XC_InitPackageResources
import de.robv.android.xposed.callbacks.XC_LoadPackage

private const val LOG_TAG = "PowerPanel.UiTuner"

fun initEzXHelper(param: XC_LoadPackage.LoadPackageParam) {
    EzXHelperInit.initHandleLoadPackage(param)
    EzXHelperInit.setLogTag(LOG_TAG)
    findMethod(Application::class.java) {
        name == "attach" && parameterTypes.contentEquals(arrayOf(Context::class.java))
    }.hookAfter { EzXHelperInit.initAppContext(it.args[0] as Context) }
}

fun initPkgHook(vararg hooks: Runnable) = hooks.forEach { it.run() }
fun initResHook(
    param: XC_InitPackageResources.InitPackageResourcesParam,
    vararg hooks: IXposedHookInitPackageResources
) = hooks.forEach { it.handleInitPackageResources(param) }