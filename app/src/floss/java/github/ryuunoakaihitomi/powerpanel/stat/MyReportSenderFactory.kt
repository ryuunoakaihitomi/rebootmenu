package github.ryuunoakaihitomi.powerpanel.stat

import android.content.Context
import com.google.auto.service.AutoService
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory
import timber.log.Timber
import java.io.File
import java.util.*

@Suppress("unused")
@AutoService(ReportSenderFactory::class)
class MyReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return object : ReportSender {
            override fun send(context: Context, errorContent: CrashReportData) {
                Timber.e("CRASH!")
                File("${context.externalCacheDir}/CrashReport/${Date().time}.txt").run {
                    parentFile?.mkdirs()
                    writeText(errorContent.toJSON())
                }
            }
        }
    }
}