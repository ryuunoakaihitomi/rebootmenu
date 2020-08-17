package github.ryuunoakaihitomi.powerpanel

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat

data class PowerInfo(
    var hasForceMode: Boolean = false,
    // 同时还需要转递富文本格式
    var label: CharSequence = "",
    // 由于每个的标签都不一样，可以用作标识符
    @StringRes var labelResId: Int = ResourcesCompat.ID_NULL,
    @DrawableRes var iconResId: Int = ResourcesCompat.ID_NULL,
    var execution: Runnable = Runnable { }
) {

    companion object {
        fun getLabelArray(array: Array<PowerInfo>): Array<CharSequence> {
            val labelList = mutableListOf<CharSequence>()
            array.forEach { labelList.add(it.label) }
            return labelList.toTypedArray()
        }
    }
}

fun PowerInfo.run() = this.execution.run()