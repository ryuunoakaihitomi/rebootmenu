package github.ryuunoakaihitomi.powerpanel.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.SystemClock
import androidx.annotation.RequiresApi
import timber.log.Timber
import kotlin.system.exitProcess

class ShutdownReceiver : BroadcastReceiver() {

    companion object {
        /**
         * As of Build.VERSION_CODES#P this broadcast is only sent to receivers registered through Context.registerReceiver.
         */
        @RequiresApi(Build.VERSION_CODES.P)
        fun register(context: Context) =
            context.registerReceiver(ShutdownReceiver(), IntentFilter(Intent.ACTION_SHUTDOWN))
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SHUTDOWN) {
            Timber.d("Shutdown broadcast received. Duration since boot: ${SystemClock.elapsedRealtime()}ms")
            // 偶尔收到Caused by android.os.DeadObjectException的Unable to reach IQSService异常
            // 无法修复，尝试关机时提前自杀阻止上报，但是似乎没有作用，仍然保留作为预期行为
            exitProcess(0)
        }
    }
}