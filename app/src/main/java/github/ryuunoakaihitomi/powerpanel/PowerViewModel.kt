package github.ryuunoakaihitomi.powerpanel

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.set
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.poweract.PowerActX
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PowerViewModel : ViewModel() {

    /* Root模式：分隔开受限模式 */
    val rootMode: LiveData<Boolean>
        get() = _rootMode
    private var _rootMode = MutableLiveData<Boolean>()

    /* 强制模式：分隔开特权模式 */
    private var forceMode = MutableLiveData<Boolean>()

    /* 标题：受限模式会提示用户 */
    val title: LiveData<String>
        get() = _title
    private var _title = MutableLiveData<String>()

    /* 观察对象，提供界面资源和操作接口 */
    val infoArray: LiveData<Array<PowerInfo>>
        get() = _infoArray
    private var _infoArray = MutableLiveData<Array<PowerInfo>>()

    fun goto(activity: AppCompatActivity) {
        GlobalScope.launch(Dispatchers.IO) {
            val isRoot = Shell.rootAccess() // 在这里提供root状态
            viewModelScope.launch {
                _rootMode.value = isRoot
                forceMode.value = false
                changeInfo(activity)
            }
        }
    }

    fun reverseForceMode(activity: AppCompatActivity) {
        forceMode.value = getForceMode().not()
        if (forceMode.value == true) {
            Toasty.warning(
                activity,
                R.string.toast_switch_to_force_mode
            ).show()
        } else {
            Toasty.normal(
                activity,
                R.string.toast_switch_to_privileged_mode
            ).show()
        }
        changeInfo(activity)
    }

    private fun changeInfo(activity: AppCompatActivity) {
        val lockScreen = provide(R.string.func_lock_screen, activity)
        val showSysPowerDialog = provide(R.string.func_sys_pwr_menu, activity)
        val reboot = provide(R.string.func_reboot, activity)
        val shutdown = provide(R.string.func_shutdown, activity)
        val recovery = provide(R.string.func_recovery, activity)
        val bootloader = provide(R.string.func_bootloader, activity)
        val softReboot = provide(R.string.func_soft_reboot, activity)
        val restartSysUi = provide(R.string.func_restart_sys_ui, activity)
        val safeMode = provide(R.string.func_safe_mode, activity)
        val lockScreenPrivileged = provide(R.string.func_lock_screen_privileged, activity)

        /* 这里定义了各个选项的顺序，这个顺序已经经过反复的试验，一般不需要更改 */
        val normalActions = arrayOf(lockScreen, showSysPowerDialog)
        val privilegedActions = arrayOf(
            reboot,
            shutdown,
            recovery,
            bootloader,
            softReboot,
            restartSysUi,
            safeMode,
            lockScreenPrivileged
        )

        val rawTitle = activity.title.toString()
        if (rootMode.value == true) {
            _title.value = rawTitle
            _infoArray.value = privilegedActions
        } else {
            _title.value = String.format(
                "%s %s",
                rawTitle,
                MyApplication.getInstance()
                    .getString(R.string.title_dialog_restricted_mode)
            )
            _infoArray.value = normalActions
        }
    }

    private fun provide(@StringRes labelResId: Int, activity: AppCompatActivity): PowerInfo {
        val callback = getGlobalCallback(activity)

        //<editor-fold desc="定义电源信息">
        val powerInfo = PowerInfo()
        return when (labelResId) {
            R.string.func_lock_screen -> {
                powerInfo.apply {
                    iconResId = R.drawable.ic_baseline_lock_24
                    execution = Runnable { lockScreenWithTip(activity, callback) }
                }
            }
            R.string.func_sys_pwr_menu -> {
                powerInfo.apply {
                    iconResId = R.drawable.ic_baseline_menu_24
                    execution = Runnable { showPowerDialogWithTip(activity, callback) }
                }
            }
            R.string.func_reboot -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_settings_backup_restore_24
                    execution = Runnable { PowerActX.reboot(callback, getForceMode()) }
                }
            }
            R.string.func_shutdown -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_mobile_off_24
                    execution = Runnable { PowerActX.shutdown(callback, getForceMode()) }
                }
            }
            R.string.func_recovery -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_restore_page_24
                    execution = Runnable { PowerActX.recovery(callback, getForceMode()) }
                }
            }
            R.string.func_bootloader -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_system_update_24
                    execution = Runnable { PowerActX.bootloader(callback, getForceMode()) }
                }
            }
            R.string.func_soft_reboot -> {
                powerInfo.apply {
                    iconResId = R.drawable.ic_baseline_power_24
                    execution = Runnable { PowerActX.softReboot(callback) }
                }
            }
            R.string.func_restart_sys_ui -> {
                powerInfo.apply {
                    iconResId = R.drawable.ic_baseline_aspect_ratio_24
                    execution = Runnable {
                        restartSysUi()
                        callback.done()
                    }
                }
            }
            R.string.func_safe_mode -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_android_24
                    execution = Runnable { PowerActX.safeMode(callback, getForceMode()) }
                }
            }
            R.string.func_lock_screen_privileged -> {
                powerInfo.apply {
                    iconResId = R.drawable.ic_baseline_lock_24
                    execution = Runnable { PowerActX.lockScreen(callback) }
                }
            }
            else -> powerInfo
        }.apply {
            label = colorForceLabel(activity.getString(labelResId), this)
            this.labelResId = labelResId
        }
        //</editor-fold>
    }

    fun getForceMode() = forceMode.value ?: false

    fun isOnForceMode(info: PowerInfo) = getForceMode() and info.hasForceMode

    private fun colorForceLabel(label: String, info: PowerInfo): SpannableString {
        val forceLabel = SpannableString(label)
        if (isOnForceMode(info)) {
            val range = 0..forceLabel.length
            forceLabel[range] = ForegroundColorSpan(
                ResourcesCompat.getColor(
                    MyApplication.getInstance().resources,
                    R.color.colorForceModeItem,
                    null
                )
            )
            forceLabel[range] = StyleSpan(Typeface.BOLD)
        }
        return forceLabel
    }
}