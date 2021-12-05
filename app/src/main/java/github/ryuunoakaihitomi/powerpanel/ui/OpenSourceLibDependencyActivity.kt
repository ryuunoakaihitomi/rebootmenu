package github.ryuunoakaihitomi.powerpanel.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.system.Os
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.doOnTextChanged
import com.drakeet.about.AbsAboutActivity
import com.drakeet.about.Category
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.topjohnwu.superuser.Shell
import es.dmoral.toasty.Toasty
import github.ryuunoakaihitomi.poweract.Callback
import github.ryuunoakaihitomi.poweract.PowerActX
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.util.*
import org.apache.commons.io.IOUtils
import timber.log.Timber
import java.io.IOException
import java.nio.charset.StandardCharsets
import com.drakeet.about.License as L

class OpenSourceLibDependencyActivity : AbsAboutActivity() {

    private val platformSupportList = listOf(
        /**
        本应用程序作为在Android平台及其相关支持组件下开发的产物，
        受到AOSP协议的约束。（目前将Jetpack也算入AOSP中）
         */
        L(
            "Android Open Source Project", "Google LLC",
            strOf(R.string.url_aosp_license), strOf(R.string.url_aosp_home)
        ),

        L(
            "CyanogenMod Platform SDK",
            "CyanogenMod",
            com.drakeet.about.License.APACHE_2,
            "https://github.com/CyanogenMod/cm_platform_sdk"
        ),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        ContextCompat.getDrawable(this, android.R.drawable.ic_lock_power_off)?.run {
            val d = DrawableCompat.wrap(this)
            DrawableCompat.setTint(d, RC.getColor(resources, R.color.colorIconBackground, null))
            icon.setImageDrawable(d)
        }
        icon.setOnClickListener {
            // 不被推荐使用但偶尔需要的功能，因此
            // - 不计入统计
            // - 不在PowerExecution中抽象，直接调用
            // - 不记录在帮助文档中
            val editor = EditText(this)
            editor.hint = getText(R.string.hint_edittext_custom_reboot)
            // 保证hint不为monospace，防止长度超出dialog
            editor.doOnTextChanged { text, _, _, _ ->
                editor.typeface = if (text.isNullOrEmpty()) Typeface.DEFAULT else Typeface.MONOSPACE
            }
            editor.setSingleLine()
            AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_custom_reboot)
                .setIcon(android.R.drawable.stat_sys_warning)
                .setView(editor)
                .setPositiveButton(R.string.func_reboot) { _, _ ->
                    PowerActX.customReboot(editor.text.toString(), object : Callback {
                        override fun done() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                finishAndRemoveTask()
                            } else {
                                finish()
                            }
                        }

                        override fun failed() {
                            Timber.i("Failed CR: ${editor.text}")
                        }
                    })
                }
                .show()
                .window?.decorView?.emptyAccessibilityDelegate()
            Toasty.warning(this, R.string.toast_custom_reboot, Toasty.LENGTH_LONG).show()
        }
        icon.setOnLongClickListener {
            Toast.makeText(this, "とまれかくもあはれ\nほたるほたるおいで", Toast.LENGTH_LONG).show()
            openUrlInBrowser("https://www.nicovideo.jp/watch/sm15408719")
            true
        }
        slogan.visibility = View.GONE
        version.text = BuildConfig.BUILD_TIME
        version.setOnLongClickListener {
            recordLogcat()
            true
        }
    }

    override fun onItemsCreated(items: MutableList<Any>) {
        items.add(Category("Platform Support"))
        platformSupportList.all { items.add(it) }
        inflateJsonData(items, "implementation")
        inflateJsonData(items, "debugImplementation", "debug")
        // 给个位置给Firebase
        inflateJsonData(items, "non-free", "free")
        inflateJsonData(items, "Gradle plugin", "plugin")
    }

    private fun inflateJsonData(l: MutableList<Any>, title: String, ep: String = title) = try {
        val `is` = assets.open("dependency_list/$ep.json")
        l.add(Category(title))
        JsonParser.parseString(IOUtils.toString(`is`, StandardCharsets.UTF_8))
            .asJsonArray.forEach { l.add(Gson().fromJson(it, L::class.java)) }
    } catch (e: IOException) {
        Timber.w(e.toString())
    }

    //<editor-fold desc="抓取logcat">

    /*
     * 一般来说使用错误报告取得在发布后的调试信息，
     * 不过有时可用这个后门快速抓取logcat以便首先获得一部分可在设备上直接查看的蛛丝马迹。
     * （错误报告通常很大以至于在终端设备上加载起来极为卡顿，无法直接查看，只能导入至PC后才能查看）
     */

    private val maxLineCount = 2048

    private val ar = registerForActivityResult(object : ActivityResultContracts.CreateDocument() {
        override fun createIntent(context: Context, input: String): Intent {
            return super.createIntent(context, input).apply { type = "text/plain" }
        }
    }) {
        it?.runCatching {
            val command =
                "logcat -t $maxLineCount${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) " --pid=${Os.getpid()}" else ""}"
            Shell.sh(command).submit { r ->
                contentResolver.openOutputStream(this)?.apply {
                    write(r.out.joinToString(separator = System.lineSeparator()).toByteArray())
                    close()
                    finish()
                }
            }
        }
    }

    private fun recordLogcat() {
        uiLog("Recent $maxLineCount lines Logcat…")
        ar.launch("logcat_${System.currentTimeMillis().toString(Character.MAX_RADIX).uppercase()}")
    }
    //</editor-fold>
}

// 在Activity初始化前可用
private fun strOf(@StringRes id: Int) = BlackMagic.getGlobalApp().getString(id)