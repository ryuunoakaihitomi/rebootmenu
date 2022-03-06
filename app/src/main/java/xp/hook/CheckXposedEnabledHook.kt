package xp.hook

import com.github.kyuubiran.ezxhelper.init.InitFields.ezXClassLoader
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.setStaticBooleanField

object CheckXposedEnabledHook : Runnable {
    override fun run() {
        setStaticBooleanField(
            findClass(
                "github.ryuunoakaihitomi.powerpanel.ui.tuner.UiTunerActivity",
                ezXClassLoader
            ), "hookedByXposed", true
        )
    }
}