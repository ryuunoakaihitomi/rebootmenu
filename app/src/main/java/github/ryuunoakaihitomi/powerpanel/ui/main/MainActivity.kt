package github.ryuunoakaihitomi.powerpanel.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutManager
import android.content.res.Resources
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.TypefaceSpan
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.backgroundColor
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.text.scale
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.desc.PowerExecution
import github.ryuunoakaihitomi.powerpanel.desc.getIconResIdArray
import github.ryuunoakaihitomi.powerpanel.desc.getLabelArray
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.ui.DonateActivity
import github.ryuunoakaihitomi.powerpanel.ui.OpenSourceLibDependencyActivity
import github.ryuunoakaihitomi.powerpanel.ui.Osld4WearActivity
import github.ryuunoakaihitomi.powerpanel.ui.ShortcutActivity
import github.ryuunoakaihitomi.powerpanel.ui.tile.CmCustomTile
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import github.ryuunoakaihitomi.powerpanel.util.CC
import github.ryuunoakaihitomi.powerpanel.util.PROJECT_URL
import github.ryuunoakaihitomi.powerpanel.util.PowerActHelper
import github.ryuunoakaihitomi.powerpanel.util.RC
import github.ryuunoakaihitomi.powerpanel.util.emptyAccessibilityDelegate
import github.ryuunoakaihitomi.powerpanel.util.isCrackDroidEnv
import github.ryuunoakaihitomi.powerpanel.util.isWatch
import github.ryuunoakaihitomi.powerpanel.util.openUrlInBrowser
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImagesPlugin
import org.apache.commons.io.IOUtils
import rikka.compatibility.DeviceCompatibility
import rikka.shizuku.Shizuku
import rikka.sui.Sui
import timber.log.Timber
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("start!")
        checkUnsupportedEnv()
        var mainDialog = AlertDialog.Builder(this).create()
        val powerViewModel = ViewModelProvider(this)[PowerViewModel::class.java]
        powerViewModel.labelResId.observe(this) {
            PowerExecution.execute(this, it, powerViewModel.getForceMode())
        }
        powerViewModel.shortcutInfoArray.observe(this) { it ->
            // 小天才Z6巅峰版把ShortcutManager阉割了
            if (VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && getSystemService<ShortcutManager>() == null) {
                Timber.w("ShortcutMgr == null")
                return@observe
            }
            ShortcutManagerCompat.removeAllDynamicShortcuts(this)
            val maxCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(this)
            if (maxCount >= it.size) {
                Timber.d("Allow app shortcut. ${it.size} in $maxCount")
                it.forEach {
                    val unspannedLabel = it.label.toString()
                    val shortcut = ShortcutInfoCompat.Builder(this, unspannedLabel).run {
                        setShortLabel(unspannedLabel)
                        setIcon(IconCompat.createWithResource(applicationContext, it.iconResId))
                        setIntent(ShortcutActivity.getActionIntent(it.labelResId))
                        build()
                    }
                    ShortcutManagerCompat.addDynamicShortcuts(this, listOf(shortcut))
                }
            }
        }
        powerViewModel.forceMode.observe(this) {
            val isAtLeastS = VERSION.SDK_INT >= Build.VERSION_CODES.S
            val rawTitle = getString(R.string.app_name)
            if (it == true) {
                if (isAtLeastS) {
                    powerViewModel.setTitle(SpannableStringBuilder().apply {
                        append(rawTitle, " ", buildSpannedString {
                            color(CC.getColor(application, R.color.colorForceModeItem)) {
                                scale(0.6f) {
                                    bold {
                                        append(getString(R.string.title_dialog_force_mode))
                                    }
                                }
                            }
                        })
                    })
                } else {
                    Toasty.warning(application, R.string.toast_switch_to_force_mode).show()
                }
            } else {
                if (isAtLeastS) {
                    powerViewModel.setTitle(rawTitle)
                } else {
                    Toasty.normal(application, R.string.toast_switch_to_privileged_mode).show()
                }
            }
        }
        powerViewModel.infoArray.observe(this) {
            val rootMode = powerViewModel.rootMode.value ?: false
            // 特权模式下（或者发现 Sui 时）主动请求 Shizuku 授权
            // 在受限模式下 PowerAct 已经处理好这个步骤
            if (VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                (rootMode || Sui.isSui()) && Shizuku.pingBinder() &&
                Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED
            ) {
                Timber.i("Request shizuku permission.")
                Shizuku.requestPermission(0)
                Shizuku.addRequestPermissionResultListener(object :
                    Shizuku.OnRequestPermissionResultListener {
                    override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            Timber.d("refresh items for showSysPowerDialog")
                            mainDialog.dismiss()
                            powerViewModel.prepare()
                        }
                        Shizuku.removeRequestPermissionResultListener(this)
                    }
                })
            }
            PowerActHelper.toggleExposedComponents(application, !rootMode)   // 为CMTile提前更新外置组件可见性
            CmCustomTile.start()
            mainDialog = AlertDialog.Builder(this).apply {
                setTitle(powerViewModel.title.value)
                setAdapter(
                    PowerItemAdapter(this@MainActivity, it.getLabelArray(), it.getIconResIdArray())
                ) { dialog, which ->
                    val item = it[which]
                    if (powerViewModel.shouldConfirmAgain(item)) {
                        setTitle(
                            buildSpannedString {
                                val s =
                                    getString(R.string.title_dialog_confirm_op).format(item.label)
                                val cutPos = s.length - item.label.length - 2    // 包含两个引号
                                append(s.subSequence(0, cutPos))
                                backgroundColor(
                                    CC.getColor(this@MainActivity, R.color.colorConfirmOpBackground)
                                ) {
                                    color(
                                        CC.getColor(this@MainActivity, R.color.colorConfirmOpText)
                                    ) {
                                        append(s.substring(cutPos))
                                    }
                                }
                            }
                        )
                        val confirmListener = DialogInterface.OnClickListener { _, confirmWhich ->
                            val ok = confirmWhich == 0
                            Statistics.logDialogCancel(context, item.labelResId, ok.not())
                            if (ok) {
                                powerViewModel.call(item.labelResId)
                                // dismiss防止窗口泄漏
                                dialog.dismiss()
                            } else {
                                powerViewModel.prepare()
                            }
                        }
                        // 修复本应用在CrackDroid项目上二次确认的“确认/取消”选项无法显示的问题，我恰好有台吃灰的558泡面盖可以刷机测试
                        // 参考资料看isCrackDroidEnv的文档
                        if (isCrackDroidEnv) {
                            setAdapter(
                                PowerItemAdapter(
                                    context,
                                    arrayOf(
                                        resources.getText(android.R.string.ok).emphasize(),
                                        resources.getText(android.R.string.cancel).emphasize(),
                                    ),
                                    arrayOf(
                                        R.drawable.ic_baseline_check_24,
                                        R.drawable.ic_baseline_close_24,
                                    )
                                ), confirmListener
                            )
                        } else {
                            setAdapter(null, null)
                            setItems(
                                arrayOf(
                                    resources.getText(android.R.string.ok).emphasize(),
                                    resources.getText(android.R.string.cancel).emphasize()
                                ), confirmListener
                            )
                        }
                        setOnCancelListener {
                            Statistics.logDialogCancel(this.context, item.labelResId, true)
                            powerViewModel.prepare()
                        }
                        setNeutralButton(null, null)
                        setPositiveButton(null, null)
                        show().listView.rootView.emptyAccessibilityDelegate()
                    } else {
                        powerViewModel.call(item.labelResId)
                        dialog.dismiss()
                    }
                }
                if (rootMode) {
                    setNeutralButton(R.string.btn_dialog_switch_mode) { _, _ -> powerViewModel.reverseForceMode() }
                }
                setPositiveButton(R.string.btn_dialog_help) { _, _ ->
                    /* --- 帮助界面逻辑 ---*/
                    setTitle(R.string.title_dialog_help_info)
                    // 准备使用下面的MarkDown组件
                    setMessage("placeholder")
                    setPositiveButton(R.string.btn_dialog_source_code) { _, _ ->
                        openUrlInBrowser(PROJECT_URL)
                        finish()
                    }
                    setNegativeButton(R.string.donate) { _, _ -> teleport<DonateActivity>() }
                    setNeutralButton(R.string.open_source_lib_dependency) { _, _ ->
                        if (isWatch()) teleport<Osld4WearActivity>() else teleport<OpenSourceLibDependencyActivity>()
                    }
                    setOnCancelListener { powerViewModel.prepare() }
                    /* 主要信息 */
                    markwon().setMarkdown(
                        BlackMagic.getAlertDialogMessageView(show()),
                        IOUtils.toString(
                            resources.openRawResource(R.raw.help),
                            Charset.defaultCharset()
                        )
                    )
                    // 不能设置为可选择，否则无法点击打开链接
                    //alertDialogMessageView.setTextIsSelectable(true)
                    Toasty.normal(
                        this@MainActivity,
                        """
                            ${BuildConfig.VERSION_NAME}
                            ${BuildConfig.VERSION_CODE}
                        """.trimIndent(),
                        Toasty.LENGTH_LONG
                    ).show()
                }
                setOnCancelListener { finish() }
            }.show()
            val lv = mainDialog.listView
            lv.onItemLongClickListener = AdapterView.OnItemLongClickListener { _, _, position, _ ->
                // 和上面那个用到ShortcutManagerCompat的例子不同，requestPinShortcut()从26开始才直接调用
                if (VERSION.SDK_INT >= Build.VERSION_CODES.O && getSystemService<ShortcutManager>() == null) {
                    return@OnItemLongClickListener false
                }
                val item = it[position]
                val drawable = RC.getDrawable(resources, item.iconResId, null)?.let { d ->
                    DrawableCompat.wrap(d)
                }
                drawable?.run {
                    // 注意：必须使用mutate()保持Drawable独立性以修复无法变色的Bug
                    mutate().run {
                        // 如果是强制模式，为了便于区分，改变shortcut图标颜色
                        if (powerViewModel.isOnForceMode(item)) {
                            DrawableCompat.setTint(
                                this,
                                RC.getColor(resources, R.color.colorIconForceModeShortcut, null)
                            )
                        }
                        val unspannedLabel = item.label.toString()
                        val shortcut = ShortcutInfoCompat.Builder(
                            applicationContext,
                            // 使用UUID有两种后果：可以重复添加Icon，修复图标无法变色的bug
                            UUID.randomUUID().toString()
                        ).run {
                            setShortLabel(unspannedLabel)
                            setLongLabel(unspannedLabel)
                            setIcon(IconCompat.createWithBitmap(toBitmap()))
                            setIntent(
                                ShortcutActivity.getActionIntent(
                                    item.labelResId,
                                    powerViewModel.getForceMode()
                                )
                            )
                            build()
                        }
                        ShortcutManagerCompat.requestPinShortcut(application, shortcut, null)
                    }
                }
                // 在Android8.0以下和一些自定义系统（自动拒绝）可能没有反馈
                Toasty.info(this, R.string.toast_shortcut_added).show()
                return@OnItemLongClickListener true
            }
            checkScrollableListView(lv)
            if (!rootMode) {
                checkBuiltInSupport(lv)
            }
            mainDialog.window?.run {
                decorView.run {
                    if (!isWatch()) alpha = 0.85f   // 窗口透明度，在手表上不仅没有透明效果还会使内容暗淡并出现奇怪的重影效果
                    emptyAccessibilityDelegate()
                }
                BlackMagic.collapseStatusBarPanels(application)
                // 在初次resume时，dialog还未show。所以无效，还需要在这里调用
                hideSysUi()
                if (VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setHideOverlayWindows(true)
                    // 在不支持模糊背景的环境中回滚暗淡外观
                    if (!BlackMagic.getBooleanSystemProperties("ro.surface_flinger.supports_background_blur")) {
                        // appcompat/appcompat/src/main/res/values/themes_base.xml
                        // frameworks/base/core/res/res/values/themes.xml
                        // backgroundDimAmount
                        attributes = attributes.apply { dimAmount = 0.6f }
                    }
                }
                // 这里不继续在旧版本检测是否有覆盖层，解决方案可能不可用
                // https://stackoverflow.com/questions/63152374/flag-window-is-obscured-not-working-on-newer-android
            }
        }
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            mainDialog.run {
                when (event) {
                    Lifecycle.Event.ON_DESTROY -> dismiss()
                    // 在添加shortcut取消返回时重做
                    Lifecycle.Event.ON_RESUME -> window?.hideSysUi()
                    else -> if (BuildConfig.DEBUG) Timber.d("Unimplemented lifecycle ${event.name}")
                }
            }
        })
        powerViewModel.prepare()
        // 遵照其环境下的系统电源菜单界面，基于以下文章给Android 12之前的MIUI添加背景模糊特效，手表都是黑色背景故不作处理
        // https://www.cnblogs.com/zhucai/p/miui-real-time-blur.html
        if (!isWatch() && DeviceCompatibility.isMiui() && VERSION.SDK_INT < Build.VERSION_CODES.S) {
            setTheme(R.style.MainStyleBlurCompat)
            window.run {
                decorView.alpha = 0f
                addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
            }
        }
    }
}

