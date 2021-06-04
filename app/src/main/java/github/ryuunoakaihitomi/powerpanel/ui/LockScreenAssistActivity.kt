package github.ryuunoakaihitomi.powerpanel.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.topjohnwu.superuser.Shell
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.desc.PowerExecution

class LockScreenAssistActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Intent.ACTION_ASSIST != intent.action) {
            finish()
            return
        }
        PowerExecution.execute(
            this,
            if (Shell.rootAccess()) R.string.func_lock_screen_privileged else R.string.func_lock_screen,
            false
        )
    }
}