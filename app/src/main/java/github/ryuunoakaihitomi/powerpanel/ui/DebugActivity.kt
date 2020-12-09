package github.ryuunoakaihitomi.powerpanel.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.system.Os
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.BackgroundColorSpan
import android.webkit.MimeTypeMap
import androidx.core.text.set
import androidx.documentfile.provider.DocumentFile
import com.topjohnwu.superuser.Shell
import github.ryuunoakaihitomi.powerpanel.databinding.ActivityDebugBinding
import org.apache.commons.io.FilenameUtils
import timber.log.Timber
import java.time.LocalDateTime
import java.util.*

/**
 * 发布产品中的调试活动
 * 给一些偶遇的疑难杂症的治疗提供方便
 */
class DebugActivity : Activity() {

    private lateinit var binding: ActivityDebugBinding

    private var logcat = ""

    companion object {
        private const val REQ_CODE_LOGCAT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preTitle = SpannableString("Only for debug! 本界面仅面向开发人员")
        val r = 0..preTitle.length
        preTitle[r] = BackgroundColorSpan(Color.RED)
        title = preTitle

        // 测试崩溃汇报组件是否正常工作
        binding.btnCrash.setOnClickListener { throw RuntimeException("Test " + LocalDateTime.now()) }
        // 导出自身logcat日志
        binding.btnLogcat.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
            startActivityForResult(intent, REQ_CODE_LOGCAT)
            Shell.sh(
                "logcat -d" +
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            " --pid=${Os.getpid()}"
                        } else {
                            Timber.w("Does not support pid filter.")
                            ""
                        }
            ).submit { logcat = it.out.joinToString(separator = System.lineSeparator()) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK && requestCode == REQ_CODE_LOGCAT) {
            data?.data?.let {
                val fileName = "log-${UUID.randomUUID()}.txt"
                DocumentFile.fromTreeUri(this, it)?.apply {
                    createFile(
                        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            FilenameUtils.getExtension(fileName)
                        ) as String, fileName
                    )
                    findFile(fileName)?.run {
                        contentResolver.openOutputStream(uri)?.run {
                            if (canWrite() && !TextUtils.isEmpty(logcat)) write(logcat.toByteArray())
                            flush()
                            close()
                            logcat = ""
                        }
                    }
                }
            }
        }
    }
}