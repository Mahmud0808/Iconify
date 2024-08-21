package com.drdisagree.iconify.ui.fragments.tweaks

import androidx.preference.PreferenceFragmentCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.backButtonDisabled
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.prefsList
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreference
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchPreferenceResult

class Tweaks : ControlledPreferenceFragmentCompat() {

    private lateinit var searchPreference: SearchPreference

    override val title: String
        get() = getString(R.string.navbar_tweaks)

    override val backButtonEnabled: Boolean
        get() = !Preferences.isXposedOnlyMode

    override val layoutResource: Int
        get() = R.xml.tweaks

    override val hasMenu: Boolean
        get() = true

    override val scopes: Array<String>?
        get() = null

    fun onSearchResultClicked(result: SearchPreferenceResult) {
        if (result.resourceFile == R.xml.tweaks) {
            searchPreference.isVisible = false
            SearchPreferenceResult.highlight(Tweaks(), result.key)
        } else {
            for (obj in prefsList) {
                if (obj[0] as Int == result.resourceFile) {
                    replaceFragment(obj[2] as PreferenceFragmentCompat)
                    SearchPreferenceResult.highlight(
                        obj[2] as PreferenceFragmentCompat,
                        result.key
                    )
                    break
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        backButtonDisabled()
    }
}