//region --- private extensions ---
private fun CharSequence.emphasize() = let {
    buildSpannedString { bold { inSpans(TypefaceSpan("monospace")) { append(it) } } }
}

private inline fun <reified T : Activity> Activity.teleport() {
    startActivity(Intent(this, T::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    finish()
}

private fun Context.markwon() = Markwon.builder(this)
    .usePlugin(StrikethroughPlugin.create())
    .usePlugin(ImagesPlugin.create {
        it.errorHandler { url, throwable ->
            throwable.printStackTrace()
            when (url) {
                /**
                 * 为了让md中的图像在Github页面和离线设备都能正常显示。
                 * 在文档中保留网址，不使用file:///android/asset。然后在errorHandler中匹配网址返回特定assets图像资源。
                 */
                "https://shizuku.rikka.app/logo.png" ->
                    RoundedBitmapDrawableFactory.create(resources, assets.open("shizuku_logo.webp"))
                else ->
                    RC.getDrawable(Resources.getSystem(), android.R.drawable.ic_menu_gallery, null)
            }
        }
    })
    // 防止打开链接的崩溃
    .usePlugin(object : AbstractMarkwonPlugin() {
        override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
            builder.linkResolver { _, link -> openUrlInBrowser(link) }
        }
    })
    .build()

/**
 * 隐藏导航栏和状态栏
 */
private fun Window.hideSysUi() = WindowCompat.getInsetsController(this, decorView).run {
    hide(WindowInsetsCompat.Type.systemBars())
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}
//endregion