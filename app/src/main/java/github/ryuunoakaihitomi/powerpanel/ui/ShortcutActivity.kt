package github.ryuunoakaihitomi.powerpanel.ui

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import github.ryuunoakaihitomi.powerpanel.desc.PowerExecution
import github.ryuunoakaihitomi.powerpanel.util.BlackMagic
import github.ryuunoakaihitomi.powerpanel.util.makeTransparent

class ShortcutActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_LABEL_RES_ID = "extraLabelResId"
        const val EXTRA_FORCE_MODE = "extraForceMode"

        fun getActionIntent(@StringRes labelResId: Int, forceMode: Boolean = false) = run {
            Intent(BlackMagic.getGlobalApp(), ShortcutActivity::class.java).run {
                action = Intent.ACTION_VIEW
                putExtra(EXTRA_LABEL_RES_ID, labelResId)
                putExtra(EXTRA_FORCE_MODE, forceMode)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeTransparent()
        val forceMode = intent.getBooleanExtra(EXTRA_FORCE_MODE, false)
        PowerExecution.execute(
            intent.getIntExtra(EXTRA_LABEL_RES_ID, ResourcesCompat.ID_NULL), this, forceMode
        )
    }
}