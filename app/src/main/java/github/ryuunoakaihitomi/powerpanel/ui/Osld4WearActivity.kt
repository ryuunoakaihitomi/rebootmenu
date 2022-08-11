package github.ryuunoakaihitomi.powerpanel.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import github.ryuunoakaihitomi.powerpanel.databinding.LayoutSingleImageBinding
import github.ryuunoakaihitomi.powerpanel.util.PROJECT_URL

/**
 * 在Wear OS上的[OpenSourceLibDependencyActivity]
 */
@Suppress("SpellCheckingInspection")
class Osld4WearActivity : AppCompatActivity() {

    private lateinit var binding: LayoutSingleImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutSingleImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivImage.run {
            val imgSize = 360          // 360是AVD中Wear OS设备可选最小尺寸
            val bt = QRCodeWriter().encode(
                "$PROJECT_URL/tree/master/app/src/main/assets/dependency_list",
                BarcodeFormat.QR_CODE,
                imgSize,
                imgSize
            )

            // https://stackoverflow.com/a/30529128
            val w = bt.width
            val h = bt.height
            val pixels = IntArray(w * h)
            for (y in 0 until h) {
                for (x in 0 until w) {
                    pixels[y * w + x] = if (bt.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h)

            setImageBitmap(bitmap)
        }
    }
}