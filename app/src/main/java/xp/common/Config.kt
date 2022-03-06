package xp.common

import androidx.preference.PreferenceManager
import de.robv.android.xposed.XSharedPreferences
import github.ryuunoakaihitomi.powerpanel.BuildConfig

/* res/xml/preferences.xml */
const val KEY_REMOVED_ITEM_INDEXES = "removed_item_indexes"
const val KEY_RENAME_LOCK_SCREEN_ITEM = "rename_lock_screen_item"
const val KEY_DISABLE_DONATION = "disable_donation"

/**
 * @see PreferenceManager.getDefaultSharedPreferencesName
 */
val pref by lazy {
    XSharedPreferences(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + "_preferences")
}