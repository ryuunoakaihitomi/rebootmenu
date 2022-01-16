package github.ryuunoakaihitomi.powerpanel

import android.app.Application
import android.app.UiModeManager
import android.app.WallpaperColors
import android.app.WallpaperManager
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import github.ryuunoakaihitomi.powerpanel.util.nox
import timber.log.Timber

/**
 * 在Android P中根据实验性的“设备主题”设置选项应用暗色主题，**这是为了与系统的电源菜单颜色主题保持一致**
 *
 * TODO: 暂时未进行除了HMD真机设备和AVD以外环境的更多实际测试，如在其他环境下出现兼容性问题，可能需要在这环境下禁用这项功能
 */
@RequiresApi(Build.VERSION_CODES.P)
object DarkThemeForP {

    /**
     * The current device UI theme mode effect SystemUI and Launcher.<br/>
     * <b>Values:</b><br/>
     * 0 - The mode that theme will controlled by wallpaper color.<br/>
     * 1 - The mode that will always light theme.<br/>
     * 2 - The mode that will always dark theme.<br/>
     *
     * @see Settings.Secure
     * @hide
     */
    private const val key = "theme_mode"

    /**
     * 实测只会根据主屏幕变色
     */
    private const val whichPaper = WallpaperManager.FLAG_SYSTEM

    fun main(app: Application) = app.run {
        // 实测AVD中的黑暗主题不会随着设置的“设备主题”选项变更，反而随着省电模式变更了，而且应用也自动更改了主题，无需做进一步处理
        if (getSystemService<UiModeManager>()?.nightMode == UiModeManager.MODE_NIGHT_YES) return
        contentResolver.registerContentObserver(Settings.Secure.getUriFor(key), false,
            object : ContentObserver(Handler(mainLooper)) {
                override fun onChange(selfChange: Boolean) {
                    Timber.d("DT4P: config changed")
                    changeTheme()
                    super.onChange(selfChange)
                }
            })
        changeTheme()
    }

    private fun Application.changeTheme() {
        when (Settings.Secure.getInt(contentResolver, key, -1)) {
            0 -> {
                getSystemService<WallpaperManager>()?.run {
                    if (isWallpaperSupported) {
                        if (getWallpaperColors(whichPaper)?.supportDarkTheme() == true) nox()
                        addOnColorsChangedListener({ colors, which ->
                            Timber.d("DT4P: colors changed. c=$colors w=$which")
                            if (which == whichPaper && colors.supportDarkTheme()) nox()
                        }, Handler(mainLooper))
                    }
                }
            }
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            2 -> nox()
        }
    }

    /**
     * 系统判断变色的机制
     */
    @Suppress("NewApi")
    private fun WallpaperColors.supportDarkTheme() =
        colorHints and WallpaperColors.HINT_SUPPORTS_DARK_THEME != 0
}