package github.ryuunoakaihitomi.powerpanel

import android.content.DialogInterface
import android.os.Bundle
import android.widget.AdapterView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import es.dmoral.toasty.Toasty
import io.noties.markwon.Markwon
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {

    companion object {

        // 窗口透明度
        private const val DIALOG_ALPHA = 0.85f

        // 项目链接
        private const val SOURCE_PATH_LINK = "https://github.com/ryuunoakaihitomi/rebootmenu/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeTransparent()
        var mainDialog: AlertDialog = AlertDialog.Builder(this).create()
        val powerViewModel = ViewModelProvider(this)[PowerViewModel::class.java]
        powerViewModel.infoArray.observe(this) {
            mainDialog = AlertDialog.Builder(this).apply {
                setTitle(powerViewModel.title.value)
                setItems(PowerInfo.getLabelArray(it)) { dialog, which ->
                    val item = it[which]
                    /* 如果为特权模式且不为锁屏，再次确认 */
                    if (powerViewModel.rootMode.value == true
                        and !powerViewModel.getForceMode()
                        and (item.labelResId != R.string.func_lock_screen_privileged)
                    ) {
                        setTitle(
                            String.format(
                                getString(R.string.title_dialog_confirm_op),
                                item.label
                            )
                        )
                        setItems(
                            arrayOf(
                                resources.getString(android.R.string.ok),
                                resources.getString(android.R.string.no)
                            )
                        ) { _, confirmWhich ->
                            // OK
                            if (confirmWhich == 0) {
                                item.run()
                                // dismiss防止窗口泄露
                                dialog.dismiss()
                            } else {
                                powerViewModel.goto(this@MainActivity)
                            }
                        }
                        setOnCancelListener { powerViewModel.goto(this@MainActivity) }
                        setNeutralButton(null, null)
                        show()
                    } else {
                        item.run()
                        dialog.dismiss()
                    }
                }
                if (powerViewModel.rootMode.value == true) {
                    setNeutralButton(R.string.btn_dialog_switch_mode) { _, _ ->
                        powerViewModel.reverseForceMode(this@MainActivity)
                    }
                }
                setPositiveButton(R.string.btn_dialog_help) { _, _ ->
                    /* --- 帮助界面逻辑 ---*/
                    setTitle(R.string.title_dialog_help_info)
                    // 准备使用下面的MarkDown组件
                    setMessage("placeholder")
                    setPositiveButton(R.string.btn_dialog_source_code) { _, _ ->
                        openUrlInBrowser(SOURCE_PATH_LINK)
                        finish()
                    }

                    /* 去除操作选项 */
                    setItems(null, null)
                    setNeutralButton(null, null)
                    setOnCancelListener { powerViewModel.goto(this@MainActivity) }
                    /* 主要信息 */
                    val alertDialogMessageView = BlackMagic.getAlertDialogMessageView(show())
                    Markwon.builder(this@MainActivity)
                        .usePlugin(StrikethroughPlugin.create())
                        .build()
                        .setMarkdown(
                            alertDialogMessageView,
                            IOUtils.toString(
                                resources.openRawResource(R.raw.help),
                                Charset.defaultCharset()
                            )
                        )
                    Toasty.info(
                        this@MainActivity,
                        "${BuildConfig.VERSION_NAME}\n${BuildConfig.VERSION_CODE}",
                        Toasty.LENGTH_LONG
                    ).show()
                }
                setOnCancelListener { finish() }
            }.show()
            mainDialog.listView.onItemLongClickListener =
                AdapterView.OnItemLongClickListener { _, _, position, _ ->
                    val item = it[position]
                    ResourcesCompat.getDrawable(resources, item.iconResId, null)?.run {
                        // 注意：必须使用mutate()保持Drawable独立性以修复无法变色的Bug
                        mutate().run {
                            // 如果是强制模式，为了便于区分，改变shortcut图标颜色
                            if (powerViewModel.isOnForceMode(item)) {
                                setTint(
                                    ResourcesCompat.getColor(
                                        resources,
                                        R.color.colorIconForceModeShortcut,
                                        null
                                    )
                                )
                            }
                            addLauncherShortcut(
                                MyApplication.getInstance(),
                                item.label, toBitmap(),
                                ShortcutActivity.getActionIntent(
                                    item.labelResId,
                                    powerViewModel.getForceMode()
                                )
                            )
                        }
                    }
                    return@OnItemLongClickListener true
                }
            // 半透明
            mainDialog.window?.decorView?.alpha = DIALOG_ALPHA
            // 测试：崩溃汇报组件是否正常工作
            mainDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnLongClickListener {
                throw RuntimeException("Test Crash")
            }
        }
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                mainDialog.dismiss()
            }
        })
        powerViewModel.goto(this)
    }
}