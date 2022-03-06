package xp.hook

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.widget.AdapterView
import com.github.kyuubiran.ezxhelper.utils.*
import github.ryuunoakaihitomi.powerpanel.BuildConfig
import org.apache.commons.lang3.ArrayUtils
import xp.common.KEY_REMOVED_ITEM_INDEXES
import xp.common.MAX_ITEM_COUNT
import xp.common.pref

object HideListItemHook : Runnable {

    private var shouldHook = true
    private val blockedIndexes by lazy {
        pref.getStringSet(KEY_REMOVED_ITEM_INDEXES, setOf())!!
            .toList().map { it.toInt() }.sortedDescending()
    }

    private fun mapOpIndex(index: Int) = run {
        val list = MutableList(MAX_ITEM_COUNT) { it }
        blockedIndexes.forEach { list.remove(it) }
        list[index]
    }

    override fun run() {
        if (BuildConfig.DEBUG) {
            blockedIndexes.forEach { Log.d("HideListItemHook: blockedIndexes = $it") }
        }
        if (blockedIndexes.isEmpty()) {
            Log.wx("HideListItemHook: EMPTY blockedIndexes!")
            shouldHook = false
            return
        }
        // 显示
        findConstructor("github.ryuunoakaihitomi.powerpanel.ui.main.PowerItemAdapter") {
            parameterTypes.contentEquals(
                arrayOf(
                    Context::class.java,
                    Array<CharSequence>::class.java,
                    Array<Int>::class.java
                )
            )
        }.hookBefore { param ->
            Log.d("HideListItemHook -> PowerItemAdapter (constructor)")
            var items = param.args[1] as Array<*>
            var iconResId = param.args[2] as Array<*>
            if (items.size == 2) {
                Log.wx("HideListItemHook: Hook in Restricted Mode is DISABLED!")
                shouldHook = false
                return@hookBefore
            }
            blockedIndexes.forEach {
                if (it <= items.size - 1) {
                    items = ArrayUtils.remove(items, it)
                    iconResId = ArrayUtils.remove(iconResId, it)
                }
            }
            param.args[1] = items
            param.args[2] = iconResId
        }
        // 点击
        findMethod(AlertDialog.Builder::class.java) { name == "setAdapter" }.hookBefore {
            if (!shouldHook) return@hookBefore
            Log.d("HideListItemHook -> AlertDialog.Builder setAdapter()")
            it.run {
                if (args[1] != null) {
                    val listener = args[1] as DialogInterface.OnClickListener
                    args[1] = DialogInterface.OnClickListener { dialog, which ->
                        listener.onClick(dialog, mapOpIndex(which))
                    }
                }
            }
        }
        // 长按
        findMethod(AdapterView::class.java) { name == "setOnItemLongClickListener" }.hookBefore {
            if (!shouldHook) return@hookBefore
            Log.d("HideListItemHook -> AdapterView setOnItemLongClickListener()")
            it.run {
                val listener = args[0] as AdapterView.OnItemLongClickListener
                args[0] = AdapterView.OnItemLongClickListener { parent, view, position, id ->
                    listener.onItemLongClick(parent, view, mapOpIndex(position), id)
                    return@OnItemLongClickListener true
                }
            }
        }
    }
}