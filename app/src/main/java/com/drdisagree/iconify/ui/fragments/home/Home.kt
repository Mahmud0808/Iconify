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
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.services.UpdateScheduler.scheduleUpdates
import com.drdisagree.iconify.ui.activities.MainActivity.Companion.replaceFragment
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.fragments.settings.AppUpdates
import com.drdisagree.iconify.ui.preferences.UpdateCheckerPreference
import com.drdisagree.iconify.utils.SystemUtils.saveBootId
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : ControlledPreferenceFragmentCompat(), AppBarLayout.OnOffsetChangedListener {

    private lateinit var appBarLayout: AppBarLayout

    override val title: String
        get() = getString(R.string.app_name)

    override val backButtonEnabled: Boolean
        get() = false

    override val layoutResource: Int
        get() = R.xml.home

    override val hasMenu: Boolean
        get() = true

    override val menuResource: Int
        get() = R.menu.home_menu

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

        appBarLayout = view.findViewById(R.id.appBarLayout)
        appBarLayout.addOnOffsetChangedListener(this)

        if (isToolbarFullyExpanded) {
            listView.scrollToPosition(0)
        }

        putBoolean(FIRST_INSTALL, false)
        putBoolean(UPDATE_DETECTED, false)
        putInt(VER_CODE, BuildConfig.VERSION_CODE)
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

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        if (verticalOffset == 0) {
            if (!isToolbarFullyExpanded) {
                listView.scrollToPosition(0)
                isToolbarFullyExpanded = true
            }
        } else {
            isToolbarFullyExpanded = false
        }
    }

    override fun onResume() {
        super.onResume()

        if (isToolbarFullyExpanded) {
            listView.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appBarLayout.removeOnOffsetChangedListener(this)
    }

    companion object {
        private var isToolbarFullyExpanded = true
    }
}
