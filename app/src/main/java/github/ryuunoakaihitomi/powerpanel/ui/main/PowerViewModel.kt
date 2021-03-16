package github.ryuunoakaihitomi.powerpanel.ui.main

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.set
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.powerpanel.MyApplication
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.desc.PowerInfo
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PowerViewModel : AndroidViewModel(BlackMagic.getGlobalApp()) {

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

    /* 观察对象，提供界面资源 */
    val infoArray: LiveData<Array<PowerInfo>>
        get() = _infoArray
    private var _infoArray = MutableLiveData<Array<PowerInfo>>()
    val shortcutInfoArray: LiveData<Array<PowerInfo>>
        get() = _shortcutInfoArray
    private var _shortcutInfoArray = MutableLiveData<Array<PowerInfo>>()

    /* 观察对象，执行回调 */
    val labelResId: LiveData<Int>
        get() = _labelResId
    private var _labelResId = MutableLiveData<Int>()

    // 用来显示对话框
    fun prepare() {
        GlobalScope.launch(Dispatchers.IO) {
            val isRoot = Shell.rootAccess() // 在这里提供root状态
            viewModelScope.launch {
                _rootMode.value = isRoot
                forceMode.value = false
                changeInfo()
            }
        }
    }

    // 用来执行操作
    fun call(@StringRes labelResId: Int) {
        _labelResId.value = labelResId
    }

    fun reverseForceMode() {
        forceMode.value = getForceMode().not()
        if (forceMode.value == true) {
            Toasty.warning(app(), R.string.toast_switch_to_force_mode).show()
        } else {
            Toasty.normal(app(), R.string.toast_switch_to_privileged_mode).show()
        }
        changeInfo()
    }

    private fun changeInfo() {

        val lockScreen = provide(R.string.func_lock_screen)
        val showSysPowerDialog = provide(R.string.func_sys_pwr_menu)
        val reboot = provide(R.string.func_reboot)
        val shutdown = provide(R.string.func_shutdown)
        val recovery = provide(R.string.func_recovery)
        val bootloader = provide(R.string.func_bootloader)
        val softReboot = provide(R.string.func_soft_reboot)
        val restartSysUi = provide(R.string.func_restart_sys_ui)
        val safeMode = provide(R.string.func_safe_mode)
        val lockScreenPrivileged = provide(R.string.func_lock_screen_privileged)

        /* 这里定义了各个选项的顺序，这个顺序已经经过反复的试验，一般不需要更改 */
        val restrictedActions = arrayOf(lockScreen, showSysPowerDialog)
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

        val rawTitle = app().getString(R.string.app_name)
        if (rootMode.value == true) {
            _title.value = rawTitle
            _infoArray.value = privilegedActions
            // https://developer.android.google.cn/guide/topics/ui/shortcuts?hl=en#shortcut-limitations
            // Although you can publish up to five shortcuts (static and dynamic shortcuts combined) at a time for your app, most launchers can only display four.
            _shortcutInfoArray.value = privilegedActions.copyOfRange(0, 4)
        } else {
            _title.value = String.format(
                "%s %s",
                rawTitle,
                app().getString(R.string.title_dialog_restricted_mode)
            )
            _infoArray.value = restrictedActions
            _shortcutInfoArray.value = restrictedActions
        }
    }

    //<editor-fold desc="定义电源信息">
    private fun provide(@StringRes labelResId: Int): PowerInfo {
        val powerInfo = PowerInfo()
        return when (labelResId) {
            R.string.func_lock_screen -> {
                powerInfo.apply { iconResId = R.drawable.ic_baseline_lock_24 }
            }
            R.string.func_sys_pwr_menu -> {
                powerInfo.apply { iconResId = R.drawable.ic_baseline_menu_24 }
            }
            R.string.func_reboot -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_settings_backup_restore_24
                }
            }
            R.string.func_shutdown -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_mobile_off_24
                }
            }
            R.string.func_recovery -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_restore_page_24
                }
            }
            R.string.func_bootloader -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_system_update_24
                }
            }
            R.string.func_soft_reboot -> {
                powerInfo.apply { iconResId = R.drawable.ic_baseline_power_24 }
            }
            R.string.func_restart_sys_ui -> {
                powerInfo.apply { iconResId = R.drawable.ic_baseline_aspect_ratio_24 }
            }
            R.string.func_safe_mode -> {
                powerInfo.apply {
                    hasForceMode = true
                    iconResId = R.drawable.ic_baseline_android_24
                }
            }
            R.string.func_lock_screen_privileged -> {
                powerInfo.apply { iconResId = R.drawable.ic_baseline_lock_24 }
            }
            else -> powerInfo
        }.apply {
            label = colorForceLabel(app().getString(labelResId), this)
            this.labelResId = labelResId
        }
    }
    //</editor-fold>

    private fun app() = getApplication<MyApplication>()

    fun getForceMode() = forceMode.value ?: false

    fun isOnForceMode(info: PowerInfo) = getForceMode() and info.hasForceMode

    /* 如果为特权模式且不为锁屏，再次确认 */
    fun shouldConfirmAgain(item: PowerInfo) =
        rootMode.value == true and !getForceMode() and (item.labelResId != R.string.func_lock_screen_privileged)

    private fun colorForceLabel(label: String, info: PowerInfo): SpannableString {
        val forceLabel = SpannableString(label)
        if (isOnForceMode(info)) {
            val range = 0..forceLabel.length
            forceLabel[range] = ForegroundColorSpan(
                ResourcesCompat.getColor(app().resources, R.color.colorForceModeItem, null)
            )
            forceLabel[range] = StyleSpan(Typeface.BOLD)
        }
        return forceLabel
    }
}