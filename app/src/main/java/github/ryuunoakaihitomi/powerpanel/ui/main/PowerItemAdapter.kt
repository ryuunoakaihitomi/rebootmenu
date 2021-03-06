package github.ryuunoakaihitomi.powerpanel.ui.main

import android.content.Context
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import github.ryuunoakaihitomi.powerpanel.util.RC

class PowerItemAdapter(context: Context, items: Array<CharSequence>, iconResId: Array<Int>) :
    ArrayAdapter<CharSequence>(context, android.R.layout.simple_list_item_1, items) {
    private val iconResIdList: List<Int> = listOf(*iconResId)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        if (convertView != null) return convertView
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.compoundDrawablePadding = ViewConfiguration.get(context).scaledTouchSlop
        val drawable = RC.getDrawable(context.resources, iconResIdList[position], null)?.mutate()
        drawable?.setTint(textView.textColors.defaultColor)
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
        return view
    }
}