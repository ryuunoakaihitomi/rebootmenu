package github.ryuunoakaihitomi.powerpanel.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import github.ryuunoakaihitomi.powerpanel.databinding.LayoutDonateBinding
import github.ryuunoakaihitomi.powerpanel.util.openUrlInBrowser
import timber.log.Timber

class DonateActivity : AppCompatActivity() {

    private lateinit var binding: LayoutDonateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutDonateBinding.inflate(layoutInflater)
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
                    val text = QRCodeReader().decode(binaryBitmap).text
                    openUrlInBrowser(text)
                }.onFailure { Timber.e(it) }
            }
        }
    }
}