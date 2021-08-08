package github.ryuunoakaihitomi.powerpanel.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.UserManager
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import androidx.core.text.set
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.MyApplication
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.desc.PowerExecution
import github.ryuunoakaihitomi.powerpanel.desc.getIconResIdArray
import github.ryuunoakaihitomi.powerpanel.desc.getLabelArray
import github.ryuunoakaihitomi.powerpanel.stat.Statistics
import github.ryuunoakaihitomi.powerpanel.ui.DonateActivity
import github.ryuunoakaihitomi.powerpanel.ui.OpenSourceLibDependencyActivity
import github.ryuunoakaihitomi.powerpanel.ui.ShortcutActivity
import github.ryuunoakaihitomi.powerpanel.ui.tile.CmCustomTile
import github.ryuunoakaihitomi.powerpanel.util.*
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.image.ImagesPlugin
import org.apache.commons.io.IOUtils
import rikka.shizuku.Shizuku
import timber.log.Timber
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    private fun compatibilityCheck() {
        val myApp = application as MyApplication
        if (myApp.hasShownCompatibilityWarning) return
        val modeType = getSystemService<UiModeManager>()?.currentModeType
            ?: Configuration.UI_MODE_TYPE_UNDEFINED
        val isCompatible =
            // KitKat无法长期维护，这次只不过是临时接触了这类设备才给稍微适配
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                    // Wear OS存在界面元素无法显示问题
                    modeType != Configuration.UI_MODE_TYPE_WATCH &&
                    // Android TV部分功能无法使用
                    modeType != Configuration.UI_MODE_TYPE_TELEVISION &&
                    // 工作资料之类的
                    getSystemService<UserManager>()!!.userProfiles[0].equals(Process.myUserHandle())
        if (!isCompatible) {
            Timber.i("show unsupported env hint")
            Toasty.error(this, R.string.toast_unsupported_env, Toasty.LENGTH_LONG).show()
            myApp.hasShownCompatibilityWarning = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.v("start!")
        compatibilityCheck()
        var mainDialog = AlertDialog.Builder(this).create()
        val powerViewModel = ViewModelProvider(this)[PowerViewModel::class.java]
        powerViewModel.labelResId.observe(this) {
            PowerExecution.execute(this, it, powerViewModel.getForceMode())
        }
        powerViewModel.shortcutInfoArray.observe(this) { it ->
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
        powerViewModel.infoArray.observe(this) {
            val rootMode = powerViewModel.rootMode.value ?: false
            /* 特权模式下主动请求Shizuku授权，在受限模式下PowerAct已经处理好这个步骤 */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                rootMode && Shizuku.pingBinder() &&
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
                            String.format(getString(R.string.title_dialog_confirm_op), item.label)
                        )
                        setAdapter(null, null)
                        setItems(
                            arrayOf(
                                resources.getText(android.R.string.ok).emphasize(),
                                resources.getText(android.R.string.cancel).emphasize()
                            )
                        ) { _, confirmWhich ->
                            val ok = confirmWhich == 0
                            Statistics.logDialogCancel(item.labelResId, ok.not())
                            if (ok) {
                                powerViewModel.call(item.labelResId)
                                // dismiss防止窗口泄漏
                                dialog.dismiss()
                            } else {
                                powerViewModel.prepare()
                            }
                        }
                        setOnCancelListener {
                            Statistics.logDialogCancel(item.labelResId, true)
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
                        openUrlInBrowser("https://github.com/ryuunoakaihitomi/rebootmenu")
                        finish()
                    }
                    setNegativeButton(R.string.donate) { _, _ -> teleport<DonateActivity>() }
                    setNeutralButton(R.string.open_source_lib_dependency) { _, _ ->
                        teleport<OpenSourceLibDependencyActivity>()
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
            mainDialog.listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { _, _, position, _ ->
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
            mainDialog.window?.run {
                decorView.run {
                    alpha = 0.85f   // 窗口透明度
                    emptyAccessibilityDelegate()
                }
                // 在初次resume时，dialog还未show。所以无效，还需要在这里调用
                hideSysUi()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setHideOverlayWindows(true)
                }
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
    }
}

//region --- private extensions ---
private fun CharSequence.emphasize() = let {
    val spannableString = SpannableString(it)
    val range = 0..it.length
    spannableString[range] = StyleSpan(Typeface.BOLD)
    spannableString[range] = TypefaceSpan("monospace")
    spannableString
}

/**
 * 防止无障碍服务攻击
 * 可以使用adb shell uiautomator dump验证效果
 */
private fun View.emptyAccessibilityDelegate() = run {
    accessibilityDelegate = object : View.AccessibilityDelegate() {
        override fun addExtraDataToAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfo,
            extraDataKey: String,
            arguments: Bundle?
        ) {
        }

        override fun dispatchPopulateAccessibilityEvent(
            host: View?,
            event: AccessibilityEvent?
        ) = true

        override fun getAccessibilityNodeProvider(host: View?) = null
        override fun onInitializeAccessibilityEvent(host: View?, event: AccessibilityEvent?) {}
        override fun onInitializeAccessibilityNodeInfo(host: View?, info: AccessibilityNodeInfo?) {}
        override fun onPopulateAccessibilityEvent(host: View?, event: AccessibilityEvent?) {}
        override fun onRequestSendAccessibilityEvent(
            host: ViewGroup?,
            child: View?,
            event: AccessibilityEvent?
        ) = false

        override fun performAccessibilityAction(host: View?, action: Int, args: Bundle?) = true
        override fun sendAccessibilityEvent(host: View?, eventType: Int) {}
        override fun sendAccessibilityEventUnchecked(host: View?, event: AccessibilityEvent?) {}
    }
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
private fun Window.hideSysUi() = WindowCompat.getInsetsController(this, decorView)?.run {
    hide(WindowInsetsCompat.Type.systemBars())
    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
}
//endregion