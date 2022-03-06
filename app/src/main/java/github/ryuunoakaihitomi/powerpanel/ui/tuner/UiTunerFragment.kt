package github.ryuunoakaihitomi.powerpanel.ui.tuner

import android.os.Bundle
import androidx.preference.MultiSelectListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import github.ryuunoakaihitomi.powerpanel.R
import xp.common.KEY_REMOVED_ITEM_INDEXES
import xp.common.MAX_ITEM_COUNT

class UiTunerFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val prefRmItemIdx = findPreference<MultiSelectListPreference>(KEY_REMOVED_ITEM_INDEXES)!!
        val list = IntArray(MAX_ITEM_COUNT) { it }.map { it.toString() }.toTypedArray()
        prefRmItemIdx.entries = list
        prefRmItemIdx.entryValues = list
        prefRmItemIdx.setOnPreferenceChangeListener { preference, newValue ->
            if ((newValue as Set<*>).isEmpty()) {
                preference.summary =
                    requireContext().getString(R.string.summary_xposed_index_remove_item)
            } else {
                preference.summary = newValue.toString()
            }
            return@setOnPreferenceChangeListener true
        }
        val configDatRmItemIdx = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getStringSet(KEY_REMOVED_ITEM_INDEXES, setOf())
        configDatRmItemIdx?.run {
            if (size > 0) {
                prefRmItemIdx.summary = toString()
            }
        }
    }
}