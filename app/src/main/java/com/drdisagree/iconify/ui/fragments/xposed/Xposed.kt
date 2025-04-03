package com.drdisagree.iconify.ui.fragments.xposed

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.PL_ENHANCED_PACKAGE
import com.drdisagree.iconify.data.common.Const.PL_ENHANCED_URL
import com.drdisagree.iconify.data.common.Preferences
import com.drdisagree.iconify.data.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.data.common.Preferences.LSCLOCK_MOVE_NOTIFICATION_ICONS
import com.drdisagree.iconify.data.common.Preferences.UPDATE_DETECTED
import com.drdisagree.iconify.data.common.Preferences.XPOSED_HOOK_CHECK
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.ui.base.ControlledPreferenceFragmentCompat
import com.drdisagree.iconify.ui.preferences.HookCheckPreference
import com.drdisagree.iconify.ui.preferences.PreferenceMenu
import com.drdisagree.iconify.utils.AppUtils.launchAppThrowError
import com.drdisagree.iconify.utils.AppUtils.openUrl
import com.drdisagree.iconify.utils.SystemUtils.saveVersionCode

class Xposed : ControlledPreferenceFragmentCompat() {

    private var hookCheckPreference: HookCheckPreference? = null

    override val title: String
        get() = getString(R.string.navbar_xposed)

    override val backButtonEnabled: Boolean
        get() = !Preferences.isXposedOnlyMode

    override val layoutResource: Int
        get() = R.xml.xposed

    override val hasMenu: Boolean
        get() = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        putBoolean(FIRST_INSTALL, false)
        putBoolean(UPDATE_DETECTED, false)
        saveVersionCode()

        // Disable this by default for older android versions
        putBoolean(
            LSCLOCK_MOVE_NOTIFICATION_ICONS,
            getBoolean(
                LSCLOCK_MOVE_NOTIFICATION_ICONS,
                Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU
            )
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        findPreference<HookCheckPreference>(XPOSED_HOOK_CHECK)?.apply {
            hookCheckPreference = this

            setOnPreferenceClickListener {
                try {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.setComponent(
                        ComponentName(
                            "org.lsposed.manager",
                            "org.lsposed.manager.ui.activities.MainActivity"
                        )
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                } catch (_: Exception) {
                }
                true
            }

            initializeHookCheck()
        }

        findPreference<PreferenceMenu>("xposedLauncher")?.setOnPreferenceClickListener {
            try {
                launchAppThrowError(
                    requireActivity(),
                    PL_ENHANCED_PACKAGE
                )
            } catch (_: Exception) {
                openUrl(
                    requireActivity(),
                    PL_ENHANCED_URL
                )
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()

        HookCheckPreference.isHooked = false
        hookCheckPreference?.initializeHookCheck()
    }
}
