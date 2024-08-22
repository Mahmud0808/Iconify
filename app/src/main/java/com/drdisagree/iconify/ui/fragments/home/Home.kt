package com.drdisagree.iconify.ui.fragments.home

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.common.Preferences.UPDATE_DETECTED
import com.drdisagree.iconify.common.Preferences.VER_CODE
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.services.UpdateScheduler.scheduleUpdates
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.fragments.AppUpdates
import com.drdisagree.iconify.ui.preferences.UpdateCheckerPreference
import com.drdisagree.iconify.utils.SystemUtil.saveBootId
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : ControlledPreferenceFragmentCompat() {

    override val title: String
        get() = getString(R.string.app_name)

    override val backButtonEnabled: Boolean
        get() = false

    override val layoutResource: Int
        get() = R.xml.home_page

    override val hasMenu: Boolean
        get() = false

    override val themeResource: Int
        get() = R.style.PrefsThemeCollapsingToolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intent = requireActivity().intent
        if (intent != null && intent.getBooleanExtra(AppUpdates.KEY_NEW_UPDATE, false)) {
            (requireActivity().findViewById<View>(R.id.bottomNavigationView) as BottomNavigationView)
                .selectedItemId = R.id.settings
            replaceFragment(AppUpdates())
            intent.removeExtra(AppUpdates.KEY_NEW_UPDATE)
        } else {
            scheduleUpdates(appContext)
        }

        if (getBoolean(FIRST_INSTALL, false)) {
            putBoolean(FIRST_INSTALL, false)
        }
        if (getBoolean(UPDATE_DETECTED, false)) {
            putBoolean(UPDATE_DETECTED, false)
        }
        if (getInt(VER_CODE, 0) != BuildConfig.VERSION_CODE) {
            putInt(VER_CODE, BuildConfig.VERSION_CODE)
        }
        saveBootId
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<UpdateCheckerPreference>("newUpdate")?.apply {
            onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    replaceFragment(AppUpdates())
                    true
                }

            checkForUpdate()
        }
    }
}
