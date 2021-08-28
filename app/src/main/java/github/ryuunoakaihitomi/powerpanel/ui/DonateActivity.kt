package github.ryuunoakaihitomi.powerpanel.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import github.ryuunoakaihitomi.powerpanel.R
import github.ryuunoakaihitomi.powerpanel.databinding.ActivityDonateBinding
import github.ryuunoakaihitomi.powerpanel.util.isWatch
import github.ryuunoakaihitomi.powerpanel.util.openUrlInBrowser
import timber.log.Timber

class DonateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDonateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDonateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivDonate.run {
            val bitmap = BitmapFactory.decodeStream(assets.open("donate.webp"))
            setImageBitmap(bitmap)
            setOnClickListener {
                /* 二维码解码 */
                val px = IntArray(bitmap.width * bitmap.height)
                bitmap.getPixels(px, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val source = RGBLuminanceSource(bitmap.width, bitmap.height, px)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                runCatching {
                    val url = QRCodeReader().decode(binaryBitmap).text
                    openUrlInBrowser(url)
                }.onFailure { Timber.e(it) }
            }
        }
        if (!isWatch()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            Snackbar.make(binding.root, R.string.snack_donate_guide, Snackbar.LENGTH_LONG)
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        transientBottomBar?.removeCallback(this)
                    }
                })
                .show()
        }
    }
}